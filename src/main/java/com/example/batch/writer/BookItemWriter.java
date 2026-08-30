package com.example.batch.writer;

import java.util.List;

import org.apache.commons.collections4.ListUtils;

import com.example.batch.entity.BookEntity;

import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import lombok.extern.jbosslog.JBossLog;

@Named("bookItemWriter")
@JBossLog
@Dependent
public class BookItemWriter extends AbstractItemWriter {

    private static final int DB_SUB_BATCH_SIZE = 4000;

    @Override
    @Transactional
    public void writeItems(List<Object> items) throws Exception {
        var em = BookEntity.getEntityManager();
        var subBatches = ListUtils.partition(items, DB_SUB_BATCH_SIZE);
        subBatches.forEach(subBatch -> {
            subBatch.forEach(item -> em.persist((BookEntity) item));
            
            // Flush batch to DB and clear Hibernate cache after every 'DB_SUB_BATCH_SIZE' items
            em.flush();
            em.clear();
        });
        log.infof("Inserted %d rows into the database", items.size());
    }
}
