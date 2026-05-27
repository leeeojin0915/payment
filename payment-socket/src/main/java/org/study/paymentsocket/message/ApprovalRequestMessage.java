package org.study.paymentsocket.message;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 승인 요청 전문 VO (업무구분: 1000)
 * 클라이언트로부터 수신한 512 bytes 전문을 파싱하여 생성
 */
@Getter
@Builder
@ToString
public class ApprovalRequestMessage {

    // ── 헤더 ──────────────────────────────────────
    /** 업무구분 코드 (1000=승인요청) */
    private final String msgType;
    /** 전송일시 (yyyyMMddHHmmss) */
    private final String sendDatetime;
    /** 전문 일련번호 */
    private final String msgSeq;

    // ── 바디 ──────────────────────────────────────
    /** 가맹점 ID */
    private final String merchantId;
    /** 주문번호 */
    private final String orderId;
    /** 카드번호 (평문, 처리 후 마스킹) */
    private final String cardNumber;
    /** 유효기간 (MMYY) */
    private final String expiryDate;
    /** CVV */
    private final String cvv;
    /** 결제금액 (원) */
    private final long   amount;
    /** 할부개월 (0=일시불) */
    private final int    installment;

    /**
     * 512 bytes 전문 버퍼를 파싱하여 VO 생성
     * MessageSpec에 정의된 오프셋·길이로 각 필드 추출
     */
    public static ApprovalRequestMessage parse(byte[] raw) {
        return ApprovalRequestMessage.builder()
                .msgType      (MessageCodec.getString(raw, MessageSpec.OFF_MSG_TYPE,      MessageSpec.LEN_MSG_TYPE))
                .sendDatetime (MessageCodec.getString(raw, MessageSpec.OFF_SEND_DATETIME, MessageSpec.LEN_SEND_DATETIME))
                .msgSeq       (MessageCodec.getString(raw, MessageSpec.OFF_MSG_SEQ,       MessageSpec.LEN_MSG_SEQ))
                .merchantId   (MessageCodec.getString(raw, MessageSpec.OFF_MERCHANT_ID,   MessageSpec.LEN_MERCHANT_ID))
                .orderId      (MessageCodec.getString(raw, MessageSpec.OFF_ORDER_ID,      MessageSpec.LEN_ORDER_ID))
                .cardNumber   (MessageCodec.getString(raw, MessageSpec.OFF_CARD_NUMBER,   MessageSpec.LEN_CARD_NUMBER))
                .expiryDate   (MessageCodec.getString(raw, MessageSpec.OFF_EXPIRY_DATE,   MessageSpec.LEN_EXPIRY_DATE))
                .cvv          (MessageCodec.getString(raw, MessageSpec.OFF_CVV,           MessageSpec.LEN_CVV))
                .amount       (MessageCodec.getLong  (raw, MessageSpec.OFF_AMOUNT,        MessageSpec.LEN_AMOUNT))
                .installment  (MessageCodec.getInt   (raw, MessageSpec.OFF_INSTALLMENT,   MessageSpec.LEN_INSTALLMENT))
                .build();
    }
}