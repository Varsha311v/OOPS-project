package com.university.attendance;

import com.university.attendance.Models.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AttendanceDao {
    private final JdbcTemplate jdbc;
    public AttendanceDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Insert, or update the status if that student/subject/date already exists. */
    public void mark(Attendance a) {
        jdbc.update("INSERT INTO attendance (student_id,subject_id,att_date,status) VALUES (?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE status=VALUES(status)",
                a.studentId(), a.subjectId(), Date.valueOf(a.date()), a.status());
    }
    public int delete(long id) { return jdbc.update("DELETE FROM attendance WHERE id=?", id); }

    public List<Attendance> find(Long studentId, Long subjectId, LocalDate date) {
        StringBuilder sql = new StringBuilder("SELECT * FROM attendance WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (studentId != null) { sql.append(" AND student_id=?"); args.add(studentId); }
        if (subjectId != null) { sql.append(" AND subject_id=?"); args.add(subjectId); }
        if (date != null) { sql.append(" AND att_date=?"); args.add(Date.valueOf(date)); }
        sql.append(" ORDER BY att_date DESC");
        return jdbc.query(sql.toString(), (rs, i) -> new Attendance(rs.getLong("id"), rs.getLong("student_id"),
                rs.getLong("subject_id"), rs.getDate("att_date").toLocalDate(), rs.getString("status")), args.toArray());
    }

    /** Subject-wise percentage for one student. */
    public List<Summary> subjectWise(long studentId) {
        return jdbc.query("SELECT s.id sid, s.roll_no, s.name, sub.name subject, COUNT(*) total, " +
                "SUM(a.status='PRESENT') present FROM attendance a JOIN students s ON s.id=a.student_id " +
                "JOIN subjects sub ON sub.id=a.subject_id WHERE s.id=? GROUP BY s.id, s.roll_no, s.name, sub.id, sub.name ORDER BY sub.name",
                (rs, i) -> summary(rs.getLong("sid"), rs.getString("roll_no"), rs.getString("name"), rs.getString("subject"),
                        rs.getInt("total"), rs.getInt("present")), studentId);
    }

    /** Students whose overall (or one subject's) percentage is below the threshold. */
    public List<Summary> below(double threshold, Long subjectId) {
        String label = subjectId == null ? "Overall" : "Selected subject";
        String sql = "SELECT s.id sid, s.roll_no, s.name, COUNT(*) total, SUM(a.status='PRESENT') present " +
                "FROM attendance a JOIN students s ON s.id=a.student_id " +
                (subjectId == null ? "" : "WHERE a.subject_id=" + Long.valueOf(subjectId) + " ") +
                "GROUP BY s.id, s.roll_no, s.name HAVING SUM(a.status='PRESENT')*100.0/COUNT(*) < ? ORDER BY SUM(a.status='PRESENT')*100.0/COUNT(*)";
        return jdbc.query(sql, (rs, i) -> summary(rs.getLong("sid"), rs.getString("roll_no"), rs.getString("name"), label,
                rs.getInt("total"), rs.getInt("present")), threshold);
    }

    private Summary summary(long id, String roll, String name, String subject, int total, int present) {
        double pct = total == 0 ? 0 : Math.round(present * 10000.0 / total) / 100.0;
        return new Summary(id, roll, name, subject, total, present, pct);
    }
}
