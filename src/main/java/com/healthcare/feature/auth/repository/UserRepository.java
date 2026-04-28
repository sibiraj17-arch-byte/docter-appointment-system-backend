package com.healthcare.feature.auth.repository;

import com.healthcare.entity.User;
import com.healthcare.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByMobileNumber(String mobileNumber);
    boolean existsByMobileNumber(String mobileNumber);
    boolean existsByEmail(String email);
    List<User> findByRole(UserRole role);
    List<User> findByIsActiveTrue();
}
