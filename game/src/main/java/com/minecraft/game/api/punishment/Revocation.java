package com.minecraft.game.api.punishment;

import com.minecraft.game.Constant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Revocation {

    private UUID author = Constant.DEFAULT_ID;
    private String reason = "Não informado";
    private long data = -1L;

    public boolean isValid() {
        return data > -1L;
    }
}
