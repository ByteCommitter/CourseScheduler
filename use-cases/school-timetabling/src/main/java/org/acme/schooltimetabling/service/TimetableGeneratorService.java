package org.acme.schooltimetabling.service;

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
import java.util.concurrent.ExecutionException;
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
        
        TimeTable problem = new TimeTable(timeslots, rooms, lessons);
        
        try {
            // Generate unique problem ID
            Long problemId = System.currentTimeMillis();
            logger.info("Submitting problem with ID: {}", problemId);
            
            // Submit problem to solver
            return solverManager.solveAndListen(
                problemId,
                (timeTableId) -> problem,
                // Solution handler
                (timeTable) -> {
                    logger.info("New best solution found: {}", timeTable.getScore());
                },
                // Problem handler
                (timeTableId, throwable) -> {
                    logger.error("Solver failed for problem {}", timeTableId, throwable);
                }
            ).getFinalBestSolution();
            
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Solver execution failed", e);
            throw new RuntimeException("Failed to solve timetable", e);
        }
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
        // Days 0-4 (Monday-Friday)
        for (int day = 0; day < 5; day++) {
            for (int slot = 0; slot < slotCount; slot++) {
                timeslots.add(new Timeslot(null, day, slot));
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
            
            // If it's a lab, create two lessons that must be scheduled together
            if (subject.toLowerCase().contains("lab")) {
                // Create two lessons for the lab
                lessons.add(new Lesson(subject, teacher, studentGroup));
                lessons.add(new Lesson(subject, teacher, studentGroup));
            } else {
                // Regular lesson
                for (int i = 0; i < req.getHoursPerWeek(); i++) {
                    lessons.add(new Lesson(subject, teacher, studentGroup));
                }
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