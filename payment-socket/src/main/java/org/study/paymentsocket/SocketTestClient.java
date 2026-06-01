package org.study.paymentsocket;

import org.study.paymentsocket.message.MessageCodec;
import org.study.paymentsocket.message.MessageSpec;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 소켓 승인 테스트 클라이언트
 * 서버 기동 후 main 메서드 직접 실행
 */
public class SocketTestClient {

    private static final String HOST = "localhost";
    private static final int    PORT = 9090;

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final AtomicInteger SEQ = new AtomicInteger(1);

    public static void main(String[] args) throws Exception {
        System.out.println("======= 소켓 결제 승인 테스트 =======");

        // 테스트 1: 정상 승인
        sendAndPrint("정상승인",
                buildRequest("MERCH00001", "SOCKET-ORDER-001",
                        "4532015112830366", "1230", "123", 50000L, 0));

        Thread.sleep(500);

        // 테스트 2: 강제 거절
        sendAndPrint("강제거절",
                buildRequest("MERCH00001", "SOCKET-ORDER-002",
                        "4000000000000002", "1230", "123", 30000L, 0));

        Thread.sleep(500);

        // 테스트 3: 할부 결제
        sendAndPrint("할부(3개월)",
                buildRequest("MERCH00001", "SOCKET-ORDER-003",
                        "4532015112830366", "1230", "123", 120000L, 3));
    }

    private static byte[] buildRequest(String merchantId, String orderId,
                                       String cardNumber, String expiry,
                                       String cvv, long amount, int installment) {
        byte[] buf = MessageCodec.newBuffer();

        MessageCodec.setMsgLength(buf);
        MessageCodec.setString (buf, MessageSpec.OFF_MSG_TYPE,      MessageSpec.LEN_MSG_TYPE,      MessageSpec.MSG_TYPE_APPROVAL_REQ);
        MessageCodec.setString (buf, MessageSpec.OFF_SEND_DATETIME, MessageSpec.LEN_SEND_DATETIME, LocalDateTime.now().format(DT_FMT));
        MessageCodec.setString (buf, MessageSpec.OFF_MSG_SEQ,       MessageSpec.LEN_MSG_SEQ,       String.format("%012d", SEQ.getAndIncrement()));
        MessageCodec.setString (buf, MessageSpec.OFF_MERCHANT_ID,   MessageSpec.LEN_MERCHANT_ID,   merchantId);
        MessageCodec.setString (buf, MessageSpec.OFF_ORDER_ID,      MessageSpec.LEN_ORDER_ID,      orderId);
        MessageCodec.setString (buf, MessageSpec.OFF_CARD_NUMBER,   MessageSpec.LEN_CARD_NUMBER,   cardNumber);
        MessageCodec.setString (buf, MessageSpec.OFF_EXPIRY_DATE,   MessageSpec.LEN_EXPIRY_DATE,   expiry);
        MessageCodec.setString (buf, MessageSpec.OFF_CVV,           MessageSpec.LEN_CVV,           cvv);
        MessageCodec.setNumeric(buf, MessageSpec.OFF_AMOUNT,        MessageSpec.LEN_AMOUNT,        amount);
        MessageCodec.setNumeric(buf, MessageSpec.OFF_INSTALLMENT,   MessageSpec.LEN_INSTALLMENT,   installment);

        return buf;
    }

    private static void sendAndPrint(String label, byte[] reqBuf) {
        System.out.printf("%n[%s]%n", label);
        try (Socket socket = new Socket(HOST, PORT)) {
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            DataInputStream  in  = new DataInputStream (new BufferedInputStream (socket.getInputStream()));

            out.write(reqBuf);
            out.flush();
            System.out.println("  → 요청 전송 완료 (" + reqBuf.length + " bytes)");

            byte[] resBuf = new byte[MessageSpec.TOTAL_LENGTH];
            in.readFully(resBuf);

            printResponse(resBuf);

        } catch (Exception e) {
            System.err.println("  [ERROR] " + e.getMessage());
        }
    }

    private static void printResponse(byte[] buf) {
        String respCode   = MessageCodec.getString(buf, MessageSpec.OFF_RESP_CODE,    MessageSpec.LEN_RESP_CODE);
        String respMsg    = MessageCodec.getString(buf, MessageSpec.OFF_RESP_MESSAGE, MessageSpec.LEN_RESP_MESSAGE);
        String approvalNo = MessageCodec.getString(buf, MessageSpec.OFF_APPROVAL_NO,  MessageSpec.LEN_APPROVAL_NO);
        String orderId    = MessageCodec.getString(buf, MessageSpec.OFF_ORDER_ID,     MessageSpec.LEN_ORDER_ID);
        String maskedCard = MessageCodec.getString(buf, MessageSpec.OFF_CARD_NUMBER,  MessageSpec.LEN_CARD_NUMBER);
        long   amount     = MessageCodec.getLong  (buf, MessageSpec.OFF_AMOUNT,       MessageSpec.LEN_AMOUNT);
        String sendDt     = MessageCodec.getString(buf, MessageSpec.OFF_SEND_DATETIME,MessageSpec.LEN_SEND_DATETIME);

        System.out.println("  ← 응답 수신");
        System.out.println("  ┌─────────────────────────────");
        System.out.printf ("  │ 응답코드   : %s%n", respCode);
        System.out.printf ("  │ 응답메시지  : %s%n", respMsg);
        System.out.printf ("  │ 승인번호   : %s%n", approvalNo.isBlank() ? "(없음)" : approvalNo);
        System.out.printf ("  │ 주문번호   : %s%n", orderId);
        System.out.printf ("  │ 마스킹카드  : %s%n", maskedCard);
        System.out.printf ("  │ 결제금액   : %,d 원%n", amount);
        System.out.printf ("  │ 응답일시   : %s%n", sendDt);
        System.out.println("  └─────────────────────────────");
    }
}