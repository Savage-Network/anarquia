import { config } from '../config.js';

export class ApiError extends Error {
  constructor(message, status, body) {
    super(message);
    this.status = status;
    this.body = body;
  }
}

export async function fetchPlayer(username) {
  const res = await fetch(`${config.apiUrl}/api/player/${encodeURIComponent(username)}`);
  const data = await res.json();

  if (!res.ok || !data.success) {
    throw new ApiError(
      data.error_message || 'Jogador não encontrado.',
      res.status,
      data,
    );
  }

  return data.response;
}

export async function fetchServerStatus() {
  try {
    const res = await fetch(`${config.apiUrl}/api/v1/server/status`);
    if (!res.ok) return { onlinePlayers: null };
    const data = await res.json();
    return data;
  } catch {
    return { onlinePlayers: null };
  }
}
