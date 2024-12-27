package org.acme.schooltimetabling.domain.dto;

public class CourseRequest {
    private String facultyCourseSection;
    private int hoursPerWeek;
    private int combinedSlotsPerDay;

    // Default constructor
    public CourseRequest() {}

    // Getters and setters
    public String getFacultyCourseSection() {
        return facultyCourseSection;
    }

    public void setFacultyCourseSection(String facultyCourseSection) {
        this.facultyCourseSection = facultyCourseSection;
    }

    public int getHoursPerWeek() {
        return hoursPerWeek;
    }

    public void setHoursPerWeek(int hoursPerWeek) {
        this.hoursPerWeek = hoursPerWeek;
    }

    public int getCombinedSlotsPerDay() {
        return combinedSlotsPerDay;
    }

    public void setCombinedSlotsPerDay(int combinedSlotsPerDay) {
        this.combinedSlotsPerDay = combinedSlotsPerDay;
    }
}