package com.example.batch.writer;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

import com.example.batch.entity.BookEntity;

import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;
import lombok.extern.jbosslog.JBossLog;

@Named("bookItemWriter")
@JBossLog
@Dependent
public class BookItemWriter extends AbstractItemWriter {

    private static final int DB_SUB_BATCH_SIZE = 4000;

    @Override
    public void writeItems(List<Object> items) throws Exception {
        if (CollectionUtils.isEmpty(items)) {
            return;
        }
        executeBatchWrite(items);
    }

    private void executeBatchWrite(List<Object> items) throws InterruptedException {
        Thread.ofVirtual()
            .start(() -> {
                QuarkusTransaction.requiringNew().run(() -> {
                    var em = BookEntity.getEntityManager();
                    var subBatches = ListUtils.partition(items, DB_SUB_BATCH_SIZE);
                    
                    subBatches.forEach(subBatch -> {
                        subBatch.forEach(item -> em.persist((BookEntity) item));
                        em.flush();
                        em.clear();
                    });
                    
                    log.infof("Inserted %d rows into the database", items.size());
                });
            })
            .join();
    }
}
