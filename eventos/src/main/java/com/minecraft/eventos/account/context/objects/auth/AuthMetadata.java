package com.minecraft.eventos.account.context.objects.auth;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AuthMetadata {

    private AuthState state = AuthState.REGISTER;
    private String password = "";
    private String lastPassword = "";
    private List<String> recentPasswords = new ArrayList<>();
    private long timestamp;
}
