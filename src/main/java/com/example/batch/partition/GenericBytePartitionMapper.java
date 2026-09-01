package com.example.batch.partition;

import jakarta.batch.api.partition.PartitionMapper;
import jakarta.batch.api.partition.PartitionPlan;
import jakarta.batch.api.partition.PartitionPlanImpl;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Named("genericBytePartitionMapper")
@Dependent
public class GenericBytePartitionMapper implements PartitionMapper {
	
	private String partitions;
	private String filePath;
	
	@Inject
	public GenericBytePartitionMapper(
			@ConfigProperty(name = "partitions", defaultValue = "8") String partitions,
			@ConfigProperty(name = "batch.file-path", defaultValue = "data/large_books.csv") String filePath) {
		this.partitions = partitions;
		this.filePath = filePath;
	}

    @Override
    public PartitionPlan mapPartitions() throws Exception {
        var path = Paths.get(filePath);
        var totalBytes = Files.size(path);
        
        // Adjust partitions if file is very small (< 8 KB)
        var effectivePartitions = (totalBytes < 8192) ? 1 : Integer.parseInt(partitions);
        var bytesPerPartition = totalBytes / effectivePartitions;

        var partitionProperties = new Properties[effectivePartitions];

        for (int i = 0; i < effectivePartitions; i++) {
            var props = new Properties();
            var startOffset = i * bytesPerPartition;
            var endOffset = (i == effectivePartitions - 1) ? totalBytes : (startOffset + bytesPerPartition);

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
