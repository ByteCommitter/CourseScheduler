package org.acme.schooltimetabling.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Timeslot {

    @Id
    @GeneratedValue
    private Long id;

    private Integer dayOfWeek; // Changed from int to Integer
    private Integer slot;      // Changed from int to Integer

    // No-arg constructor required for Hibernate and OptaPlanner
    public Timeslot() {
    }

    public Timeslot(Long id, Integer dayOfWeek, Integer slot) {
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.slot = slot;
    }

    @Override
    public String toString() {
        return "Day " + dayOfWeek + ", Slot " + slot;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Integer getDayOfWeek() { // Changed return type to Integer
        return dayOfWeek;
    }

    public Integer getSlot() { // Changed return type to Integer
        return slot;
    }
}