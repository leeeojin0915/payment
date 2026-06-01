import { useState } from 'react';
import type { ApprovalRequest } from '../types/payment';

interface Props {
    onSubmit: (req: ApprovalRequest) => Promise<void>;
    loading: boolean;
}

// 카드 프리셋
const PRESETS = [
    { label: 'VISA', cardNumber: '4532015112830366', description: '정상 승인' },
    { label: '거절', cardNumber: '4000000000000002', description: '강제 거절' },
    { label: 'MC',   cardNumber: '5500005555555559', description: 'Mastercard' },
];

const INSTALLMENTS = [0, 2, 3, 4, 5, 6, 9, 12, 18, 24, 36];

const inputStyle: React.CSSProperties = {
    width: '100%',
    padding: '9px 12px',
    background: '#0d0d16',
    border: '1px solid #2a2a3a',
    borderRadius: 8,
    color: '#e8e6f0',
    fontFamily: "'DM Mono', monospace",
    fontSize: 13,
    outline: 'none',
};

const labelStyle: React.CSSProperties = {
    display: 'block',
    fontSize: 11,
    color: '#6b6880',
    letterSpacing: '0.05em',
    textTransform: 'uppercase',
    marginBottom: 6,
};

export default function ApprovalForm({ onSubmit, loading }: Props) {
    const [activePreset, setActivePreset] = useState(0);
    const [form, setForm] = useState<ApprovalRequest>({
        // eslint-disable-next-line react-hooks/purity
        orderId: 'ORDER-' + Date.now(),
        merchantId: 'MERCH00001',
        cardNumber: PRESETS[0].cardNumber,
        expiryDate: '1230',
        cvv: '123',
        amount: 50000,
        installment: 0,
    });

    const handlePreset = (i: number) => {
        setActivePreset(i);
        setForm(f => ({ ...f, cardNumber: PRESETS[i].cardNumber }));
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setForm(f => ({
            ...f,
            [name]: name === 'amount' || name === 'installment' ? Number(value) : value,
        }));
    };

    const handleSubmit = async () => {
        await onSubmit(form);
        // 제출 후 새 주문 ID 생성
        setForm(f => ({ ...f, orderId: 'ORDER-' + Date.now() }));
    };

    return (
        <div>
            {/* 카드 프리셋 */}
            <div style={{ marginBottom: 14 }}>
                <label style={labelStyle}>카드 프리셋</label>
                <div style={{ display: 'flex', gap: 6 }}>
                    {PRESETS.map((p, i) => (
                        <button
                            key={i}
                            onClick={() => handlePreset(i)}
                            style={{
                                flex: 1,
                                padding: '8px 6px',
                                background: activePreset === i ? '#1a1a2e' : '#0d0d16',
                                border: `1px solid ${activePreset === i ? '#6366f1' : '#2a2a3a'}`,
                                borderRadius: 8,
                                fontSize: 11,
                                color: activePreset === i ? '#a5b4fc' : '#9d9ab0',
                                cursor: 'pointer',
                                fontFamily: "'DM Mono', monospace",
                                transition: 'all 0.15s',
                            }}
                        >
                            <div style={{ fontWeight: 500 }}>{p.label}</div>
                            <div style={{ fontSize: 10, marginTop: 2, opacity: 0.7 }}>{p.description}</div>
                        </button>
                    ))}
                </div>
            </div>

            {/* 주문 ID / 가맹점 ID */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 14 }}>
                <div>
                    <label style={labelStyle}>주문 ID</label>
                    <input style={inputStyle} name="orderId" value={form.orderId} onChange={handleChange} />
                </div>
                <div>
                    <label style={labelStyle}>가맹점 ID</label>
                    <input style={inputStyle} name="merchantId" value={form.merchantId} onChange={handleChange} />
                </div>
            </div>

            {/* 카드번호 */}
            <div style={{ marginBottom: 14 }}>
                <label style={labelStyle}>카드번호</label>
                <input style={inputStyle} name="cardNumber" value={form.cardNumber} onChange={handleChange} maxLength={19} />
            </div>

            {/* 유효기간 / CVV */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 14 }}>
                <div>
                    <label style={labelStyle}>유효기간 (MMYY)</label>
                    <input style={inputStyle} name="expiryDate" value={form.expiryDate} onChange={handleChange} maxLength={4} />
                </div>
                <div>
                    <label style={labelStyle}>CVV</label>
                    <input style={inputStyle} name="cvv" value={form.cvv} onChange={handleChange} maxLength={4} />
                </div>
            </div>

            {/* 결제금액 / 할부 */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 16 }}>
                <div>
                    <label style={labelStyle}>결제 금액 (원)</label>
                    <input style={inputStyle} name="amount" type="number" value={form.amount} onChange={handleChange} />
                </div>
                <div>
                    <label style={labelStyle}>할부 개월</label>
                    <select
                        style={{ ...inputStyle }}
                        name="installment"
                        value={form.installment}
                        onChange={handleChange}
                    >
                        {INSTALLMENTS.map(n => (
                            <option key={n} value={n} style={{ background: '#111118' }}>
                                {n === 0 ? '일시불' : `${n}개월`}
                            </option>
                        ))}
                    </select>
                </div>
            </div>

            {/* 제출 버튼 */}
            <button
                onClick={handleSubmit}
                disabled={loading}
                style={{
                    width: '100%',
                    padding: 11,
                    background: loading ? '#2a2a3a' : '#6366f1',
                    border: 'none',
                    borderRadius: 9,
                    color: loading ? '#4a4a6a' : '#fff',
                    fontFamily: "'Sora', sans-serif",
                    fontSize: 14,
                    fontWeight: 500,
                    cursor: loading ? 'not-allowed' : 'pointer',
                    transition: 'all 0.15s',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: 8,
                }}
            >
                {loading && (
                    <div style={{
                        width: 16, height: 16,
                        border: '2px solid rgba(255,255,255,0.2)',
                        borderTopColor: '#fff',
                        borderRadius: '50%',
                        animation: 'spin 0.7s linear infinite',
                    }} />
                )}
                {loading ? '처리 중...' : '결제 승인 요청'}
            </button>

            <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
        </div>
    );
}