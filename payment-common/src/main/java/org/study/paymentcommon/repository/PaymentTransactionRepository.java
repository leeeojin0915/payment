package org.study.paymentcommon.repository;

import org.springframework.stereotype.Repository;
import org.study.paymentcommon.domain.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 결제 트랜잭션 JPA 레포지토리
 * 기본 CRUD는 JpaRepository가 자동 제공
 * 커스텀 조회 메서드만 추가 선언
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    /** 트랜잭션 ID로 단건 조회 */
    Optional<PaymentTransaction> findByTransactionId(String transactionId);

    /** 주문 ID로 단건 조회 */
    Optional<PaymentTransaction> findByOrderId(String orderId);

    /** 주문 ID 중복 여부 확인 (중복 결제 방지) */
    boolean existsByOrderId(String orderId);
}