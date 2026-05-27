package org.study.paymentsocket.message;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 승인 응답 전문 VO (업무구분: 1010)
 * 처리 결과를 512 bytes 전문으로 직렬화하여 클라이언트에 송신
 */
@Getter
@Builder
public class ApprovalResponseMessage {

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ── 헤더 (요청 전문에서 echo) ─────────────────
    private final String msgSeq;

    // ── 공통 바디 (요청 전문에서 echo) ───────────
    private final String merchantId;
    private final String orderId;
    /** 마스킹된 카드번호 */
    private final String cardNumber;
    private final String expiryDate;
    private final long   amount;
    private final int    installment;

    // ── 응답 전용 ────────────────────────────────
    /** 응답 코드 (0000=승인, 0100=거절 등) */
    private final String responseCode;
    /** 응답 메시지 (한글) */
    private final String responseMessage;
    /** 승인 번호 (승인 시에만 존재) */
    private final String approvalNumber;

    /**
     * VO → 512 bytes 전문 직렬화
     * MessageSpec에 정의된 오프셋·길이로 각 필드 기록
     */
    public byte[] serialize() {
        byte[] buf = MessageCodec.newBuffer();

        // 헤더 기록
        MessageCodec.setMsgLength(buf);
        MessageCodec.setString (buf, MessageSpec.OFF_MSG_TYPE,      MessageSpec.LEN_MSG_TYPE,      MessageSpec.MSG_TYPE_APPROVAL_RES);
        MessageCodec.setString (buf, MessageSpec.OFF_SEND_DATETIME, MessageSpec.LEN_SEND_DATETIME, LocalDateTime.now().format(DT_FMT));
        MessageCodec.setString (buf, MessageSpec.OFF_MSG_SEQ,       MessageSpec.LEN_MSG_SEQ,       msgSeq);

        // 공통 바디 기록 (요청 echo)
        MessageCodec.setString (buf, MessageSpec.OFF_MERCHANT_ID,   MessageSpec.LEN_MERCHANT_ID,   merchantId);
        MessageCodec.setString (buf, MessageSpec.OFF_ORDER_ID,      MessageSpec.LEN_ORDER_ID,      orderId);
        MessageCodec.setString (buf, MessageSpec.OFF_CARD_NUMBER,   MessageSpec.LEN_CARD_NUMBER,   cardNumber);
        MessageCodec.setString (buf, MessageSpec.OFF_EXPIRY_DATE,   MessageSpec.LEN_EXPIRY_DATE,   expiryDate);
        MessageCodec.setNumeric(buf, MessageSpec.OFF_AMOUNT,        MessageSpec.LEN_AMOUNT,        amount);
        MessageCodec.setNumeric(buf, MessageSpec.OFF_INSTALLMENT,   MessageSpec.LEN_INSTALLMENT,   installment);

        // 응답 전용 기록
        MessageCodec.setString (buf, MessageSpec.OFF_RESP_CODE,    MessageSpec.LEN_RESP_CODE,    responseCode);
        MessageCodec.setString (buf, MessageSpec.OFF_RESP_MESSAGE, MessageSpec.LEN_RESP_MESSAGE, responseMessage != null ? responseMessage : "");
        MessageCodec.setString (buf, MessageSpec.OFF_APPROVAL_NO,  MessageSpec.LEN_APPROVAL_NO,  approvalNumber  != null ? approvalNumber  : "");

        return buf;
    }

    // ── 팩토리 메서드 ────────────────────────────

    /** 승인 성공 응답 전문 생성 */
    public static ApprovalResponseMessage approved(ApprovalRequestMessage req,
                                                   String maskedCard,
                                                   String approvalNumber) {
        return ApprovalResponseMessage.builder()
                .msgSeq(req.getMsgSeq())
                .merchantId(req.getMerchantId())
                .orderId(req.getOrderId())
                .cardNumber(maskedCard)
                .expiryDate(req.getExpiryDate())
                .amount(req.getAmount())
                .installment(req.getInstallment())
                .responseCode(MessageSpec.RC_APPROVED)
                .responseMessage("승인완료")
                .approvalNumber(approvalNumber)
                .build();
    }

    /** 승인 거절 응답 전문 생성 */
    public static ApprovalResponseMessage declined(ApprovalRequestMessage req,
                                                   String maskedCard,
                                                   String responseCode,
                                                   String reason) {
        return ApprovalResponseMessage.builder()
                .msgSeq(req.getMsgSeq())
                .merchantId(req.getMerchantId())
                .orderId(req.getOrderId())
                .cardNumber(maskedCard)
                .expiryDate(req.getExpiryDate())
                .amount(req.getAmount())
                .installment(req.getInstallment())
                .responseCode(responseCode)
                .responseMessage(reason)
                .approvalNumber("")
                .build();
    }

    /** 시스템 오류 응답 전문 생성 */
    public static ApprovalResponseMessage error(String msgSeq, String reason) {
        return ApprovalResponseMessage.builder()
                .msgSeq(msgSeq != null ? msgSeq : "")
                .merchantId("").orderId("").cardNumber("")
                .expiryDate("").amount(0).installment(0)
                .responseCode(MessageSpec.RC_SYSTEM_ERROR)
                .responseMessage(reason)
                .approvalNumber("")
                .build();
    }
}