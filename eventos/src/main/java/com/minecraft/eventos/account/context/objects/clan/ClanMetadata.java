package com.minecraft.eventos.account.context.objects.clan;

import com.minecraft.eventos.Constant;
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

    /** Id do estilo de cor ({@code padrao}, {@code brasil}, ...). */
    private String tagColorId = "padrao";
}
