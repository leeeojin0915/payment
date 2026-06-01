package org.study.paymentrest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * payment-rest 실행 진입점
 *
 * @EntityScan          : payment-common의 JPA 엔티티 스캔 경로 추가
 * @EnableJpaRepositories: payment-common의 JPA 레포지토리 스캔 경로 추가
 * @ComponentScan       : payment-common의 Service, Component 스캔
 *                        SpringBootApplication에 scanBasePackages로 지정
 */
@SpringBootApplication(scanBasePackages = {
        "org.study.paymentrest",    // REST 모듈 패키지
        "org.study.paymentcommon"   // common 모듈 패키지
})
@EntityScan(basePackages = "org.study.paymentcommon.domain")
@EnableJpaRepositories(basePackages = "org.study.paymentcommon.repository")
public class PaymentRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentRestApplication.class, args);
    }
}