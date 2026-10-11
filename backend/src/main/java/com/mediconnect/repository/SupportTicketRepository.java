package com.mediconnect.repository;

import com.mediconnect.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket,Long> {
    List<SupportTicket> findByHospitalIdOrderByCreatedAtDesc(Long hospitalId);
    List<SupportTicket> findByCreatedByIdOrderByCreatedAtDesc(Long userId);
}
