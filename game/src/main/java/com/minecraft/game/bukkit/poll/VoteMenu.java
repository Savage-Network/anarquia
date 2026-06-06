package com.minecraft.game.bukkit.poll;

import com.minecraft.game.bukkit.menu.Menu;
import com.minecraft.game.bukkit.menu.MenuItem;
import com.minecraft.game.message.MenuTexts;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/** GUI de bau para votar numa enquete ativa. */
public final class VoteMenu extends Menu {

    private static final Material[] OPTION_MATERIALS = {
            Material.LIME_CONCRETE, Material.RED_CONCRETE,
            Material.BLUE_CONCRETE, Material.YELLOW_CONCRETE
    };

    private static final int[] OPTION_SLOTS_2 = {12, 14};
    private static final int[] OPTION_SLOTS_3 = {11, 13, 15};
    private static final int[] OPTION_SLOTS_4 = {10, 12, 14, 16};

    private final VoteSession session;

    public VoteMenu(Player player, VoteSession session) {
        super(player, MenuTexts.legacy(player, "poll.vote.menu-title", Map.of(
                "seconds", String.valueOf(session.secondsLeft()))), 3, null);
        this.session = session;
    }

    @Override
    public void build() {
        clear();

        set(4, MenuItem.of(Material.PAPER)
                .name(MenuTexts.legacy(player, "poll.vote.info-name"))
                .lore(
                        MenuTexts.legacy(player, "poll.vote.info-creator", Map.of(
                                "creator", session.getCreatorName())),
                        MenuTexts.legacy(player, "poll.vote.info-time", Map.of(
                                "seconds", String.valueOf(session.secondsLeft()))),
                        "",
                        MenuTexts.legacy(player, "poll.vote.info-hint")
                ));

        int[] slots = slotsFor(session.getOptions().size());
        List<String> options = session.getOptions();
        for (int i = 0; i < options.size() && i < slots.length; i++) {
            int index = i;
            Material mat = OPTION_MATERIALS[i % OPTION_MATERIALS.length];
            Integer chosen = session.choiceOf(player.getUniqueId());
            boolean selected = chosen != null && chosen == index;

            MenuItem item = MenuItem.of(mat)
                    .name(MenuTexts.legacy(player, "poll.vote.option-name", Map.of(
                            "option", options.get(index))))
                    .lore(
                            MenuTexts.legacy(player, "poll.vote.option-votes", Map.of(
                                    "votes", String.valueOf(session.count(index)))),
                            selected
                                    ? MenuTexts.legacy(player, "poll.vote.option-selected")
                                    : MenuTexts.legacy(player, "poll.vote.option-click")
                    );
            if (selected) item.glow();

            item.click(p -> {
                if (VoteService.vote(p, index)) {
                    clickSound();
                    successSound();
                    p.sendMessage(com.minecraft.game.message.Messages.prefixed(
                            "poll.vote.voted", com.minecraft.game.message.LanguageService.of(p),
                            Map.of("option", options.get(index))));
                    new VoteMenu(p, session).open();
                } else {
                    p.sendMessage(com.minecraft.game.message.Messages.prefixed(
                            "poll.vote.failed", com.minecraft.game.message.LanguageService.of(p)));
                }
            });

            set(slots[i], item);
        }

        closeButton(22);
    }

    @Override
    protected boolean bordered() {
        return true;
    }

    private static int[] slotsFor(int count) {
        if (count <= 2) return OPTION_SLOTS_2;
        if (count == 3) return OPTION_SLOTS_3;
        return OPTION_SLOTS_4;
    }
}
