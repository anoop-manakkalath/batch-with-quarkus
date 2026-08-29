package com.example.batch.partition;

import jakarta.batch.api.partition.PartitionMapper;
import jakarta.batch.api.partition.PartitionPlan;
import jakarta.batch.api.partition.PartitionPlanImpl;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@Named("genericBytePartitionMapper")
@Dependent
public class GenericBytePartitionMapper implements PartitionMapper {

    private static final int PARTITION_COUNT = 8;
    private static final String FILE_PATH = "data/large_books.csv";

    @Override
    public PartitionPlan mapPartitions() throws Exception {
        var path = Paths.get(FILE_PATH);
        var totalBytes = Files.size(path);
        
        // Adjust partitions if file is very small (< 8 KB)
        var effectivePartitions = (totalBytes < 8192) ? 1 : PARTITION_COUNT;
        var bytesPerPartition = totalBytes / effectivePartitions;

        var partitionProperties = new Properties[effectivePartitions];

        for (int i = 0; i < effectivePartitions; i++) {
            var props = new Properties();
            long startOffset = i * bytesPerPartition;
            long endOffset = (i == effectivePartitions - 1) ? totalBytes : (startOffset + bytesPerPartition);

            props.setProperty("startOffset", String.valueOf(startOffset));
            props.setProperty("endOffset", String.valueOf(endOffset));
            props.setProperty("partitionIndex", String.valueOf(i));
            partitionProperties[i] = props;
        }

        var plan = new PartitionPlanImpl();
        plan.setPartitions(effectivePartitions);
        plan.setPartitionProperties(partitionProperties);
        return plan;
    }
}
