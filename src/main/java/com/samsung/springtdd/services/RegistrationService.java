package com.samsung.springtdd.services;

import com.samsung.springtdd.models.Course;
import com.samsung.springtdd.models.Registration;
import com.samsung.springtdd.models.Student;
import com.samsung.springtdd.models.repository.CourseRepository;
import com.samsung.springtdd.models.repository.RegistrationRepository;
import com.samsung.springtdd.models.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RegistrationService {
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private Clock clock;

    public List<Course> register(String email, Long courseId) {
        LocalDateTime now = LocalDateTime.now(clock);

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getStartTime().isBefore(now)) {
            throw new IllegalArgumentException("Course has already started");
        }

        if (registrationRepository.findByStudentAndCourse(student, course).isPresent()) {
            throw new IllegalArgumentException("Already registered for this course");
        }

        int ongoingCourses = registrationRepository.countByStudentAndCourseStartTimeBeforeAndCourseEndTimeAfter(student, now, now);

        long price = course.getPrice();
        if (ongoingCourses >= 2) {
            price = (long) (price * 0.75);
        }

        Registration registration = Registration.builder()
                .student(student)
                .course(course)
                .price(price)
                .registeredDate(now)
                .build();
        registrationRepository.save(registration);

        List<Registration> futureRegistrations = registrationRepository.findByStudentAndCourseStartTimeAfter(student, now);
        return futureRegistrations.stream()
                .map(Registration::getCourse)
                .collect(Collectors.toList());
    }

    public void unregister(Long courseId, String email) {
        LocalDateTime now = LocalDateTime.now();

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getStartTime().isBefore(now)) {
            throw new IllegalArgumentException("Cannot unregister from a course that has already started");
        }

        Registration registration = registrationRepository.findByStudentAndCourse(student, course)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        registrationRepository.delete(registration);
    }
}
