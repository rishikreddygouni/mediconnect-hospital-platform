package com.mediconnect.repository;
import com.mediconnect.entity.Role;
import com.mediconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<User> findByRoleAndActiveTrueAndApprovedTrueOrderByNameAsc(Role role);
    List<User> findByHospitalIdAndRoleAndActiveTrueAndApprovedTrueOrderByNameAsc(Long hospitalId, Role role);
    List<User> findByHospitalId(Long hospitalId);
    List<User> findByHospitalIdAndRole(Long hospitalId, Role role);
    long countByRole(Role role);
    long countByHospitalIdAndRole(Long hospitalId, Role role);
}
