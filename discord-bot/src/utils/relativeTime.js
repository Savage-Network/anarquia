const UNITS = [
  { ms: 31_536_000_000, one: 'ano', many: 'anos' },
  { ms: 2_592_000_000, one: 'mes', many: 'meses' },
  { ms: 86_400_000, one: 'dia', many: 'dias' },
  { ms: 3_600_000, one: 'hora', many: 'horas' },
  { ms: 60_000, one: 'minuto', many: 'minutos' },
];

export function formatRelative(ms) {
  if (!ms || ms <= 0) return '—';
  const diff = Date.now() - ms;
  if (diff < 0) return 'agora';

  for (const unit of UNITS) {
    const value = Math.floor(diff / unit.ms);
    if (value >= 1) {
      const label = value === 1 ? unit.one : unit.many;
      return `ha ${value} ${label}`;
    }
  }

  return 'agora';
}

export function formatRelativeFuture(ms) {
  if (!ms || ms <= 0) return '—';
  const diff = ms - Date.now();
  if (diff <= 0) return 'expirado';

  for (const unit of UNITS) {
    const value = Math.floor(diff / unit.ms);
    if (value >= 1) {
      const label = value === 1 ? unit.one : unit.many;
      return `em ${value} ${label}`;
    }
  }

  return 'em instantes';
}
