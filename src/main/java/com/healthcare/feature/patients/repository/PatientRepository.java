package com.healthcare.feature.patients.repository;

import com.healthcare.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUserId(Long userId);
    Optional<Patient> findByUserMobileNumber(String mobileNumber);
    List<Patient> findByUserIdIn(List<Long> userIds);
}
