package com.samsung.springtdd.models.repository;

import com.samsung.springtdd.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
