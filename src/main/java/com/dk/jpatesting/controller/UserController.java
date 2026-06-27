package com.dk.jpatesting.controller;


import com.dk.jpatesting.dto.request.CreateUserRequest;
import com.dk.jpatesting.dto.request.UpdateUserRequest;
import com.dk.jpatesting.dto.response.UserResponse;
import com.dk.jpatesting.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new User")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request ){
        log.debug("POST /users - creating user with email: {}", request.getEmail());
        UserResponse userResponse = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id){
        log.debug("GET /users/{} - fetching user", id);
        UserResponse userResponse = userService.getUserById(id);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.debug("GET /users - fetching all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active users")
    public ResponseEntity<List<UserResponse>> getActiveUsers(){
        log.debug("GET /users/active - fetching active users");
        return ResponseEntity.ok(userService.getActiveUsers());
    }

    @GetMapping("/search")
    @Operation(summary = "Get user by email")
    public ResponseEntity<UserResponse> getUserByEmail(
            @Parameter(description = "user email address")
            @RequestParam String email) {
        log.debug("GET /users/search?email={} - fetching user", email);
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "user not found"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.debug("PUT /users/{} - updating user", id);
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "user Id") @PathVariable Long id) {
        log.debug("DELETE /users/{} - deleting user", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


}
