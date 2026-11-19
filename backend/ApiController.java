package com.university.attendance;

import com.university.attendance.Models.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final StudentDao students; private final SubjectDao subjects; private final AttendanceDao attendance;
    public ApiController(StudentDao s, SubjectDao sub, AttendanceDao a) { students = s; subjects = sub; attendance = a; }

    // ---- Students: add, update, delete, search ----
    @GetMapping("/students")
    public List<Student> students(@RequestParam(required = false) String q) {
        return (q == null || q.isBlank()) ? students.findAll() : students.search(q.trim());
    }
    @PostMapping("/students") public Student addStudent(@RequestBody Student s) { return students.add(s); }
    @PutMapping("/students/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable long id, @RequestBody Student s) {
        return students.update(id, s) > 0 ? ResponseEntity.ok(s) : ResponseEntity.notFound().build();
    }
    @DeleteMapping("/students/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable long id) {
        return students.delete(id) > 0 ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // ---- Subjects ----
    @GetMapping("/subjects") public List<Subject> subjects() { return subjects.findAll(); }
    @PostMapping("/subjects") public Subject addSubject(@RequestBody Subject s) { return subjects.add(s); }
    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable long id) {
        return subjects.delete(id) > 0 ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // ---- Attendance (subject-wise, date-wise) ----
    @PostMapping("/attendance")
    public ResponseEntity<?> mark(@RequestBody List<Attendance> records) {
        records.forEach(attendance::mark);
        return ResponseEntity.ok(Map.of("saved", records.size()));
    }
    @GetMapping("/attendance")
    public List<Attendance> attendance(@RequestParam(required = false) Long studentId, @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attendance.find(studentId, subjectId, date);
    }
    @DeleteMapping("/attendance/{id}")
    public ResponseEntity<?> deleteAttendance(@PathVariable long id) {
        return attendance.delete(id) > 0 ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // ---- Reports ----
    @GetMapping("/reports/student/{id}") public List<Summary> studentReport(@PathVariable long id) { return attendance.subjectWise(id); }
    @GetMapping("/reports/low")
    public List<Summary> low(@RequestParam(defaultValue = "75") double threshold, @RequestParam(required = false) Long subjectId) {
        return attendance.below(threshold, subjectId);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> conflict(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Duplicate or invalid reference (roll no / code already exists, or student/subject missing)."));
    }
}
