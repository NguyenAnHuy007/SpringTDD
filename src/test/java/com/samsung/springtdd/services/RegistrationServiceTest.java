package com.samsung.springtdd.services;

import com.samsung.springtdd.models.Course;
import com.samsung.springtdd.models.Registration;
import com.samsung.springtdd.models.Student;
import com.samsung.springtdd.models.repository.CourseRepository;
import com.samsung.springtdd.models.repository.RegistrationRepository;
import com.samsung.springtdd.models.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private RegistrationService registrationService;

    private final LocalDateTime fixedDateTime = LocalDateTime.of(2023, 1, 1, 10, 0);
    private final Clock fixedClock = Clock.fixed(Instant.parse("2023-01-01T10:00:00Z"), ZoneId.systemDefault());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(registrationService).build();
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    void should_return_register_successfully_and_return_course_list() {

        Student student = Student.builder().id(1L).email("test@example.com").build();

        Course course = Course.builder()
                .id(1L)
                .name("Future Course")
                .startTime(LocalDateTime.of(2023, 1, 2, 10, 0)) // Chưa bắt đầu
                .endTime(LocalDateTime.of(2023, 1, 3, 10, 0))
                .price(1000L)
                .build();

        LocalDateTime now = LocalDateTime.now(fixedClock); // Sử dụng cùng cách tính thời gian như trong service

        when(studentRepository.findByEmail("test@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(registrationRepository.findByStudentAndCourse(student, course)).thenReturn(Optional.empty());
        when(registrationRepository.countByStudentAndCourseStartTimeLessThanAndCourseEndTimeGreaterThan(any(), any(), any())).thenReturn(0);
        when(registrationRepository.save(any(Registration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(registrationRepository.findByStudentAndCourseStartTimeAfter(any(Student.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new Registration(student, course, 1000L, now)));

        List<Course> result = registrationService.register("test@example.com", 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Future Course", result.get(0).getName());

        verify(registrationRepository, times(1)).save(any(Registration.class));
    }

    @Test
    void should_return_exception_for_course_already_started() {
        Student student = Student.builder().id(1L).email("test@example.com").build();

        Course course = Course.builder()
                .id(1L)
                .name("Past Course")
                .startTime(LocalDateTime.of(2022, 12, 1, 10, 0)) // Đã bắt đầu
                .endTime(LocalDateTime.of(2023, 1, 1, 10, 0))
                .build();

        when(studentRepository.findByEmail("test@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        assertThrows(IllegalArgumentException.class, () -> {
            registrationService.register("test@example.com", 1L);
        });

        verify(registrationRepository, times(0)).save(any());
    }

    @Test
    void should_return_exception_for_course_already_registed() {
        Student student = Student.builder().id(1L).email("test@example.com").build();

        Course course = Course.builder()
                .id(1L)
                .name("Future Course")
                .startTime(LocalDateTime.of(2023, 1, 2, 10, 0))
                .endTime(LocalDateTime.of(2023, 1, 3, 10, 0))
                .price(1000L)
                .build();

        when(studentRepository.findByEmail("test@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(registrationRepository.findByStudentAndCourse(student, course))
                .thenReturn(Optional.of(new Registration(student, course, course.getPrice(), LocalDateTime.now(clock))));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                registrationService.register("test@example.com", 1L)
        );
        assertEquals("Already registered for this course", exception.getMessage());

        verify(registrationRepository, never()).save(any(Registration.class));
    }


    @Test
    void should_return_exception_for_student_does_not_exist() {
        when(studentRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            registrationService.register("unknown@example.com", 1L);
        });

        verify(registrationRepository, times(0)).save(any());
    }

    @Test
    void should_return_exception_for_course_not_exist() {
        Student student = Student.builder().id(1L).email("test@example.com").build();

        when(studentRepository.findByEmail("test@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.register("test@example.com", 1L);
        });
        assertEquals("Course not found", exception.getMessage());

        verify(registrationRepository, times(0)).save(any(Registration.class));
    }

    @Test
    public void should_return_unregister_successfully() throws Exception {
        Student student = new Student();
        student.setId(1L);
        student.setEmail("test@example.com");

        Course course = new Course();
        course.setId(1L);
        course.setStartTime(LocalDateTime.now().plusDays(1));

        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setCourse(course);

        when(studentRepository.findByEmail("test@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(registrationRepository.findByStudentAndCourse(student, course)).thenReturn(Optional.of(registration));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/unregister/1/test@example.com"))
                .andExpect(status().isNoContent());

        verify(registrationRepository, times(1)).delete(registration);
    }


    @Test
    void should_return_exception_for_unregister_with_nonexistent_email() {
        String nonExistentEmail = "nonexistent@example.com";
        when(studentRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                registrationService.unregister(1L, nonExistentEmail));
        assertEquals("Student not found", exception.getMessage());

        verifyNoInteractions(courseRepository, registrationRepository);
    }

    @Test
    void should_return_exception_for_unregister_with_nonexistent_course() {
        Student student = Student.builder().id(1L).email("test@example.com").build();
        when(studentRepository.findByEmail("test@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                registrationService.unregister(999L, "test@example.com"));
        assertEquals("Course not found", exception.getMessage());

        verify(registrationRepository, never()).delete(any(Registration.class));
    }

    @Test
    void should_return_exception_for_unregister_course_already_started() {
        // Arrange
        Student student = Student.builder().id(1L).email("test@example.com").build();
        // Course started in the past
        Course course = Course.builder()
                .id(1L)
                .name("Started Course")
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().plusDays(1))
                .price(1000L)
                .build();

        when(studentRepository.findByEmail("test@example.com")).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                registrationService.unregister(1L, "test@example.com")
        );
        assertEquals("Cannot unregister from a course that has already started", exception.getMessage());
        verify(registrationRepository, never()).delete(any(Registration.class));
    }

}