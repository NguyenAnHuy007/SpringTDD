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


    @Autowired
    public RegistrationService(StudentRepository studentRepository, CourseRepository courseRepository, RegistrationRepository registrationRepository, Clock clock) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.registrationRepository = registrationRepository;
        this.clock = clock;
    }

    public List<Course> register(String email, Long courseId) {
        // Lấy thời gian hiện tại
        LocalDateTime now = LocalDateTime.now(clock);

        // Tìm sinh viên theo email
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        // Tìm khóa học theo ID
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        // Kiểm tra khóa học chưa bắt đầu
        if (course.getStartTime().isBefore(now)) {
            throw new IllegalArgumentException("Course has already started");
        }

        // Kiểm tra sinh viên chưa đăng ký khóa học này
        if (registrationRepository.findByStudentAndCourse(student, course).isPresent()) {
            throw new IllegalArgumentException("Already registered for this course");
        }

        // Đếm số lượng khóa học đang diễn ra
        int ongoingCourses = registrationRepository.countByStudentAndCourseStartTimeBeforeAndCourseEndTimeAfter(student, now, now);

        // Tính giá khóa học
        long price = course.getPrice();
        if (ongoingCourses >= 2) {
            price = (long) (price * 0.75); // Giảm 25%
        }

        // Tạo và lưu đăng ký
        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setCourse(course);
        registration.setPrice(price);
        registration.setRegisteredDate(now);
        registrationRepository.save(registration);

        // Trả về danh sách các khóa học chưa bắt đầu
        List<Registration> futureRegistrations = registrationRepository.findByStudentAndCourseStartTimeAfter(student, now);
        return futureRegistrations.stream()
                .map(Registration::getCourse)
                .collect(Collectors.toList());
    }

    public void unregister(Long courseId, String email) {
        LocalDateTime now = LocalDateTime.now(); // Lấy thời gian hiện tại

        // Tìm sinh viên theo email
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        // Tìm khóa học theo ID
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        // Kiểm tra xem khóa học đã diễn ra chưa
        if (course.getStartTime().isBefore(now)) {
            throw new IllegalArgumentException("Cannot unregister from a course that has already started");
        }

        // Tìm bản ghi đăng ký
        Registration registration = registrationRepository.findByStudentAndCourse(student, course)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        // Xóa bản ghi đăng ký
        registrationRepository.delete(registration);
    }
}
