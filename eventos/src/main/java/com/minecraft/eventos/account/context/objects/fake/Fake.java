package com.minecraft.eventos.account.context.objects.fake;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Fake {

    private String nick = "";
    private String lastNick = "";
    private long updatedAt;

    public boolean isValid() {
        return nick != null && !nick.isEmpty();
    }
}
