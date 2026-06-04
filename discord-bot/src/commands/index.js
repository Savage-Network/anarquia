import * as conta from './conta.js';
import * as status from './status.js';

export const commands = [conta, status];

export const commandMap = new Map(commands.map((c) => [c.data.name, c]));
