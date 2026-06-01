import type { HistoryItem } from '../types/payment';

interface Props {
    history: HistoryItem[];
    onClickItem: (txId: string) => void;
}

const fmt = (n: number) => n.toLocaleString('ko-KR') + '원';

export default function HistoryList({ history, onClickItem }: Props) {
    return (
        <div>
            <div style={{ fontSize: 13, fontWeight: 500, color: '#9d9ab0', letterSpacing: '0.04em', textTransform: 'uppercase', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: 8 }}>
                처리 내역 ({history.length})
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 8, maxHeight: 280, overflowY: 'auto' }}>
                {history.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '2rem', color: '#4a4a6a', fontSize: 13 }}>
                        아직 처리된 결제가 없습니다
                    </div>
                ) : history.map((h, i) => (
                    <div
                        key={i}
                        onClick={() => h.transactionId && onClickItem(h.transactionId)}
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: 12,
                            padding: '10px 12px',
                            background: '#0d0d16',
                            border: '1px solid #1e1e2e',
                            borderRadius: 9,
                            cursor: 'pointer',
                            transition: 'border-color 0.15s',
                        }}
                        onMouseEnter={e => (e.currentTarget.style.borderColor = '#2a2a4a')}
                        onMouseLeave={e => (e.currentTarget.style.borderColor = '#1e1e2e')}
                    >
                        {/* 상태 뱃지 */}
                        <span style={{
                            fontSize: 10,
                            fontWeight: 500,
                            padding: '3px 8px',
                            borderRadius: 20,
                            fontFamily: "'DM Mono', monospace",
                            flexShrink: 0,
                            ...(h.success
                                    ? { background: '#0d1f14', color: '#4ade80', border: '1px solid #1a4a28' }
                                    : { background: '#1f0d0d', color: '#f87171', border: '1px solid #4a1a1a' }
                            ),
                        }}>
              {h.success ? '승인' : '거절'}
            </span>

                        {/* 주문 정보 */}
                        <div style={{ flex: 1, minWidth: 0 }}>
                            <div style={{ fontSize: 12, fontFamily: "'DM Mono', monospace", color: '#9d9ab0', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                {h.orderId}
                            </div>
                            <div style={{ fontSize: 13, fontWeight: 500, fontFamily: "'DM Mono', monospace", color: '#e8e6f0' }}>
                                {fmt(h.amount)}
                            </div>
                        </div>

                        {/* 시각 */}
                        <div style={{ fontSize: 11, color: '#4a4a6a', flexShrink: 0 }}>
                            {h._localTime.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}