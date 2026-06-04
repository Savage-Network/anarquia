import { query } from '../db/pool.js';

export async function listReports({ status, target, limit = 50, offset = 0 } = {}) {
  let sql = `SELECT id, reporter_uuid, reporter_name, target_uuid, target_name,
                    reason, details, server_id, status, staff_uuid, staff_note,
                    created_at, updated_at
             FROM player_reports`;
  const clauses = [];
  const params = [];

  if (status) {
    params.push(String(status).toUpperCase());
    clauses.push('status = ?');
  }
  if (target) {
    params.push(target);
    clauses.push('(LOWER(target_name) = LOWER(?) OR target_uuid = ?)');
    params.push(target);
  }

  if (clauses.length) sql += ` WHERE ${clauses.join(' AND ')}`;
  sql += ' ORDER BY created_at DESC LIMIT ? OFFSET ?';
  params.push(Math.min(limit, 100), offset);

  const { rows } = await query(sql, params);

  return {
    total: rows.length,
    limit,
    offset,
    items: rows.map((r) => ({
      id: r.id,
      reporter: { uuid: r.reporter_uuid, name: r.reporter_name },
      target: { uuid: r.target_uuid, name: r.target_name },
      reason: r.reason,
      details: r.details,
      serverId: r.server_id,
      status: r.status,
      staffUuid: r.staff_uuid,
      staffNote: r.staff_note,
      createdAt: r.created_at,
      updatedAt: r.updated_at,
    })),
  };
}
