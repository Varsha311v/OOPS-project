CREATE TABLE IF NOT EXISTS students (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  roll_no VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  course VARCHAR(100)
);
CREATE TABLE IF NOT EXISTS subjects (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL
);
CREATE TABLE IF NOT EXISTS attendance (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  student_id BIGINT NOT NULL,
  subject_id BIGINT NOT NULL,
  att_date DATE NOT NULL,
  status VARCHAR(10) NOT NULL,
  UNIQUE KEY uq_att (student_id, subject_id, att_date),
  FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
  FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);
INSERT IGNORE INTO students (roll_no,name,email,course) VALUES
 ('CS101','Aarav Sharma','aarav@uni.edu','B.Tech CSE'),
 ('CS102','Simran Kaur','simran@uni.edu','B.Tech CSE'),
 ('CS103','Rohan Verma','rohan@uni.edu','B.Tech CSE');
INSERT IGNORE INTO subjects (code,name) VALUES ('DBMS','Database Management'),('OOPS','Object Oriented Programming');
