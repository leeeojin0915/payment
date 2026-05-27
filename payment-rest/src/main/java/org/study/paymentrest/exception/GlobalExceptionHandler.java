package org.study.paymentrest.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.study.paymentrest.dto.PaymentDto;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 * 컨트롤러에서 발생하는 예외를 일관된 형식으로 응답
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 요청 파라미터 유효성 검증 실패
     * @Valid 어노테이션 검증 실패 시 발생
     * HTTP 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PaymentDto.ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        // 여러 검증 오류를 하나의 메시지로 합침
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("[ExceptionHandler] 유효성 검증 실패: {}", message);

        return ResponseEntity.badRequest().body(
                PaymentDto.ErrorResponse.builder()
                        .code(400)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * 비즈니스 로직 예외
     * 존재하지 않는 트랜잭션 조회, 중복 주문 등
     * HTTP 409 Conflict
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PaymentDto.ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex) {

        log.warn("[ExceptionHandler] 비즈니스 오류: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                PaymentDto.ErrorResponse.builder()
                        .code(409)
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * 그 외 모든 예외
     * HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<PaymentDto.ErrorResponse> handleGeneral(Exception ex) {
        log.error("[ExceptionHandler] 서버 오류", ex);

        return ResponseEntity.internalServerError().body(
                PaymentDto.ErrorResponse.builder()
                        .code(500)
                        .message("서버 내부 오류가 발생했습니다")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}