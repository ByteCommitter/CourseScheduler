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
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.annotations.providers.multipart.PartType;
import java.io.File;
import javax.ws.rs.FormParam;
import java.nio.file.Files;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.optaplanner.core.api.solver.SolverStatus;
import org.optaplanner.core.api.solver.SolverManager;

@Path("/timetable-generator")
@ApplicationScoped
@Tag(name = "Timetable Generator", description = "Generate timetable from CSV file upload")
public class TimetableGeneratorResource {

    private static final Logger logger = LoggerFactory.getLogger(TimetableGeneratorResource.class);

    @Inject 
    TimetableGeneratorService generatorService;
    @Inject
    SolverManager<TimeTable, Long> solverManager;

    public static class FileUploadForm {
        @FormParam("file")
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        public InputStream inputStream;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(
        summary = "Generate timetable from CSV file",
        description = "Upload CSV file and parameters here"
    )
    @APIResponse(
        responseCode = "200",
        description = "Successfully generated timetable",
        content = @Content(mediaType = MediaType.TEXT_PLAIN)
    )
    public Response generateTimetable(
            @MultipartForm FileUploadForm form,
            @Parameter(
                name = "rooms",
                description = "Number of rooms available",
                schema = @Schema(type = SchemaType.INTEGER, defaultValue = "3")
            )
            @QueryParam("rooms") @DefaultValue("10") int rooms,
            
            @Parameter(
                name = "slots",
                description = "Number of time slots per day",
                schema = @Schema(type = SchemaType.INTEGER, defaultValue = "3")
            )
            @QueryParam("slots") @DefaultValue("9") int slots) {
        
        try {
            logger.info("Received timetable generation request: rooms={}, slots={}", rooms, slots);
            
            if (form.inputStream == null) {
                logger.warn("No CSV file received");
                return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("CSV file is required")
                    .build();
            }

            // Convert InputStream to String
            String csvData = readInputStream(form.inputStream);

            if (!csvData.trim().startsWith("faculty_course_section,hours_per_week,combined_slots_per_day")) {
                logger.warn("Invalid CSV format received");
                return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("Invalid CSV format. First line must be: faculty_course_section,hours_per_week,combined_slots_per_day")
                    .build();
            }

            TimeTable timeTable = generatorService.generateTimeTable(csvData, rooms, slots);
            String csvOutput = convertToCsv(timeTable);
            return Response.ok(csvOutput)
                .header("Content-Disposition", "attachment; filename=schedule.csv")
                .build();

        } catch (Exception e) {
            logger.error("Failed to generate timetable", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.TEXT_PLAIN)
                .entity("Failed to generate timetable: " + e.getMessage())
                .build();
        }
    }

    //Not Required
    // @POST
    // @Path("/solve")
    // @Produces(MediaType.TEXT_PLAIN)
    // public Response solveTimetable() {
    //     try {
    //         generatorService.solve();
    //         return Response.ok("Solver started").build();
    //     } catch (Exception e) {
    //         logger.error("Failed to start solver", e);
    //         return Response.serverError().entity("Failed to start solver: " + e.getMessage()).build();
    //     }
    // }

    // @GET
    // @Path("/status")
    // @Produces(MediaType.TEXT_PLAIN)
    // public Response getSolverStatus() {
    //     try {
    //         SolverStatus status = solverManager.getSolverStatus(TimetableGeneratorService.SINGLETON_TIME_TABLE_ID);
    //         return Response.ok(status.toString()).build();
    //     } catch (Exception e) {
    //         logger.error("Failed to get solver status", e);
    //         return Response.serverError().entity("Failed to get solver status: " + e.getMessage()).build();
    //     }
    // }

    private String readInputStream(InputStream input) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    private String convertToCsv(TimeTable timeTable) {
        StringBuilder csv = new StringBuilder();
        csv.append("room_number,slot_number,faculty_course_section,day_of_the_week\n");
        
        timeTable.getLessonList().stream()
            .filter(lesson -> lesson.getRoom() != null && lesson.getTimeslot() != null)
            .forEach(lesson -> {
                int roomNumber = Integer.parseInt(lesson.getRoom().getName().replace("Room ", ""));
                // Remove the group indicator from the course ID
                String courseId = lesson.getCourseId().replaceAll("_G\\d+$", "");
                
                csv.append(String.format("%d,%d,%s,%d\n",
                    roomNumber,
                    lesson.getTimeslot().getSlot(),
                    courseId,
                    lesson.getTimeslot().getDayOfWeek()));
            });
            
        return csv.toString();
    }
}