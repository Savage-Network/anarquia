import express from 'express';
import { config } from '../config.js';
import { sendStaffReport } from '../webhooks/reports.js';
import {
  normalizePunishment,
  sendPublicBan,
  sendPublicMute,
  sendRevocation,
  sendStaffPunishment,
} from '../webhooks/punishments.js';

function checkSecret(req, res) {
  if (config.http.secret && req.headers['x-bot-secret'] !== config.http.secret) {
    res.status(401).json({ ok: false, error: 'unauthorized' });
    return false;
  }
  return true;
}

export function startHttpServer() {
  const app = express();
  app.use(express.json({ limit: '64kb' }));

  app.get('/health', (_req, res) => res.json({ ok: true }));

  app.post('/punishment', async (req, res) => {
    if (!checkSecret(req, res)) return;

    const { action, punishment: raw } = req.body || {};
    if (!raw) {
      return res.status(400).json({ ok: false, error: 'missing punishment' });
    }

    const punishment = normalizePunishment({ punishment: raw });

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

  app.post('/report', async (req, res) => {
    if (!checkSecret(req, res)) return;

    const report = req.body?.report || req.body;
    if (!report?.targetName && !report?.target_name) {
      return res.status(400).json({ ok: false, error: 'missing report' });
    }

    try {
      await sendStaffReport({
        id: report.id,
        reporterName: report.reporterName || report.reporter_name,
        targetName: report.targetName || report.target_name,
        reason: report.reason,
        details: report.details,
        serverId: report.serverId || report.server_id,
      });
      res.json({ ok: true });
    } catch (e) {
      console.error('[http] report', e);
      res.status(500).json({ ok: false });
    }
  });

  app.listen(config.http.port, () => {
    console.log(
      `[http] Webhooks em :${config.http.port} (/punishment, /report)`,
    );
  });
}
