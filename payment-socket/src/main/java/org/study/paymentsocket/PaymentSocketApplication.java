package org.study.paymentsocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * payment-socket 실행 진입점
 *
 * @EntityScan          : payment-common의 JPA 엔티티 스캔 경로 추가
 * @EnableJpaRepositories: payment-common의 JPA 레포지토리 스캔 경로 추가
 * @ComponentScan       : payment-common의 Service, Component 스캔
 *                        SpringBootApplication에 scanBasePackages로 지정
 */
@SpringBootApplication(scanBasePackages = {
        "org.study.paymentsocket",  // socket 모듈 패키지
        "org.study.paymentcommon"   // common 모듈 패키지
})
@EntityScan(basePackages = "org.study.paymentcommon.domain")
@EnableJpaRepositories(basePackages = "org.study.paymentcommon.repository")
public class PaymentSocketApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(PaymentSocketApplication.class, args);

        // 소켓 서버가 계속 실행되도록 메인 스레드 블로킹
        Thread.currentThread().join();
    }
}