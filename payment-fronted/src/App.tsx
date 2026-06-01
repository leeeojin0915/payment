import { useState } from 'react';
import { approvePayment, getPayment } from './api/paymentApi';
import type { ApprovalRequest, ApprovalResponse, HistoryItem, Stats } from './types/payment';
import StatCards from './components/StatCards';
import ApprovalForm from './components/ApprovalForm';
import QueryForm from './components/QueryForm';
import ResultCard from './components/ResultCard';
import HistoryList from './components/HistoryList';

// 카드 스타일
const card: React.CSSProperties = {
  background: '#111118',
  border: '1px solid #1e1e2e',
  borderRadius: 14,
  padding: '1.5rem',
};

const cardTitle: React.CSSProperties = {
  fontSize: 13,
  fontWeight: 500,
  color: '#9d9ab0',
  letterSpacing: '0.04em',
  textTransform: 'uppercase',
  marginBottom: '1.25rem',
};

export default function App() {
  // 승인 요청 상태
  const [approveLoading, setApproveLoading] = useState(false);
  const [approveResult, setApproveResult] = useState<ApprovalResponse | null>(null);
  const [approveError, setApproveError] = useState<string>('');

  // 조회 상태
  const [queryLoading, setQueryLoading] = useState(false);
  const [queryResult, setQueryResult] = useState<ApprovalResponse | null>(null);
  const [queryError, setQueryError] = useState<string>('');
  const [queryTxId, setQueryTxId] = useState<string>('');

  // 처리 내역
  const [history, setHistory] = useState<HistoryItem[]>([]);

  // 통계 계산
  const stats: Stats = {
    total: history.length,
    approved: history.filter(h => h.success).length,
    declined: history.filter(h => !h.success).length,
    totalAmount: history
        .filter(h => h.success)
        .reduce((sum, h) => sum + h.amount, 0),
  };

  // 결제 승인 요청
  const handleApprove = async (req: ApprovalRequest) => {
    setApproveLoading(true);
    setApproveResult(null);
    setApproveError('');
    try {
      const res = await approvePayment(req);
      setApproveResult(res);
      setHistory(prev => [{ ...res, _localTime: new Date() }, ...prev].slice(0, 30));
    } catch (e: any) {
      const msg = e?.response?.data?.message ?? '서버에 연결할 수 없습니다. REST 서버가 실행 중인지 확인하세요.';
      setApproveError(msg);
    } finally {
      setApproveLoading(false);
    }
  };

  // 결제 조회
  const handleQuery = async (txId: string) => {
    if (!txId.trim()) return;
    setQueryLoading(true);
    setQueryResult(null);
    setQueryError('');
    try {
      const res = await getPayment(txId.trim());
      setQueryResult(res);
    } catch (e: any) {
      setQueryError('조회 실패: 트랜잭션 ID를 확인하세요.');
    } finally {
      setQueryLoading(false);
    }
  };

  // 트랜잭션 ID 클릭 시 조회창에 자동 입력
  const handleTxIdClick = (txId: string) => {
    setQueryTxId(txId);
  };

  return (
      <div style={{ maxWidth: 1100, margin: '0 auto', padding: '2.5rem 1.5rem' }}>

        {/* 헤더 */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 14, marginBottom: '2.5rem' }}>
          <div style={{
            width: 40, height: 40,
            background: '#1a1a2e',
            border: '1px solid #2a2a4a',
            borderRadius: 10,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#818cf8" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
              <rect x="2" y="5" width="20" height="14" rx="3"/>
              <line x1="2" y1="10" x2="22" y2="10"/>
            </svg>
          </div>
          <div>
            <h1 style={{ fontSize: 20, fontWeight: 500, letterSpacing: '-0.3px' }}>결제 승인 시스템</h1>
            <p style={{ fontSize: 13, color: '#6b6880', marginTop: 2 }}>Payment Approval Dashboard</p>
          </div>
        </div>

        {/* 통계 카드 */}
        <StatCards stats={stats} />

        {/* 메인 그리드 */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>

          {/* 왼쪽 — 결제 승인 폼 */}
          <div style={card}>
            <div style={cardTitle}>결제 승인 요청</div>
            <ApprovalForm onSubmit={handleApprove} loading={approveLoading} />
            <ResultCard
                result={approveResult}
                error={approveError}
                onClickTxId={handleTxIdClick}
            />
          </div>

          {/* 오른쪽 — 조회 + 내역 */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>

            {/* 결제 조회 */}
            <div style={card}>
              <div style={cardTitle}>결제 내역 조회</div>
              <QueryForm
                  onQuery={handleQuery}
                  loading={queryLoading}
                  initialValue={queryTxId}
              />
              <ResultCard
                  result={queryResult}
                  error={queryError}
              />
            </div>

            {/* 처리 내역 */}
            <div style={{ ...card, flex: 1 }}>
              <HistoryList
                  history={history}
                  onClickItem={(txId) => {
                    setQueryTxId(txId);
                    handleQuery(txId);
                  }}
              />
            </div>
          </div>
        </div>
      </div>
  );
}