import type { Stats } from '../types/payment';

interface Props {
    stats: Stats;
}

// 금액 포맷 (한국 원화)
const fmt = (n: number) => n.toLocaleString('ko-KR') + '원';

export default function StatCards({ stats }: Props) {
    return (
        <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(4, 1fr)',
            gap: 12,
            marginBottom: '2rem',
        }}>
            {[
                { label: '총 요청', value: String(stats.total), color: '#e8e6f0' },
                { label: '승인', value: String(stats.approved), color: '#4ade80' },
                { label: '거절', value: String(stats.declined), color: '#f87171' },
                { label: '승인 금액', value: fmt(stats.totalAmount), color: '#818cf8', small: true },
            ].map(({ label, value, color, small }) => (
                <div key={label} style={{
                    background: '#111118',
                    border: '1px solid #1e1e2e',
                    borderRadius: 12,
                    padding: '1rem 1.25rem',
                }}>
                    <div style={{ fontSize: 11, color: '#6b6880', letterSpacing: '0.05em', textTransform: 'uppercase', marginBottom: 8 }}>
                        {label}
                    </div>
                    <div style={{
                        fontSize: small ? 15 : 22,
                        fontWeight: 500,
                        fontFamily: "'DM Mono', monospace",
                        color,
                    }}>
                        {value}
                    </div>
                </div>
            ))}
        </div>
    );
}