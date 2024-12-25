package org.acme.schooltimetabling.service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.acme.schooltimetabling.domain.*;
import org.acme.schooltimetabling.domain.dto.CourseRequest;
import org.acme.schooltimetabling.persistence.LessonRepository;
import org.acme.schooltimetabling.persistence.RoomRepository;
import org.acme.schooltimetabling.persistence.TimeslotRepository;
import org.optaplanner.core.api.solver.SolverManager;
import java.util.*;
import com.opencsv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.StringReader;

@ApplicationScoped
public class TimetableGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(TimetableGeneratorService.class);
    
    @Inject
    SolverManager<TimeTable, Long> solverManager;
    
    @Inject
    LessonRepository lessonRepository;
    
    @Inject
    RoomRepository roomRepository;
    
    @Inject
    TimeslotRepository timeslotRepository;

    @Transactional
    public TimeTable generateTimeTable(String csvData, int roomCount, int slotCount) {
        logger.info("Starting timetable generation with {} rooms and {} slots", roomCount, slotCount);
        
        List<CourseRequest> requests = parseCsvInput(csvData);
        logger.info("Parsed {} course requests from CSV", requests.size());
        
        List<Room> rooms = createRooms(roomCount);
        List<Timeslot> timeslots = createTimeslots(slotCount);
        List<Lesson> lessons = createLessons(requests);
        
        // Persist rooms and timeslots to ensure they have IDs
        roomRepository.persist(rooms);
        timeslotRepository.persist(timeslots);
        lessonRepository.persist(lessons);
        
        return new TimeTable(timeslots, rooms, lessons);
    }

    private List<Room> createRooms(int roomCount) {
        List<Room> rooms = new ArrayList<>();
        for (int i = 1; i <= roomCount; i++) {
            rooms.add(new Room("Room " + i));
        }
        return rooms;
    }

    private List<Timeslot> createTimeslots(int slotCount) {
        List<Timeslot> timeslots = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day.getValue() <= 5) { // Monday to Friday
                for (int slot = 0; slot < slotCount; slot++) {
                    LocalTime startTime = LocalTime.of(8, 0).plusHours(slot);
                    timeslots.add(new Timeslot(day, startTime, startTime.plusMinutes(50)));
                }
            }
        }
        return timeslots;
    }

    private List<Lesson> createLessons(List<CourseRequest> requests) {
        List<Lesson> lessons = new ArrayList<>();
        for (CourseRequest req : requests) {
            String[] parts = req.getFacultyCourseSection().split("_");
            String teacher = parts[0];
            String subject = parts[1];
            String studentGroup = parts.length > 2 ? parts[2] : "DEFAULT";
            
            for (int i = 0; i < req.getHoursPerWeek(); i++) {
                lessons.add(new Lesson(subject, teacher, studentGroup));
            }
        }
        return lessons;
    }

    public List<CourseRequest> parseCsvInput(String csvData) {
        List<CourseRequest> requests = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new StringReader(csvData))) {
            reader.skip(1); // Skip header
            String[] line;
            while ((line = reader.readNext()) != null) {
                CourseRequest req = new CourseRequest();
                req.setFacultyCourseSection(line[0]);
                req.setHoursPerWeek(Integer.parseInt(line[1]));
                requests.add(req);
            }
        } catch (Exception e) {
            logger.error("Failed to parse CSV input", e);
            throw new RuntimeException("Failed to parse CSV input", e);
        }
        return requests;
    }
}