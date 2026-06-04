package com.minecraft.eventos;

/** Valores fixos do servidor de eventos. */
public final class EventosSettings {

    private EventosSettings() {}

    public static final String POST_END_TARGET = "lobby";

    public static final boolean RULE_DAMAGE = true;
    public static final boolean RULE_FOOD = false;
    public static final boolean RULE_DROP_ITEMS = false;
    public static final boolean RULE_BLOCK_PLACE = false;
    public static final boolean RULE_BLOCK_BREAK = false;
    public static final boolean RULE_BUCKETS = false;

    public static final double NAMETAG_BASE_Y = 2.05;
    public static final double NAMETAG_LINE_SPACING = 0.28;
    public static final float NAMETAG_TEXT_SCALE = 1.05f;
    public static final int NAMETAG_BG_ALPHA = 72;
    public static final long NAMETAG_FOLLOW_TICKS = 2L;
}
