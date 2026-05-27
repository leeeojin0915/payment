package org.study.paymentsocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.study.paymentsocket.handler.PaymentMessageHandler;
import org.study.paymentsocket.message.MessageSpec;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * 클라이언트 소켓 1개를 처리하는 워커 스레드
 *
 * 처리 흐름:
 * 1. readFully()로 512 bytes 수신 (Short-read 방지)
 * 2. PaymentMessageHandler.handle() 호출
 * 3. 512 bytes 응답 전문 송신
 * 4. 소켓 종료
 *
 * Short-read란?
 * TCP는 스트림 기반이라 한 번의 read()로 원하는 바이트를
 * 모두 못 받을 수 있음. readFully()로 512 bytes가 모두
 * 수신될 때까지 블로킹하여 방지
 */
@Slf4j
@RequiredArgsConstructor
public class ClientSocketWorker implements Runnable {

    /** 수신 타임아웃 (30초) */
    private static final int READ_TIMEOUT_MS = 30_000;

    private final Socket                socket;
    private final PaymentMessageHandler handler;

    @Override
    public void run() {
        String remote = socket.getRemoteSocketAddress().toString();
        log.info("[Worker] 접속 수신: {}", remote);

        try {
            // 수신 타임아웃 설정 (30초 내 데이터 없으면 종료)
            socket.setSoTimeout(READ_TIMEOUT_MS);

            DataInputStream  in  = new DataInputStream (new BufferedInputStream (socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            // ── 1. 전문 수신 ──────────────────────
            byte[] reqBuf = new byte[MessageSpec.TOTAL_LENGTH];
            try {
                in.readFully(reqBuf); // Short-read 방지
            } catch (SocketTimeoutException e) {
                log.warn("[Worker] 수신 타임아웃: {}", remote);
                return;
            } catch (EOFException e) {
                log.warn("[Worker] 클라이언트 접속 종료(EOF): {}", remote);
                return;
            }

            log.debug("[Worker] 전문수신 완료 {} bytes from {}", reqBuf.length, remote);

            // ── 2. 전문 처리 ──────────────────────
            byte[] resBuf = handler.handle(reqBuf);

            // ── 3. 응답 송신 ──────────────────────
            out.write(resBuf);
            out.flush();
            log.debug("[Worker] 응답송신 완료 {} bytes to {}", resBuf.length, remote);

        } catch (IOException e) {
            log.error("[Worker] I/O 오류: {}", remote, e);
        } finally {
            // 소켓 정상 종료 보장
            closeQuietly(socket);
            log.info("[Worker] 접속 종료: {}", remote);
        }
    }

    private void closeQuietly(Socket s) {
        try { if (s != null && !s.isClosed()) s.close(); } catch (IOException ignored) {}
    }
}