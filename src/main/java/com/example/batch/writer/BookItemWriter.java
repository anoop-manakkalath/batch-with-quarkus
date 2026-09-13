package com.example.batch.writer;

import com.example.batch.entity.BookEntity;
import com.example.batch.partition.PartitionStepBarrier;
import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.batch.runtime.context.StepContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@Named("bookItemWriter")
@Dependent
@JBossLog
public class BookItemWriter extends AbstractItemWriter {

    private final StepContext stepContext;
    private final PartitionStepBarrier barrier;
    private final int batchSize;
    private final String partitionIndexProp;

    @Inject
    public BookItemWriter(
            StepContext stepContext,
            PartitionStepBarrier barrier,
            @ConfigProperty(name = "quarkus.hibernate-orm.jdbc.statement-batch-size", defaultValue = "4000") int batchSize,
            @BatchProperty(name = "partitionIndex") String partitionIndexProp) {
        this.stepContext = stepContext;
        this.barrier = barrier;
        this.batchSize = batchSize;
        this.partitionIndexProp = partitionIndexProp;
    }

    @Override
    public void writeItems(List<Object> items) {
        if (CollectionUtils.isEmpty(items)) {
            return;
        }
        int partitionId = getPartitionId();
        executeBatchWrite(items, partitionId);
    }

    private void executeBatchWrite(List<Object> items, int partitionId) {
        var em = BookEntity.getEntityManager();
        var subBatches = ListUtils.partition(items, batchSize);
        for (var subBatch : subBatches) {
            for (Object item : subBatch) {
                em.persist(item);
            }
            em.flush();
            em.clear();
        }
        log.infof("Partition %d: Inserted chunk of %d rows into database", partitionId, items.size());
    }

    @Override
    public void close() {
        // Once this partition closes its writer, unlock Partition N + 1
        int partitionId = getPartitionId();
        barrier.completeTurn(partitionId);
    }

    private int getPartitionId() {
        if (partitionIndexProp != null && !partitionIndexProp.isBlank()) {
            return Integer.parseInt(partitionIndexProp.trim());
        }
        if (stepContext != null && stepContext.getProperties() != null) {
            String prop = stepContext.getProperties().getProperty("partitionIndex");
            if (prop != null) {
                return Integer.parseInt(prop);
            }
        }
        return 0;
    }
}
