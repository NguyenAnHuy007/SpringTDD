package com.samsung.springtdd.models.repository;

import com.samsung.springtdd.models.Course;
import com.samsung.springtdd.models.Registration;
import com.samsung.springtdd.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByStudentAndCourseStartTimeAfter(Student student, LocalDateTime now);
    Optional<Registration> findByStudentAndCourse(Student student, Course course);
    int countByStudentAndCourseStartTimeLessThanAndCourseEndTimeGreaterThan(Student student, LocalDateTime startTime, LocalDateTime endTime);
    int countByStudentAndCourseStartTimeBeforeAndCourseEndTimeAfter(Student student, LocalDateTime start, LocalDateTime end);
}
