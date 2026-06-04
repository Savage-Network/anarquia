import express from 'express';
import { config } from '../config.js';
import {
  sendPublicBan,
  sendPublicMute,
  sendRevocation,
  sendStaffPunishment,
} from '../webhooks/punishments.js';

export function startHttpServer() {
  const app = express();
  app.use(express.json({ limit: '64kb' }));

  app.get('/health', (_req, res) => res.json({ ok: true }));

  app.post('/punishment', async (req, res) => {
    if (config.http.secret && req.headers['x-bot-secret'] !== config.http.secret) {
      return res.status(401).json({ ok: false });
    }

    const { action, punishment } = req.body || {};
    if (!punishment) {
      return res.status(400).json({ ok: false, error: 'missing punishment' });
    }

    try {
      switch (action) {
        case 'create':
          await sendStaffPunishment(punishment);
          if (punishment.category === 'BAN') await sendPublicBan(punishment);
          if (punishment.category === 'MUTE') await sendPublicMute(punishment);
          break;
        case 'revoke':
          await sendRevocation(punishment);
          break;
        default:
          await sendStaffPunishment(punishment);
      }
      res.json({ ok: true });
    } catch (e) {
      console.error('[http] punishment', e);
      res.status(500).json({ ok: false });
    }
  });

  app.listen(config.http.port, () => {
    console.log(`[http] Bungee webhook em :${config.http.port}/punishment`);
  });
}
