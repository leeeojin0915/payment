package org.study.paymentsocket;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.study.paymentsocket.handler.PaymentMessageHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * TCP 소켓 승인 서버
 *
 * - Spring 컨텍스트 완전 기동 후 ApplicationReadyEvent에서 자동 시작
 * - 클라이언트 접속마다 스레드풀에서 ClientSocketWorker 실행
 * - 애플리케이션 종료 시 @PreDestroy로 Graceful Shutdown
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSocketServer {

    /** 소켓 서버 포트 (기본: 9090) */
    @Value("${payment.socket.port:9090}")
    private int port;

    /** 스레드풀 크기 (기본: 10) */
    @Value("${payment.socket.thread-pool-size:10}")
    private int threadPoolSize;

    /** 연결 대기 큐 크기 (기본: 50) */
    @Value("${payment.socket.backlog:50}")
    private int backlog;

    private final PaymentMessageHandler handler;

    private ServerSocket    serverSocket;
    private ExecutorService threadPool;
    private volatile boolean running = false;

    /**
     * Spring 컨텍스트 완전 기동 후 소켓 서버 시작
     * ApplicationReadyEvent 사용 이유:
     * @PostConstruct는 Bean 초기화 시점이라 다른 Bean이 준비 안 됐을 수 있음
     * ApplicationReadyEvent는 모든 Bean이 준비된 후 발생
     */
    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        try {
            threadPool   = Executors.newFixedThreadPool(threadPoolSize);
            serverSocket = new ServerSocket(port, backlog);
            running      = true;

            log.info("================================================");
            log.info("  결제 승인 소켓 서버 시작 — PORT: {}", port);
            log.info("  스레드풀 크기: {}", threadPoolSize);
            log.info("================================================");

            // Accept 루프는 별도 데몬 스레드에서 실행
            // 데몬 스레드: JVM 종료 시 자동으로 종료됨
            Thread acceptThread = new Thread(this::acceptLoop, "socket-accept");
            acceptThread.setDaemon(true);
            acceptThread.start();

        } catch (IOException e) {
            log.error("[SocketServer] 서버 소켓 오픈 실패 port={}", port, e);
            throw new RuntimeException("소켓 서버 시작 실패", e);
        }
    }

    /**
     * 클라이언트 접속 대기 루프
     * accept()는 접속이 올 때까지 블로킹
     * 접속 시 스레드풀에 ClientSocketWorker 제출
     */
    private void acceptLoop() {
        while (running) {
            try {
                Socket client = serverSocket.accept();
                threadPool.execute(new ClientSocketWorker(client, handler));
            } catch (SocketException e) {
                // running=false면 shutdown() 중이므로 정상 종료
                if (running) log.error("[SocketServer] Accept 오류", e);
            } catch (IOException e) {
                log.error("[SocketServer] I/O 오류", e);
            }
        }
        log.info("[SocketServer] Accept 루프 종료");
    }

    /**
     * Graceful Shutdown
     * Spring 컨텍스트 종료 시 자동 호출
     * 1. ServerSocket 닫아 accept() 블록 해제
     * 2. 진행 중인 워커 최대 30초 대기 후 강제 종료
     */
    @PreDestroy
    public void shutdown() {
        log.info("[SocketServer] Graceful Shutdown 시작...");
        running = false;

        // ServerSocket 닫기 → acceptLoop의 accept() 블록 해제
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.warn("[SocketServer] ServerSocket 종료 오류", e);
        }

        // 진행 중인 워커 최대 30초 대기
        if (threadPool != null) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                    log.warn("[SocketServer] 강제 종료 (30초 초과)");
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("[SocketServer] 소켓 서버 종료 완료");
    }
}