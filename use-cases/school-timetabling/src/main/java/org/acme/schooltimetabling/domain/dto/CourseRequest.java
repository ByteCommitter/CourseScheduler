package org.acme.schooltimetabling.domain.dto;

public class CourseRequest {
    private String facultyCourseSection;
    private int hoursPerWeek;

    // Getters and setters
    public String getFacultyCourseSection() { return facultyCourseSection; }
    public void setFacultyCourseSection(String value) { this.facultyCourseSection = value; }
    public int getHoursPerWeek() { return hoursPerWeek; }
    public void setHoursPerWeek(int value) { this.hoursPerWeek = value; }
}