package com.mediconnect.repository;

import com.mediconnect.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    Optional<Hospital> findBySlugIgnoreCase(String slug);
    List<Hospital> findByActiveTrueOrderByNameAsc();
    boolean existsBySlugIgnoreCase(String slug);
}
