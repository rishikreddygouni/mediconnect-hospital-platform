package com.mediconnect.controller;
import com.mediconnect.dto.ApiDtos;import com.mediconnect.entity.User;import com.mediconnect.service.AppointmentService;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.web.bind.annotation.*;import java.util.List;import java.util.Map;
@RestController @RequestMapping("/api/appointments")
public class AppointmentController {
 private final AppointmentService service;
 public AppointmentController(AppointmentService service){this.service=service;}
 @GetMapping public List<ApiDtos.AppointmentView> all(@AuthenticationPrincipal User u,@RequestParam(required=false)String status){return service.list(u,status).stream().map(ApiDtos.AppointmentView::of).toList();}
 @PostMapping @PreAuthorize("hasRole('PATIENT')") public ApiDtos.AppointmentView book(@AuthenticationPrincipal User u,@RequestBody Map<String,Object>b){return ApiDtos.AppointmentView.of(service.book(u,b));}
 @PutMapping("/{id}/status") public ApiDtos.AppointmentView status(@AuthenticationPrincipal User u,@PathVariable Long id,@RequestBody Map<String,Object>b){return ApiDtos.AppointmentView.of(service.status(id,u,b));}
 @PutMapping("/{id}/cancel") public ApiDtos.AppointmentView cancel(@AuthenticationPrincipal User u,@PathVariable Long id,@RequestBody Map<String,Object>b){return ApiDtos.AppointmentView.of(service.cancel(id,u,b));}
 @PutMapping("/{id}/reschedule") public ApiDtos.AppointmentView reschedule(@AuthenticationPrincipal User u,@PathVariable Long id,@RequestBody Map<String,Object>b){return ApiDtos.AppointmentView.of(service.reschedule(id,u,b));}
 @GetMapping("/{id}/reschedules") public List<ApiDtos.AppointmentRescheduleView> reschedules(@AuthenticationPrincipal User u,@PathVariable Long id){return service.rescheduleHistory(id,u).stream().map(ApiDtos.AppointmentRescheduleView::of).toList();}
}
