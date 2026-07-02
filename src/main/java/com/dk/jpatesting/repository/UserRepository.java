package com.dk.jpatesting.repository;

import com.dk.jpatesting.entity.User;
import com.dk.jpatesting.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByStatus(UserStatus status);

    List<User> findByFirstNameContainingIgnoreCase(String firstName);
    Page<User> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);
    List<User> findByLastNameContainingIgnoreCase(String lastName);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.status = :status ORDER BY u.createdAt DESC")
    List<User> findAllByStatusOrderByCreatedAtDesc(@Param("status") UserStatus status);

    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmailNative(@Param("email") String email);
}
