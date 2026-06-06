import { findAccount, formatAccountPublic } from './accountService.js';
import { applyIdentityToAccount, loadAccountIdentity } from './accountIdentityService.js';
import { isPlayerOnline, resolveAccountPremium } from './presenceService.js';
import { resolveClanForAccount, getPlayerStats } from './clanService.js';
import { findLinkByPlayer, formatLink } from './linkService.js';
import { getPlayerPunishments } from './punishmentService.js';
import { findCatalogItem, MEDALS, TAGS } from '../data/catalog.js';

function formatSkin(ctx) {
  const skin = ctx.skin;
  if (!skin) return null;
  return {
    type: skin.type || skin.skinType,
    value: skin.value,
    lastUpdate: skin.lastUpdate,
  };
}

function formatFriends(ctx) {
  const friendMeta = ctx.friend;
  if (!friendMeta) return { count: 0, max: 12, requests: 0, list: [] };

  const raw = friendMeta.friends || {};
  const list = (Array.isArray(raw) ? raw : Object.values(raw)).map((f) => ({
    id: f.id,
    name: f.name,
    type: f.type,
    since: f.startedAt,
  }));

  return {
    count: list.length,
    max: 12,
    requests: (friendMeta.requests || []).length,
    list,
  };
}

function splitPunishments(punishments) {
  const active = punishments.filter((p) => p.active);
  const history = punishments.filter((p) => !p.active);
  return { active, history, total: punishments.length, activeCount: active.length };
}

function formatAccountExtended(account) {
  const base = formatAccountPublic(account);
  const ctx = account.context || {};

  const tagId = typeof ctx.tag === 'string' ? ctx.tag : ctx.tag?.name || 'MEMBER';
  const tagCatalog = findCatalogItem(TAGS, tagId);

  const medalId = typeof ctx.medal === 'string' ? ctx.medal : ctx.medal?.name || 'NONE';
  const medalCatalog = findCatalogItem(MEDALS, medalId);

  return {
    ...base,
    friends: formatFriends(ctx),
    tag: {
      ...base.tag,
      order: tagCatalog?.order,
      role: tagCatalog?.role,
      rankSource: tagCatalog?.rankSource,
    },
    medal: {
      ...base.medal,
      symbol: medalCatalog?.symbol,
    },
    skin: formatSkin(ctx),
    toggles: ctx.toggle || {},
    auth: {
      state: ctx.auth?.state,
      registered: ctx.auth?.registered,
    },
    route: ctx.route?.server
      ? { server: ctx.route.server, arcade: ctx.route.arcade }
      : null,
    party:
      ctx.partyIdentifier && ctx.partyIdentifier !== '...'
        ? ctx.partyIdentifier
        : null,
    customTitle: ctx.customTitle || null,
  };
}

export async function getFullProfile(identifier) {
  const account = await findAccount(identifier);
  if (!account) return null;

  const punishmentData = await getPlayerPunishments(account.id);
  const punishments = splitPunishments(punishmentData?.punishments || []);
  const clan = await resolveClanForAccount(account.context || {});
  const stats = await getPlayerStats(account.id);
  const discordLink = formatLink(await findLinkByPlayer(account.id));

  const formatted = formatAccountExtended(account);
  const identity = await loadAccountIdentity(account.id);
  applyIdentityToAccount(formatted, identity);

  const [online, premiumInfo] = await Promise.all([
    isPlayerOnline(account.id),
    resolveAccountPremium(account.id, account.context || {}),
  ]);
  formatted.online = online;
  formatted.premium = premiumInfo.premium;
  if (clan) {
    formatted.clan = {
      id: clan.id,
      name: clan.name,
      tag: clan.tag,
      tagColorId: clan.tagColorId,
    };
  }

  return {
    id: account.id,
    name: account.name,
    account: formatted,
    punishments,
    minigames: {},
    stats: stats
      ? {
          kills: stats.kills,
          deaths: stats.deaths,
          golds: stats.golds,
          bestKillStreak: stats.best_kill_streak,
        }
      : null,
    discord: discordLink,
    meta: {
      fetchedAt: Date.now(),
      website: 'https://savagemc.net',
    },
  };
}
