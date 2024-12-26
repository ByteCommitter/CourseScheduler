package org.acme.schooltimetabling.bootstrap;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.persistence.LessonRepository;
import org.acme.schooltimetabling.persistence.RoomRepository;
import org.acme.schooltimetabling.persistence.TimeslotRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class DemoDataGenerator {

    @ConfigProperty(name = "timeTable.demoData", defaultValue = "SMALL")
    DemoData demoData;

    @Inject
    TimeslotRepository timeslotRepository;
    @Inject
    RoomRepository roomRepository;
    @Inject
    LessonRepository lessonRepository;
    @ConfigProperty(name = "demo.data.enabled", defaultValue = "false")
    boolean isDemoDataEnabled;

    @Transactional
    public void generateDemoData(@Observes StartupEvent startupEvent) {
        if (!isDemoDataEnabled) {
            return;
        }

        List<Timeslot> timeslotList = new ArrayList<>(10);
        // Monday slots
        timeslotList.add(new Timeslot(null, 1, 0)); // Monday, first slot
        timeslotList.add(new Timeslot(null, 1, 1)); // Monday, second slot
        timeslotList.add(new Timeslot(null, 1, 2)); // Monday, third slot
        timeslotList.add(new Timeslot(null, 1, 3)); // Monday, fourth slot
        timeslotList.add(new Timeslot(null, 1, 4)); // Monday, fifth slot

        // Tuesday slots
        timeslotList.add(new Timeslot(null, 2, 0));
        timeslotList.add(new Timeslot(null, 2, 1));
        timeslotList.add(new Timeslot(null, 2, 2));
        timeslotList.add(new Timeslot(null, 2, 3));
        timeslotList.add(new Timeslot(null, 2, 4));

        if (demoData == DemoData.LARGE) {
            // Wednesday to Friday
            for (int day = 3; day <= 5; day++) {
                for (int slot = 0; slot < 5; slot++) {
                    timeslotList.add(new Timeslot(null, day, slot));
                }
            }
        }
        timeslotRepository.persist(timeslotList);

        List<Room> roomList = new ArrayList<>(3);
        roomList.add(new Room("Room A"));
        roomList.add(new Room("Room B"));
        roomList.add(new Room("Room C"));
        if (demoData == DemoData.LARGE) {
            roomList.add(new Room("Room D"));
            roomList.add(new Room("Room E"));
            roomList.add(new Room("Room F"));
        }
        roomRepository.persist(roomList);

        List<Lesson> lessonList = new ArrayList<>();
        lessonList.add(new Lesson("Math", "A. Turing", "9th grade"));
        lessonList.add(new Lesson("Math", "A. Turing", "9th grade"));
        lessonList.add(new Lesson("Physics", "M. Curie", "9th grade"));
        lessonList.add(new Lesson("Chemistry", "M. Curie", "9th grade"));
        lessonList.add(new Lesson("Biology", "C. Darwin", "9th grade"));
        lessonList.add(new Lesson("History", "I. Jones", "9th grade"));
        lessonList.add(new Lesson("English", "I. Jones", "9th grade"));
        lessonList.add(new Lesson("English", "I. Jones", "9th grade"));
        lessonList.add(new Lesson("Spanish", "P. Cruz", "9th grade"));
        lessonList.add(new Lesson("Spanish", "P. Cruz", "9th grade"));
        if (demoData == DemoData.LARGE) {
            lessonList.add(new Lesson("Math", "A. Turing", "9th grade"));
            lessonList.add(new Lesson("Math", "A. Turing", "9th grade"));
            lessonList.add(new Lesson("Math", "A. Turing", "9th grade"));
            lessonList.add(new Lesson("ICT", "A. Turing", "9th grade"));
            lessonList.add(new Lesson("Physics", "M. Curie", "9th grade"));
            lessonList.add(new Lesson("Geography", "C. Darwin", "9th grade"));
            lessonList.add(new Lesson("Geology", "C. Darwin", "9th grade"));
            lessonList.add(new Lesson("History", "I. Jones", "9th grade"));
            lessonList.add(new Lesson("English", "I. Jones", "9th grade"));
            lessonList.add(new Lesson("Drama", "I. Jones", "9th grade"));
            lessonList.add(new Lesson("Art", "S. Dali", "9th grade"));
            lessonList.add(new Lesson("Art", "S. Dali", "9th grade"));
            lessonList.add(new Lesson("Physical education", "C. Lewis", "9th grade"));
            lessonList.add(new Lesson("Physical education", "C. Lewis", "9th grade"));
            lessonList.add(new Lesson("Physical education", "C. Lewis", "9th grade"));
        }

        lessonList.add(new Lesson("Math", "A. Turing", "10th grade"));
        lessonList.add(new Lesson("Math", "A. Turing", "10th grade"));
        lessonList.add(new Lesson("Math", "A. Turing", "10th grade"));
        lessonList.add(new Lesson("Physics", "M. Curie", "10th grade"));
        lessonList.add(new Lesson("Chemistry", "M. Curie", "10th grade"));
        lessonList.add(new Lesson("French", "M. Curie", "10th grade"));
        lessonList.add(new Lesson("Geography", "C. Darwin", "10th grade"));
        lessonList.add(new Lesson("History", "I. Jones", "10th grade"));
        lessonList.add(new Lesson("English", "P. Cruz", "10th grade"));
        lessonList.add(new Lesson("Spanish", "P. Cruz", "10th grade"));
        if (demoData == DemoData.LARGE) {
            lessonList.add(new Lesson("Math", "A. Turing", "10th grade"));
            lessonList.add(new Lesson("Math", "A. Turing", "10th grade"));
            lessonList.add(new Lesson("ICT", "A. Turing", "10th grade"));
            lessonList.add(new Lesson("Physics", "M. Curie", "10th grade"));
            lessonList.add(new Lesson("Biology", "C. Darwin", "10th grade"));
            lessonList.add(new Lesson("Geology", "C. Darwin", "10th grade"));
            lessonList.add(new Lesson("History", "I. Jones", "10th grade"));
            lessonList.add(new Lesson("English", "P. Cruz", "10th grade"));
            lessonList.add(new Lesson("English", "P. Cruz", "10th grade"));
            lessonList.add(new Lesson("Drama", "I. Jones", "10th grade"));
            lessonList.add(new Lesson("Art", "S. Dali", "10th grade"));
            lessonList.add(new Lesson("Art", "S. Dali", "10th grade"));
            lessonList.add(new Lesson("Physical education", "C. Lewis", "10th grade"));
            lessonList.add(new Lesson("Physical education", "C. Lewis", "10th grade"));
            lessonList.add(new Lesson("Physical education", "C. Lewis", "10th grade"));

            lessonList.add(new Lesson("Math", "A. Turing", "11th grade"));
            lessonList.add(new Lesson("Math", "A. Turing", "11th grade"));
            lessonList.add(new Lesson("Math", "A. Turing", "11th grade"));
            lessonList.add(new Lesson("Math", "A. Turing", "11th grade"));
            lessonList.add(new Lesson("Math", "A. Turing", "11th grade"));
            lessonList.add(new Lesson("ICT", "A. Turing", "11th grade"));
            lessonList.add(new Lesson("Physics", "M. Curie", "11th grade"));
            lessonList.add(new Lesson("Chemistry", "M. Curie", "11th grade"));
            lessonList.add(new Lesson("French", "M. Curie", "11th grade"));
            lessonList.add(new Lesson("Physics", "M. Curie", "11th grade"));
            lessonList.add(new Lesson("Geography", "C. Darwin", "11th grade"));
            lessonList.add(new Lesson("Biology", "C. Darwin", "11th grade"));
            lessonList.add(new Lesson("Geology", "C. Darwin", "11th grade"));
            lessonList.add(new Lesson("History", "I. Jones", "11th grade"));
            lessonList.add(new Lesson("History", "I. Jones", "11th grade"));
            lessonList.add(new Lesson("English", "P. Cruz", "11th grade"));
            lessonList.add(new Lesson("English", "P. Cruz", "11th grade"));
            lessonList.add(new Lesson("English", "P. Cruz", "11th grade"));
            lessonList.add(new Lesson("Spanish", "P. Cruz", "11th grade"));
            lessonList.add(new Lesson("Drama", "P. Cruz", "11th grade"));
            lessonList.add(new Lesson("Art", "S. Dali", "11th grade"));
            lessonList.add(new Lesson("Art", "S. Dali", "11th grade"));
            lessonList.add(new Lesson("Physical education", "C. Lewis", "11th grade"));
            lessonList.add(new Lesson("Physical education", "C. Lewis", "11th grade"));
            lessonList.add(new Lesson("Physical education", "C. Lewis", "11th grade"));

            lessonList.add(new Lesson("Math", "A. Turing", "12th grade"));
            lessonList.add(new Lesson("Math", "A. Turing", "12th grade"));
            lessonList.add(new Lesson("Math", "A. Turing", "12th grade"));
            lessonList.add(new Lesson("Math", "A. Turing", "12th grade"));
            lessonList.add(new Lesson("Math", "A. Turing", "12th grade"));
            lessonList.add(new Lesson("ICT", "A. Turing", "12th grade"));
            lessonList.add(new Lesson("Physics", "M. Curie", "12th grade"));
            lessonList.add(new Lesson("Chemistry", "M. Curie", "12th grade"));
            lessonList.add(new Lesson("French", "M. Curie", "12th grade"));
            lessonList.add(new Lesson("Physics", "M. Curie", "12th grade"));
            lessonList.add(new Lesson("Geography", "C. Darwin", "12th grade"));
            lessonList.add(new Lesson("Biology", "C. Darwin", "12th grade"));
            lessonList.add(new Lesson("Geology", "C. Darwin", "12th grade"));
            lessonList.add(new Lesson("History", "I. Jones", "12th grade"));
            lessonList.add(new Lesson("History", "I. Jones", "12th grade"));
            lessonList.add(new Lesson("English", "P. Cruz", "12th grade"));
            lessonList.add(new Lesson("English", "P. Cruz", "12th grade"));
            lessonList.add(new Lesson("English", "P. Cruz", "12th grade"));
            lessonList.add(new Lesson("Spanish", "P. Cruz", "12th grade"));
            lessonList.add(new Lesson("Drama", "P. Cruz", "12th grade"));
            lessonList.add(new Lesson("Art", "S. Dali", "12th grade"));
            lessonList.add(new Lesson("Art", "S. Dali", "12th grade"));
            lessonList.add(new Lesson("Physical education", "C. Lewis", "12th grade"));
            lessonList.add(new Lesson("Physical education", "C. Lewis", "12th grade"));
            lessonList.add(new Lesson("Physical education", "C. Lewis", "12th grade"));
        }

        Lesson lesson = lessonList.get(0);
        lesson.setTimeslot(timeslotList.get(0));
        lesson.setRoom(roomList.get(0));

        lessonRepository.persist(lessonList);
    }

    public enum DemoData {
        NONE,
        SMALL,
        LARGE
    }

}
