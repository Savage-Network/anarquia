import 'dotenv/config';

export const config = {
  token: process.env.DISCORD_TOKEN || '',
  guildId: process.env.DISCORD_GUILD_ID || '',
  apiUrl: (process.env.SAVAGE_API_URL || 'http://127.0.0.1:25575').replace(/\/$/, ''),
  apiSecret: process.env.API_SECRET || '',
  mysql: {
    host: process.env.MYSQL_HOST || '127.0.0.1',
    port: Number(process.env.MYSQL_PORT || 3306),
    database: process.env.MYSQL_DATABASE || 'anarquia',
    user: process.env.MYSQL_USER || 'root',
    password: process.env.MYSQL_PASSWORD || '',
    connectionLimit: Number(process.env.MYSQL_POOL_MAX || 5),
    waitForConnections: true,
    enableKeepAlive: true,
  },
  webhooks: {
    staff: process.env.WEBHOOK_PUNISHMENT_STAFF || '',
    public: process.env.WEBHOOK_PUNISHMENT_PUBLIC || '',
    report: process.env.WEBHOOK_REPORT_STAFF || '',
  },
  http: {
    port: Number(process.env.BOT_HTTP_PORT || 4040),
    secret: process.env.BOT_HTTP_SECRET || '',
  },
  roles: {
    linked: process.env.ROLE_LINKED || '1488336331322163333',
    ranks: {
      CHAMPION: process.env.ROLE_CHAMPION || '1486476170202452089',
      CREATOR_PLUS: process.env.ROLE_CREATOR_PLUS || '1486476170189865190',
      CREATOR: process.env.ROLE_CREATOR || '1486476170189865189',
      PRECIOUS: process.env.ROLE_PRECIOUS || '1486476170189865187',
      BETA: process.env.ROLE_BETA || '1486476170189865186',
      SAVAGE: process.env.ROLE_SAVAGE || '1486476170189865183',
      EMBER: process.env.ROLE_EMBER || '1486476170189865184',
      NATURE: process.env.ROLE_NATURE || '1486476170177417357',
    },
    labels: {
      CHAMPION: 'Champion',
      CREATOR_PLUS: 'Creator+',
      CREATOR: 'Creator',
      PRECIOUS: 'Precious',
      BETA: 'Beta',
      SAVAGE: 'Savage',
      EMBER: 'Ember',
      NATURE: 'Nature',
    },
  },
  tickets: {
    enabled: process.env.TICKETS_ENABLED !== 'false',
    categoryId: process.env.TICKET_CATEGORY_ID || '',
    staffRoleId: process.env.STAFF_ROLE_ID || '',
    logsChannelId: process.env.TICKET_LOGS_CHANNEL_ID || '',
    maxOpenPerUser: Number(process.env.TICKET_MAX_OPEN || 3),
    storeUrl: process.env.STORE_URL || 'https://loja.savagemc.net',
    discordInvite: process.env.DISCORD_INVITE || 'https://discord.gg/savagemc',
  },
  minotar: (username, size = 120) =>
    `https://minotar.net/avatar/${encodeURIComponent(username)}/${size}`,
};
