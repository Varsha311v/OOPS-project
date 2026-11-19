# Student Attendance Management System
Spring Boot 3 + JdbcTemplate (JDBC) + MySQL, with a vanilla HTML/JS frontend served by Spring Boot.

## Run
1. Install JDK 17+, Maven, MySQL. Edit username/password in `src/main/resources/application.properties`.
2. `mvn spring-boot:run`
3. Open http://localhost:8080 (database and tables are created automatically from `schema.sql`, with sample data).

## REST API
| Method | URL | Purpose |
|---|---|---|
| GET/POST | /api/students, `?q=` to search | list/search, add |
| PUT/DELETE | /api/students/{id} | update, delete |
| GET/POST/DELETE | /api/subjects[/{id}] | manage subjects |
| POST | /api/attendance | body: list of {studentId, subjectId, date, status} (upsert) |
| GET | /api/attendance?studentId&subjectId&date | date/subject-wise records |
| GET | /api/reports/student/{id} | subject-wise percentage |
| GET | /api/reports/low?threshold=75&subjectId= | students below threshold |

Percentage = present / total classes held x 100.
