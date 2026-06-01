import { useState } from 'react';

interface Props {
    onQuery: (txId: string) => Promise<void>;
    loading: boolean;
    initialValue?: string;
}

export default function QueryForm({ onQuery, loading, initialValue = '' }: Props) {
    const [txId, setTxId] = useState(initialValue);

    // initialValue가 바뀌면 동기화
    if (initialValue && initialValue !== txId) {
        setTxId(initialValue);
    }

    return (
        <div style={{ display: 'flex', gap: 8 }}>
            <input
                value={txId}
                onChange={e => setTxId(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && !loading && onQuery(txId)}
                placeholder="트랜잭션 ID 입력..."
                style={{
                    flex: 1,
                    padding: '9px 12px',
                    background: '#0d0d16',
                    border: '1px solid #2a2a3a',
                    borderRadius: 8,
                    color: '#e8e6f0',
                    fontFamily: "'DM Mono', monospace",
                    fontSize: 13,
                    outline: 'none',
                }}
            />
            <button
                onClick={() => onQuery(txId)}
                disabled={loading || !txId.trim()}
                style={{
                    padding: '9px 16px',
                    background: '#1a1a2e',
                    border: '1px solid #2a2a4a',
                    borderRadius: 8,
                    color: '#818cf8',
                    fontSize: 13,
                    cursor: loading || !txId.trim() ? 'not-allowed' : 'pointer',
                    whiteSpace: 'nowrap',
                    fontFamily: "'Sora', sans-serif",
                    opacity: loading || !txId.trim() ? 0.5 : 1,
                    transition: 'all 0.15s',
                }}
            >
                {loading ? '조회 중...' : '조회'}
            </button>
        </div>
    );
}