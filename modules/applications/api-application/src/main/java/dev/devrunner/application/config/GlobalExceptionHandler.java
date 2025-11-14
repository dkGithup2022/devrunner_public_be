package dev.devrunner.application.config;

import dev.devrunner.exception.ApplicationException;
import dev.devrunner.exception.BadRequestException;
import dev.devrunner.exception.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Validation 실패 처리 (@Valid, @Validated)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        log.info("Validation failed: {}", e.getMessage());

        Map<String, Object> errors = new HashMap<>();
        errors.put("status", HttpStatus.BAD_REQUEST.value());
        errors.put("error", "Validation Failed");

        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        errors.put("fields", fieldErrors);
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * BadRequestException 처리
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException e) {
        log.warn("Bad request - {}: {}", e.getClass().getSimpleName(), e.getMessage());

        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", e.getClass().getSimpleName());
        error.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * ClientException 처리 (400번대)
     */
    @ExceptionHandler(ClientException.class)
    public ResponseEntity<Map<String, Object>> handleClientException(ClientException e) {
        log.warn("Client error - {}: {}", e.getClass().getSimpleName(), e.getMessage());

        int statusCode = parseStatusCode(e.getStatus(), HttpStatus.BAD_REQUEST.value());

        Map<String, Object> error = new HashMap<>();
        error.put("status", statusCode);
        error.put("error", e.getClass().getSimpleName());
        error.put("message", e.getMessage());
        return ResponseEntity.status(statusCode).body(error);
    }

    /**
     * ApplicationException 처리 (500번대)
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Map<String, Object>> handleApplicationException(ApplicationException e) {
        log.error("Application error - {}: {}", e.getClass().getSimpleName(), e.getMessage(), e);

        int statusCode = parseStatusCode(e.getStatus(), HttpStatus.INTERNAL_SERVER_ERROR.value());

        Map<String, Object> error = new HashMap<>();
        error.put("status", statusCode);
        error.put("error", e.getClass().getSimpleName());
        error.put("message", "오류가 발생했습니다. 지속될 경우 운영자에게 알려주시면 감사하겠습니다.");
        return ResponseEntity.status(statusCode).body(error);
    }

    /**
     * RuntimeException 처리 (fallback)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        log.error("Unexpected error: {}", e.getMessage(), e);

        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "RuntimeException");
        error.put("message", "오류가 발생했습니다. 지속될 경우 운영자에게 알려주시면 감사하겠습니다.");
        return ResponseEntity.internalServerError().body(error);
    }

    private int parseStatusCode(String status, int defaultValue) {
        try {
            return Integer.parseInt(status);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

