import * as conta from './conta.js';
import * as desvincular from './desvincular.js';
import * as menu from './menu.js';
import * as status from './status.js';
import * as syncroles from './syncroles.js';
import * as vincular from './vincular.js';

export const commands = [conta, menu, status, vincular, desvincular, syncroles];

export const commandMap = new Map(commands.map((c) => [c.data.name, c]));
