package com.healthcare.feature.specializations.repository;

import com.healthcare.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpecializationRepository extends JpaRepository<Specialization, Long> {
    Optional<Specialization> findByName(String name);
    boolean existsByName(String name);
}
