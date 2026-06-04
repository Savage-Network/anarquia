package com.minecraft.lobbyhost.account.context.objects.clan;

import com.minecraft.lobbyhost.Constant;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ClanMetadata {

    private UUID id = Constant.DEFAULT_ID;
    private long lastUpdate;

    /** Tag textual do cla (ex.: {@code "SVG"}). Vazio = sem cla. */
    private String tag = "";
}
