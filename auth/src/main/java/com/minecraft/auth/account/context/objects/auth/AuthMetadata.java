package com.minecraft.auth.account.context.objects.auth;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadados leves no JSON de {@code accounts} (estado da sessao / roteamento no proxy).
 * Senhas ficam somente na tabela {@code player_auth}.
 */
@Getter
@Setter
public class AuthMetadata {

    private AuthState state = AuthState.REGISTER;

    /** Legado Gson — nao persistir senha aqui (sanitizado no auth). */
    private String password = "";
    private String lastPassword = "";
    private List<String> recentPasswords = new ArrayList<>();

    private long timestamp;
}
