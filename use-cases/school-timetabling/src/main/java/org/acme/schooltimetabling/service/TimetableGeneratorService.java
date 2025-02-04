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
import io.quarkus.panache.common.Sort;
import java.util.function.Function;

@ApplicationScoped
public class TimetableGeneratorService {
    public static final long SINGLETON_TIME_TABLE_ID = 1L;

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
        
        // Clear existing data
        lessonRepository.deleteAll();
        roomRepository.deleteAll();
        timeslotRepository.deleteAll();
        
        List<Room> rooms = createRooms(roomCount);
        List<Timeslot> timeslots = createTimeslots(slotCount);
        List<Lesson> lessons = createLessons(requests);
        
        // Persist all entities
        roomRepository.persist(rooms);
        timeslotRepository.persist(timeslots);
        lessonRepository.persist(lessons);
        
        TimeTable problem = new TimeTable(timeslots, rooms, lessons);
        
        try {
            Long problemId = System.currentTimeMillis();
            logger.info("Submitting problem with ID: {}", problemId);
            
            TimeTable solution = solverManager.solveAndListen(
                problemId,
                (timeTableId) -> problem,
                this::updateSolution,
                (timeTableId, throwable) -> logger.error("Solver failed for problem {}", timeTableId, throwable)
            ).getFinalBestSolution();
            
            // Persist the final solution
            persistSolution(solution);
            
            return solution;
            
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Solver execution failed", e);
            throw new RuntimeException("Failed to solve timetable", e);
        }
    }

    @Transactional
    protected void updateSolution(TimeTable timeTable) {
        logger.info("New best solution found: {}", timeTable.getScore());
        persistSolution(timeTable);
    }

    @Transactional
    protected void persistSolution(TimeTable solution) {
        if (solution == null || solution.getLessonList() == null) {
            logger.warn("No solution to persist");
            return;
        }

        solution.getLessonList().forEach(lesson -> {
            try {
                Lesson entity = lessonRepository.findById(lesson.getId());
                if (entity != null) {
                    entity.setRoom(lesson.getRoom());
                    entity.setTimeslot(lesson.getTimeslot());
                    lessonRepository.persist(entity);
                    logger.debug("Updated lesson {}: room={}, timeslot={}", 
                        lesson.getId(), 
                        lesson.getRoom() != null ? lesson.getRoom().getName() : "null",
                        lesson.getTimeslot() != null ? lesson.getTimeslot().getId() : "null");
                } else {
                    logger.warn("Could not find lesson with id {}", lesson.getId());
                }
            } catch (Exception e) {
                logger.error("Failed to persist lesson {}", lesson.getId(), e);
            }
        });
        
        // Force a flush to ensure all changes are written
        lessonRepository.flush();
    }

    @Transactional
    public void initializeSolver(TimeTable timeTable) {
        // Clear any existing solution
        lessonRepository.streamAll()
            .forEach(lesson -> {
                lesson.setRoom(null);
                lesson.setTimeslot(null);
                lessonRepository.persist(lesson);
            });

        try {
            Long problemId = System.currentTimeMillis();
            // Fix: Pass the TimeTable directly instead of a Function
            solverManager.solveAndListen(problemId, id -> timeTable, this::save);
        } catch (Exception e) {
            logger.error("Failed to initialize solver", e);
            throw new RuntimeException("Failed to initialize solver", e);
        }
    }

    @Transactional
    public TimeTable getTimeTable() {
        return new TimeTable(
            timeslotRepository.listAll(Sort.by("dayOfWeek").and("slot").and("id")),
            roomRepository.listAll(Sort.by("name").and("id")),
            lessonRepository.listAll(Sort.by("subject").and("teacher").and("studentGroup").and("id"))
        );
    }

    @Transactional
    public void solve() {
        TimeTable timeTable = getTimeTable();
        solverManager.solveAndListen(
            SINGLETON_TIME_TABLE_ID,
            id -> timeTable,
            this::save
        );
    }


    @Transactional
    protected void save(TimeTable timeTable) {
        timeTable.getLessonList().forEach(lesson -> {
            Lesson attachedLesson = lessonRepository.findById(lesson.getId());
            if (attachedLesson != null) {
                attachedLesson.setTimeslot(lesson.getTimeslot());
                attachedLesson.setRoom(lesson.getRoom());
                lessonRepository.persist(attachedLesson);
            }
        });
        lessonRepository.flush();
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
        int lessonId = 0;
        
        for (CourseRequest req : requests) {
            String[] parts = req.getFacultyCourseSection().split("_");
            String teacher = parts[0];
            String subject = parts[1];
            String studentGroup = parts.length > 2 ? parts[2] : "DEFAULT";
            String courseId = req.getFacultyCourseSection();
            
            int totalHours = req.getHoursPerWeek();
            int combinedSlots = req.getCombinedSlotsPerDay();
            
            // Calculate how many groups of combined slots we need
            int numberOfGroups = (int) Math.ceil((double) totalHours / combinedSlots);
            
            for (int group = 0; group < numberOfGroups; group++) {
                int slotsInThisGroup = Math.min(combinedSlots, totalHours);
                for (int slot = 0; slot < slotsInThisGroup; slot++) {
                    lessons.add(new Lesson(String.format("%s_G%d", subject, group), 
                                         teacher, 
                                         studentGroup, 
                                         combinedSlots, 
                                         courseId + "_G" + group));
                }
                totalHours -= slotsInThisGroup;
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
                req.setCombinedSlotsPerDay(Integer.parseInt(line[2]));
                requests.add(req);
            }
        } catch (Exception e) {
            logger.error("Failed to parse CSV input", e);
            throw new RuntimeException("Failed to parse CSV input", e);
        }
        return requests;
    }
}