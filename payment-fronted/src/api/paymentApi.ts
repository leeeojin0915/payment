import axios from 'axios';
import type { ApprovalRequest, ApprovalResponse } from '../types/payment';

// REST 서버 base URL
const api = axios.create({
    baseURL: 'http://localhost:8080/api/v1/payments',
    headers: { 'Content-Type': 'application/json' },
    timeout: 10000,
});

// 결제 승인 요청
export const approvePayment = async (
    request: ApprovalRequest
): Promise<ApprovalResponse> => {
    const { data } = await api.post<ApprovalResponse>('/approve', request);
    return data;
};

// 트랜잭션 ID로 결제 조회
export const getPayment = async (
    transactionId: string
): Promise<ApprovalResponse> => {
    const { data } = await api.get<ApprovalResponse>(`/${transactionId}`);
    return data;
};