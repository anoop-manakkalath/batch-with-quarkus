package com.example.batch.resource;

import java.util.Properties;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.example.batch.entity.BookEntity;
import com.example.batch.mapper.BookMapper;

import jakarta.batch.operations.JobSecurityException;
import jakarta.batch.operations.JobStartException;
import jakarta.batch.operations.NoSuchJobExecutionException;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;

@Path("/batch")
@JBossLog
public class BatchResource {
	
	@Inject
	@ConfigProperty(name = "batch.chunk-size", defaultValue = "10")
	String chunkSize;
	
	@Inject
    @ConfigProperty(name = "batch.job-name", defaultValue = "myJob")
    String jobName;
	
	@Inject
	BookMapper mapper;

    @POST
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startBatch() {
        var jobOperator = BatchRuntime.getJobOperator();
        var jobParameters = new Properties();
        jobParameters.setProperty("chunkSize", chunkSize);
        try {
	        var executionId = jobOperator.start(jobName, jobParameters);
	        return Response.accepted()
	        		.entity( """
			                {
			                  "status": "STARTED",
			                  "name": "%s",
			                  "executionId": %d
			                }
	        				""".formatted(jobName, executionId))
	        		.build();
        }
        catch (JobStartException | JobSecurityException e) {
        	return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
    }

    @GET
    @Path("/status/{executionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus(@PathParam("executionId") long executionId) {
    	var jobOperator = BatchRuntime.getJobOperator();
    	try {
        	var jobExecution = jobOperator.getJobExecution(executionId);
        	return Response.ok()
        	        .entity("""
        	                {
        	                  "executionId": %d,
        	                  "name": "%s",
        	                  "batchStatus": "%s"
        	                }
        	                """.formatted(executionId, jobExecution.getJobName(), jobExecution.getBatchStatus()))
        	        .build();
        }
        catch (NoSuchJobExecutionException e) {
        	 return Response.status(Response.Status.NOT_FOUND)
        			 .entity("""
        	                {
        	                  "executionId": %d,
        	                  "batchStatus": "Unknown"
        	                }
        	                """.formatted(executionId))
        			 .build();
		}
    }
    
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findBooks() {
    	var books = BookEntity.findAllBooks()
                .stream()
                .map(mapper::toDto)
                .toList();

    	log.infof("Fetched %d books for search criteria.", books.size());
        return Response.ok(books).build();
	}
}
