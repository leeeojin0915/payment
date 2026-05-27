package org.study.paymentsocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.study")
public class PaymentSocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentSocketApplication.class, args);
    }

}
