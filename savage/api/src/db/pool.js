import pg from 'pg';
import { config } from '../config.js';

const { Pool } = pg;

export const pool = new Pool(config.pg);

pool.on('error', (err) => {
  console.error('[postgres] erro inesperado no pool', err);
});

export async function query(text, params) {
  return pool.query(text, params);
}

export async function healthCheck() {
  const { rows } = await query('SELECT 1 AS ok');
  return rows[0]?.ok === 1;
}
