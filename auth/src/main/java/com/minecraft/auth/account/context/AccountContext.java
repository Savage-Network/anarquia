package com.minecraft.auth.account.context;

import com.minecraft.auth.Constant;
import com.minecraft.auth.account.context.objects.aura.ImageAuraMetadata;
import com.minecraft.auth.account.context.objects.auth.AuthMetadata;
import com.minecraft.auth.account.context.objects.block.BlockedPlayer;
import com.minecraft.auth.account.context.objects.clan.ClanMetadata;
import com.minecraft.auth.account.context.objects.cosmetic.CosmeticEntry;
import com.minecraft.auth.account.context.objects.fake.Fake;
import com.minecraft.auth.account.context.objects.friend.FriendMetadata;
import com.minecraft.auth.account.context.objects.joinmessage.JoinMessageMetadata;
import com.minecraft.auth.account.context.objects.medal.Medal;
import com.minecraft.auth.account.context.objects.permission.Permission;
import com.minecraft.auth.account.context.objects.punishment.PunishmentHistory;
import com.minecraft.auth.account.context.objects.rank.RankInfo;
import com.minecraft.auth.account.context.objects.restrict.Restrict;
import com.minecraft.auth.account.context.objects.route.RouteContext;
import com.minecraft.auth.account.context.objects.skin.SkinMetadata;
import com.minecraft.auth.account.context.objects.tag.Tag;
import com.minecraft.auth.account.context.objects.title.CustomTitle;
import com.minecraft.auth.account.context.objects.toggle.Toggle;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Espelho EXATO de {@code com.minecraft.core.account.context.AccountContext} do Savage.
 *
 * <p>Todos os campos da {@code Spec - §1.6} estao presentes para garantir
 * compatibilidade Gson com o JSON do Savage e permitir migracoes futuras.
 *
 * <p>Tipos cujos catalogos completos ainda nao foram portados (Tag,
 * Medal, cosmeticos) usam enums/POJOs minimos que serao expandidos
 * nas Fases 2-4 sem quebrar a serializacao.
 */
@Getter
@Setter
public class AccountContext {

    private RouteContext route = RouteContext.builder().build();

    private RankInfo rankInfo = new RankInfo();

    private Fake fake = new Fake();
    private SkinMetadata skin = new SkinMetadata();

    private FriendMetadata friend = new FriendMetadata();
    private Toggle toggle = new Toggle();

    private AuthMetadata auth = new AuthMetadata();
    private ClanMetadata clan = new ClanMetadata();
    private JoinMessageMetadata joinMessage = new JoinMessageMetadata();
    private ImageAuraMetadata imageAura = new ImageAuraMetadata();

    private PunishmentHistory history = new PunishmentHistory(false);

    private Tag tag = Tag.MEMBER;
    private Medal medal = Medal.NONE;

    private CustomTitle customTitle = null;

    /** Cor do {@code +} do rank EMBER+ (ex.: {@code §5}). */
    private String emberPlusColor = "§5";

    private UUID lastMessage = Constant.DEFAULT_ID;
    private String partyIdentifier = "...";

    private int level = 1;
    private int golds = 0;
    private int vipKeys = 0;

    private long lastLogin = System.currentTimeMillis();

    private String ipAddress = "127.0.0.1";
    private boolean premium = false;
    private boolean online = false;

    private final List<CosmeticEntry> collectibles = new ArrayList<>();
    private final List<String> activeCollectibles = new ArrayList<>();
    private final List<String> favoriteCollectibles = new ArrayList<>();

    /** Mantido por compatibilidade com JSON Savage (arcade); ignorado em semi-anarquia. */
    private final Map<String, List<Integer>> favoriteMaps = new ConcurrentHashMap<>();

    private final List<Permission> permissions = new ArrayList<>();
    private final List<Restrict> restricts = new ArrayList<>();

    private final List<Medal> medals = new ArrayList<>(Collections.singletonList(Medal.NONE));
    private final List<BlockedPlayer> blocks = new ArrayList<>();

    private final Map<String, Long> cooldown = new ConcurrentHashMap<>();

    /** Tempo em ms, -1 = nunca completou. */
    private long parkourBestTime = -1;

    private final long createdAt = System.currentTimeMillis();
}
