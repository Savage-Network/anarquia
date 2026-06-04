package com.minecraft.core.bungee.bot.command.list;

import com.minecraft.core.Constant;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.account.context.objects.medal.Medal;
import com.minecraft.core.account.context.objects.punishment.PunishmentHistory;
import com.minecraft.core.api.punishment.Punishment;
import com.minecraft.core.api.punishment.objects.enums.PunishmentCategory;
import com.minecraft.core.bungee.bot.command.structure.SlashCommand;
import com.minecraft.core.bungee.bot.command.structure.category.SlashCommandCategory;
import com.minecraft.core.bungee.bot.command.structure.embed.SlashEmbed;
import com.minecraft.core.util.list.DateUtil;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.interaction.SlashCommandOptionBuilder;
import org.javacord.api.interaction.SlashCommandOptionType;

import java.awt.*;

public class AccountSlashCommand extends SlashCommand {

    private final String NAME_OPTION_KEY = "nome";

    public AccountSlashCommand() {
        super("conta", "Colete informações dos jogadores.", SlashCommandCategory.SERVER);

        getOptions().add(
                // Parâmetro 1: Nome da conta (Obrigatório)
                new SlashCommandOptionBuilder()
                        .setType(SlashCommandOptionType.STRING)
                        .setName(NAME_OPTION_KEY)
                        .setDescription("Específique a conta.")
                        .setRequired(true)
                        .build()
        );
    }

    @Override
    public void execute(SlashCommandInteraction interaction) {
        SlashCommandInteractionOption optionName = interaction.getArgumentByName(NAME_OPTION_KEY).orElse(null);

        if (optionName != null) {
            String name = optionName.getStringValue().orElse("...");
            Account account = Core.getAccountData().of(name);

            if (account == null) {
                interaction.createImmediateResponder()
                        .setContent("Não foi possível encontrar a conta \"" + name + "\"...")
                        .respond();
                return;
            }

            // Coletar informações da conta
            String rankName = account.getRank().getName();
            String tagName = account.getTag().getName();
            
            Medal medal = account.getMedal();
            String medalName = medal != null && medal != Medal.NONE ? medal.getName() : "Nenhuma!";
            
            String accountType = account.isPremium() ? "Original" : "Pirata";
            String onlineStatus = account.isOnline() ? "Online" : "Offline";
            
            String clanName = account.hasClan() ? account.getClanName() : "Nenhum";
            
            String skinLink = "[Clique aqui](https://namemc.com/profile/" + account.getName() + ")";
            
            String firstLogin = DateUtil.getDate(account.getContext().getCreatedAt());
            String lastLogin = DateUtil.getDate(account.getContext().getLastLogin());
            
            // Verificar punições
            PunishmentHistory history = account.getHistory();
            Punishment banPunishment = account.getActivePunishment(PunishmentCategory.BAN);
            Punishment mutePunishment = account.getActivePunishment(PunishmentCategory.MUTE);
            
            String isBanned = banPunishment != null ? "Sim" : "Não";
            String isMuted = mutePunishment != null ? "Sim" : "Não";

            // Construir descrição formatada
            StringBuilder description = new StringBuilder();
            description.append("**Menu:** ").append(account.getName()).append("\n\n");
            description.append("➠ **Rank:** ").append(rankName).append("\n");
            description.append("➠ **Tag de Perfil:** ").append(tagName).append("\n");
            description.append("➠ **Medalha:** ").append(medalName).append("\n");
            description.append("➠ **Conta:** ").append(accountType).append("\n");
            description.append("➠ **Online:** ").append(onlineStatus).append("\n");
            description.append("➠ **Clan:** ").append(clanName).append("\n");
            description.append("➠ **Skin:** ").append(skinLink).append("\n\n");
            description.append("➠ **Primeiro login:** ").append(firstLogin).append("\n");
            description.append("➠ **Último login:** ").append(lastLogin).append("\n\n");
            description.append("➠ **Banido:** ").append(isBanned).append("\n");
            description.append("➠ **Silenciado:** ").append(isMuted);

            EmbedBuilder embed = new SlashEmbed()
                    .setTitle("Informações da Conta")
                    .setThumbnail(String.format(Constant.MINOTAR_AVATAR_API_URL, account.getName(), 120))
                    .setDescription(description.toString())
                    .setColor(Color.decode("#1a7a1a"));

            interaction.createImmediateResponder()
                    .addEmbed(embed)
                    .respond();
        }
    }
}
