package com.dk.jpatesting.service.impl;

import com.dk.jpatesting.dto.request.CreateUserRequest;
import com.dk.jpatesting.dto.request.UpdateUserRequest;
import com.dk.jpatesting.dto.response.PageResponse;
import com.dk.jpatesting.dto.response.UserResponse;
import com.dk.jpatesting.entity.User;
import com.dk.jpatesting.entity.UserStatus;
import com.dk.jpatesting.exception.DuplicateEmailException;
import com.dk.jpatesting.exception.UserNotFoundException;
import com.dk.jpatesting.mapper.UserMapper;
import com.dk.jpatesting.repository.UserRepository;
import com.dk.jpatesting.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.debug("Creating user with email: {}", request.getEmail());

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        User user = userMapper.toEntity(request);
        User saveUser = userRepository.save(user);

        log.info("User created successfully with id: {}", saveUser.getId());

        return userMapper.toResponse(saveUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsers() {
        log.debug("Fetching all active users");

        return userRepository.findByStatus(UserStatus.ACTIVE)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.debug("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if(request.getEmail() != null
            && !request.getEmail().equals(user.getEmail())
            && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        userMapper.updateEntityFromRequest(request, user);
        User updatedUser = userRepository.save(user);

        log.info("User updated successfully with id: {}", updatedUser.getId());
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.debug("Deleting user with id: {}", id);

        User user = userRepository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException(id));

        userRepository.delete(user);
        log.info("User deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsersPaged(int page, int size, String sortBy, String sortDir) {
        log.debug("Fetching users page={}, size={}, sortBy={}, sortDir={}", page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> userPage = userRepository.findAll(pageable);

        return toPageResponse(userPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsersByStatus(UserStatus status, int page, int size) {
        log.debug("Fetching users by status={}, page={}, size={}", status, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userRepository.findByStatus(status, pageable);

        return toPageResponse(userPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> searchUsersByName(String firstName, int page, int size) {
        log.debug("Searching users by firstName={}, page={}, size={}", firstName, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
        Page<User> userPage = userRepository.findByFirstNameContainingIgnoreCase(firstName, pageable);

        return toPageResponse(userPage);
    }

    // Helper — converts Spring Page<User> to your PageResponse<UserResponse>
    private PageResponse<UserResponse> toPageResponse(Page<User> userPage) {
        List<UserResponse> content = userPage.getContent()
                .stream()
                .map(userMapper::toResponse)
                .toList();

        return PageResponse.<UserResponse>builder()
                .content(content)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .empty(userPage.isEmpty())
                .build();
    }
}
