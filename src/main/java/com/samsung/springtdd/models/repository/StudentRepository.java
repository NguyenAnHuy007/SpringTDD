package com.samsung.springtdd.models.repository;

import com.samsung.springtdd.models.Course;
import com.samsung.springtdd.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
}
