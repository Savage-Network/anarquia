package com.minecraft.arcade.bedwars.listener;

import com.minecraft.arcade.bedwars.BedWars;
import com.minecraft.arcade.bedwars.arcade.arena.Arena;
import com.minecraft.arcade.bedwars.menu.shop.ShopMenu;
import com.minecraft.arcade.bedwars.structure.egg.EggBridgeManager;
import com.minecraft.arcade.bedwars.structure.egg.EggBridgeTask;
import com.minecraft.arcade.bedwars.structure.team.Team;
import com.minecraft.arcade.bedwars.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.arcade.room.Room;
import com.minecraft.core.arcade.room.map.area.Cuboid;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.type.Type;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.cooldown.Cooldown;
import com.minecraft.core.bukkit.api.hologram.type.client.HologramClient;
import com.minecraft.core.bukkit.api.protocol.ProtocolHandler;
import com.minecraft.core.bukkit.event.type.player.PlayerDamageTargetEvent;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import com.minecraft.core.bukkit.manager.list.CooldownManager;
import com.minecraft.core.bukkit.manager.list.DropValidator;
import com.minecraft.core.member.list.bedwars.objects.enums.BedShop;
import com.minecraft.core.util.list.StringUtil;
import com.minecraft.core.util.list.TimeUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.minecraft.core.util.list.bukkit.BukkitUtil.consumeItem;
import static com.minecraft.core.util.list.bukkit.BukkitUtil.removeItemInHand;

public class ItemListener implements Listener {

    private final BedWars plugin;

    private final String SPAWNED_BY_KEY = "spawnedBy";
    private final String TEAM_KEY = "teamOwner"; // Nova key para armazenar o time

    private final List<EntityDamageEvent.DamageCause> causes;

    private final Set<Material> BLOCKS_PERMITTED_TO_EXPLODE = Stream
            .of(Material.ENDER_STONE, Material.WOOD, Material.WOOL, Material.STAINED_CLAY)
            .collect(Collectors.toSet());

    public ItemListener() {
        this.plugin = BedWars.getPlugin(BedWars.class);

        this.causes = Arrays.asList(
                EntityDamageEvent.DamageCause.BLOCK_EXPLOSION,
                EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
        );
    }

    @EventHandler
    public void onUtilityItems(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Arena arena = user.getArena();

        Team team = user.getTeam();

        ItemStack hand = player.getItemInHand();

        if (arena.isPhase(RoomPhase.PLAYING) && hand != null && !hand.getType().equals(Material.AIR) && event.getAction().name().contains("RIGHT")) {

            switch (hand.getType()) {
                case COMPASS: {
                    player.performCommand("bussola");
                    break;
                }

                case BOOK: {
                    event.setCancelled(true);
                    if (user.getContext().isPurchasedPortableShop()) {
                        ShopMenu shopMenu = new ShopMenu(player, BedShop.FAVORITE);
                        shopMenu.handle();
                        shopMenu.display();
                    } else {
                        player.sendMessage("§cVocê precisa comprar a Loja Portátil na loja primeiro!");
                    }
                    break;
                }

                case MAGMA_CREAM: {
                    String SLINGSHOT_COOLDOWN = "Estilingue";
                    if (hasCooldown(player, SLINGSHOT_COOLDOWN)) return;

                    consumeItem(player);

                    // Calcular vetor de impulso baseado na direção do jogador
                    Vector direction = player.getEyeLocation().getDirection();
                    
                    // Impulso forte horizontal mas baixo vertical
                    double horizontalMultiplier = 2.5;
                    double verticalBoost = 0.6;
                    
                    Vector vector = direction.multiply(horizontalMultiplier);
                    vector.setY(Math.max(vector.getY(), 0.2) + verticalBoost);

                    // Resetar fall distance para evitar dano de queda
                    player.setFallDistance(-5.0F);
                    player.setVelocity(vector);

                    // Efeitos sonoros e visuais melhorados
                    Location loc = player.getLocation();
                    
                    // Sons de lançamento mais impactantes
                    player.playSound(loc, Sound.ENDERDRAGON_WINGS, 1.5f, 1.2f);
                    player.playSound(loc, Sound.NOTE_PLING, 1.0f, 2.0f);
                    player.playSound(loc, Sound.FIREWORK_BLAST, 0.8f, 1.5f);
                    
                    // Efeito de explosão no lançamento
                    ParticleBuilder explosionParticle = new ParticleBuilder(ParticleEffect.EXPLOSION_LARGE, loc);
                    explosionParticle.setSpeed(0.1f)
                            .setAmount(3)
                            .display();
                    
                    // Efeito de partículas de fogo no local de lançamento
                    ParticleBuilder launchParticle = new ParticleBuilder(ParticleEffect.FLAME, loc);
                    launchParticle.setSpeed(0.15f)
                            .setAmount(25)
                            .setOffset(0.5f, 0.3f, 0.5f)
                            .display();
                    
                    // Efeito de fumaça
                    ParticleBuilder smokeParticle = new ParticleBuilder(ParticleEffect.CLOUD, loc);
                    smokeParticle.setSpeed(0.08f)
                            .setAmount(15)
                            .setOffset(0.3f, 0.2f, 0.3f)
                            .display();
                    
                    // Efeito de redstone (vermelho) para mais impacto visual
                    ParticleBuilder redstoneParticle = new ParticleBuilder(ParticleEffect.REDSTONE, loc);
                    redstoneParticle.setSpeed(0.1f)
                            .setAmount(20)
                            .setOffset(0.4f, 0.3f, 0.4f)
                            .display();

                    // Trail de partículas durante o voo - mais intenso
                    new BukkitRunnable() {
                        int ticks = 0;
                        
                        @Override
                        public void run() {
                            if (!player.isOnline() || player.isOnGround() || ticks >= 30) {
                                cancel();
                                return;
                            }
                            
                            Location playerLoc = player.getLocation();
                            
                            // Partículas de fogo atrás do jogador
                            ParticleBuilder trailParticle = new ParticleBuilder(ParticleEffect.FLAME, playerLoc);
                            trailParticle.setSpeed(0.03f)
                                    .setAmount(5)
                                    .setOffset(0.3f, 0.3f, 0.3f)
                                    .display();
                            
                            // Partículas de fumaça no trail
                            if (ticks % 2 == 0) {
                                ParticleBuilder smokeTrail = new ParticleBuilder(ParticleEffect.CLOUD, playerLoc);
                                smokeTrail.setSpeed(0.02f)
                                        .setAmount(3)
                                        .setOffset(0.2f, 0.2f, 0.2f)
                                        .display();
                            }
                            
                            // Som de voo a cada 10 ticks
                            if (ticks % 10 == 0) {
                                player.playSound(playerLoc, Sound.FIREWORK_TWINKLE, 0.5f, 2.0f);
                            }
                            
                            ticks++;
                        }
                    }.runTaskTimer(plugin, 1L, 1L);
                    
                    // Proteção contra dano de queda por 4 segundos
                    user.getContext().setSlingshotProtectionTime(System.currentTimeMillis() + 4000L);
                    user.getContext().setSlingshotLanded(false);

                    setCooldown(player, SLINGSHOT_COOLDOWN, 5);
                    break;
                }

                case FIREBALL: {
                    if (hasCooldown(player, "Fireball")) return;

                    consumeItem(player);

                    Fireball fireball = player.launchProjectile(Fireball.class);

                    fireball.getWorld().playSound(fireball.getLocation(), Sound.GHAST_FIREBALL, 1, 1);

                    fireball.setYield(0);
                    fireball.setFireTicks(0);
                    fireball.setIsIncendiary(false);

                    setCooldown(player, "Fireball", 1);
                    break;
                }

                case SNOW_BALL: {
                    String WOLF_COOLDOWN_KEY = "Lobos";

                    if (hasCooldown(player, WOLF_COOLDOWN_KEY)) {
                        event.setCancelled(true);
                        return;
                    }

                    if (team == null) {
                        event.setCancelled(true);
                        return;
                    }

                    Snowball snowball = player.launchProjectile(Snowball.class);

                    snowball.setVelocity(snowball.getVelocity().multiply(1.5));

                    // Armazenar o TEAM ao invés da lista de players
                    snowball.setMetadata(TEAM_KEY, new FixedMetadataValue(plugin, team));

                    setCooldown(player, WOLF_COOLDOWN_KEY, 15);
                    break;
                }

                case MONSTER_EGG: {
                    event.setCancelled(true);
                    
                    String GOLEM_COOLDOWN_KEY = "Golem";

                    if (hasCooldown(player, GOLEM_COOLDOWN_KEY)) return;

                    if (team == null) return;

                    // Remover apenas 1 item (o ovo de monstro) ao invés de todos
                    if (hand != null && hand.getAmount() > 1) {
                        hand.setAmount(hand.getAmount() - 1);
                    } else {
                        player.setItemInHand(null);
                    }
                    player.updateInventory();

                    // Spawnar golem
                    Location spawnLocation = player.getLocation().clone();
                    
                    IronGolem golem = (IronGolem) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.IRON_GOLEM);
                    golem.setCustomName("§cDefensor (" + TimeUtil.time(60 * 4) + ")");
                    golem.setCustomNameVisible(true);

                    // Armazenar o TEAM ao invés da lista de players
                    golem.setMetadata(TEAM_KEY, new FixedMetadataValue(plugin, team));

                    // Task para procurar alvos continuamente
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!golem.isValid() || golem.isDead()) {
                                cancel();
                                return;
                            }
                            setEntityTarget(golem);
                        }
                    }.runTaskTimer(plugin, 1L, 20L);
                    
                    player.sendMessage("§aGolem de Ferro invocado!");

                    new BukkitRunnable() {
                        int time = 60 * 4;

                        @Override
                        public void run() {
                            time--;

                            if (time <= 0 && golem.isValid()) {
                                golem.remove();

                                cancel();
                                return;
                            }

                            golem.setCustomName("§cDefensor (" + TimeUtil.time(time) + ")");
                        }
                    }.runTaskTimer(plugin, 0, 20);

                    setCooldown(player, GOLEM_COOLDOWN_KEY, 120);
                    break;
                }
                
                case SKULL_ITEM: {
                    if (hand.hasItemMeta() && hand.getItemMeta().hasDisplayName() 
                        && hand.getItemMeta().getDisplayName().contains("Torre Compacta")) {
                        
                        event.setCancelled(true);
                        
                        String TOWER_COOLDOWN_KEY = "Castelinho";
                        if (hasCooldown(player, TOWER_COOLDOWN_KEY)) {
                            return;
                        }
                        
                        Block clickedBlock = event.getClickedBlock();
                        if (clickedBlock == null || clickedBlock.getType() == Material.AIR) {
                            player.sendMessage("§cVocê precisa clicar em um bloco!");
                            return;
                        }
                        
                        BlockFace face = event.getBlockFace();
                        if (face != BlockFace.UP) {
                            player.sendMessage("§cVocê precisa clicar na parte de cima de um bloco!");
                            return;
                        }
                        
                        Block centerBlock = clickedBlock.getRelative(BlockFace.UP);
                        
                        // Verificar se há espaço
                        if (centerBlock.getType() != Material.AIR) {
                            player.sendMessage("§cNão há espaço suficiente para construir a torre!");
                            return;
                        }
                        
                        // Consumir item
                        consumeItem(player);
                        
                        // Determinar direção baseada no yaw do jogador
                        float yaw = player.getLocation().getYaw();
                        BlockFace direction;
                        
                        if (yaw >= -45 && yaw < 45) {
                            direction = BlockFace.SOUTH;
                        } else if (yaw >= 45 && yaw < 135) {
                            direction = BlockFace.WEST;
                        } else if (yaw >= 135 || yaw < -135) {
                            direction = BlockFace.NORTH;
                        } else {
                            direction = BlockFace.EAST;
                        }
                        
                        // Construir torre
                        byte woolData = getWoolData(team);
                        new com.minecraft.arcade.bedwars.structure.tower.PopUpTower(
                            centerBlock, 
                            arena, 
                            woolData, 
                            direction
                        );
                        
                        player.sendMessage("§aTorre compacta construída!");
                        
                        // Adicionar cooldown de 10 segundos
                        setCooldown(player, TOWER_COOLDOWN_KEY, 10);
                    }
                    break;
                }
            }
        }
    }
    
    private byte getWoolData(Team team) {
        // Converter TeamPresetType para wool data (byte)
        String teamType = team.getTypeName();
        switch (teamType) {
            case "RED": return 14;
            case "BLUE": return 11;
            case "GREEN": return 13;
            case "YEL": return 4;
            case "ORAN": return 1;
            case "PURP": return 10;
            case "CYAN": return 9;
            case "WHIT": return 0;
            case "GRAY": return 7;
            default: return 0;
        }
    }
    
    private org.bukkit.DyeColor getDyeColor(Team team) {
        // Converter TeamPresetType para DyeColor
        String teamType = team.getTypeName();
        switch (teamType) {
            case "RED": return org.bukkit.DyeColor.RED;
            case "BLUE": return org.bukkit.DyeColor.BLUE;
            case "GREEN": return org.bukkit.DyeColor.GREEN;
            case "YEL": return org.bukkit.DyeColor.YELLOW;
            case "ORAN": return org.bukkit.DyeColor.ORANGE;
            case "PURP": return org.bukkit.DyeColor.PURPLE;
            case "CYAN": return org.bukkit.DyeColor.CYAN;
            case "WHIT": return org.bukkit.DyeColor.WHITE;
            case "GRAY": return org.bukkit.DyeColor.GRAY;
            default: return org.bukkit.DyeColor.WHITE;
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        // Bolinha de lobo (Snowball)
        if (projectile instanceof Snowball) {
            Snowball snowball = (Snowball) projectile;

            if (!snowball.hasMetadata(TEAM_KEY)) return;

            Team team = (Team) snowball.getMetadata(TEAM_KEY).stream()
                    .findFirst().map(MetadataValue::value).orElse(null);

            if (team == null) return;

            World world = snowball.getWorld();
            Location location = snowball.getLocation();

            // Spawnar 3 lobos (wolves)
            for (int i = 0; i < 3; i++) {
                Wolf wolf = (Wolf) world.spawnEntity(location, EntityType.WOLF);
                
                // Configurar o lobo
                wolf.setAngry(true);
                wolf.setAdult();
                wolf.setCustomName("§cLobo (" + TimeUtil.time(15) + ")");
                wolf.setCustomNameVisible(true);
                
                // Armazenar o time do lobo
                wolf.setMetadata(TEAM_KEY, new FixedMetadataValue(plugin, team));
                
                // Task para procurar alvos continuamente
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!wolf.isValid() || wolf.isDead()) {
                            cancel();
                            return;
                        }
                        setEntityTarget(wolf);
                    }
                }.runTaskTimer(plugin, 2L, 20L);
            }

            // Timer para remover os lobos após 15 segundos
            new BukkitRunnable() {
                int time = 15;

                @Override
                public void run() {
                    time--;

                    if (time <= 0) {
                        // Remover todos os lobos próximos que pertencem ao time
                        world.getNearbyEntities(location, 20, 20, 20).stream()
                                .filter(entity -> entity instanceof Wolf)
                                .filter(entity -> entity.hasMetadata(TEAM_KEY))
                                .filter(entity -> {
                                    Team entityTeam = (Team) entity.getMetadata(TEAM_KEY).stream()
                                            .findFirst().map(MetadataValue::value).orElse(null);
                                    return entityTeam != null && entityTeam.equals(team);
                                })
                                .forEach(Entity::remove);

                        cancel();
                        return;
                    }
                    
                    // Atualizar nome dos lobos
                    world.getNearbyEntities(location, 20, 20, 20).stream()
                            .filter(entity -> entity instanceof Wolf)
                            .filter(entity -> entity.hasMetadata(TEAM_KEY))
                            .filter(entity -> {
                                Team entityTeam = (Team) entity.getMetadata(TEAM_KEY).stream()
                                        .findFirst().map(MetadataValue::value).orElse(null);
                                return entityTeam != null && entityTeam.equals(team);
                            })
                            .forEach(entity -> ((Wolf) entity).setCustomName("§cLobo (" + TimeUtil.time(time) + ")"));
                }
            }.runTaskTimer(plugin, 0, 20);

            return;
        }

        // Fireball
        if (projectile instanceof Fireball) {
            World world = projectile.getWorld();

            Location location = projectile.getLocation();

            int yield = 3;

            world.createExplosion(location.getX(), location.getY(), location.getZ(), yield, false, false);

            List<Block> blocks = new ArrayList<>(Cuboid.getBlocksFromCenter(projectile, yield));

            blocks.removeIf(block -> isOre(block.getType()));

            EntityExplodeEvent entityExplodeEvent = new EntityExplodeEvent(projectile, location, blocks, yield);

            Bukkit.getPluginManager().callEvent(entityExplodeEvent);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void entityTarget(EntityTargetLivingEntityEvent event) {
        Entity entity = event.getEntity();

        if (!entity.hasMetadata(TEAM_KEY)) return;

        Team entityTeam = (Team) entity.getMetadata(TEAM_KEY).stream()
                .findFirst().map(MetadataValue::value).orElse(null);

        if (entityTeam == null) return;

        // Se o alvo for um Player
        if (event.getTarget() instanceof Player) {
            Player target = (Player) event.getTarget();
            User targetUser = (User) User.of(target.getUniqueId());

            if (targetUser == null) return;

            Team targetTeam = targetUser.getTeam();

            // Cancelar se o alvo for do mesmo time
            if (targetTeam != null && targetTeam.equals(entityTeam)) {
                event.setCancelled(true);
            }
        }

        // Se o alvo for um Wolf (Lobo)
        if (event.getTarget() instanceof Wolf) {
            Wolf targetWolf = (Wolf) event.getTarget();

            // Se o lobo tem o mesmo time, cancelar
            if (targetWolf.hasMetadata(TEAM_KEY)) {
                Team targetTeam = (Team) targetWolf.getMetadata(TEAM_KEY).stream()
                        .findFirst().map(MetadataValue::value).orElse(null);

                if (targetTeam != null && targetTeam.equals(entityTeam)) {
                    event.setCancelled(true);
                }
            }
        }

        // Se o alvo for um IronGolem
        if (event.getTarget() instanceof IronGolem) {
            IronGolem targetGolem = (IronGolem) event.getTarget();

            // Se o golem tem o mesmo time, cancelar
            if (targetGolem.hasMetadata(TEAM_KEY)) {
                Team targetTeam = (Team) targetGolem.getMetadata(TEAM_KEY).stream()
                        .findFirst().map(MetadataValue::value).orElse(null);

                if (targetTeam != null && targetTeam.equals(entityTeam)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void entityAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Entity entity = event.getEntity();

            if (entity.hasMetadata(TEAM_KEY)) {
                Team entityTeam = (Team) entity.getMetadata(TEAM_KEY).stream()
                        .findFirst().map(MetadataValue::value).orElse(null);

                if (entityTeam == null) return;

                Player damager = (Player) event.getDamager();
                User damagerUser = (User) User.of(damager.getUniqueId());

                if (damagerUser == null) return;

                Team damagerTeam = damagerUser.getTeam();

                // Cancelar se o jogador for do mesmo time da entidade
                if (damagerTeam != null && damagerTeam.equals(entityTeam)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // ... (rest of the code remains the same)

    private boolean isWolfFromSnowball(Wolf entity, Snowball snowball) {
        return entity.isValid() && entity.hasMetadata("ball") && entity.getMetadata("ball").stream()
                .findFirst()
                .map(MetadataValue::value)
                .orElse(null) == snowball;
    }

    private void setEntityTarget(Creature creature) {
        if (!creature.hasMetadata(TEAM_KEY)) return;

        Team entityTeam = (Team) creature.getMetadata(TEAM_KEY).stream()
                .findFirst().map(MetadataValue::value).orElse(null);

        if (entityTeam == null) return;

        // Pegando entidades próximas em um raio maior
        for (Entity nearbyEntity : creature.getNearbyEntities(30, 30, 30)) {
            if (!(nearbyEntity instanceof Player)) continue;

            Player target = (Player) nearbyEntity;

            User user = (User) User.of(target.getUniqueId());

            if (user == null || user.isProtected() || !user.inState(ArcadeState.ALIVE)) continue;

            Team targetTeam = user.getTeam();

            // Atacar apenas se for de time diferente
            if (targetTeam != null && !targetTeam.equals(entityTeam)) {
                creature.setTarget(target);
                return;
            }
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Arena arena = user.getArena();

        ItemStack item = event.getItem();

        switch (item.getType()) {
            case MILK_BUCKET: {
                event.setCancelled(true);

                user.getContext().setMilkTime(System.currentTimeMillis() + 30000L);

                player.sendMessage("§dLeite Mágico: §cVocê está imune de armadilhas por 30 segundos!");

                removeItemInHand(player);
                break;
            }

            case POTION: {
                int id = item.getDurability();

                event.setCancelled(true);

                switch (id) {
                    // Agilidade
                    case 2: {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 45, 1));

                        removeItemInHand(player);
                        break;
                    }

                    // Super Pulo V
                    case 11: {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 45, 4));

                        removeItemInHand(player);
                        break;
                    }

                    // Invisibilidade
                    case 14: {
                        /* Escondendo armaduras do jogador */
                        for (User arenaUser : arena.getUsers()) {
                            if (arenaUser == null) continue;

                            Player receiver = arenaUser.getAccount().player();

                            if (!arenaUser.isPlayer() || arenaUser.getTeam() != null && !arenaUser.getTeam().equals(user.getTeam())) {
                                ProtocolHandler.hideArmor(player, receiver);
                                ProtocolHandler.hidePlayerName(player, receiver);
                            }
                        }

                        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 30, 0));

                        Core.getPlatform().runSync(() -> arena.getPlayers().forEach(receiver -> {
                            ProtocolHandler.showArmor(player, receiver);
                            ProtocolHandler.showPlayerName(player, receiver);
                        }), 20 * 30);

                        removeItemInHand(player);
                        break;
                    }
                }

                break;
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onInvisibilityDamage(PlayerDamageTargetEvent event) {
        Player player = event.getTarget(), damager = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Arena arena = user.getArena();

        if (!event.isCancelled() && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);

            arena.getPlayers().forEach(receiver -> {
                ProtocolHandler.showArmor(player, receiver);
                ProtocolHandler.showPlayerName(player, receiver);
            });
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        ItemStack potion = event.getPotion().getItem();

        if (potion == null || potion.getType().equals(Material.AIR)) return;

        // Lentidão Arremessável
        if (potion.getDurability() == 16394) {
            event.setCancelled(true);

            event.getAffectedEntities().forEach(entity -> entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 5, 1)));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        User user = (User) User.of(event.getPlayer().getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Player player = event.getPlayer();

        Block block = event.getBlock();
        World world = block.getWorld();

        Location location = block.getLocation();

        Arena arena = user.getArena();

        if (!event.isCancelled() && arena.isPhase(RoomPhase.PLAYING)) {
            consumeItem(player);

            if (block.getType().equals(Material.TNT)) {
                Location adjustedLocation = location.clone().add(0.5, 0.5, 0.5);

                // Criar holograma de contagem
                arena.getAliveUsers().forEach(target -> {
                    // Holograma de TNT ativado
                    if (target.getMember().getMetadata().isTntTimerEnabled()) {
                        Player bukkitPlayer = target.getAccount().player();

                        // Renderizar holograma
                        if (bukkitPlayer != null) {
                            HologramClient hologram = BukkitCore.getManager().getHologram().spawnClient(
                                    bukkitPlayer, "tnt-" + StringUtil.generateNumberCode(5),
                                    adjustedLocation.clone().subtract(0, 1.5, 0)
                            );

                            final long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3);

                            hologram.setText(Collections.singletonList("§c" + TimeUtil.formatCooldown(endTime) + "..."));

                            hologram.spawnTo(bukkitPlayer);

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    long remainingTime = endTime - System.currentTimeMillis();

                                    if (remainingTime <= 0) {
                                        cancel();
                                        BukkitCore.getManager().getHologram().removeClient(hologram);
                                        return;
                                    }

                                    hologram.setText(0, "§c" + TimeUtil.formatCooldown(endTime) + "...");
                                }
                            }.runTaskTimer(BedWars.getInstance(), 0, 1);
                        }
                    }
                });

                // Spawnar 3 lobos
                Team team = user.getTeam();
                if (team != null) {
                    for (int i = 0; i < 3; i++) {
                        Wolf wolf = (Wolf) world.spawnEntity(adjustedLocation, org.bukkit.entity.EntityType.WOLF);
                        
                        // Configurar o lobo
                        wolf.setAngry(true);
                        wolf.setAdult();
                        wolf.setCustomName("§cLobo (" + TimeUtil.time(60) + ")");
                        wolf.setCustomNameVisible(true);
                        
                        // Armazenar o time do lobo
                        wolf.setMetadata(TEAM_KEY, new FixedMetadataValue(plugin, team));
                        
                        // Definir alvo após spawn
                        Core.getPlatform().runSync(() -> setEntityTarget(wolf), 2L);
                    }
                    
                    // Timer para remover os lobos após 60 segundos
                    new BukkitRunnable() {
                        int time = 60;

                        @Override
                        public void run() {
                            time--;

                            if (time <= 0) {
                                // Remover todos os lobos próximos que pertencem ao time
                                world.getNearbyEntities(adjustedLocation, 30, 30, 30).stream()
                                        .filter(entity -> entity instanceof Wolf)
                                        .filter(entity -> entity.hasMetadata(TEAM_KEY))
                                        .filter(entity -> {
                                            Team entityTeam = (Team) entity.getMetadata(TEAM_KEY).stream()
                                                    .findFirst().map(MetadataValue::value).orElse(null);
                                            return entityTeam != null && entityTeam.equals(team);
                                        })
                                        .forEach(Entity::remove);

                                cancel();
                                return;
                            }
                            
                            // Atualizar nome dos lobos
                            world.getNearbyEntities(adjustedLocation, 30, 30, 30).stream()
                                    .filter(entity -> entity instanceof Wolf)
                                    .filter(entity -> entity.hasMetadata(TEAM_KEY))
                                    .filter(entity -> {
                                        Team entityTeam = (Team) entity.getMetadata(TEAM_KEY).stream()
                                                .findFirst().map(MetadataValue::value).orElse(null);
                                        return entityTeam != null && entityTeam.equals(team);
                                    })
                                    .forEach(entity -> ((Wolf) entity).setCustomName("§cLobo (" + TimeUtil.time(time) + ")"));
                        }
                    }.runTaskTimer(plugin, 0, 20);
                }

                TNTPrimed tntPrimed = world.spawn(adjustedLocation, TNTPrimed.class);

                block.setType(Material.AIR);

                tntPrimed.setIsIncendiary(false);
                tntPrimed.setYield(0);
                tntPrimed.setFuseTicks(60);
                tntPrimed.setFireTicks(0);

                tntPrimed.setVelocity(new Vector(0, 0, 0));
                tntPrimed.teleport(adjustedLocation);

                world.playSound(adjustedLocation, Sound.FUSE, 1F, 0.8F);
            }

            if (arena.isType(Type.CASUAL) && block.getType().equals(Material.SLIME_BLOCK)) {
                if (!block.hasMetadata("jumper"))
                    block.setMetadata("jumper", new FixedMetadataValue(plugin, true));

                ParticleBuilder particle = new ParticleBuilder(ParticleEffect.VILLAGER_HAPPY, location.clone().add(0.5, 0.5, 0.5));

                particle.setSpeed(0.15f)
                        .setAmount(25)
                        .display();

                world.playSound(location, Sound.FALL_BIG, 2, 2f);

                Core.getPlatform().runSync(() -> block.setType(Material.AIR), 20 * 8);
            }
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        Block block = event.getBlock();

        if (block.hasMetadata("jumper"))
            event.setCancelled(true);
    }

    @EventHandler
    public void jumper(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

        Arena arena = user.getArena();

        Block down = event.getTo().getBlock().getRelative(BlockFace.DOWN);

        if (arena.isType(Type.CASUAL) && arena.isPhase(RoomPhase.PLAYING)) {

            if (down.getType().equals(Material.SLIME_BLOCK) && down.hasMetadata("jumper")) {
                Vector vector = player.getEyeLocation().getDirection().multiply(1.1F).setY(0.96F);

                player.setFallDistance(-1.0F);
                player.setVelocity(vector);

                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
            }
        }
        
        // Efeito visual ao pousar após usar estilingue
        if (player.isOnGround() && event.getFrom().getY() > event.getTo().getY()) {
            long slingshotProtection = user.getContext().getSlingshotProtectionTime();
            boolean slingshotLanded = user.getContext().isSlingshotLanded();
            
            if (slingshotProtection > 0 && System.currentTimeMillis() < slingshotProtection && !slingshotLanded) {
                
                // Marcar que já pousou para não repetir o efeito
                user.getContext().setSlingshotLanded(true);
                
                Location loc = player.getLocation();
                
                // Efeito de explosão grande no impacto
                ParticleBuilder explosionParticle = new ParticleBuilder(ParticleEffect.EXPLOSION_HUGE, loc);
                explosionParticle.setSpeed(0.1f)
                        .setAmount(2)
                        .display();
                
                // Efeito de nuvem de poeira
                ParticleBuilder cloudParticle = new ParticleBuilder(ParticleEffect.CLOUD, loc);
                cloudParticle.setSpeed(0.15f)
                        .setAmount(30)
                        .setOffset(0.8f, 0.2f, 0.8f)
                        .display();
                
                // Efeito de lava/fogo no impacto
                ParticleBuilder lavaParticle = new ParticleBuilder(ParticleEffect.LAVA, loc);
                lavaParticle.setSpeed(0.1f)
                        .setAmount(10)
                        .setOffset(0.5f, 0.1f, 0.5f)
                        .display();
                
                // Efeito de redstone (vermelho) espalhado
                ParticleBuilder redstoneParticle = new ParticleBuilder(ParticleEffect.REDSTONE, loc);
                redstoneParticle.setSpeed(0.2f)
                        .setAmount(25)
                        .setOffset(1.0f, 0.2f, 1.0f)
                        .display();
                
                // Sons de impacto mais fortes
                player.playSound(loc, Sound.FALL_BIG, 1.5f, 0.6f);
                player.playSound(loc, Sound.EXPLODE, 0.8f, 1.2f);
                player.playSound(loc, Sound.ANVIL_LAND, 0.6f, 1.5f);
            }
        }
    }

    @EventHandler
    public void entityDamageEvent(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player && causes.contains(event.getCause()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onEntityExplodes(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        Location location = event.getLocation();

        World world = location.getWorld();

        Arena arena = (Arena) BukkitCore.getManager().getArcade().getArenas().stream()
                .filter(room -> room.getWorld().equals(world)).findFirst().orElse(null);

        if (entity instanceof TNTPrimed && event.blockList().isEmpty()) {
            int yield = 2;

            world.createExplosion(location.getX(), location.getY(), location.getZ(), yield, false, false);

            List<Block> blocks = new ArrayList<>(Cuboid.getBlocksFromCenter(entity, yield));

            blocks.removeIf(block -> isOre(block.getType()));

            EntityExplodeEvent entityExplodeEvent = new EntityExplodeEvent(entity, location, blocks, yield);

            Bukkit.getPluginManager().callEvent(entityExplodeEvent);
        }

        event.setCancelled(true);

        if (arena != null) {
            for (Player player : arena.getPlayers()) {
                if (player.getLocation().distance(location) <= 5) {
                    Vector direction = player.getLocation().toVector().subtract(location.toVector());

                    if (direction.length() > 0)
                        direction.normalize();
                    else
                        direction = new Vector(0, 0, 1);

                    double horizontal = entity instanceof TNTPrimed ? 2.5 : entity instanceof Fireball ? 3.2 : 0;

                    Vector horizontalVelocity = direction.multiply(horizontal);

                    double vertical = entity instanceof TNTPrimed ? 1.1 : entity instanceof Fireball ? 1.080 : 0;

                    horizontalVelocity.setY(vertical);

                    player.setVelocity(horizontalVelocity);
                }
            }

            Core.getPlatform().runSync(() -> {
                List<Block> blocks = event.blockList()
                        .stream()
                        .filter(block -> BLOCKS_PERMITTED_TO_EXPLODE.contains(block.getType()) && arena.isReversible(block))
                        .limit(Core.RANDOM.ints(5, 8).findFirst().orElse(5))
                        .collect(Collectors.toList());

                for (Block block : blocks) {
                    if (block.isEmpty()) continue;

                    block.breakNaturally();
                }
            }, 2L);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Egg) {
            Egg egg = (Egg) event.getEntity();

            if (egg.getShooter() instanceof Player) {
                Player shooter = (Player) egg.getShooter();

                User user = (User) User.of(shooter.getUniqueId());

                if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) return;

                EggBridgeManager.save(new EggBridgeTask(egg, shooter, user.getArena(), user.getTeam().getColor()));
            }
        }
    }

    protected boolean hasCooldown(Player player, String name) {
        CooldownManager manager = BukkitCore.getManager().getCooldown();

        if (manager.hasCooldown(player, "item-" + name)) {
            Cooldown cooldown = manager.getCooldown(player, "item-" + name);

            if (cooldown == null) return false;

            player.sendMessage("§cAguarde " + new DecimalFormat("#.#").format(cooldown.getRemaining()) + " para usar " + name + " novamente.");
            return true;
        }

        return false;
    }

    protected void setCooldown(Player player, String name, long time) {
        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Cooldown cooldown = new Cooldown("item-" + name, time);

        cooldown.setShowBar(user.getMember().getMetadata().isShowBar());

        BukkitCore.getManager().getCooldown().addCooldown(player, cooldown);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent evt) {
        Player profile = evt.getPlayer();
        User user = (User) User.of(profile.getUniqueId());

        if (user.isSpectator()) {
            evt.setCancelled(true);
        } else {
            ItemStack item = evt.getItemDrop().getItemStack();
            if (!DropValidator.canDropItem(evt.getPlayer())) {
                evt.setCancelled(true);
                evt.getPlayer().sendMessage("§cVocê não pode executar esta ação no momento!");
                return;
            }

            if (item.getType().name().contains("_SWORD") || item.getType().name().contains("_HELMET") ||
                    item.getType().name().contains("_CHESTPLATE") || item.getType().name().contains("_LEGGINGS") ||
                    item.getType().name().contains("_BOOTS") || item.getType().name().contains("BOW") ||
                    item.getType().name().contains("_PICKAXE") || item.getType().name().contains("_AXE") ||
                    item.getType().name().contains("SHEARS") || item.getType().name().contains("COMPASS")) {
                evt.setCancelled(true);
            }
        }
    }

    private static boolean isOre(Material material) {
        return material == Material.IRON_INGOT || material == Material.GOLD_INGOT 
                || material == Material.EMERALD || material == Material.DIAMOND;
    }
}