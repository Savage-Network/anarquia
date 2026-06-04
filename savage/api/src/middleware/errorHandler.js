export function notFoundHandler(_req, res) {
  res.status(404).json({
    error: 'NOT_FOUND',
    message: 'Rota não encontrada.',
  });
}

export function errorHandler(err, _req, res, _next) {
  console.error('[api]', err);

  if (err.code === 'ECONNREFUSED' || err.code === 'ENOTFOUND') {
    return res.status(503).json({
      error: 'DATABASE_UNAVAILABLE',
      message: 'Não foi possível conectar ao PostgreSQL.',
    });
  }

  res.status(err.status || 500).json({
    error: err.code || 'INTERNAL_ERROR',
    message: err.message || 'Erro interno.',
  });
}
