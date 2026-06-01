package org.study.paymentcommon.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.study.paymentcommon.domain.PaymentTransaction;
import org.study.paymentcommon.repository.PaymentTransactionRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 결제 공통 서비스
 * REST API와 소켓 채널 양쪽에서 공통으로 사용하는 핵심 결제 처리 로직
 * - 중복 주문 체크
 * - 트랜잭션 PENDING 선저장
 * - PG사 승인 요청 위임
 * - 결과 저장 및 반환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository repository;
    private final MockPgClient pgClient;

    /**
     * 카드 결제 승인 처리
     * @param merchantId  가맹점 ID
     * @param orderId     주문 ID (중복 불가)
     * @param cardNumber  카드번호 (평문, 내부에서 마스킹)
     * @param expiryDate  유효기간 (MMYY)
     * @param cvv         CVV
     * @param amount      결제 금액 (원)
     * @param installment 할부 개월 (0=일시불)
     * @return ApprovalResult 승인/거절 결과
     */
    @Transactional
    public ApprovalResult approve(String merchantId, String orderId,
                                  String cardNumber, String expiryDate,
                                  String cvv, long amount, int installment) {

        log.info("[Service] 승인 요청 merchantId={} orderId={} amount={}", merchantId, orderId, amount);

        // 중복 주문 체크 - 같은 주문 ID로 두 번 결제 방지
        if (repository.existsByOrderId(orderId)) {
            log.warn("[Service] 중복 주문 orderId={}", orderId);
            return ApprovalResult.declined("0300", "중복주문거절");
        }

        String transactionId = UUID.randomUUID().toString();
        String maskedCard    = maskCardNumber(cardNumber);

        // PENDING 상태로 선저장 (PG사 요청 전에 저장하여 유실 방지)
        PaymentTransaction tx = PaymentTransaction.builder()
                .transactionId(transactionId)
                .orderId(orderId)
                .merchantId(merchantId)
                .maskedCardNumber(maskedCard)
                .cardCompany("UNKNOWN")
                .amount(amount)
                .installment(installment)
                .status(PaymentTransaction.PaymentStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();
        repository.save(tx);

        // PG사 승인 요청
        MockPgClient.PgApprovalResult pg = pgClient.requestApproval(
                cardNumber, expiryDate, cvv, amount, installment);

        // 카드사 정보 업데이트
        tx.setCardCompany(pg.cardCompany);

        if (pg.approved) {
            // 승인 성공 처리
            tx.setStatus(PaymentTransaction.PaymentStatus.APPROVED);
            tx.setApprovalNumber(pg.approvalNumber);
            tx.setApprovedAt(LocalDateTime.now());
            repository.save(tx);
            log.info("[Service] 승인완료 transactionId={} approvalNo={}", transactionId, pg.approvalNumber);
            return ApprovalResult.approved(pg.approvalNumber);
        } else {
            // 승인 거절 처리
            tx.setStatus(PaymentTransaction.PaymentStatus.DECLINED);
            tx.setFailureReason(pg.failureReason);
            repository.save(tx);
            log.info("[Service] 승인거절 transactionId={} reason={}", transactionId, pg.failureReason);
            return ApprovalResult.declined("0100", pg.failureReason);
        }
    }

    /**
     * 트랜잭션 ID로 결제 내역 조회
     * @throws IllegalArgumentException 존재하지 않는 트랜잭션 ID
     */
    @Transactional(readOnly = true)
    public PaymentTransaction findByTransactionId(String transactionId) {
        return repository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 트랜잭션: " + transactionId));
    }

    /**
     * 카드번호 마스킹
     * 앞 6자리 + ****** + 뒤 4자리
     * 예) 4532015112830366 → 453201******0366
     */
    private String maskCardNumber(String cardNumber) {
        int len = cardNumber.length();
        return cardNumber.substring(0, 6) + "******" + cardNumber.substring(len - 4);
    }

    /**
     * 승인 처리 결과 DTO
     * approved      : 승인 여부
     * approvalNumber: 승인 번호 (승인 시에만 존재)
     * responseCode  : 응답 코드 (0000=승인, 0100=거절, 0300=중복)
     * failureReason : 실패 사유 (거절 시에만 존재)
     */
    @Getter
    public static class ApprovalResult {
        private final boolean approved;
        private final String  approvalNumber;
        private final String  responseCode;
        private final String  failureReason;

        private ApprovalResult(boolean approved, String approvalNumber,
                               String responseCode, String failureReason) {
            this.approved = approved;
            this.approvalNumber = approvalNumber;
            this.responseCode = responseCode;
            this.failureReason = failureReason;
        }

        /** 승인 성공 결과 생성 */
        public static ApprovalResult approved(String approvalNumber) {
            return new ApprovalResult(true, approvalNumber, "0000", null);
        }

        /** 승인 거절 결과 생성 */
        public static ApprovalResult declined(String responseCode, String reason) {
            return new ApprovalResult(false, null, responseCode, reason);
        }
    }

    @Transactional(readOnly = true)
    public PaymentTransaction findByOrderId(String orderId) {
        return repository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문: " + orderId));
    }
}