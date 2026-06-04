export function formatDate(ts) {
  if (!ts || ts <= 0) return '—';
  return new Date(ts).toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function formatExpiry(expiresAt) {
  if (!expiresAt || expiresAt <= -1) return 'Permanente';
  const left = expiresAt - Date.now();
  if (left <= 0) return 'Expirado';
  const days = Math.floor(left / 86_400_000);
  const hours = Math.floor((left % 86_400_000) / 3_600_000);
  if (days > 0) return `${days} dia${days > 1 ? 's' : ''}`;
  if (hours > 0) return `${hours} hora${hours > 1 ? 's' : ''}`;
  const mins = Math.floor((left % 3_600_000) / 60_000);
  return `${mins} minuto${mins > 1 ? 's' : ''}`;
}
