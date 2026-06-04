package com.minecraft.arcade.eggwars.listener;

import com.minecraft.arcade.eggwars.arcade.Arcade;
import com.minecraft.arcade.eggwars.arcade.arena.Arena;
import com.minecraft.arcade.eggwars.arcade.arena.context.objects.top.enums.TopType;
import com.minecraft.arcade.eggwars.event.PlayerEggBreakEvent;
import com.minecraft.arcade.eggwars.structure.team.Team;
import com.minecraft.arcade.eggwars.user.User;
import com.minecraft.core.Core;
import com.minecraft.core.account.Account;
import com.minecraft.core.api.party.Party;
import com.minecraft.core.arcade.ArcadeHolder;
import com.minecraft.core.arcade.room.event.ArenaSearchedEvent;
import com.minecraft.core.arcade.room.map.rollback.RollbackBlock;
import com.minecraft.core.arcade.room.phase.RoomPhase;
import com.minecraft.core.arcade.room.slot.Slot;
import com.minecraft.core.arcade.route.ArcadeRouteContext;
import com.minecraft.core.arcade.route.state.ArcadeState;
import com.minecraft.core.bukkit.BukkitCore;
import com.minecraft.core.bukkit.api.option.ServerOptions;
import com.minecraft.core.bukkit.event.type.account.AccountVanishLogEvent;
import com.minecraft.core.bukkit.event.type.player.PlayerArenaWarpEvent;
import com.minecraft.core.bukkit.event.type.update.type.UpdateType;
import com.minecraft.core.bukkit.event.type.update.type.list.SyncUpdateEvent;
import com.minecraft.core.bukkit.manager.Manager;
import com.minecraft.core.member.list.eggwars.EggMember;
import com.minecraft.core.member.list.eggwars.objects.enums.item.EggWarsItem;
// TODO: Implementar collectibles para EggWars se necessário
// import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.BedDestroyCollectible;
// import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.list.bed.list.Random;
// import com.minecraft.core.member.list.bedwars.objects.metadata.objects.collectible.objects.type.BedCollectibleType;
import com.minecraft.core.util.list.bukkit.BukkitUtil;
import com.minecraft.core.util.list.bukkit.ColorUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.github.paperspigot.Title;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ArcadeListener implements Listener {

    private final Manager manager = BukkitCore.getManager();

    private final List<Material> ALLOWED_DROP_ITEMS = Arrays.asList(
            Material.BOW, Material.ARROW, Material.GOLDEN_APPLE, Material.ENDER_PEARL,
            Material.EGG, Material.FIREBALL, Material.MAGMA_CREAM, Material.STICK
    );

    private final List<String> BLOCKED_CHEST_ITEMS = Arrays.asList(
            "SHEARS", "_AXE", "_PICKAXE", "WOOD_SWORD"
    );

    @EventHandler
    public void onSyncUpdate(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (ArcadeHolder holder : manager.getArcade().getArcades()) {
                if (!(holder instanceof Arcade arcade)) continue;

                List<Arena> arenaList = arcade.getRooms().stream().map(room -> (Arena) room).toList();

                // Lazy loading: arenas são criadas sob demanda, não mais automaticamente

                arenaList.forEach(Arena::handlePulse);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onArenaSearched(ArenaSearchedEvent event) {
        Arena arena = (Arena) event.getArena();

        User user = (User) User.of(event.getSender().getId());

        if (user == null) return;

        Player player = user.getPlayer();

        // Já está no servidor, apenas enviar para a sala
        if (player != null) {
            event.setCancelled(true);

            ArcadeRouteContext route = ArcadeRouteContext.builder()
                    .arcade(arena.getArcade().getCategory())
                    .roomId(arena.getId())
                    .mapId(arena.getMap().getId())
                    .slot(arena.getSlot())
                    .serverId(Core.getServerId())
                    .arenaIdentifier(arena.getIdentifier())
                    .state(ArcadeState.ALIVE)
                    .maxPlayers(arena.getMaxPlayers())
                    .join(event.getRoute().getJoin())
                    .build();

            Core.getPlatform().runSync(() -> {
                Arena current = user.getArena();

                current.quit(player);

                user.setArena(arena);

                arena.join(player);
                Party party = user.getAccount().getParty();

                if (party != null && party.isAuthor(player.getUniqueId()))
                    party.redirect(route);
            });
        }
    }

    @EventHandler
    public void onPartyWarp(PlayerArenaWarpEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user != null && user.isPlayer()) {
            Arena current = user.getArena(), arena = (Arena) event.getArena();

            Core.getPlatform().runSync(() -> {
                current.quit(player);

                user.setArena(arena);

                arena.join(player);
            });
        }
    }

    @EventHandler
    public void onChestInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Arena arena = user.getArena();

        Team team = user.getTeam();

        Block clicked = event.getClickedBlock();

        if (event.hasBlock()) {

            if (!user.isPlayer() || !user.inState(ArcadeState.ALIVE)) {
                event.setCancelled(true);
                return;
            }

            if (clicked.getType().equals(Material.CHEST) && !team.inIsland(clicked.getLocation())) {
                Team chestTeam = arena.getTeamList().stream().filter(search -> search.inIsland(clicked.getLocation()))
                        .findFirst().orElse(null);

                if (chestTeam != null && !chestTeam.isDead()) {
                    event.setCancelled(true);

                    player.sendMessage("§cVocê não pode abrir o baú de outro time enquanto ele está vivo.");
                }
            }
        }
    }

    @EventHandler
    public void onChestDeposit(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        User user = (User) User.of(player.getUniqueId());

        if (user == null) return;

        Arena arena = user.getArena();
        Team team = user.getTeam();
        Block clicked = event.getClickedBlock();

        if (!event.hasBlock() || clicked == null) return;

        // Verificar se é um baú ou ender chest
        Material blockType = clicked.getType();
        if (!blockType.equals(Material.CHEST) && !blockType.equals(Material.ENDER_CHEST)) return;

        if (!user.isPlayer() || !user.inState(ArcadeState.ALIVE)) {
            event.setCancelled(true);
            return;
        }

        // Verificar se o baú é da ilha do jogador (apenas para CHEST normal)
        if (blockType.equals(Material.CHEST) && !team.inIsland(clicked.getLocation())) {
            Team chestTeam = arena.getTeamList().stream()
                    .filter(search -> search.inIsland(clicked.getLocation()))
                    .findFirst()
                    .orElse(null);

            if (chestTeam != null && !chestTeam.isDead()) {
                event.setCancelled(true);
                player.sendMessage("§cVocê não pode interagir com o baú de outro time enquanto ele está vivo.");
                return;
            }
        }

        // Verificar o item na mão
        ItemStack handItem = player.getItemInHand();

        if (handItem == null || handItem.getType() == Material.AIR) {
            event.setCancelled(true);
            player.sendMessage("§cVocê precisa estar segurando um minério para depositar!");
            return;
        }

        // Verificar se é um minério válido
        Material oreType = handItem.getType();
        List<Material> validOres = Arrays.asList(
                Material.CLAY_BRICK,
                Material.IRON_INGOT,
                Material.GOLD_INGOT,
                Material.DIAMOND
        );

        if (!validOres.contains(oreType)) {
            event.setCancelled(true);
            player.sendMessage("§cVocê só pode depositar minérios: §6Tijolo§c, §fFerro§c, §eOuro§c ou §bDiamante§c!");
            return;
        }

        event.setCancelled(true);

        // Contar quantos minérios o jogador tem no inventário
        int totalOres = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == oreType) {
                totalOres += item.getAmount();
            }
        }

        if (totalOres == 0) {
            player.sendMessage("§cVocê não possui nenhum minério deste tipo!");
            return;
        }

        // Obter o inventário correto (Chest ou EnderChest)
        Inventory targetInventory;

        if (blockType.equals(Material.ENDER_CHEST)) {
            targetInventory = player.getEnderChest();
        } else {
            Chest chest = (Chest) clicked.getState();
            targetInventory = chest.getInventory();
        }

        // Remover todos os minérios do inventário do jogador
        player.getInventory().remove(oreType);

        // Adicionar os minérios no baú/enderchest
        ItemStack oreStack = new ItemStack(oreType, totalOres);
        HashMap<Integer, ItemStack> leftover = targetInventory.addItem(oreStack);

        // Se não couber tudo, devolver o que sobrou
        if (!leftover.isEmpty()) {
            int totalLeftover = 0;

            for (ItemStack remaining : leftover.values()) {
                totalLeftover += remaining.getAmount();
                player.getInventory().addItem(remaining);
            }

            int deposited = totalOres - totalLeftover;
            player.sendMessage("§eVocê depositou §b" + deposited + "x " + getOreName(oreType) + "§e no " +
                    (blockType.equals(Material.ENDER_CHEST) ? "ender chest" : "baú") + "!");
            player.sendMessage("§c" + totalLeftover + "x §enão couberam e foram devolvidos!");
        } else {
            player.sendMessage("§aVocê depositou §b" + totalOres + "x " + getOreName(oreType) + "§a no " +
                    (blockType.equals(Material.ENDER_CHEST) ? "ender chest" : "baú") + "!");
        }

        player.updateInventory();
        player.playSound(player.getLocation(),
                blockType.equals(Material.ENDER_CHEST) ? Sound.ENDERMAN_TELEPORT : Sound.CHEST_OPEN,
                1.0f, 1.0f);
    }

    private String getOreName(Material material) {
        return switch (material) {
            case CLAY_BRICK -> "§6Tijolo";
            case IRON_INGOT -> "§fFerro";
            case GOLD_INGOT -> "§eOuro";
            case DIAMOND -> "§bDiamante";
            default -> material.name();
        };
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) {
            event.setCancelled(true);
            return;
        }

        ItemStack stack = event.getItemDrop().getItemStack();

        if (stack == null || (stack.getType() == Material.AIR || stack.getType() == Material.WOOD_SWORD)) {
            event.setCancelled(true);
            return;
        }

        Arena arena = user.getArena();

        if (!arena.isPhase(RoomPhase.PLAYING)) {
            event.setCancelled(true);
            return;
        }

        if (isOre(stack.getType()) || stack.getType().isBlock()) {
            event.setCancelled(false);
            return;
        }

        /* Dropou espada de ferro, pedra, etc, volta a sword de madeira pro inventário */
        if (stack.getType().name().contains("_SWORD") && !stack.getType().equals(Material.WOOD_SWORD)) {
            event.setCancelled(false);

            if (!BukkitUtil.hasItemInInventory(player, Material.WOOD_SWORD))
                player.getInventory().addItem(new ItemStack(Material.WOOD_SWORD));

            return;
        }

        event.setCancelled(!ALLOWED_DROP_ITEMS.contains(stack.getType()));
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        ItemStack hand = player.getItemInHand();

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                && event.hasBlock() && ServerOptions.NON_INTERACTIVE_BLOCKS.contains(event.getClickedBlock().getType())) {

            if (!player.isSneaking() || hand == null || !hand.getType().isBlock())
                event.setCancelled(true);
        }
        
        // Interceptar clique no ovo para prevenir teletransporte
        if (event.hasBlock() && event.getClickedBlock().getType() == Material.DRAGON_EGG) {
            User user = (User) User.of(player.getUniqueId());
            if (user != null && user.isPlayer() && user.getArena() != null) {
                Arena arena = user.getArena();
                Team team = user.getTeam();
                
                if (team != null) {
                    Block block = event.getClickedBlock();
                    
                    // Se é o próprio ovo, cancelar interação
                    if (team.isYourEgg(block.getLocation())) {
                        event.setCancelled(true);
                        player.sendMessage("§cVocê não pode quebrar o seu ovo.");
                        return;
                    }
                    
                    // Se é ovo do inimigo e está quebrando (LEFT_CLICK), processar quebra
                    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        Team eggTeam = arena.getTeam(block.getLocation());
                        if (eggTeam != null && !eggTeam.equals(team)) {
                            event.setCancelled(true);
                            
                            // Remover o ovo e chamar evento
                            Core.getPlatform().runSync(() -> {
                                Location eggLocation = block.getLocation();
                                eggLocation.getBlock().setType(Material.AIR);
                                
                                new PlayerEggBreakEvent(player, arena, team, eggTeam, eggLocation).call();
                            }, 1L);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockChestItems(InventoryClickEvent event) {
        // Bloquear interação com armadura
        if (event.getClickedInventory() != null && event.getClickedInventory().getType().equals(InventoryType.PLAYER)
                && event.getSlot() >= 36 && event.getSlot() <= 39) {
            event.setCancelled(true);
            return;
        }

        // Bloquear interações com itens específicos no baú
        Inventory top = event.getView().getTopInventory();

        if (top.getSize() == 27 && top.getType().name().contains("CHEST")) {
            ItemStack item = event.getAction().equals(InventoryAction.HOTBAR_SWAP)
                    ? event.getWhoClicked().getInventory().getItem(event.getHotbarButton())
                    : event.getCurrentItem();

            if (item != null && (BLOCKED_CHEST_ITEMS.stream().anyMatch(name -> item.getType().name().contains(name))
                    || item.isSimilar(EggWarsItem.UTIL_TRACKING.getStack())))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        Arena arena = user.getArena();

        Arena.ChatType type = arena.isSlot(Slot.SOLO) || !arena.isPhase(RoomPhase.PLAYING) || !user.isPlayer()
                ? Arena.ChatType.GLOBAL
                : Arena.ChatType.TEAM;

        arena.chat(type, user, event.getMessage());
    }

    @EventHandler
    public void onVanishLeave(AccountVanishLogEvent event) {
        Account account = event.getAccount();

        if (event.isState(AccountVanishLogEvent.VanishState.LEAVE)) {
            User user = (User) User.of(account.getId());

            if (user != null) {
                Arena arena = user.getArena();

                if (arena.isPreGame())
                    arena.getTeamList().stream()
                            .filter(search -> !search.isFull())
                            .findFirst().ifPresent(user::setTeam);
            }
        }
    }

    @EventHandler
    public void onSeeds(ItemSpawnEvent event) {
        ItemStack stack = event.getEntity().getItemStack();

        if (stack != null && stack.getType() == Material.SEEDS)
            event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        ItemStack stack = item.getItemStack();
        
        if (stack == null) return;
        
        // Verificar se é um minério de gerador (tem display name único)
        ItemMeta meta = stack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            String displayName = meta.getDisplayName();
            
            // Se tiver o código único invisível, remover após um pequeno delay
            // para que o merge não aconteça, mas o nome não apareça ao pegar
            if (displayName.startsWith("§r") && displayName.length() > 2) {
                // Manter o nome único para prevenir merge
                // Será removido quando o jogador pegar o item
                return;
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) {
            event.setCancelled(true);
            return;
        }

        Arena arena = user.getArena();

        Block block = event.getBlockPlaced();

        if (arena.isProtectedArea(block.getLocation())) {
            event.setCancelled(true);

            player.sendMessage("§cVocê não pode colocar blocos aqui.");
            return;
        }

        event.setCancelled(!arena.isPhase(RoomPhase.PLAYING));

        if (!arena.isReversible(block))
            arena.addRollBack(block, RollbackBlock.RollbackType.PLACE_BLOCK);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE)) {
            event.setCancelled(true);
            return;
        }

        Arena arena = user.getArena();

        if (!arena.isPhase(RoomPhase.PLAYING)) {
            event.setCancelled(true);
            return;
        }

        Team team = user.getTeam();

        Block block = event.getBlock();

        boolean isEgg = block.getType() == Material.DRAGON_EGG;

        if (isEgg && team.isYourEgg(block.getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§cVocê não pode quebrar o seu ovo.");
            return;
        }

        if (block.getType().name().contains("GRASS") && !block.getType().equals(Material.GRASS)) {
            event.setCancelled(true);

            block.setType(Material.AIR);
            return;
        }

        if (!arena.isReversible(block) && !isEgg) {
            event.setCancelled(true);

            player.sendMessage("§cVocê não pode quebrar este bloco.");
            return;
        }

        if (!event.isCancelled()) {

            if (isEgg) {
                Team eggTeam = arena.getTeam(block.getLocation());

                if (eggTeam != null && !eggTeam.equals(team)) {
                    // Cancelar o evento para evitar que o ovo se teletransporte
                    event.setCancelled(true);

                    // Remover o ovo manualmente e chamar o evento
                    Location eggLocation = block.getLocation();
                    block.setType(Material.AIR);
                    
                    // Chamar o evento de quebra
                    Core.getPlatform().runSync(() -> {
                        new PlayerEggBreakEvent(player, arena, team, eggTeam, eggLocation).call();
                    }, 1L);
                } else {
                    event.setCancelled(true);
                    if (eggTeam == null) {
                        player.sendMessage("§cNão foi possível quebrar o ovo. Contate um administrador!");
                    }
                }
            }
        }
    }

    private final List<String> eggBreakMessages = Arrays.asList(
            "foi completamente obliterado por", "foi destruído por", "foi transformado em cinzas por", "foi desmontado por"
    );

    @EventHandler
    public void onPlayerEggBreak(PlayerEggBreakEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        Arena arena = event.getArena();
        Team team = event.getTeam(), eggTeam = event.getEggTeam();

        // Garantir que o ovo seja removido
        eggTeam.destructEgg();

        String message = eggBreakMessages.get(Core.RANDOM.nextInt(eggBreakMessages.size()));

        EggMember member = user.getMember();

        user.setEggDestruction();
        arena.getArcade().updateSidebar(user);

        arena.verifyTop(TopType.EGG_DESTRUCTION, player.getUniqueId(), user.getEggDestruction());

        arena.send("",
                "§f§lOVO DESTRUÍDO §f» §7O " + eggTeam.getColor() + "Ovo do " + eggTeam.getName()
                        + "§7 " + message + " " + team.getColor() + player.getName() + "§7.",
                "");

        // Renderizar animação
        // TODO: Implementar collectibles para EggWars se necessário
        // BedDestroyCollectible destroy = (BedDestroyCollectible) member.getActiveCollectible(BedCollectibleType.BED_DESTRUCTION);
        // if (destroy != null) {
        //     if (!destroy.isNotEqual(Random.class)) {
        //         destroy = (BedDestroyCollectible) member.getRandomCollectible(BedCollectibleType.BED_DESTRUCTION);
        //         if (destroy == null)
        //             destroy = (BedDestroyCollectible) member.getCollectible(BedCollectibleType.BED_DESTRUCTION, "Nenhuma");
        //     }
        //     destroy.execute(event.getLocation());
        // }

        Core.getPlatform().runSync(() -> arena.getUsers().forEach(arena::updateSidebarTeams), 2L);

        eggTeam.getPlayers().forEach(target -> target.sendTitle(new Title("§c§lOVO DESTRUÍDO", "§7Você não vai mais renascer!", 0, 40, 90)));

        arena.sound(Sound.ENDERDRAGON_GROWL, 13, 1);

        Core.getReconnectData().list().stream()
            .filter(reconnect -> reconnect.getRoute().getArenaIdentifier() != null 
                && reconnect.getRoute().getArenaIdentifier().equals(arena.getIdentifier())
                && reconnect.getTeamId() != null
                && reconnect.getTeamId().equalsIgnoreCase(eggTeam.getCodeId()))
            .forEach(reconnect -> Core.getReconnectData().delete(reconnect.getSender()));
    }

    @EventHandler
    public void onPlayerPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        User user = (User) User.of(player.getUniqueId());

        if (user == null || !user.isPlayer() || !user.inState(ArcadeState.ALIVE) || !user.getArena().isPhase(RoomPhase.PLAYING)) {
            event.setCancelled(true);
            return;
        }

        Item item = event.getItem();

        ItemStack stack = item.getItemStack();

        if (isOre(stack.getType())) {
            ItemMeta meta = stack.getItemMeta();

            if (meta != null && meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                
                // Remover display name único invisível de geradores
                if (displayName.startsWith("§r") && displayName.length() > 2) {
                    meta.setDisplayName(null);
                    stack.setItemMeta(meta);
                    item.setItemStack(stack);
                }
            }
            
            if (meta != null && meta.hasDisplayName()) {
                meta.setDisplayName(null);
                stack.setItemMeta(meta);
            }

            return;
        }

        if (stack.getType().name().contains("_SWORD") && !stack.getType().equals(Material.WOOD_SWORD)) {
            if (BukkitUtil.hasItemInInventory(player, Material.WOOD_SWORD))
                player.getInventory().remove(Material.WOOD_SWORD);

            return;
        }

        if (stack.getType() == Material.WOOL || stack.getType() == Material.STAINED_CLAY) {
            Team team = user.getTeam();

            int hexColor = ColorUtil.getIdByColor(team.getColor());

            if (stack.getDurability() != hexColor)
                stack.setDurability((short) hexColor);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL ||
            event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CHUNK_GEN ||
            event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DEFAULT) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEggTeleport(BlockFromToEvent event) {
        Block block = event.getBlock();
        
        // Interceptar teletransporte do DRAGON_EGG e cancelar se for ovo protegido
        if (block.getType() == Material.DRAGON_EGG) {
            Location fromLoc = block.getLocation();
            
            // Verificar todos os jogadores na arena
            for (Player player : block.getWorld().getPlayers()) {
                User user = (User) User.of(player.getUniqueId());
                if (user != null && user.isPlayer() && user.getArena() != null) {
                    Arena arena = user.getArena();
                    Team team = user.getTeam();
                    
                    if (team != null) {
                        // Se é o próprio ovo do jogador, cancelar teletransporte e restaurar
                        if (team.isYourEgg(fromLoc)) {
                            event.setCancelled(true);
                            Core.getPlatform().runSync(() -> {
                                Location eggLoc = team.getEggLocation();
                                if (eggLoc != null && eggLoc.getBlock().getType() != Material.DRAGON_EGG) {
                                    eggLoc.getBlock().setType(Material.DRAGON_EGG);
                                }
                            }, 1L);
                            return;
                        }
                    }
                }
            }
        }
    }

    private static boolean isOre(Material material) {
        return material == Material.IRON_INGOT || material == Material.GOLD_INGOT 
                || material == Material.DIAMOND || material == Material.CLAY_BRICK;
    }
}