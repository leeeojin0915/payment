import type { ApprovalResponse } from '../types/payment';

interface ResultCardProps {
    result: ApprovalResponse | null;
    error?: string;
    onClickTxId?: (txId: string) => void;
}

const fmt = (n: number) => n.toLocaleString('ko-KR') + '원';

const fmtTime = (dt: string | null) => {
    if (!dt) return '-';
    return new Date(dt).toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
    });
};

const STATUS_STYLE = {
    approved: { bg: '#0d1f14', border: '#1a4a28', color: '#4ade80', label: '승인 완료' },
    declined: { bg: '#1f0d0d', border: '#4a1a1a', color: '#f87171', label: '승인 거절' },
    error:    { bg: '#1a1a0d', border: '#4a420d', color: '#facc15', label: '오류' },
};

export default function ResultCard({ result, error, onClickTxId }: ResultCardProps) {
    if (!result && !error) return null;

    const type = error ? 'error' : result?.success ? 'approved' : 'declined';
    const style = STATUS_STYLE[type];

    return (
        <div style={{
            background: style.bg,
            border: `1px solid ${style.border}`,
            borderRadius: 10,
            padding: '1rem 1.25rem',
            marginTop: 14,
        }}>
            {/* 상태 헤더 */}
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 10 }}>
                <div style={{
                    width: 7, height: 7,
                    borderRadius: '50%',
                    background: style.color,
                    flexShrink: 0,
                }} />
                <span style={{ fontSize: 13, fontWeight: 500, color: style.color }}>
          {style.label}
        </span>
            </div>

            {/* 오류 메시지 */}
            {error && (
                <div style={{ fontSize: 12, color: '#facc15' }}>{error}</div>
            )}

            {/* 결과 정보 */}
            {result && (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '6px 16px' }}>
                    {result.approvalNumber && (
                        <Field label="승인번호" value={result.approvalNumber} />
                    )}
                    {result.failureReason && (
                        <Field label="거절사유" value={result.failureReason} />
                    )}
                    <Field label="결제금액" value={fmt(result.amount)} />
                    <Field label="카드사" value={result.cardCompany ?? '-'} />
                    <Field label="마스킹카드" value={result.maskedCardNumber ?? '-'} small />
                    <Field label="승인시각" value={fmtTime(result.approvedAt)} />
                    {result.transactionId && (
                        <div style={{ gridColumn: '1 / -1' }}>
                            <div style={{
                                fontSize: 11,
                                color: '#6b6880',
                                letterSpacing: '0.05em',
                                textTransform: 'uppercase',
                                marginBottom: 4,
                            }}>
                                트랜잭션 ID
                            </div>
                            <div
                                onClick={() => onClickTxId?.(result.transactionId!)}
                                style={{
                                    fontFamily: "'DM Mono', monospace",
                                    fontSize: 10,
                                    wordBreak: 'break-all',
                                    color: '#818cf8',
                                    cursor: onClickTxId ? 'pointer' : 'default',
                                    textDecoration: onClickTxId ? 'underline' : 'none',
                                }}
                            >
                                {result.transactionId}
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}

interface FieldProps {
    label: string;
    value: string;
    small?: boolean;
}

function Field({ label, value, small }: FieldProps) {
    return (
        <div>
            <div style={{
                fontSize: 11,
                color: '#6b6880',
                letterSpacing: '0.05em',
                textTransform: 'uppercase',
                marginBottom: 2,
            }}>
                {label}
            </div>
            <div style={{
                fontFamily: "'DM Mono', monospace",
                fontSize: small ? 11 : 12,
                color: '#e8e6f0',
            }}>
                {value}
            </div>
        </div>
    );
}