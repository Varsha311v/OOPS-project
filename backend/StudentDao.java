package com.university.attendance;

import com.university.attendance.Models.Student;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.Statement;
import java.util.List;

@Repository
public class StudentDao {
    private final JdbcTemplate jdbc;
    public StudentDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<Student> mapper = (rs, i) -> new Student(rs.getLong("id"), rs.getString("roll_no"),
            rs.getString("name"), rs.getString("email"), rs.getString("course"));

    public Student add(Student s) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(c -> {
            var ps = c.prepareStatement("INSERT INTO students (roll_no,name,email,course) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, s.rollNo()); ps.setString(2, s.name()); ps.setString(3, s.email()); ps.setString(4, s.course());
            return ps;
        }, kh);
        return new Student(kh.getKey().longValue(), s.rollNo(), s.name(), s.email(), s.course());
    }
    public int update(long id, Student s) {
        return jdbc.update("UPDATE students SET roll_no=?, name=?, email=?, course=? WHERE id=?",
                s.rollNo(), s.name(), s.email(), s.course(), id);
    }
    public int delete(long id) { return jdbc.update("DELETE FROM students WHERE id=?", id); }
    public List<Student> findAll() { return jdbc.query("SELECT * FROM students ORDER BY roll_no", mapper); }
    public List<Student> search(String q) {
        String like = "%" + q + "%";
        return jdbc.query("SELECT * FROM students WHERE roll_no LIKE ? OR name LIKE ? OR course LIKE ? ORDER BY roll_no", mapper, like, like, like);
    }
}
