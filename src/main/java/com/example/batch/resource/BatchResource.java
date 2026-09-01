package com.example.batch.resource;

import com.example.batch.model.JobStatusResponse;
import com.example.batch.repository.BookRepository;

import jakarta.batch.operations.JobOperator;
import jakarta.batch.operations.JobSecurityException;
import jakarta.batch.operations.JobStartException;
import jakarta.batch.operations.NoSuchJobExecutionException;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Properties;

@Path("/batch")
@JBossLog
@Produces(MediaType.APPLICATION_JSON)
public class BatchResource {
	
    private String chunkSize;
    private String jobName;
    private JobOperator jobOperator;
    private BookRepository bookRepository;
    
	@Inject
    public BatchResource(
    	    @ConfigProperty(name = "batch.chunk-size")
    	    String chunkSize,
    	    @ConfigProperty(name = "batch.job-name")
    	    String jobName,
    	    JobOperator jobOperator,
    	    BookRepository bookRepository) {
		this.chunkSize = chunkSize;
		this.jobName = jobName;
		this.jobOperator = jobOperator;
		this.bookRepository = bookRepository;
    }

    @POST
    @Path("/start")
    public Response startBatch() {
        var jobParameters = new Properties();
        jobParameters.setProperty("chunkSize", chunkSize);
        try {
            var executionId = jobOperator.start(jobName, jobParameters);
            log.infof("Started the batch job %s", jobName);
            return Response.accepted(new JobStatusResponse(executionId, jobName, "STARTED")).build();
        } catch (JobStartException | JobSecurityException e) {
            log.errorf(e, "Failed to start batch job %s", jobName);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/status/{executionId}")
    public Response getStatus(@PathParam("executionId") long executionId) {
        try {
            var jobExecution = jobOperator.getJobExecution(executionId);
            return Response.ok(new JobStatusResponse(
                    executionId, 
                    jobExecution.getJobName(), 
                    jobExecution.getBatchStatus().toString()
            )).build();
        } catch (NoSuchJobExecutionException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new JobStatusResponse(executionId, "", "Unknown"))
                    .build();
        }
    }

    @GET
    @Path("/list")
    public Response findBooks(
            @QueryParam("page") @DefaultValue("0") int pageIndex,
            @QueryParam("size") @DefaultValue("50") int pageSize) {
        var books = bookRepository.findPagedBooks(pageIndex, pageSize);
        log.infof("Fetched page %d (%d books)", pageIndex, books.size());
        return Response.ok(books).build();
    }
    
    @GET
    @Path("/count")
    public Response countBooks() {
        var count = bookRepository.count();
        log.infof("The total no. of books: %d", count);
        return Response.ok(count).build();
    }
}
