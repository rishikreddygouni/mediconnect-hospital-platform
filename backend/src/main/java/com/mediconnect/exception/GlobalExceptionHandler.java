package com.mediconnect.exception;
import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import org.springframework.web.server.ResponseStatusException;import java.time.LocalDateTime;import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class) ResponseEntity<Map<String,Object>> status(ResponseStatusException e){return body(e.getStatusCode(),e.getReason()==null?"Request failed":e.getReason());}
    @ExceptionHandler(Exception.class) ResponseEntity<Map<String,Object>> generic(Exception e){return body(HttpStatus.INTERNAL_SERVER_ERROR,"An unexpected server error occurred");}
    private ResponseEntity<Map<String,Object>> body(HttpStatusCode status,String message){return ResponseEntity.status(status).body(Map.of("timestamp",LocalDateTime.now().toString(),"status",status.value(),"message",message));}
}
