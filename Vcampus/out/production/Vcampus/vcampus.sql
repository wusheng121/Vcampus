CREATE DATABASE vcampus CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE vcampus;

CREATE TABLE user (
    user_id VARCHAR(20) PRIMARY KEY,
    password VARCHAR(64) NOT NULL,
    role ENUM('student','teacher','admin') NOT NULL,
    name VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO user(user_id,password,role)
VALUES
('admin1','123456','admin'),
('teacher1','123456','teacher'),
('student1','123456','student');
