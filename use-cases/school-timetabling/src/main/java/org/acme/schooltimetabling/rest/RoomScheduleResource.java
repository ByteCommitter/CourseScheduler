package org.acme.schooltimetabling.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.RoomSchedule;
import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.persistence.RoomRepository;
import org.acme.schooltimetabling.persistence.LessonRepository;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

@Path("/roomSchedule")
@Tag(name = "Room Schedule Resource", description = "Provides room schedule operations")
public class RoomScheduleResource {

    @Inject
    RoomRepository roomRepository;

    @Inject
    LessonRepository lessonRepository;

    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get room schedule",
        description = "Returns the weekly schedule for a specific room"
    )
    public RoomSchedule getRoomSchedule(@PathParam("roomId") Long roomId) {
        Room room = roomRepository.findById(roomId);
        if (room == null) {
            throw new javax.ws.rs.NotFoundException("Room not found with id: " + roomId);
        }

        List<Lesson> roomLessons = lessonRepository.listAll().stream()
            .filter(lesson -> lesson.getRoom() != null && lesson.getRoom().getId().equals(roomId))
            .collect(Collectors.toList());

        return new RoomSchedule(room, roomLessons);
    }
}
