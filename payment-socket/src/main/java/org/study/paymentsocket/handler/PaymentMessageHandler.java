package org.study.paymentsocket.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.study.paymentcommon.service.PaymentService;
import org.study.paymentsocket.message.*;

/**
 * 소켓 전문 처리 핸들러
 *
 * 처리 순서:
 * 1. 전문 길이 / 업무구분 코드 검증
 * 2. ApprovalRequestMessage 파싱
 * 3. 필드 유효성 검증
 * 4. PaymentService에 승인 처리 위임
 * 5. ApprovalResponseMessage 조립 → 직렬화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentMessageHandler {

    private final PaymentService paymentService;

    /**
     * 수신 전문 처리 후 응답 전문 반환
     * @param raw 수신된 512 bytes 전문
     * @return 직렬화된 512 bytes 응답 전문
     */
    public byte[] handle(byte[] raw) {
        String msgSeq = null;

        try {
            // ── 1. 기본 검증 ──────────────────────
            if (raw == null || raw.length != MessageSpec.TOTAL_LENGTH) {
                log.error("[Handler] 전문 길이 오류: expected={}, actual={}",
                        MessageSpec.TOTAL_LENGTH, raw == null ? 0 : raw.length);
                return ApprovalResponseMessage.error(null, "전문길이오류").serialize();
            }

            // 업무구분 코드 검증
            String msgType = MessageCodec.getString(raw, MessageSpec.OFF_MSG_TYPE, MessageSpec.LEN_MSG_TYPE);
            if (!MessageSpec.MSG_TYPE_APPROVAL_REQ.equals(msgType)) {
                log.error("[Handler] 미지원 업무구분: {}", msgType);
                return ApprovalResponseMessage.error(null, "미지원업무구분").serialize();
            }

            // ── 2. 전문 파싱 ──────────────────────
            ApprovalRequestMessage req = ApprovalRequestMessage.parse(raw);
            msgSeq = req.getMsgSeq();
            log.info("[Handler] 전문수신 seq={} orderId={} amount={}",
                    msgSeq, req.getOrderId(), req.getAmount());

            // ── 3. 필드 유효성 검증 ───────────────
            String validationError = validate(req);
            if (validationError != null) {
                log.warn("[Handler] 유효성오류 seq={} reason={}", msgSeq, validationError);
                return ApprovalResponseMessage
                        .declined(req, maskCard(req.getCardNumber()),
                                MessageSpec.RC_INVALID_CARD, validationError)
                        .serialize();
            }

            // ── 4. 결제 처리 위임 ─────────────────
            PaymentService.ApprovalResult result = paymentService.approve(
                    req.getMerchantId(),
                    req.getOrderId(),
                    req.getCardNumber(),
                    req.getExpiryDate(),
                    req.getCvv(),
                    req.getAmount(),
                    req.getInstallment()
            );

            String maskedCard = maskCard(req.getCardNumber());

            // ── 5. 응답 전문 조립 ─────────────────
            if (result.isApproved()) {
                log.info("[Handler] 승인완료 seq={} approvalNo={}", msgSeq, result.getApprovalNumber());
                return ApprovalResponseMessage
                        .approved(req, maskedCard, result.getApprovalNumber())
                        .serialize();
            } else {
                log.info("[Handler] 승인거절 seq={} reason={}", msgSeq, result.getFailureReason());
                return ApprovalResponseMessage
                        .declined(req, maskedCard, result.getResponseCode(), result.getFailureReason())
                        .serialize();
            }

        } catch (Exception e) {
            log.error("[Handler] 처리 중 예외 seq={}", msgSeq, e);
            return ApprovalResponseMessage.error(msgSeq, "시스템오류").serialize();
        }
    }

    /**
     * 요청 전문 필드 유효성 검증
     * @return null=정상, 오류 사유 문자열=검증 실패
     */
    private String validate(ApprovalRequestMessage req) {
        if (isBlank(req.getMerchantId()))  return "가맹점ID누락";
        if (isBlank(req.getOrderId()))     return "주문번호누락";
        if (isBlank(req.getCardNumber()) || !req.getCardNumber().matches("\\d{13,19}"))
            return "카드번호오류";
        if (isBlank(req.getExpiryDate()) || !req.getExpiryDate().matches("\\d{4}"))
            return "유효기간오류";
        if (isBlank(req.getCvv()) || !req.getCvv().matches("\\d{3,4}"))
            return "CVV오류";
        if (req.getAmount() < 100)         return "금액오류";
        if (req.getInstallment() < 0 || req.getInstallment() > 36)
            return "할부개월오류";
        return null;
    }

    /** 카드번호 마스킹 (앞 6자리 + ****** + 뒤 4자리) */
    private String maskCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 10) return cardNumber;
        int len = cardNumber.length();
        return cardNumber.substring(0, 6) + "******" + cardNumber.substring(len - 4);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}