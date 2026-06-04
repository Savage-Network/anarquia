package com.minecraft.proxyhost.account.context.objects.tag;

/**
 * Espelho de {@code com.minecraft.core.account.tag.TagRole} do Savage.
 *
 * <p>Define a regra de visibilidade aplicada por {@code Account.hasTag(Tag)}:
 *
 * <ul>
 *   <li>{@link #NONE} - liberada sem rank (default {@code MEMBER}).</li>
 *   <li>{@link #COLORED} - tags de cor "puras"; usuarios com rank nao usam.</li>
 *   <li>{@link #VIP} - liberada se {@code source} bate com rank do jogador, ou staff alto.</li>
 *   <li>{@link #SPECIAL} - liberada por permissao ({@code tag.<nome>}) ou rank source.</li>
 *   <li>{@link #STAFF} - rank exato, rank superior (Admin+), ou Vermelho para tag Vermelho.</li>
 *   <li>{@link #ONLY_THESE} - lista de UUIDs definida em {@link Tag#getOnlyThese()}.</li>
 * </ul>
 */
public enum TagRole {
    NONE,
    COLORED,
    VIP,
    SPECIAL,
    STAFF,
    ONLY_THESE
}
