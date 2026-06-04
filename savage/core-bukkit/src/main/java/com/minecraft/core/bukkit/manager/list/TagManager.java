package com.minecraft.core.bukkit.manager.list;

import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.tag.Tag;
import com.minecraft.core.account.context.objects.tag.prefix.TagPrefix;
import com.minecraft.core.bukkit.user.UserModel;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TagManager {

    private static final Map<UUID, Integer> animationState = new HashMap<>();

    public static List<Tag> getAvailableTags(Account account) {
        return Arrays.stream(Tag.values())
                .filter(account::hasTag)
                .collect(Collectors.toList());
    }

    protected static TextComponent create(Account account, Tag tag, boolean end) {
        TextComponent message = new TextComponent(tag.getColoredName());

        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                "§7Prévia:"
                        + "\n§8> " + tag.getByPrefix(account.getTagPrefix()) + account.getNickname()
                        + "\n\n" + (account.isUsingTag(tag) ? "§cJá selecionada." : "§eClique para escolher.")
        )));

        message.addExtra("§r" + (end ? "." : ", "));

        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tag " + tag.getName().toLowerCase()));

        return message;
    }

    public static void sendTags(Account account) {
        List<Tag> tags = getAvailableTags(account);

        TextComponent message = new TextComponent("§aSuas tags: ");

        boolean end = false;

        int index = 1;
        for (Tag tag : tags) {
            if (index >= tags.size())
                end = true;

            message.addExtra(create(account, tag, end));
            index++;
        }

        message.addExtra(new TextComponent("§r"));

        account.send(message);
    }

    public static void updateTag(Account account) {
        updateTag(account, UserModel.has(account.getId()) ? UserModel.of(account.getId()).getTag() : account.getTag());
    }

    public static void updateTag(Player player) {
        Account account = Core.getAccountController().of(player.getUniqueId());

        if (account != null)
            updateTag(account);
    }

    public static void updateTag(Account account, Tag tag) {
        Player player = account.player();

        if (player == null) return; // Jogador não está online

        if (tag == null) tag = Tag.MEMBER;

        String order = "tag:" + tag.getOrder() + ":" + player.getEntityId();

        String prefix = getAdjustedPrefix(account.getTagPrefix(), tag, player.getUniqueId());
        
        // No BedWars, usar cor do time ao invés da cor do rank
        if (account.getServerType() == com.minecraft.core.server.type.ServerType.BEDWARS) {
            String teamColor = getTeamColor(player);
            if (teamColor != null) {
                // Substituir completamente o prefix pela cor do time
                prefix = teamColor;
            }
        }
        
        String suffix = !account.isUsingFake() ? (account.hasClan() && account.getToggle().isAllowClanTag() ? account.getClanTag() : "") : "";
        suffix = truncateSuffix(suffix);

        String displayName = prefix + player.getName() + suffix;
        player.setDisplayName(displayName);
        player.setPlayerListName(displayName);

        // Se o servidor tem NAME_TAG_CHANGE flag (arcade servers), não manipular scoreboard
        // Apenas atualizar display name e player list name
        if (account.getServerType().hasFlag(com.minecraft.core.server.flag.ServerFlag.NAME_TAG_CHANGE)) {
            // Para arcade servers, apenas atualizar tags no scoreboard existente sem substituí-lo
            Scoreboard currentScoreboard = player.getScoreboard();
            
            // Limpar equipes antigas do jogador no scoreboard atual
            for (Team old : currentScoreboard.getTeams()) {
                if (old.getName().startsWith("tag:") && old.hasEntry(player.getName())) {
                    old.unregister();
                }
            }
            
            // Criar team no scoreboard existente
            Team team = currentScoreboard.getTeam(order);
            if (team == null) {
                team = currentScoreboard.registerNewTeam(order);
            }
            team.setCanSeeFriendlyInvisibles(false);
            team.setPrefix(prefix != null ? prefix : "");
            team.setSuffix(truncateSuffix(suffix));
            if (!team.hasEntry(player.getName()))
                team.addEntry(player.getName());
            
            // Atualizar visualização para outros jogadores
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.equals(player)) continue;
                applyTagForViewer(player, onlinePlayer, tag);
                Tag onlineTag = UserModel.of(onlinePlayer.getUniqueId()).getTag();
                applyTagForViewer(onlinePlayer, player, onlineTag);
            }
            
            return;
        }

        // Para servidores sem NAME_TAG_CHANGE flag (lobby, etc), usar comportamento normal
        // Limpar equipes antigas do jogador
        for (Team old : player.getScoreboard().getTeams()) {
            if (old.getName().startsWith("tag:") && old.hasEntry(player.getName())) {
                old.unregister();
            }
        }

        Team team = createTeamIfNotExists(order, player, player.getName(), prefix, suffix);
        Scoreboard scoreboard = team.getScoreboard();

        player.setScoreboard(scoreboard);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) continue;

            // Exibe a tag do jogador `player` para `onlinePlayer`
            applyTagForViewer(player, onlinePlayer, tag);

            // Exibe a tag do `onlinePlayer` para o `player`
            Tag onlineTag = UserModel.of(onlinePlayer.getUniqueId()).getTag();

            applyTagForViewer(onlinePlayer, player, onlineTag);
        }
    }

    private static void applyTagForViewer(Player player, Player viewer, Tag tag) {
        UserModel viewerModel = UserModel.of(viewer.getUniqueId());
        if (viewerModel == null) return;

        Account viewerAccount = viewerModel.getAccount();

        // Definindo o prefixo do viewer com base no TagPrefix do visualizador
        TagPrefix viewerPrefixType = viewerAccount.getTagPrefix();
        String viewerPrefix = getAdjustedPrefix(viewerPrefixType, tag, player.getUniqueId());
        
        // Adicionar letra do time antes da tag no BedWars
        if (viewerAccount.getServerType() == com.minecraft.core.server.type.ServerType.BEDWARS) {
            String teamPrefix = getTeamPrefix(player);
            if (teamPrefix != null) {
                viewerPrefix = teamPrefix + viewerPrefix;
            }
        }

        // Verifica a própria tag de clã do viewer
        String viewerClanTag = !viewerAccount.isUsingFake() ? (viewerAccount.hasClan() && viewerAccount.getToggle().isAllowClanTag()
                ? viewerAccount.getClanTag()
                : "") : "";
        viewerClanTag = truncateSuffix(viewerClanTag);

        // Verifica a tag de clã do player
        Account playerAccount = Core.getAccountController().of(player.getUniqueId());
        String playerClanTag = playerAccount != null && !playerAccount.isUsingFake() ? (playerAccount.hasClan() && playerAccount.getToggle().isAllowClanTag()
                ? playerAccount.getClanTag()
                : "") : "";
        playerClanTag = truncateSuffix(playerClanTag);

        // Gerar ordem para o time específico do visualizador
        String viewerOrder = "tag:" + tag.getOrder() + ":" + player.getEntityId();

        // Removendo times antigos para o visualizador
        for (Team old : viewer.getScoreboard().getTeams()) {
            if (old.getName().startsWith("tag:") && old.hasEntry(player.getName())) {
                old.unregister();
            }
        }

        // Criando ou obtendo a equipe com a tag ajustada para o visualizador, com a tag de clã do player
        Team viewerTeam = createTeamIfNotExists(viewerOrder, viewer, player.getName(), viewerPrefix, playerClanTag);

        // Adicionando o jogador (player) à equipe com a visualização do viewer
        if (!viewerTeam.hasEntry(player.getName())) {
            viewerTeam.addEntry(player.getName());
        }

        // Garantir que o prefixo está atualizado (importante para o tab list)
        viewerTeam.setPrefix(viewerPrefix != null ? viewerPrefix : "");
        viewerTeam.setSuffix(truncateSuffix(playerClanTag));

        // Adicionando a própria tag de clã do viewer
        viewer.setDisplayName(viewerPrefix + viewer.getName() + viewerClanTag);
    }

    private static Team createTeamIfNotExists(String order, Player player, String entry, String prefix, String suffix) {
        Scoreboard scoreboard = player.getScoreboard();

        if (scoreboard == null || scoreboard.equals(Bukkit.getScoreboardManager().getMainScoreboard()))
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        Team team = scoreboard.getTeam(order);
        if (team == null) {
            team = scoreboard.registerNewTeam(order);
        }

        team.setCanSeeFriendlyInvisibles(false);
        // Sempre atualiza o prefixo e sufixo, mesmo se o Team já existir
        team.setPrefix(prefix != null ? prefix : "");
        team.setSuffix(truncateSuffix(suffix));

        if (!team.hasEntry(entry))
            team.addEntry(entry);

        return team;
    }

    public static String getAdjustedPrefix(TagPrefix prefixType, Tag tag) {
        return getAdjustedPrefix(prefixType, tag, null);
    }

    public static String getAdjustedPrefix(TagPrefix prefixType, Tag tag, UUID animationBearerId) {
        // Para tags animadas, ignorar o prefixType e usar o sistema de animação
        if (tag.isAnimated()) {
            List<String> frames = tag.getAnimationFrames();
            UUID playerId = animationBearerId;
            if (playerId != null && !frames.isEmpty()) {
                int state = animationState.getOrDefault(playerId, 0);
                return frames.get(state % frames.size());
            }

            if (playerId != null) {
                int state = animationState.getOrDefault(playerId, 0);

                // Se tem 3 cores (thirdPrefix não é null), ciclar entre 0, 1, 2
                if (tag.getThirdPrefix() != null) {
                    switch (state) {
                        case 0:
                            return tag.getPrefix(); // §1§lBETA §1 (dark blue)
                        case 1:
                            return tag.getAlternatePrefix(); // §9§lBETA §9 (blue)
                        case 2:
                            return tag.getThirdPrefix(); // §b§lBETA §b (aqua)
                        default:
                            return tag.getPrefix();
                    }
                } else if (tag.getAlternatePrefix() != null) {
                    return state == 1 ? tag.getAlternatePrefix() : tag.getPrefix();
                }
            }
            return tag.getPrefix();
        }

        return tag.getByPrefix(prefixType);
    }
    
    public static void startAnimationTask(org.bukkit.plugin.Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Account account = Core.getAccountController().of(player.getUniqueId());
                if (account != null && account.getTag() != null && account.getTag().isAnimated()) {
                    UUID playerId = player.getUniqueId();
                    int currentState = animationState.getOrDefault(playerId, 0);
                    
                    // Quadros explícitos (ex.: TAG_OWNER), três cores (BETA), ou duas cores
                    int nextState;
                    List<String> frames = account.getTag().getAnimationFrames();
                    if (!frames.isEmpty()) {
                        nextState = (currentState + 1) % frames.size();
                    } else if (account.getTag().getThirdPrefix() != null) {
                        nextState = (currentState + 1) % 3;
                    } else {
                        nextState = (currentState + 1) % 2;
                    }
                    
                    animationState.put(playerId, nextState);
                    updateTag(account);
                }
            }
        }, 40L, 40L); // 40 ticks = 2 segundos
    }

    private static String truncateSuffix(String suffix) {
        if (suffix == null || suffix.isEmpty()) return suffix;

        // O protocolo do Minecraft limita suffix a 16 caracteres totais (incluindo códigos de cor)
        if (suffix.length() <= 16) {
            return suffix;
        }

        // Truncar para 16 caracteres, tentando manter códigos de cor completos
        StringBuilder result = new StringBuilder();
        int totalChars = 0;
        int i = 0;

        while (i < suffix.length() && totalChars < 16) {
            char c = suffix.charAt(i);

            if (c == '§' && i + 1 < suffix.length()) {
                // É um código de cor, verificar se cabe
                if (totalChars + 2 <= 16) {
                    result.append(c);
                    result.append(suffix.charAt(i + 1));
                    totalChars += 2;
                    i += 2; // Pular o próximo caractere
                } else {
                    // Não cabe mais, parar
                    break;
                }
            } else {
                // É um caractere normal
                if (totalChars < 16) {
                    result.append(c);
                    totalChars++;
                    i++;
                } else {
                    // Limite atingido, parar
                    break;
                }
            }
        }

        return result.toString();
    }
    
    private static String getTeamPrefix(Player player) {
        try {
            // Usar reflexão para evitar dependência direta do módulo bedwars
            Class<?> userClass = Class.forName("com.minecraft.core.bukkit.user.User");
            java.lang.reflect.Method ofMethod = userClass.getMethod("of", java.util.UUID.class);
            Object user = ofMethod.invoke(null, player.getUniqueId());
            
            if (user != null) {
                // Verificar se tem método getTeam (BedWars User)
                try {
                    java.lang.reflect.Method getTeamMethod = user.getClass().getMethod("getTeam");
                    Object team = getTeamMethod.invoke(user);
                    
                    if (team != null) {
                        // Pegar o nome completo do time (Vermelho, Azul, etc)
                        java.lang.reflect.Method getNameMethod = team.getClass().getMethod("getName");
                        String teamName = (String) getNameMethod.invoke(team);
                        
                        // Pegar a cor do time
                        java.lang.reflect.Method getColorMethod = team.getClass().getMethod("getColor");
                        String teamColor = (String) getColorMethod.invoke(team);
                        
                        if (teamName != null && !teamName.isEmpty()) {
                            String color = teamColor != null ? teamColor : "§7";
                            // Retornar apenas a primeira letra do time com a cor
                            char firstLetter = teamName.toUpperCase().charAt(0);
                            return color + "§l" + firstLetter + " §r";
                        }
                    }
                } catch (NoSuchMethodException e) {
                    // Não é um User do BedWars, ignorar
                }
            }
        } catch (Exception e) {
            // Ignorar erros silenciosamente
        }
        
        return null;
    }
    
    private static String getTeamColor(Player player) {
        try {
            // Usar reflexão para evitar dependência direta do módulo bedwars
            Class<?> userClass = Class.forName("com.minecraft.core.bukkit.user.User");
            java.lang.reflect.Method ofMethod = userClass.getMethod("of", java.util.UUID.class);
            Object user = ofMethod.invoke(null, player.getUniqueId());
            
            if (user != null) {
                // Verificar se tem método getTeam (BedWars User)
                try {
                    java.lang.reflect.Method getTeamMethod = user.getClass().getMethod("getTeam");
                    Object team = getTeamMethod.invoke(user);
                    
                    if (team != null) {
                        // Pegar a cor do time
                        java.lang.reflect.Method getColorMethod = team.getClass().getMethod("getColor");
                        String teamColor = (String) getColorMethod.invoke(team);
                        
                        return teamColor != null ? teamColor : null;
                    }
                } catch (NoSuchMethodException e) {
                    // Não é um User do BedWars, ignorar
                }
            }
        } catch (Exception e) {
            // Ignorar erros silenciosamente
        }
        
        return null;
    }

    public static void removeTag(Player player) {
        player.setDisplayName(player.getName());
        player.setPlayerListName(player.getName());

        player.getScoreboard().getTeams().stream()
                .filter(team -> team.getName().startsWith("tag:"))
                .forEach(Team::unregister);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player) || onlinePlayer.getScoreboard().equals(Bukkit.getScoreboardManager().getMainScoreboard()))
                continue;

            onlinePlayer.getScoreboard().getTeams().forEach(team -> removeEntries(team, player.getName()));
        }
    }

    private static void removeEntries(Team team, String entry) {
        if (team.getName().startsWith("tag:")) {
            team.removeEntry(entry);

            if (team.getEntries().isEmpty())
                team.unregister();
        }
    }
}