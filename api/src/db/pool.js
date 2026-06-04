import mysql from 'mysql2/promise';
import { config } from '../config.js';

export const pool = mysql.createPool(config.mysql);

export async function query(sql, params = []) {
  const [rows] = await pool.execute(sql, params);
  return { rows: Array.isArray(rows) ? rows : [] };
}

export async function healthCheck() {
  const { rows } = await query('SELECT 1 AS ok');
  return rows[0]?.ok === 1;
}
