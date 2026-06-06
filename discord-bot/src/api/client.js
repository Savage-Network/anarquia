import { config } from '../config.js';

export class ApiError extends Error {
  constructor(message, status, body) {
    super(message);
    this.status = status;
    this.body = body;
  }
}

async function apiFetch(path, options = {}) {
  const headers = {
    Accept: 'application/json',
    ...(options.headers || {}),
  };

  if (options.auth) {
    headers['X-Api-Secret'] = config.apiSecret;
  }

  let res;
  try {
    res = await fetch(`${config.apiUrl}${path}`, {
      ...options,
      headers,
      signal: AbortSignal.timeout(10_000),
    });
  } catch (e) {
    const reason = e?.cause?.code || e?.code || e?.message || 'fetch failed';
    throw new ApiError(
      `Nao foi possivel conectar a API (${config.apiUrl}). ${reason}`,
      0,
      null,
    );
  }

  let data = null;
  try {
    data = await res.json();
  } catch {
    data = null;
  }

  if (!res.ok) {
    throw new ApiError(
      data?.message || data?.error || 'Erro na API.',
      res.status,
      data,
    );
  }

  return data;
}

export async function fetchPlayer(username) {
  const data = await apiFetch(`/api/player/${encodeURIComponent(username)}`);
  if (!data.success) {
    throw new ApiError(
      data.error_message || 'Jogador nao encontrado.',
      404,
      data,
    );
  }
  return data.response;
}

export async function fetchMenuProfile(username) {
  const data = await apiFetch(`/api/v1/discord/menu/${encodeURIComponent(username)}`);
  return data.profile;
}

export async function fetchLinkByDiscord(discordId) {
  try {
    const data = await apiFetch(`/api/v1/discord/link/discord/${encodeURIComponent(discordId)}`);
    return data.link;
  } catch (e) {
    if (e.status === 404) return null;
    throw e;
  }
}

export async function linkDiscord(code, discordId, discordTag) {
  return apiFetch('/api/v1/discord/link', {
    method: 'POST',
    auth: true,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ code, discordId, discordTag }),
  });
}

export async function unlinkDiscord(discordId) {
  return apiFetch('/api/v1/discord/link', {
    method: 'DELETE',
    auth: true,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ discordId }),
  });
}

export async function fetchServerStatus() {
  try {
    const res = await fetch(`${config.apiUrl}/api/v1/server/status`);
    if (!res.ok) return { onlinePlayers: null };
    return await res.json();
  } catch {
    return { onlinePlayers: null };
  }
}
