import { useCallback, useEffect, useState } from 'react';

const API_BASE = import.meta.env.VITE_API_URL ?? '';

async function sendProcessRequests(quantity: number): Promise<void> {
  const response = await fetch(`${API_BASE}/api/process?quantity=${quantity}`, {
    method: 'POST',
  });
  if (!response.ok) {
    throw new Error(`Failed to send ${quantity} requests`);
  }
}

async function fetchCount(): Promise<number> {
  const response = await fetch(`${API_BASE}/api/count`);
  if (!response.ok) {
    throw new Error('Failed to fetch count');
  }
  const data = (await response.json()) as { count: number };
  return data.count;
}

function App() {
  const [count, setCount] = useState<number>(0);
  const [loading, setLoading] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  const refreshCount = useCallback(async () => {
    try {
      const value = await fetchCount();
      setCount(value);
      setError(null);
    } catch {
      setError('Erro ao obter count');
    }
  }, []);

  useEffect(() => {
    refreshCount();
    const interval = setInterval(refreshCount, 5000);
    return () => clearInterval(interval);
  }, [refreshCount]);

  const handleProcess = async (quantity: number) => {
    setLoading(quantity);
    setError(null);
    try {
      await sendProcessRequests(quantity);
    } catch {
      setError(`Erro ao enviar ${quantity} request(s)`);
    } finally {
      setLoading(null);
    }
  };

  return (
    <div className="page">
      <div className="panel">
        <h1>Stressful Kafka</h1>
        <p className="subtitle">Simulação de carga com processamento lento</p>

        <div className="count-box">
          <span className="count-label">Itens processados</span>
          <span className="count-value">{count}</span>
        </div>

        <div className="buttons">
          <button disabled={loading !== null} onClick={() => handleProcess(1)}>
            {loading === 1 ? 'Enviando...' : '1 request'}
          </button>
          <button disabled={loading !== null} onClick={() => handleProcess(10)}>
            {loading === 10 ? 'Enviando...' : '10 requests'}
          </button>
          <button disabled={loading !== null} onClick={() => handleProcess(100)}>
            {loading === 100 ? 'Enviando...' : '100 requests'}
          </button>
        </div>

        {error && <p className="error">{error}</p>}
      </div>
    </div>
  );
}

export default App;
