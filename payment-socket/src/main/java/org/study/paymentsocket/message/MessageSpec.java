package org.study.paymentsocket.message;

/**
 * 고정길이 전문 레이아웃 명세
 * 전문 전체 크기: 512 bytes (EUC-KR 인코딩)
 *
 * ┌──────────┬──────┬──────────────────────────┐
 * │  필드     │ 길이  │ 설명                      │
 * ├──────────┼──────┼──────────────────────────┤
 * │ 전문길이   │   4  │ 전체 전문 바이트 수 (0512) │
 * │ 업무구분   │   4  │ 1000=요청 / 1010=응답     │
 * │ 전송일시   │  14  │ yyyyMMddHHmmss            │
 * │ 전문번호   │  12  │ 일련번호                   │
 * │ 가맹점ID   │  10  │ 좌정렬, 공백패딩           │
 * │ 주문번호   │  20  │ 좌정렬, 공백패딩           │
 * │ 카드번호   │  19  │ 좌정렬, 공백패딩           │
 * │ 유효기간   │   4  │ MMYY                      │
 * │ CVV       │   4  │ 우정렬, 공백패딩            │
 * │ 결제금액   │  12  │ 우정렬, 0패딩              │
 * │ 할부개월   │   2  │ 0패딩                     │
 * │ 응답코드   │   4  │ 0000=승인                 │
 * │ 응답메시지  │  20  │ 좌정렬, 공백패딩           │
 * │ 승인번호   │  12  │ 승인 시 발급              │
 * │ 예비       │ 371  │ 공백                      │
 * └──────────┴──────┴──────────────────────────┘
 */
public final class MessageSpec {

    private MessageSpec() {}

    /** 전문 고정 길이 (bytes) */
    public static final int TOTAL_LENGTH = 512;

    /** 전문 인코딩 */
    public static final String CHARSET = "EUC-KR";

    // ── 업무구분 코드 ─────────────────────────────
    /** 승인 요청 전문 */
    public static final String MSG_TYPE_APPROVAL_REQ = "1000";
    /** 승인 응답 전문 */
    public static final String MSG_TYPE_APPROVAL_RES = "1010";

    // ── 응답 코드 ─────────────────────────────────
    /** 승인 완료 */
    public static final String RC_APPROVED        = "0000";
    /** 승인 거절 */
    public static final String RC_DECLINED        = "0100";
    /** 유효하지 않은 카드 */
    public static final String RC_INVALID_CARD    = "0200";
    /** 중복 주문 */
    public static final String RC_DUPLICATE_ORDER = "0300";
    /** 시스템 오류 */
    public static final String RC_SYSTEM_ERROR    = "9999";

    // ── 헤더 오프셋·길이 ──────────────────────────
    public static final int OFF_MSG_LENGTH    = 0;   public static final int LEN_MSG_LENGTH    = 4;
    public static final int OFF_MSG_TYPE      = 4;   public static final int LEN_MSG_TYPE      = 4;
    public static final int OFF_SEND_DATETIME = 8;   public static final int LEN_SEND_DATETIME = 14;
    public static final int OFF_MSG_SEQ       = 22;  public static final int LEN_MSG_SEQ       = 12;

    // ── 공통 바디 오프셋·길이 ─────────────────────
    public static final int OFF_MERCHANT_ID   = 34;  public static final int LEN_MERCHANT_ID   = 10;
    public static final int OFF_ORDER_ID      = 44;  public static final int LEN_ORDER_ID      = 20;
    public static final int OFF_CARD_NUMBER   = 64;  public static final int LEN_CARD_NUMBER   = 19;
    public static final int OFF_EXPIRY_DATE   = 83;  public static final int LEN_EXPIRY_DATE   = 4;
    public static final int OFF_CVV           = 87;  public static final int LEN_CVV           = 4;
    public static final int OFF_AMOUNT        = 91;  public static final int LEN_AMOUNT        = 12;
    public static final int OFF_INSTALLMENT   = 103; public static final int LEN_INSTALLMENT   = 2;

    // ── 응답 전용 오프셋·길이 ─────────────────────
    public static final int OFF_RESP_CODE     = 105; public static final int LEN_RESP_CODE     = 4;
    public static final int OFF_RESP_MESSAGE  = 109; public static final int LEN_RESP_MESSAGE  = 20;
    public static final int OFF_APPROVAL_NO   = 129; public static final int LEN_APPROVAL_NO   = 12;

    // OFF 141 ~ 511 : 예비 (371 bytes)
}