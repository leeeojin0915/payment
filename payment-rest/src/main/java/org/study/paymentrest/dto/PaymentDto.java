package org.study.paymentrest.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.study.paymentcommon.domain.PaymentTransaction;

import java.time.LocalDateTime;

/**
 * REST API 요청/응답 DTO
 * 외부에 카드번호 등 민감 정보가 노출되지 않도록 별도 관리
 */
public class PaymentDto {

    /**
     * 결제 승인 요청 DTO
     * @Valid 어노테이션으로 자동 검증
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ApprovalRequest {

        @NotBlank(message = "주문 ID는 필수입니다")
        @Size(max = 64, message = "주문 ID는 64자 이하여야 합니다")
        private String orderId;

        @NotBlank(message = "가맹점 ID는 필수입니다")
        private String merchantId;

        @NotBlank(message = "카드번호는 필수입니다")
        @Pattern(regexp = "\\d{13,19}", message = "카드번호는 13~19자리 숫자여야 합니다")
        private String cardNumber;

        @NotBlank(message = "유효기간은 필수입니다 (MMYY)")
        @Pattern(regexp = "\\d{4}", message = "유효기간 형식이 올바르지 않습니다 (MMYY)")
        private String expiryDate;

        @NotBlank(message = "CVV는 필수입니다")
        @Pattern(regexp = "\\d{3,4}", message = "CVV는 3~4자리 숫자여야 합니다")
        private String cvv;

        @NotNull(message = "결제 금액은 필수입니다")
        @Min(value = 100, message = "최소 결제 금액은 100원입니다")
        @Max(value = 100_000_000, message = "최대 결제 금액은 1억원입니다")
        private Long amount;

        @NotNull(message = "할부 개월은 필수입니다")
        @Min(value = 0, message = "할부 개월은 0(일시불) 이상이어야 합니다")
        @Max(value = 36, message = "최대 할부 개월은 36개월입니다")
        private Integer installment;
    }

    /**
     * 결제 승인 응답 DTO
     * 카드번호는 마스킹된 형태로만 반환
     */
    @Getter
    @Builder
    public static class ApprovalResponse {
        /** 승인 성공 여부 */
        private boolean success;
        /** 서버 발급 트랜잭션 ID */
        private String transactionId;
        /** 주문 ID */
        private String orderId;
        /** 가맹점 ID */
        private String merchantId;
        /** PG사 발급 승인 번호 (승인 시에만 존재) */
        private String approvalNumber;
        /** 마스킹된 카드번호 */
        private String maskedCardNumber;
        /** 카드사 */
        private String cardCompany;
        /** 결제 금액 */
        private Long amount;
        /** 할부 개월 */
        private Integer installment;
        /** 결제 상태 */
        private PaymentTransaction.PaymentStatus status;
        /** 실패 사유 (거절 시에만 존재) */
        private String failureReason;
        /** 요청 시각 */
        private LocalDateTime requestedAt;
        /** 승인 시각 (승인 시에만 존재) */
        private LocalDateTime approvedAt;
    }

    /**
     * 공통 에러 응답 DTO
     */
    @Getter
    @Builder
    public static class ErrorResponse {
        /** HTTP 상태 코드 */
        private int code;
        /** 에러 메시지 */
        private String message;
        /** 발생 시각 */
        private LocalDateTime timestamp;
    }
}