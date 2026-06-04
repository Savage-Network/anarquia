const MODE_LABELS = {
  GENERAL: 'BedWars Geral',
  BEDWARS_SOLO: 'BedWars Solo',
  BEDWARS_DUO: 'BedWars Duplas',
  BEDWARS_TRIO: 'BedWars Trios',
  BEDWARS_QUARTET: 'BedWars Quartetos',
  BEDWARS_VERSUS_SOLO: 'BedWars 1v1',
  BEDWARS_VERSUS_DUO: 'BedWars 2v2',
  BEDWARS_VERSUS_TRIO: 'BedWars 3v3',
  BEDWARS_VERSUS_QUARTET: 'BedWars 4v4',
  BEDWARS_INFECTION: 'BedWars Infection',
};

const MODE_PREFIX = {
  GENERAL: null,
  BEDWARS_SOLO: 'solo',
  BEDWARS_DUO: 'doubles',
  BEDWARS_TRIO: '3v3',
  BEDWARS_QUARTET: '4v4',
  BEDWARS_VERSUS_SOLO: '1v1',
  BEDWARS_VERSUS_DUO: '2v2',
  BEDWARS_VERSUS_TRIO: '3v3v3v3',
  BEDWARS_VERSUS_QUARTET: '4v4v4v4',
  BEDWARS_INFECTION: 'infection_solo',
};

export function getModeLabel(mode) {
  return MODE_LABELS[mode] || mode;
}

export function pickBedwarsStats(bw, mode) {
  if (!bw) return null;

  if (mode === 'GENERAL') {
    return {
      matches: bw.games_played ?? 0,
      wins: bw.wins ?? 0,
      defeats: bw.losses ?? 0,
      kills: bw.kills ?? 0,
      deaths: bw.deaths ?? 0,
      finalKills: bw.final_kills ?? 0,
      bedDestruction: bw.beds_broken ?? 0,
      winStreak: bw.winstreak ?? 0,
      highWinStreak: bw.max_winstreak ?? 0,
      level: bw.level ?? 0,
      xp: bw.xp ?? 0,
      rankedPoints: bw.ranked_points ?? 0,
      rankedTier: bw.ranked_tier ?? 'Rookie',
    };
  }

  const prefix = MODE_PREFIX[mode];
  if (!prefix) return null;

  return {
    matches: bw[`${prefix}_games_played`] ?? 0,
    wins: bw[`${prefix}_wins`] ?? 0,
    defeats: bw[`${prefix}_losses`] ?? 0,
    kills: bw[`${prefix}_kills`] ?? 0,
    deaths: bw[`${prefix}_deaths`] ?? 0,
    finalKills: bw[`${prefix}_final_kills`] ?? 0,
    bedDestruction: bw[`${prefix}_beds_broken`] ?? 0,
    winStreak: bw[`${prefix}_winstreak`] ?? 0,
    highWinStreak: bw[`${prefix}_max_winstreak`] ?? 0,
  };
}

export function buildStatsDescription(s, modeName, extra = {}) {
  const matches = s.matches ?? 0;
  const wins = s.wins ?? 0;
  const defeats = s.defeats ?? 0;
  const kills = s.kills ?? 0;
  const deaths = s.deaths ?? 0;
  const beds = s.bedDestruction ?? 0;
  const bedsLost = Math.max(0, matches - wins);
  const winPct = matches === 0 ? '0%' : `${Math.round((wins / matches) * 1000) / 10}%`;
  const kdr = deaths === 0 ? kills : Math.round((kills / deaths) * 100) / 100;
  const wlr = defeats === 0 ? wins : Math.round((wins / defeats) * 100) / 100;
  const bblr = bedsLost === 0 ? beds : Math.round((beds / bedsLost) * 100) / 100;

  let desc = '';
  if (extra.level != null) {
    desc += `**Nível:** ${extra.level}\n**XP:** ${extra.xp ?? 0}\n\n`;
  }
  desc +=
    `**Partidas:** ${matches}\n` +
    `**Vitórias:** ${wins} (${winPct})\n` +
    `**Derrotas:** ${defeats}\n\n` +
    `**Kills:** ${kills}\n` +
    `**Deaths:** ${deaths}\n` +
    `**KDR:** ${kdr}\n\n` +
    `**Winstreak:** ${s.winStreak ?? 0}\n` +
    `**Melhor:** ${s.highWinStreak ?? 0}\n` +
    `**WLR:** ${wlr}\n\n` +
    `**Camas quebradas:** ${beds}\n` +
    `**Camas perdidas:** ${bedsLost}\n` +
    `**BBLR:** ${bblr}\n\n` +
    `**Final Kills:** ${s.finalKills ?? 0}`;

  if (extra.rankedTier) {
    desc += `\n\n**Rank:** ${extra.rankedTier}`;
  }

  return { desc, title: `${modeName}` };
}
