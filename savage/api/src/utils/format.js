const MC_COLOR = /§[0-9a-fk-or]/gi;

export function stripMcColors(text) {
  if (text == null) return '';
  return String(text).replace(MC_COLOR, '');
}

export function isUuid(value) {
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(value);
}

export function pickInt(obj, key, fallback = 0) {
  const v = obj?.[key];
  if (v == null || Number.isNaN(Number(v))) return fallback;
  return Number(v);
}

export function computeRatios(stats) {
  const matches = pickInt(stats, 'matches');
  const wins = pickInt(stats, 'wins');
  const defeats = pickInt(stats, 'defeats');
  const kills = pickInt(stats, 'kills');
  const deaths = pickInt(stats, 'deaths');

  const winRate = matches === 0 ? 0 : Math.round((wins / matches) * 1000) / 10;
  const kdr = deaths === 0 ? kills : Math.round((kills / deaths) * 100) / 100;
  const wlr = defeats === 0 ? wins : Math.round((wins / defeats) * 100) / 100;

  return { winRate, kdr, wlr };
}

export function getRankedTier(points) {
  if (points >= 4000) return 'Master';
  if (points >= 3000) return 'Titan';
  if (points >= 2500) return 'Gladiator';
  if (points >= 2000) return 'Elite';
  if (points >= 1500) return 'Veteran';
  if (points >= 1000) return 'Soldier';
  if (points >= 500) return 'Recruit';
  return 'Rookie';
}

export function paginate(list, limit = 50, offset = 0) {
  const safeLimit = Math.min(Math.max(limit, 1), 200);
  const safeOffset = Math.max(offset, 0);
  const slice = list.slice(safeOffset, safeOffset + safeLimit);
  return {
    total: list.length,
    limit: safeLimit,
    offset: safeOffset,
    hasMore: safeOffset + safeLimit < list.length,
    items: slice,
  };
}
