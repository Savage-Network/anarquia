package com.minecraft.arcade.skywars.user;

import com.minecraft.arcade.skywars.arcade.arena.Arena;
import com.minecraft.arcade.skywars.arcade.Arcade;
import com.minecraft.arcade.skywars.user.context.UserContext;
import com.minecraft.core.member.list.skywars.SkyMember;
import com.minecraft.core.arcade.route.join.Join;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.api.sidebar.Sidebar;
import com.minecraft.core.bukkit.user.UserModel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class User extends UserModel {

    private final SkyMember member;
    private UserContext context;

    private Arena arena;

    public User(SkyMember member, Arena arena, Join join) {
        super(member.getId(), join);

        this.member = member;
        this.arena = arena;

        this.context = new UserContext();
    }

    @Override
    public void handle() {
        Player player = getAccount().player();

        setSidebar(new Sidebar(player, "§b§lSKY WARS"));

        if (arena != null)
            arena.join(player);
    }

    public void reset() {
        setContext(new UserContext());
    }

    public Player getPlayer() {
        return getAccount().player();
    }

    public Arcade getArcade() {
        return arena != null ? (Arcade) arena.getArcade() : null;
    }

    public SkyMember getMember() {
        return member;
    }

    public int getKills() {
        return context.getKills();
    }

    public void setKills(int kills) {
        context.setKills(kills);
    }

    public int getXpGain() {
        return context.getXpGain();
    }

    public void setXpGain(int xpGain) {
        context.setXpGain(xpGain);
    }

    @Override
    public void setState(ArcadeState state) {
        super.setState(state);
    }
}

