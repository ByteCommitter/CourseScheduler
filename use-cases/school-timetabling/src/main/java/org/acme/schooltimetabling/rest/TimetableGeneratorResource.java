package org.acme.schooltimetabling.rest;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.acme.schooltimetabling.domain.TimeTable;
import org.acme.schooltimetabling.service.TimetableGeneratorService;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.enterprise.context.ApplicationScoped;

@Path("/timetable-generator")
@ApplicationScoped
@Tag(name = "Timetable Generator", description = "Generates timetables from CSV input")
public class TimetableGeneratorResource {

    private static final Logger logger = LoggerFactory.getLogger(TimetableGeneratorResource.class);

    @Inject 
    TimetableGeneratorService generatorService;

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateTimetable(
            String csvData,
            @QueryParam("rooms") @DefaultValue("10") int rooms,
            @QueryParam("slots") @DefaultValue("9") int slots) {
        
        try {
            logger.info("Received timetable generation request: rooms={}, slots={}", rooms, slots);
            
            if (csvData == null || csvData.trim().isEmpty()) {
                logger.warn("Empty CSV data received");
                return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("CSV data cannot be empty")
                    .build();
            }

            if (!csvData.trim().startsWith("faculty_course_section,hours_per_week")) {
                logger.warn("Invalid CSV format received");
                return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("Invalid CSV format. First line must be: faculty_course_section,hours_per_week")
                    .build();
            }

            TimeTable timeTable = generatorService.generateTimeTable(csvData, rooms, slots);
            return Response.ok(timeTable)
                .type(MediaType.APPLICATION_JSON)
                .build();

        } catch (Exception e) {
            logger.error("Failed to generate timetable", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.TEXT_PLAIN)
                .entity("Failed to generate timetable: " + e.getMessage())
                .build();
        }
    }
}