import 'dotenv/config';

export const config = {
  token: process.env.DISCORD_TOKEN || '',
  guildId: process.env.DISCORD_GUILD_ID || '',
  apiUrl: (process.env.SAVAGE_API_URL || 'http://127.0.0.1:3000').replace(/\/$/, ''),
  ticket: {
    categoryId: process.env.TICKET_CATEGORY_ID || '',
    staffRoleId: process.env.STAFF_ROLE_ID || '',
    logsChannelId: process.env.TICKET_LOGS_CHANNEL_ID || '',
  },
  webhooks: {
    staff: process.env.WEBHOOK_PUNISHMENT_STAFF || '',
    public: process.env.WEBHOOK_PUNISHMENT_PUBLIC || '',
  },
  http: {
    port: Number(process.env.BOT_HTTP_PORT || 4040),
    secret: process.env.BOT_HTTP_SECRET || '',
  },
  minotar: (username, size = 120) =>
    `https://minotar.net/avatar/${encodeURIComponent(username)}/${size}`,
};
