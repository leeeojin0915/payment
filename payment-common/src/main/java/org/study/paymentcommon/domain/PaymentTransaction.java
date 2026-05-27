package org.study.paymentcommon.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 결제 트랜잭션 엔티티
 * 모든 결제 요청/응답 정보를 DB에 저장하는 JPA 엔티티
 */
@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    /** 자동 증가 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 서버에서 발급하는 고유 트랜잭션 ID (UUID) */
    @Column(nullable = false, unique = true)
    private String transactionId;

    /** 가맹점에서 발급하는 주문 ID (중복 불가) */
    @Column(nullable = false)
    private String orderId;

    /** 가맹점 ID */
    @Column(nullable = false)
    private String merchantId;

    /** 마스킹된 카드번호 (앞 6자리 + ****** + 뒤 4자리) */
    @Column(nullable = false)
    private String maskedCardNumber;

    /** 카드사 (VISA, MASTERCARD, BC카드 등) */
    @Column(nullable = false)
    private String cardCompany;

    /** 결제 금액 (원) */
    @Column(nullable = false)
    private Long amount;

    /** 할부 개월 (0 = 일시불) */
    @Column(nullable = false)
    private Integer installment;

    /** 결제 상태 (PENDING, APPROVED, DECLINED) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    /** 승인 번호 (PG사에서 발급, 승인 시에만 존재) */
    private String approvalNumber;

    /** 실패 사유 (거절 시에만 존재) */
    private String failureReason;

    /** 결제 요청 시각 */
    @Column(nullable = false)
    private LocalDateTime requestedAt;

    /** 승인 완료 시각 (승인 시에만 존재) */
    private LocalDateTime approvedAt;

    /**
     * 결제 상태 enum
     * PENDING  : 처리 중 (PG사 요청 전)
     * APPROVED : 승인 완료
     * DECLINED : 거절
     */
    public enum PaymentStatus {
        PENDING, APPROVED, DECLINED
    }
}