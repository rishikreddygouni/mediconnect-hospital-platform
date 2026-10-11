package com.mediconnect.repository;
import com.mediconnect.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByActiveTrueOrderByNameAsc();
    List<Department> findByHospitalIdAndActiveTrueOrderByNameAsc(Long hospitalId);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndHospitalId(String name, Long hospitalId);
    Optional<Department> findByNameIgnoreCase(String name);
    Optional<Department> findByNameIgnoreCaseAndHospitalId(String name, Long hospitalId);
}
