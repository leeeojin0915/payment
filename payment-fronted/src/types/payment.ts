// 결제 상태 enum
export type PaymentStatus = 'PENDING' | 'APPROVED' | 'DECLINED';

// 결제 승인 요청 타입
export interface ApprovalRequest {
    orderId: string;
    merchantId: string;
    cardNumber: string;
    expiryDate: string;
    cvv: string;
    amount: number;
    installment: number;
}

// 결제 승인 응답 타입
export interface ApprovalResponse {
    success: boolean;
    transactionId: string | null;
    orderId: string;
    merchantId: string;
    approvalNumber: string | null;
    maskedCardNumber: string | null;
    cardCompany: string | null;
    amount: number;
    installment: number;
    status: PaymentStatus;
    failureReason: string | null;
    requestedAt: string | null;
    approvedAt: string | null;
}

// 처리 내역 아이템 타입 (응답 + 로컬 시각)
export interface HistoryItem extends ApprovalResponse {
    _localTime: Date;
}

// 통계 타입
export interface Stats {
    total: number;
    approved: number;
    declined: number;
    totalAmount: number;
}

// 카드 프리셋 타입
export interface CardPreset {
    label: string;
    cardNumber: string;
    description: string;
}