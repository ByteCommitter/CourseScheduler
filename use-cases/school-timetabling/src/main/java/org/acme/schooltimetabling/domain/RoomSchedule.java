package org.acme.schooltimetabling.domain;

import java.util.List;

public class RoomSchedule {
    private Room room;
    private List<Lesson> weeklyLessons;

    // Constructors
    public RoomSchedule() {
    }

    public RoomSchedule(Room room, List<Lesson> weeklyLessons) {
        this.room = room;
        this.weeklyLessons = weeklyLessons;
    }

    // Getters and setters
    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public List<Lesson> getWeeklyLessons() {
        return weeklyLessons;
    }

    public void setWeeklyLessons(List<Lesson> weeklyLessons) {
        this.weeklyLessons = weeklyLessons;
    }
}
