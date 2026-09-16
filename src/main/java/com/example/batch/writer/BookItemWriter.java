package com.example.batch.writer;

import com.example.batch.entity.BookEntity;
import com.example.batch.partition.PartitionStepBarrier;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.batch.api.BatchProperty;
import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.batch.runtime.context.StepContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.jbosslog.JBossLog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Objects;

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
        executeBatchWrite(items);
    }

    private void executeBatchWrite(List<Object> items) {
        var partitionId = getPartitionId();
        var subBatches = ListUtils.partition(items, batchSize);
        QuarkusTransaction.requiringNew().run(() -> {
            var em = BookEntity.getEntityManager();
            subBatches.forEach(subBatch -> {
                subBatch.forEach(item -> em.persist(item));
                em.flush();
                em.clear();
                log.infof("Partition %d: Inserted chunk of %d rows into DB", partitionId, subBatch.size());
            });
        });
    }

    @Override
    public void close() {
        var partitionId = getPartitionId();
        // UNLOCK NEXT PARTITION: Signals Partition N + 1 to proceed
        barrier.completeTurn(partitionId);
        log.infof("Partition %d writer completed all chunks.", partitionId);
    }

    private int getPartitionId() {
        if (StringUtils.isNotBlank(partitionIndexProp)) {
            return Integer.parseInt(partitionIndexProp.trim());
        }
        if (Objects.nonNull(stepContext) && Objects.nonNull(stepContext.getProperties())) {
            var prop = stepContext.getProperties().getProperty("partitionIndex");
            if (Objects.nonNull(prop)) {
                return Integer.parseInt(prop);
            }
        }
        return 0;
    }
}
