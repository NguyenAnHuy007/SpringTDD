package com.samsung.springtdd.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samsung.springtdd.models.Course;
import com.samsung.springtdd.models.DTO.RegistrationRequest;
import com.samsung.springtdd.services.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RegistrationController.class)
public class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrationService registrationService;

    @Test
    void should_return_register_successfully_and_return_course_list() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request = objectMapper.readValue("{\"email\":\"test@example.com\",\"courseId\":1}", RegistrationRequest.class);

        Course course = Course.builder()
                .id(1L)
                .name("Future Course")
                .startTime(LocalDateTime.of(2023, 1, 2, 10, 0))
                .endTime(LocalDateTime.of(2023, 1, 3, 10, 0))
                .price(1000L)
                .build();

        when(registrationService.register(anyString(), anyLong())).thenReturn(List.of(course));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Future Course"));
    }

    @Test
    void should_return_unregister_successfully() throws Exception {
        doNothing().when(registrationService).unregister(anyLong(), anyString());

        mockMvc.perform(delete("/unregister/1/test@example.com"))
                .andExpect(status().isNoContent());
    }
}
