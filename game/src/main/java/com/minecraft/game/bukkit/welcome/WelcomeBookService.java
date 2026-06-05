package com.minecraft.game.bukkit.welcome;

import com.minecraft.game.GameSettings;
import com.minecraft.game.account.Account;
import com.minecraft.game.account.context.objects.stats.GameStats;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Entrega o livro guia na primeira entrada do jogador no servidor GAME. */
public final class WelcomeBookService {

    private static final String BOOK_TITLE = "SavageMC — Guia";
    private static final String BOOK_AUTHOR = "SavageMC";

    /** Cores legíveis no papel do livro (evitar §e e §7). */
    private static final String SEP = "§8▬▬▬▬▬▬▬▬▬▬▬▬";
    private static final String BODY = "§0";
    private static final String MUTED = "§8";
    private static final String TITLE = "§6§l";
    private static final String NAME = "§6§lSavage§e§lMC§r";
    private static final String CMD = "§1§l";
    private static final String LINK = "§9§n";
    private static final String GOOD = "§2§l";
    private static final String BAD = "§4§l";
    private static final String WARN = "§c§l";
    private static final String GOLD = "§6";
    private static final String RESET = "§r";

    private WelcomeBookService() {}

    public static void tryGiveOnJoin(Account account, Player player) {
        if (account == null || player == null) return;

        GameStats stats = account.getContext().getGameStats();
        if (stats.isWelcomeBookGiven()) return;

        if (isLegacyPlayer(account)) {
            stats.setWelcomeBookGiven(true);
            account.markDirty();
            return;
        }

        stats.setWelcomeBookGiven(true);
        account.markDirty();
        deliver(player);
        player.sendMessage("§6§lSavageMC §7» §aRecebeste o §fGuia do Jogador§a! Abre o livro no inventário.");
    }

    private static boolean isLegacyPlayer(Account account) {
        var stats = account.getContext().getGameStats();
        if (stats.getKills() > 0 || stats.getDeaths() > 0) return true;
        if (!account.getContext().getHomes().getHomes().isEmpty()) return true;
        return account.getGolds() != GameSettings.STARTING_BALANCE;
    }

    public static void deliver(Player player) {
        ItemStack book = createBook();
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(book);
        if (!overflow.isEmpty()) {
            overflow.values().forEach(leftover ->
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        }
    }

    public static ItemStack createBook() {
        ItemStack stack = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) stack.getItemMeta();
        if (meta == null) return stack;

        meta.setTitle(BOOK_TITLE);
        meta.setAuthor(BOOK_AUTHOR);
        for (String page : buildPages()) {
            meta.addPage(page);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private static List<String> buildPages() {
        List<String> pages = new ArrayList<>();

        pages.add("""
                %s
                %s
                %sBem-vindo ao modo
                %sSemi-Anarquia%s!

                %sSite: %ssavagemc.net
                %sLoja: %snloja.savagemc.net

                %sEste livro lista
                regras e comandos.

                %sPerdeste o livro?
                %s/livro %s(cooldown 8h)"""
                .formatted(NAME, SEP, BODY, TITLE, RESET + BODY,
                        BODY, LINK, BODY, LINK,
                        BODY, MUTED, CMD, MUTED));

        pages.add("""
                %sREGRAS%s
                %s
                %s1.%s Proibido o uso
                de %sHACKS
                %s2.%s Proibido quebrar
                ou explodir bases
                %s3.%s O uso de xray
                é %sLIBERADO
                %s4.%s Abusar de bugs
                é %sPROIBIDO
                %s5.%s Não desrespeite
                outros jogadores
                %s6.%s %sSE DIVIRTA :)%s"""
                .formatted(TITLE, RESET, SEP,
                        BAD, BODY, BAD,
                        BAD, BODY,
                        GOOD, BODY, GOOD,
                        BAD, BODY, BAD,
                        BAD, BODY,
                        GOLD, BODY, GOLD, RESET + BODY));

        pages.add("""
                %sModo de jogo%s
                %s
                %s• %sPvP liberado fora
                do spawn protegido
                %s• %sRoubo de drops ok
                %s• %sSem claims (só spawn)
                %s• %sCombat tag %s15s
                %s• %sNether/End abertos"""
                .formatted(TITLE, RESET, SEP,
                        MUTED, BODY, MUTED, BODY, MUTED, BODY,
                        MUTED, BODY, WARN, MUTED, BODY));

        pages.add("""
                %sSpawn & viagem%s
                %s
                %s/spawn %s— spawn seguro
                %s/lobby %s— volta ao hub
                %s/rtp %s— TP aleatório
                %s/warp %s— warps do mapa
                %s/tpa %s— pedir TP
                %s/tpaccept %s— aceitar TP

                %sBloqueado em combate!"""
                .formatted(TITLE, RESET, SEP,
                        CMD, MUTED, CMD, MUTED, CMD, MUTED,
                        CMD, MUTED, CMD, MUTED, CMD, MUTED,
                        WARN));

        pages.add("""
                %sHomes%s
                %s
                %s/sethome %s— criar home
                %s/home %s— ir à home
                %s/homes %s— listar homes
                %s/delhome %s— apagar

                %sCooldown padrão: %s30s
                %sVIP: %s15s"""
                .formatted(TITLE, RESET, SEP,
                        CMD, MUTED, CMD, MUTED, CMD, MUTED, CMD, MUTED,
                        BODY, GOLD, BODY, GOLD));

        pages.add("""
                %sEconomia%s
                %s
                %s/money %s— ver saldo
                %s/loja %s— loja GUI
                %s/sell %s— vender itens
                %s/ah %s— leilão P2P
                %s/trade %s— troca segura
                %s/bounty %s— recompensa
                %sKills dão %s$200+%s"""
                .formatted(TITLE, RESET, SEP,
                        CMD, MUTED, CMD, MUTED, CMD, MUTED,
                        CMD, MUTED, CMD, MUTED, CMD, MUTED,
                        BODY, GOLD, BODY));

        pages.add("""
                %sLoja & kits%s
                %s
                %s/loja %s— menu central
                %s  %s• loja do servidor
                %s  %s• vender itens
                %s  %s• leilão
                %s/kits %s— kits por rank
                %sVIP desbloqueia kits
                extras no painel."""
                .formatted(TITLE, RESET, SEP,
                        CMD, MUTED, MUTED, BODY, MUTED, BODY, MUTED, BODY,
                        CMD, MUTED, BODY));

        pages.add("""
                %sClãs%s
                %s
                %s/clan %s— painel GUI
                %sCriar, convidar,
                promover, guerra.
                %s/cc %s— chat do clã
                %s/clantag %s— cor da tag
                %sTag visível no chat
                em %s/prefs%s."""
                .formatted(TITLE, RESET, SEP,
                        CMD, MUTED, BODY,
                        CMD, MUTED, CMD, MUTED,
                        CMD, BODY));

        pages.add("""
                %sPerfil & chat%s
                %s
                %s/account %s— perfil
                %s/tag %s— escolher tag
                %s/medal %s— medalhas
                %s/prefixtype %s— estilo tag
                %s/skin %s— trocar skin
                %s/prefs %s— preferências
                %s/chat %s— ignore/list"""
                .formatted(TITLE, RESET, SEP,
                        CMD, MUTED, CMD, MUTED, CMD, MUTED,
                        CMD, MUTED, CMD, MUTED, CMD, MUTED, CMD, MUTED));

        pages.add("""
                %sRankings%s
                %s
                %s/leaderboard %s— menu
                %s/top %s— atalho
                %sCategorias:
                %skills, deaths, kdr,
                money, clans
                %sAtualiza em tempo real."""
                .formatted(TITLE, RESET, SEP,
                        CMD, MUTED, CMD, MUTED,
                        BODY, BODY, MUTED));

        pages.add("""
                %sDenúncias%s
                %s
                %s/report %s— denunciar
                %sMotivos: cheat, chat,
                assedio, bug, outro
                %sEx.: %s/report Nick cheat
                %sCooldown: %s5 min"""
                .formatted(TITLE, RESET, SEP,
                        CMD, MUTED, BODY, BODY, CMD, BODY, GOLD));

        pages.add("""
                %sDicas%s
                %s
                %s• %sFica no spawn para
                comprar na loja
                %s• %sUsa %s/rtp %scom cuidado
                %s• %sForma um clã cedo
                %s• %sVIP = kits e perks
                %s• %sLoja: %snloja.savagemc.net
                %s• %sPerdeste o livro?
                %s/livro %s(cooldown 8h)

                %sBoa sorte, guerreiro!"""
                .formatted(TITLE, RESET, SEP,
                        MUTED, BODY, MUTED, BODY, CMD, BODY,
                        MUTED, BODY, MUTED, BODY,
                        BODY, LINK, MUTED, BODY, CMD, MUTED,
                        TITLE));

        return pages;
    }
}
