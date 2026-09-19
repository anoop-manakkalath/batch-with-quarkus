package com.example.batch.resource;

import java.util.Objects;

import com.example.batch.service.CsvFromDbGeneratorService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/batch-test")
@RunOnVirtualThread
public class TestFileResource {

	private CsvFromDbGeneratorService generatorService;
    
	@Inject
    public TestFileResource(CsvFromDbGeneratorService generatorService) {
    	this.generatorService = generatorService;
    }

    @GET
    @Path("/csv-from-db-line")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateFileFromDbForLines(@QueryParam("targetLineNumber") int maxLineNumber) {
        try {
            var targetLineNumber = Objects.nonNull(maxLineNumber) ? maxLineNumber : 100;
            generatorService.generateCsvFromDatabaseForLine("data/large_books_line.csv", targetLineNumber);
            return Response.ok("Successfully generated " + targetLineNumber + " lined CSV file directly from DB records!").build();
        } catch (Exception e) {
            return Response.serverError().entity("Error generating file: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/csv-from-db-size")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateFileFromDbForSize(@QueryParam("sizeMb") double sizeMb) {
        try {
            var targetSize = Objects.nonNull(sizeMb) ? sizeMb : 10.0;
            generatorService.generateCsvFromDatabaseForSize("data/large_books_size.csv", targetSize);
            return Response.ok("Successfully generated " + targetSize + " MB CSV file directly from DB records!").build();
        } catch (Exception e) {
            return Response.serverError().entity("Error generating file: " + e.getMessage()).build();
        }
    }
}
