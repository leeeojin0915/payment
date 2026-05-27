package org.study.paymentcommon.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.study.paymentcommon.domain.PaymentTransaction;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis 매퍼 인터페이스
 * JPA로 처리하기 복잡한 조회 쿼리를 MyBatis XML로 처리
 * 실제 SQL은 PaymentMapper.xml에 작성
 */
@Mapper
public interface PaymentMapper {

    /** 트랜잭션 ID로 단건 조회 */
    Optional<PaymentTransaction> findByTransactionId(String transactionId);

    /** 주문 ID로 단건 조회 */
    Optional<PaymentTransaction> findByOrderId(String orderId);

    /** 전체 결제 내역 조회 (최신순) */
    List<PaymentTransaction> findAll();
}