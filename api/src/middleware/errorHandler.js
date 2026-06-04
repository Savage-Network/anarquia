export function notFoundHandler(_req, res) {
  res.status(404).json({
    error: 'NOT_FOUND',
    message: 'Rota não encontrada.',
  });
}

export function errorHandler(err, _req, res, _next) {
  console.error('[api]', err);
  res.status(500).json({
    error: 'INTERNAL_ERROR',
    message: 'Erro interno na API.',
  });
}
