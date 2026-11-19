package com.university.attendance;

import java.time.LocalDate;

public class Models {
    public record Student(Long id, String rollNo, String name, String email, String course) {}
    public record Subject(Long id, String code, String name) {}
    public record Attendance(Long id, Long studentId, Long subjectId, LocalDate date, String status) {}
    public record Summary(Long studentId, String rollNo, String name, String subject, int total, int present, double percentage) {}
}
