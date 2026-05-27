package org.study.paymentsocket.message;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * 고정길이 전문 직렬화 / 역직렬화 유틸리티
 *
 * 전문 규칙:
 * - 문자열 필드: 좌정렬, 부족 시 공백 패딩, 초과 시 잘라냄
 * - 숫자 필드  : 우정렬, 부족 시 앞쪽 0 패딩
 * - 인코딩     : EUC-KR (금융권 레거시 호환)
 */
public final class MessageCodec {

    private MessageCodec() {}

    private static final Charset CS = Charset.forName(MessageSpec.CHARSET);

    // ── 역직렬화 (bytes → field) ──────────────────

    /**
     * 전문에서 문자열 필드 추출
     * 우측 공백을 trim하여 반환
     */
    public static String getString(byte[] msg, int offset, int length) {
        byte[] slice = Arrays.copyOfRange(msg, offset, offset + length);
        return new String(slice, CS).stripTrailing();
    }

    /**
     * 전문에서 숫자 필드 추출
     * 앞쪽 0 패딩 제거 후 long으로 변환
     */
    public static long getLong(byte[] msg, int offset, int length) {
        String raw = getString(msg, offset, length).replaceFirst("^0+(?!$)", "");
        return raw.isEmpty() ? 0L : Long.parseLong(raw);
    }

    /**
     * 전문에서 숫자 필드 추출 → int
     */
    public static int getInt(byte[] msg, int offset, int length) {
        return (int) getLong(msg, offset, length);
    }

    // ── 직렬화 (field → bytes) ────────────────────

    /**
     * 512 bytes 버퍼를 공백으로 초기화하여 반환
     * 이후 set* 메서드로 각 필드를 채움
     */
    public static byte[] newBuffer() {
        byte[] buf = new byte[MessageSpec.TOTAL_LENGTH];
        Arrays.fill(buf, (byte) 0x20); // 공백(space)으로 초기화
        return buf;
    }

    /**
     * 문자열 필드 쓰기
     * 좌정렬, 부족 시 공백 패딩, 초과 시 잘라냄
     */
    public static void setString(byte[] buf, int offset, int length, String value) {
        if (value == null) value = "";
        byte[] encoded = value.getBytes(CS);
        int copyLen = Math.min(encoded.length, length);
        Arrays.fill(buf, offset, offset + length, (byte) 0x20); // 공백으로 초기화
        System.arraycopy(encoded, 0, buf, offset, copyLen);
    }

    /**
     * 숫자 필드 쓰기
     * 우정렬, 앞쪽 0 패딩
     */
    public static void setNumeric(byte[] buf, int offset, int length, long value) {
        String formatted = String.format("%0" + length + "d", value);
        if (formatted.length() > length) {
            formatted = formatted.substring(formatted.length() - length);
        }
        byte[] encoded = formatted.getBytes(CS);
        System.arraycopy(encoded, 0, buf, offset, length);
    }

    /**
     * 전문 총 길이를 헤더에 기록
     * 항상 TOTAL_LENGTH(512)를 기록
     */
    public static void setMsgLength(byte[] buf) {
        setNumeric(buf, MessageSpec.OFF_MSG_LENGTH, MessageSpec.LEN_MSG_LENGTH,
                MessageSpec.TOTAL_LENGTH);
    }
}