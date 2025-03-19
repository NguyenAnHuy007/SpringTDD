package com.samsung.springtdd.models.DTO;

public class RegistrationRequest {
    private String email;
    private Long courseId;

    public String getEmail() {
        return email;
    }

    public Long getCourseId() {
        return courseId;
    }
}
