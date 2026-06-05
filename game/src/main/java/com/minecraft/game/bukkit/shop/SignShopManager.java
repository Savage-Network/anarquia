package com.minecraft.game.bukkit.shop;

import com.minecraft.game.Core;
import com.minecraft.game.account.Account;
import com.minecraft.game.api.shop.SignShop;
import com.minecraft.game.api.shop.SignShopType;
import com.minecraft.game.backend.data.SignShopData;
import com.minecraft.game.bukkit.semi.SemiAnarchyConfig;
import com.minecraft.game.bukkit.semi.SpawnProtection;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.UUID;

public final class SignShopManager {

    private static final UUID SERVER_OWNER = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private SignShopManager() {}

    public static SignShopData data() {
        return Core.getSignShopData();
    }

    public static Optional<SignShop> getShop(Block signBlock) {
        return data().get(SignShop.locationId(signBlock.getLocation()));
    }

    public static void register(SignShop shop) {
        data().save(shop);
    }

    public static void unregister(Block signBlock) {
        data().delete(SignShop.locationId(signBlock.getLocation()));
    }

    public static String tryCreate(Sign sign, Player owner) {
        if (!SignShopConfig.enabled()) return "§cLojas de placa desativadas.";
        if (!owner.hasPermission("game.shop.create") && !owner.hasPermission("game.shop.admin")) {
            return "§cSem permissao para criar lojas.";
        }
        if (SpawnProtection.isProtected(sign.getLocation()) && !owner.hasPermission("game.shop.admin")) {
            return "§cNao e possivel criar loja na area protegida do spawn.";
        }
        if (SignShopStorageUtil.findStorageBlock(sign.getBlock()) == null) {
            return "§cColoque a placa em um bau ou barril.";
        }

        Optional<SignShop> parsed = SignShopParser.parse(sign, owner);
        if (parsed.isEmpty()) {
            return "§cFormato invalido. Veja §f/loja ajuda";
        }

        SignShop shop = parsed.get();
        register(shop);
        return "§aLoja criada! §7" + formatType(shop) + " §f" + ItemDisplayNames.plain(shop.getMaterial())
                + " §7por §a" + SemiAnarchyConfig.currencySymbol() + shop.getPricePerUnit() + "/un";
    }

    public static String processClick(Player player, Sign sign, boolean shift) {
        if (!SignShopConfig.enabled()) return "§cLojas desativadas.";

        Block signBlock = sign.getBlock();
        String id = SignShop.locationId(signBlock.getLocation());
        Optional<SignShop> shopOpt = data().get(id);

        if (shopOpt.isEmpty()) {
            return "§cLoja nao registrada. Edite e salve a placa (ou peca ao dono).";
        }

        SignShop shop = shopOpt.get();
        int amount = shift ? 64 : 1;

        return shop.getType() == SignShopType.BUY
                ? processBuy(player, shop, signBlock, amount)
                : processSell(player, shop, signBlock, amount);
    }

    private static String processBuy(Player buyer, SignShop shop, Block signBlock, int amount) {
        Account buyerAcc = Core.getAccountController().of(buyer.getUniqueId());
        if (buyerAcc == null) return "§cConta nao carregada.";

        Inventory chest = shop.isServerShop() ? null : SignShopStorageUtil.findChestInventory(signBlock);
        if (!shop.isServerShop() && chest == null) return "§cBau da loja nao encontrado.";

        int stock = shop.isServerShop() ? Integer.MAX_VALUE : countMaterial(chest, shop.getMaterial());
        if (stock <= 0) return "§cSem estoque.";

        int fit = fitAmount(buyer.getInventory(), shop.getMaterial(), amount);
        amount = Math.min(amount, Math.min(stock, fit));
        if (amount <= 0) return "§cInventario cheio.";

        int total = shop.getPricePerUnit() * amount;
        int tax = (total * SignShopConfig.taxPercent()) / 100;
        int cost = total + tax;

        if (!buyerAcc.removeGolds(cost)) {
            int affordable = buyerAcc.getGolds() / Math.max(1, shop.getPricePerUnit());
            if (affordable <= 0) return "§cSaldo insuficiente.";
            amount = Math.min(amount, Math.min(affordable, stock));
            fit = fitAmount(buyer.getInventory(), shop.getMaterial(), amount);
            amount = Math.min(amount, fit);
            if (amount <= 0) return "§cInventario cheio.";
            total = shop.getPricePerUnit() * amount;
            tax = (total * SignShopConfig.taxPercent()) / 100;
            cost = total + tax;
            if (!buyerAcc.removeGolds(cost)) return "§cSaldo insuficiente.";
        }

        if (!shop.isServerShop()) {
            removeFromChest(chest, shop.getMaterial(), amount);
        }

        buyer.getInventory().addItem(new ItemStack(shop.getMaterial(), amount));
        buyer.updateInventory();

        int ownerShare = total;
        if (!shop.isServerShop()) {
            payOwner(shop.getOwnerId(), ownerShare);
        }

        buyerAcc.markDirty();
        buyer.playSound(buyer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
        return "§aComprou §f" + amount + "x " + formatMaterial(shop.getMaterial())
                + " §apor §a" + SemiAnarchyConfig.currencySymbol() + cost;
    }

    private static String processSell(Player seller, SignShop shop, Block signBlock, int amount) {
        Account sellerAcc = Core.getAccountController().of(seller.getUniqueId());
        if (sellerAcc == null) return "§cConta nao carregada.";

        int has = countInInventory(seller.getInventory(), shop.getMaterial());
        if (has <= 0) return "§cVoce nao tem este item.";

        Inventory chest = shop.isServerShop() ? null : SignShopStorageUtil.findChestInventory(signBlock);
        if (!shop.isServerShop() && chest == null) return "§cBau da loja nao encontrado.";

        amount = Math.min(amount, has);
        if (!shop.isServerShop()) {
            int space = spaceInChest(chest, shop.getMaterial(), amount);
            amount = Math.min(amount, space);
        }
        if (amount <= 0) return "§cBau cheio.";

        int total = shop.getPricePerUnit() * amount;
        if (!shop.isServerShop()) {
            Account ownerAcc = Core.getAccountController().of(shop.getOwnerId());
            if (ownerAcc == null) return "§cDono da loja offline (conta nao carregada).";
            if (!ownerAcc.removeGolds(total)) return "§cO dono da loja nao tem saldo suficiente.";
            ownerAcc.markDirty();
        }

        removeFromInventory(seller.getInventory(), shop.getMaterial(), amount);
        if (!shop.isServerShop()) {
            chest.addItem(new ItemStack(shop.getMaterial(), amount));
        }

        sellerAcc.addGolds(total);
        sellerAcc.markDirty();
        seller.updateInventory();
        seller.playSound(seller.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

        return "§aVendeu §f" + amount + "x " + formatMaterial(shop.getMaterial())
                + " §apor §a" + SemiAnarchyConfig.currencySymbol() + total;
    }

    private static void payOwner(UUID ownerId, int amount) {
        if (amount <= 0 || SERVER_OWNER.equals(ownerId)) return;
        Account owner = Core.getAccountController().of(ownerId);
        if (owner != null) {
            owner.addGolds(amount);
            owner.markDirty();
        }
    }

    private static int countMaterial(Inventory inv, Material mat) {
        int c = 0;
        for (ItemStack stack : inv.getContents()) {
            if (stack != null && stack.getType() == mat) c += stack.getAmount();
        }
        return c;
    }

    private static void removeFromChest(Inventory inv, Material mat, int amount) {
        int left = amount;
        for (int i = 0; i < inv.getSize() && left > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack == null || stack.getType() != mat) continue;
            int take = Math.min(left, stack.getAmount());
            left -= take;
            if (stack.getAmount() == take) inv.setItem(i, null);
            else stack.setAmount(stack.getAmount() - take);
        }
    }

    private static int countInInventory(Inventory inv, Material mat) {
        return countMaterial(inv, mat);
    }

    private static void removeFromInventory(Inventory inv, Material mat, int amount) {
        removeFromChest(inv, mat, amount);
    }

    private static int fitAmount(Inventory inv, Material mat, int wanted) {
        int fit = 0;
        for (int i = 0; i < inv.getSize() && fit < wanted; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack == null || stack.getType().isAir()) {
                fit += mat.getMaxStackSize();
                continue;
            }
            if (stack.getType() == mat && stack.getAmount() < stack.getMaxStackSize()) {
                fit += stack.getMaxStackSize() - stack.getAmount();
            }
        }
        return Math.min(fit, wanted);
    }

    private static int spaceInChest(Inventory inv, Material mat, int wanted) {
        return fitAmount(inv, mat, wanted);
    }

    private static String formatType(SignShop shop) {
        return shop.getType() == SignShopType.BUY ? "Compra" : "Venda";
    }

    private static String formatMaterial(Material mat) {
        return ItemDisplayNames.plain(mat);
    }

    public static void sendHelp(Player player) {
        player.sendMessage("§6§lLOJAS DE PLACA §7(/loja placa)");
        player.sendMessage("§71) Coloque um §fbaú§7 e uma §fplaca§7 nele.");
        player.sendMessage("§72) Escreva na placa (4 linhas):");
        player.sendMessage("§8  §f[LojaServer] §7(loja do spawn, staff)");
        player.sendMessage("§8  §f[Loja] §7(loja de jogador + estoque no baú)");
        player.sendMessage("§8  §fComprar §7ou §fVender");
        player.sendMessage("§8  §f<MATERIAL> §7(ex: DIAMOND, COBBLESTONE)");
        player.sendMessage("§8  §f<preço por unidade>");
        player.sendMessage("§7Comprar = jogador paga e recebe itens.");
        player.sendMessage("§7Vender = jogador entrega itens e recebe $.");
        player.sendMessage("§eClique direito §7= 1 un | §eShift + clique §7= 64 un");
        player.sendMessage("§7Lista completa: §fplugins/game/physical-shop.yml");
        if (player.hasPermission("game.shop.admin")) {
            player.sendMessage("§6Staff: monte a loja presencial no spawn com §f[LojaServer]§6.");
        }
    }
}
