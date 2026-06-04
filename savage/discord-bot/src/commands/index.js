import * as conta from './conta.js';
import * as stats from './stats.js';
import * as ticket from './ticket.js';

export const commands = [conta, stats, ticket];

export const commandMap = new Map(commands.map((c) => [c.data.name, c]));
