package com.minecraft.hungergames;

import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.hungergames.game.kit.KitManager;
import com.minecraft.hungergames.game.kit.list.NoneKit;
import com.minecraft.hungergames.game.stage.GameStage;
import com.minecraft.hungergames.scoreboard.ScoreboardController;
import lombok.Getter;
import lombok.Setter;

public final class Hungergames extends BukkitCore {

    @Setter @Getter
    private static Hungergames instance;

    @Getter
    private KitManager kitManager;

    @Getter
    private ScoreboardController scoreboardController;

    @Getter @Setter
    private GameStage currentStage = GameStage.WAITING;

    @Getter @Setter
    private String boardTitle = "HUNGERGAMES";

    private long stageStartAt = -1L;
    private long stageEndAt = -1L;
    private long matchStartAt = -1L;

    @Getter @Setter
    private int startingCountdownSeconds = 300;

    @Getter @Setter
    private int invincibilitySeconds = 45; 

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;

        kitManager = new KitManager();
        kitManager.register(new NoneKit());

        scoreboardController = new ScoreboardController(this);
        scoreboardController.renderForAll(currentStage);

        getServer().getPluginManager().registerEvents(new com.minecraft.hungergames.listener.player.PlayerListener(), this);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (kitManager != null) kitManager.unregisterAll();
        if (scoreboardController != null) scoreboardController.cancelAll();
    }


    public void setStage(GameStage stage, long durationSeconds) {
        this.currentStage = stage;

        long now = System.currentTimeMillis();
        switch (stage) {
            case STARTING:
                stageStartAt = now;
                stageEndAt = now + 1000L * (durationSeconds > 0 ? durationSeconds : startingCountdownSeconds);
                break;
            case INVINCIBILITY:
                stageStartAt = now;
                stageEndAt = now + 1000L * (durationSeconds > 0 ? durationSeconds : invincibilitySeconds);
                if (matchStartAt <= 0) matchStartAt = -1L;
                break;
            case PLAYING:
                stageStartAt = now;
                stageEndAt = -1L;
                if (matchStartAt <= 0) matchStartAt = now;
                break;
            default:
                stageStartAt = now;
                stageEndAt = -1L;
                break;
        }

        if (scoreboardController != null) {
            scoreboardController.renderForAll(stage);
        }
    }


    public long getRemainingSeconds() {
        if (stageEndAt <= 0) {
            // Fallback para mostrar 5:00 no WAITING/STARTING quando a contagem ainda não iniciou
            if (currentStage == GameStage.WAITING || currentStage == GameStage.STARTING) {
                return startingCountdownSeconds;
            }
            return 0L;
        }
        long remaining = (stageEndAt - System.currentTimeMillis()) / 1000L;
        return Math.max(remaining, 0L);
    }

    public long getElapsedSeconds() {
        long start = matchStartAt > 0 ? matchStartAt : stageStartAt;
        if (start <= 0) return 0L;
        return (System.currentTimeMillis() - start) / 1000L;
    }
}
