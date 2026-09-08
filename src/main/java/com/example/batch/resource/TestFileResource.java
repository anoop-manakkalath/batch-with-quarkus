package com.example.batch.resource;

import java.util.Objects;

import com.example.batch.service.CsvFromDbGeneratorService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/batch-test")
public class TestFileResource {

	private CsvFromDbGeneratorService generatorService;
    
	@Inject
    public TestFileResource(CsvFromDbGeneratorService generatorService) {
    	this.generatorService = generatorService;
    }

    @GET
    @Path("/csv-from-db")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateFileFromDb(@QueryParam("sizeMb") Double sizeMb) {
        try {
            var targetSize = Objects.nonNull(sizeMb) ? sizeMb : 10.0;
            generatorService.generateCsvFromDatabase("data/large_books_.csv", targetSize);
            return Response.ok("Successfully generated " + targetSize + " MB CSV file directly from DB records!").build();
        } catch (Exception e) {
            return Response.serverError().entity("Error generating file: " + e.getMessage()).build();
        }
    }
}
