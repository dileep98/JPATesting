package com.dk.jpatesting.controller;


import com.dk.jpatesting.config.AppProperties;
import com.dk.jpatesting.dto.request.CreateUserRequest;
import com.dk.jpatesting.dto.request.UpdateUserRequest;
import com.dk.jpatesting.dto.response.PageResponse;
import com.dk.jpatesting.dto.response.UserResponse;
import com.dk.jpatesting.entity.UserStatus;
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

    private final AppProperties appProperties;

    @PostMapping
    @Operation(summary = "Create a new User")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
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
            @Parameter(description = "User ID") @PathVariable Long id) {
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
    public ResponseEntity<List<UserResponse>> getActiveUsers() {
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


    @GetMapping("/paged")
    @Operation(summary = "Get all users with pagination and sorting")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    })
    public ResponseEntity<PageResponse<UserResponse>> getAllUsersPaged(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String sortDir) {
        int p = page != null ? page : 0;
        int s = size != null ? Math.min(size, appProperties.getPagination().getMaxPageSize())
                                : appProperties.getPagination().getDefaultPageSize();
        String sb = sortBy != null ? sortBy : appProperties.getPagination().getDefaultSortBy();
        String sd = sortDir != null ? sortDir : appProperties.getPagination().getDefaultSortDir();
        log.debug("GET /users/paged page={}, size={}, sortBy={}, sortDir={}", page, size, sortBy, sortDir);
        return ResponseEntity.ok(userService.getAllUsersPaged(p, s, sb, sd));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get users by status with pagination")
    public ResponseEntity<PageResponse<UserResponse>> getUsersByStatus(
            @Parameter(description = "User status") @PathVariable UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("GET /users/status/{} page={}, size={}", status, page, size);
        return ResponseEntity.ok(userService.getUsersByStatus(status, page, size));
    }

    @GetMapping("/search/paged")
    @Operation(summary = "Search users by name with pagination")
    public ResponseEntity<PageResponse<UserResponse>> searchUsers(
            @Parameter(description = "First name to search") @RequestParam String firstName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("GET /users/search/paged?firstName={}", firstName);
        return ResponseEntity.ok(userService.searchUsersByName(firstName, page, size));
    }


}
