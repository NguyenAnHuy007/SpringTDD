package com.samsung.springtdd.controllers;

import com.samsung.springtdd.models.Course;
import com.samsung.springtdd.models.DTO.RegistrationRequest;
import com.samsung.springtdd.services.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RegistrationController {
    @Autowired
    private RegistrationService registrationService;

    @PostMapping("/register")
    public List<Course> register(@RequestBody RegistrationRequest request) {
        return registrationService.register(request.getEmail(), request.getCourseId());
    }

    @DeleteMapping("/unregister/{courseId}/{email}")
    public ResponseEntity<Void> unregister(@PathVariable Long courseId, @PathVariable String email) {
        registrationService.unregister(courseId, email); // Gọi phương thức service
        return ResponseEntity.noContent().build(); // Trả về 204 No Content
    }
}
