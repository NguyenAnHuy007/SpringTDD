package com.samsung.springtdd.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "registrations")
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "StudentId")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "CourseId")
    private Course course;

    @Column(name = "Price")
    private Long price;

    @Column(name = "RegisteredDate")
    private LocalDateTime registeredDate;

    public Registration(Student student, Course course, long price, LocalDateTime registeredDate) {
        this.student = student;
        this.course = course;
        this.price = price;
        this.registeredDate = registeredDate;
    }
}
