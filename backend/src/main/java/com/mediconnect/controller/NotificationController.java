package com.mediconnect.controller;
import com.mediconnect.dto.ApiDtos;import com.mediconnect.entity.*;import com.mediconnect.repository.NotificationRepository;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/notifications") public class NotificationController{
 private final NotificationRepository repo;public NotificationController(NotificationRepository r){repo=r;}
 @GetMapping public List<ApiDtos.NotificationView> mine(@AuthenticationPrincipal User u){return repo.findByUserIdOrderByCreatedAtDesc(u.getId()).stream().map(ApiDtos.NotificationView::of).toList();}
 @PutMapping("/{id}/read") public ApiDtos.NotificationView read(@AuthenticationPrincipal User u,@PathVariable Long id){Notification n=repo.findById(id).orElseThrow();if(!n.getUser().getId().equals(u.getId()))throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,"Not your notification");n.setReadFlag(true);return ApiDtos.NotificationView.of(repo.save(n));}
 @PutMapping("/read-all") public void readAll(@AuthenticationPrincipal User u){repo.findByUserIdOrderByCreatedAtDesc(u.getId()).forEach(n->{n.setReadFlag(true);repo.save(n);});}
}
