package com.dk.jpatesting.service.impl;

import com.dk.jpatesting.dto.request.CreateUserRequest;
import com.dk.jpatesting.dto.request.UpdateUserRequest;
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
}
