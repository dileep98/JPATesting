package com.dk.jpatesting.controller;

import com.dk.jpatesting.dto.request.CreateUserRequest;
import com.dk.jpatesting.dto.request.UpdateUserRequest;
import com.dk.jpatesting.dto.response.UserResponse;
import com.dk.jpatesting.entity.UserStatus;
import com.dk.jpatesting.exception.DuplicateEmailException;
import com.dk.jpatesting.exception.UserNotFoundException;
import com.dk.jpatesting.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private UserService userService;

    private UserResponse userResponse;
    private CreateUserRequest validRequest;

    @BeforeEach
    void setUp() {
        userResponse = UserResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+11234567890")
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validRequest = CreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+11234567890")
                .build();
    }

    @Nested
    @DisplayName("POST /users")
    class CreateUser {

        @Test
        @DisplayName("should return 201 when user created successfully")
        void shouldReturn201WhenCreated() throws Exception {
            when(userService.createUser(any())).thenReturn(userResponse);

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("should return 400 when request is invalid")
        void shouldReturn400WhenInvalid() throws Exception {
            CreateUserRequest invalidRequest = CreateUserRequest.builder()
                    .firstName("")           // blank — fails @NotBlank
                    .email("not-an-email")  // fails @Email
                    .build();

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.fieldErrors.email").exists())
                    .andExpect(jsonPath("$.fieldErrors.firstName").exists());
        }

        @Test
        @DisplayName("should return 409 when email already exists")
        void shouldReturn409WhenEmailDuplicate() throws Exception {
            when(userService.createUser(any()))
                    .thenThrow(new DuplicateEmailException("john.doe@example.com"));

            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("Duplicate Email"));
        }
    }

    @Nested
    @DisplayName("GET /users/{id}")
    class GetUserById {

        @Test
        @DisplayName("should return 200 with user when found")
        void shouldReturn200WhenFound() throws Exception {
            when(userService.getUserById(1L)).thenReturn(userResponse);

            mockMvc.perform(get("/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.firstName").value("John"));
        }

        @Test
        @DisplayName("should return 404 when user not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(userService.getUserById(99L))
                    .thenThrow(new UserNotFoundException(99L));

            mockMvc.perform(get("/users/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("User Not Found"));
        }
    }

    @Nested
    @DisplayName("DELETE /users/{id}")
    class DeleteUser {

        @Test
        @DisplayName("should return 204 when deleted successfully")
        void shouldReturn204WhenDeleted() throws Exception {
            mockMvc.perform(delete("/users/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when user not found")
        void shouldReturn404WhenNotFound() throws Exception {
            org.mockito.Mockito.doThrow(new UserNotFoundException(99L))
                    .when(userService).deleteUser(99L);

            mockMvc.perform(delete("/users/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /users")
    class GetAllUsers {

        @Test
        @DisplayName("should return 200 with all users")
        void shouldReturnAllUsers() throws Exception {
            when(userService.getAllUsers()).thenReturn(List.of(userResponse));

            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Nested
    @DisplayName("PUT /users/{id}")
    class UpdateUser {

        @Test
        @DisplayName("should return 200 when updated")
        void shouldReturn200WhenUpdated() throws Exception {
            when(userService.updateUser(eq(1L), any())).thenReturn(userResponse);

            UpdateUserRequest update = UpdateUserRequest.builder()
                    .firstName("Jane").build();

            mockMvc.perform(put("/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("John"));
        }
    }
}