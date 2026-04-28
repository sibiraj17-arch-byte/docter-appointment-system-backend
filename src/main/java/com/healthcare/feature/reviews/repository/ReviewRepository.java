package com.healthcare.feature.reviews.repository;

import com.healthcare.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByAppointmentId(Long appointmentId);
    List<Review> findByDoctorId(Long doctorId);
    List<Review> findByPatientId(Long patientId);
    boolean existsByAppointmentId(Long appointmentId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.doctor.id = :profId")
    Double getAverageRating(@Param("profId") Long profId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.doctor.id = :profId")
    long countByProfessionalId(@Param("profId") Long profId);
    
    @Query("SELECT new com.healthcare.feature.reviews.dto.RatingStatsDTO(" +
           "AVG(r.rating), COUNT(r), " +
           "SUM(CASE WHEN r.rating = 5 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.rating = 4 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.rating = 3 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.rating = 2 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.rating = 1 THEN 1 ELSE 0 END)) " +
           "FROM Review r WHERE r.doctor.id = :profId")
    Object[] getRatingStats(@Param("profId") Long profId);
}
