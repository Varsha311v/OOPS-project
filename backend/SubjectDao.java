package com.university.attendance;

import com.university.attendance.Models.Subject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.Statement;
import java.util.List;

@Repository
public class SubjectDao {
    private final JdbcTemplate jdbc;
    public SubjectDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Subject> findAll() {
        return jdbc.query("SELECT * FROM subjects ORDER BY code", (rs, i) -> new Subject(rs.getLong("id"), rs.getString("code"), rs.getString("name")));
    }
    public Subject add(Subject s) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(c -> {
            var ps = c.prepareStatement("INSERT INTO subjects (code,name) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, s.code()); ps.setString(2, s.name());
            return ps;
        }, kh);
        return new Subject(kh.getKey().longValue(), s.code(), s.name());
    }
    public int delete(long id) { return jdbc.update("DELETE FROM subjects WHERE id=?", id); }
}
