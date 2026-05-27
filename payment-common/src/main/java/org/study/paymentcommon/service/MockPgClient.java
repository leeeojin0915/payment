package org.study.paymentcommon.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * Mock PG사 클라이언트
 * 실제 PG사 API 호출을 시뮬레이션
 * Luhn 알고리즘으로 카드번호 유효성 검증 후 확률적으로 승인/거절 처리
 */
@Slf4j
@Service
public class MockPgClient {

    /** 승인 성공 확률 (기본 95%) */
    @Value("${payment.mock.approval-rate:0.95}")
    private double approvalRate;

    /** PG사 응답 지연 시뮬레이션 (ms) */
    @Value("${payment.mock.processing-delay-ms:300}")
    private long processingDelayMs;

    private final Random random = new Random();

    /** 강제 거절 테스트용 카드 BIN (앞 4자리) */
    private static final List<String> FORCE_DECLINE_BINS = List.of("4000", "9999");

    /**
     * PG사 승인 결과 DTO
     * approved      : 승인 여부
     * approvalNumber: 승인 번호 (승인 시에만 존재)
     * failureReason : 실패 사유 (거절 시에만 존재)
     * cardCompany   : 카드사
     */
    public static class PgApprovalResult {
        public final boolean approved;
        public final String approvalNumber;
        public final String failureReason;
        public final String cardCompany;

        public PgApprovalResult(boolean approved, String approvalNumber,
                                String failureReason, String cardCompany) {
            this.approved = approved;
            this.approvalNumber = approvalNumber;
            this.failureReason = failureReason;
            this.cardCompany = cardCompany;
        }
    }

    /**
     * 카드 승인 요청
     * 처리 순서:
     * 1. 네트워크 지연 시뮬레이션
     * 2. 카드사 감지
     * 3. 카드 유효성 검증 (Luhn + 유효기간)
     * 4. 강제 거절 카드 체크
     * 5. 확률적 승인/거절
     */
    public PgApprovalResult requestApproval(String cardNumber, String expiryDate,
                                            String cvv, Long amount, Integer installment) {
        simulateNetworkDelay();

        String cardCompany = detectCardCompany(cardNumber);

        // 카드 유효성 검증 실패 시 즉시 거절
        if (!isCardValid(cardNumber, expiryDate)) {
            log.warn("[MockPG] 카드 유효성 검증 실패");
            return new PgApprovalResult(false, null, "유효하지 않은 카드입니다", cardCompany);
        }

        // 강제 거절 카드 (테스트용)
        if (isForceDecline(cardNumber)) {
            log.info("[MockPG] 강제 거절 카드");
            return new PgApprovalResult(false, null, "카드사 승인 거절", cardCompany);
        }

        // 확률적 승인/거절 (기본 5% 거절)
        if (random.nextDouble() > approvalRate) {
            String reason = pickDeclineReason();
            log.info("[MockPG] 랜덤 거절 reason={}", reason);
            return new PgApprovalResult(false, null, reason, cardCompany);
        }

        // 승인 성공
        String approvalNumber = generateApprovalNumber();
        log.info("[MockPG] 승인 성공 approvalNumber={}", approvalNumber);
        return new PgApprovalResult(true, approvalNumber, null, cardCompany);
    }

    /**
     * 카드 유효성 검증
     * 1. Luhn 알고리즘으로 카드번호 체크섬 검증
     * 2. 유효기간(MMYY) 만료 여부 확인
     */
    private boolean isCardValid(String cardNumber, String expiryDate) {
        if (!luhnCheck(cardNumber)) return false;
        int month = Integer.parseInt(expiryDate.substring(0, 2));
        int year  = Integer.parseInt(expiryDate.substring(2, 4)) + 2000;
        if (month < 1 || month > 12) return false;
        java.time.YearMonth expiry  = java.time.YearMonth.of(year, month);
        java.time.YearMonth current = java.time.YearMonth.now();
        return !expiry.isBefore(current);
    }

    /**
     * Luhn 알고리즘
     * 카드번호의 체크섬을 검증하는 국제 표준 알고리즘
     * 끝자리부터 홀수 번째는 그대로, 짝수 번째는 2배 후 합산
     */
    private boolean luhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(String.valueOf(cardNumber.charAt(i)));
            if (alternate) { n *= 2; if (n > 9) n -= 9; }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    /** 강제 거절 카드 여부 확인 */
    private boolean isForceDecline(String cardNumber) {
        return FORCE_DECLINE_BINS.stream().anyMatch(cardNumber::startsWith);
    }

    /**
     * 카드 BIN(앞 자리)으로 카드사 감지
     * 실제 카드사별 BIN 범위를 단순화한 Mock 버전
     */
    private String detectCardCompany(String cardNumber) {
        if (cardNumber.startsWith("4"))        return "VISA";
        if (cardNumber.matches("5[1-5].*"))    return "MASTERCARD";
        if (cardNumber.startsWith("35"))       return "JCB";
        if (cardNumber.startsWith("37"))       return "AMEX";
        if (cardNumber.startsWith("9"))        return "BC카드";
        if (cardNumber.startsWith("6"))        return "신한카드";
        return "기타";
    }

    /** 8자리 랜덤 승인 번호 생성 */
    private String generateApprovalNumber() {
        return String.format("%08d", random.nextInt(100_000_000));
    }

    /** 거절 사유 랜덤 선택 */
    private String pickDeclineReason() {
        List<String> reasons = List.of("한도 초과", "잔액 부족", "분실/도난 카드", "카드사 일시 장애");
        return reasons.get(random.nextInt(reasons.size()));
    }

    /** PG사 네트워크 지연 시뮬레이션 */
    private void simulateNetworkDelay() {
        try {
            Thread.sleep(processingDelayMs + random.nextInt(200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}