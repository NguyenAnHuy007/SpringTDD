package com.samsung.springtdd.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Name")
    private String name;

    @Column(name = "StartTime")
    private LocalDateTime startTime;

    @Column(name = "EndTime")
    private LocalDateTime endTime;

    @Column(name = "Price")
    private Long price;
}