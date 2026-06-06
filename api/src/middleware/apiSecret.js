import { config } from '../config.js';

export function requireApiSecret(req, res, next) {
  if (!config.apiSecret) {
    return res.status(503).json({
      ok: false,
      error: 'API_SECRET_NOT_CONFIGURED',
      message: 'API_SECRET nao configurado no servidor.',
    });
  }

  const header = req.headers['x-api-secret'];
  const bodySecret = req.body?.secret;
  const provided = header || bodySecret;

  if (!provided || provided !== config.apiSecret) {
    return res.status(401).json({
      ok: false,
      error: 'UNAUTHORIZED',
      message: 'Credencial invalida.',
    });
  }

  next();
}
