package com.healthcare.feature.doctors.repository;

import com.healthcare.entity.Doctor;
import com.healthcare.enums.AvailabilityStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    @EntityGraph(attributePaths = {"user", "specialization"})
    Optional<Doctor> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "specialization"})
    Optional<Doctor> findByUserMobileNumber(String mobileNumber);

    @EntityGraph(attributePaths = {"user", "specialization"})
    List<Doctor> findBySpecializationId(Long specializationId);

    @EntityGraph(attributePaths = {"user", "specialization"})
    List<Doctor> findByAvailabilityStatus(AvailabilityStatus status);

    @EntityGraph(attributePaths = {"user", "specialization"})
    List<Doctor> findByIsVerifiedTrue();
    
    @EntityGraph(attributePaths = {"user", "specialization"})
    @Query("SELECT d FROM Doctor d WHERE d.availabilityStatus = 'AVAILABLE' AND d.isVerified = true")
    List<Doctor> findAvailableDoctors();
    
    @EntityGraph(attributePaths = {"user", "specialization"})
    @Query("SELECT d FROM Doctor d WHERE d.availabilityStatus = 'AVAILABLE' AND d.isVerified = true AND d.specialization.id = :specId")
    List<Doctor> findAvailableBySpecialization(@Param("specId") Long specId);
    
    @EntityGraph(attributePaths = {"user", "specialization"})
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.user.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Doctor> searchByName(@Param("name") String name);
}
