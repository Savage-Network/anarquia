import { findAccount, formatAccountPublic } from './accountService.js';
import { getPlayerPunishments } from './punishmentService.js';
import { getAllMinigameStats } from './memberService.js';
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

function formatCollectibles(ctx) {
  const owned = ctx.collectibles || [];
  const active = ctx.activeCollectibles || [];
  const favorites = ctx.favoriteCollectibles || [];

  return {
    ownedCount: owned.length,
    activeCount: active.length,
    favoritesCount: favorites.length,
    active,
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
    friends: formatFriends(ctx),
    collectibles: formatCollectibles(ctx),
    toggles: ctx.toggle || {},
    auth: {
      state: ctx.auth?.state,
      registered: ctx.auth?.registered,
    },
    route: ctx.route?.server
      ? { server: ctx.route.server, arcade: ctx.route.arcade }
      : null,
    party: ctx.partyIdentifier && ctx.partyIdentifier !== '...' ? ctx.partyIdentifier : null,
    customTitle: ctx.customTitle || null,
    permissions: (ctx.permissions || []).map((p) => ({
      permission: p.permission,
      expiresAt: p.expiresAt,
    })),
  };
}

/**
 * Perfil completo do jogador num único objeto.
 */
export async function getFullProfile(identifier) {
  const account = await findAccount(identifier);
  if (!account) return null;

  const [punishmentData, statsData] = await Promise.all([
    getPlayerPunishments(account.id),
    getAllMinigameStats(account.id),
  ]);

  const punishments = splitPunishments(punishmentData?.punishments || []);

  return {
    id: account.id,
    name: account.name,
    account: formatAccountExtended(account),
    punishments,
    minigames: statsData?.minigames ?? {},
    meta: {
      fetchedAt: Date.now(),
      website: 'https://savagemc.net',
    },
  };
}
