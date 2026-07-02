package com.dk.jpatesting.service;

import com.dk.jpatesting.dto.request.CreateUserRequest;
import com.dk.jpatesting.dto.request.UpdateUserRequest;
import com.dk.jpatesting.dto.response.PageResponse;
import com.dk.jpatesting.dto.response.UserResponse;
import com.dk.jpatesting.entity.UserStatus;

import java.util.List;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    List<UserResponse> getAllUsers();

    List<UserResponse> getActiveUsers();

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    PageResponse<UserResponse> getAllUsersPaged(int page, int size, String sortBy, String sortDir);

    PageResponse<UserResponse> getUsersByStatus(UserStatus status, int page, int size);

    PageResponse<UserResponse> searchUsersByName(String firstName, int page, int size);

}
