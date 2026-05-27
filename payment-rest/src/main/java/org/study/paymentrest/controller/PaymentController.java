package org.study.paymentrest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.study.paymentcommon.domain.PaymentTransaction;
import org.study.paymentcommon.service.PaymentService;
import org.study.paymentrest.dto.PaymentDto;

/**
 * 결제 REST API 컨트롤러
 *
 * 엔드포인트:
 * POST /api/v1/payments/approve  - 카드 결제 승인
 * GET  /api/v1/payments/{id}     - 결제 내역 조회
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 카드 결제 승인 요청
     * 승인 성공: HTTP 200
     * 승인 거절: HTTP 402 Payment Required
     */
    @PostMapping("/approve")
    public ResponseEntity<PaymentDto.ApprovalResponse> approve(
            @Valid @RequestBody PaymentDto.ApprovalRequest request) {

        log.info("[Controller] 승인 요청 orderId={} amount={}",
                request.getOrderId(), request.getAmount());

        // PaymentService에 처리 위임
        PaymentService.ApprovalResult result = paymentService.approve(
                request.getMerchantId(),
                request.getOrderId(),
                request.getCardNumber(),
                request.getExpiryDate(),
                request.getCvv(),
                request.getAmount(),
                request.getInstallment()
        );

        // 응답 DTO 조립
        PaymentDto.ApprovalResponse response = PaymentDto.ApprovalResponse.builder()
                .success(result.isApproved())
                .orderId(request.getOrderId())
                .merchantId(request.getMerchantId())
                .approvalNumber(result.getApprovalNumber())
                .failureReason(result.getFailureReason())
                .amount(request.getAmount())
                .installment(request.getInstallment())
                .status(result.isApproved() ?
                        PaymentTransaction.PaymentStatus.APPROVED :
                        PaymentTransaction.PaymentStatus.DECLINED)
                .build();

        // 승인 성공: 200, 거절: 402
        if (result.isApproved()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(402).body(response);
        }
    }

    /**
     * 트랜잭션 ID로 결제 내역 조회
     * HTTP 200
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentDto.ApprovalResponse> getPayment(
            @PathVariable String transactionId) {

        log.info("[Controller] 결제 조회 transactionId={}", transactionId);

        // 트랜잭션 ID로 조회
        PaymentTransaction tx = paymentService.findByTransactionId(transactionId);

        PaymentDto.ApprovalResponse response = PaymentDto.ApprovalResponse.builder()
                .success(tx.getStatus() == PaymentTransaction.PaymentStatus.APPROVED)
                .transactionId(tx.getTransactionId())
                .orderId(tx.getOrderId())
                .merchantId(tx.getMerchantId())
                .approvalNumber(tx.getApprovalNumber())
                .maskedCardNumber(tx.getMaskedCardNumber())
                .cardCompany(tx.getCardCompany())
                .amount(tx.getAmount())
                .installment(tx.getInstallment())
                .status(tx.getStatus())
                .failureReason(tx.getFailureReason())
                .requestedAt(tx.getRequestedAt())
                .approvedAt(tx.getApprovedAt())
                .build();

        return ResponseEntity.ok(response);
    }
}