package me.matl114.logitech.core.Cargo.Transportation;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.List;
import me.matl114.logitech.Language;
import me.matl114.logitech.core.Cargo.CargoMachine.AbstractSyncTickCargo;
import me.matl114.logitech.core.Cargo.Config.ChipControllable;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.UtilClass.TickerClass.SyncBlockTick;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class ChipAdjacentCargo extends AdjacentCargo implements ChipControllable, SyncBlockTick.SyncTickers {
    public ChipAdjacentCargo(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            List<ItemStack> displayList) {
        super(itemGroup, item, recipeType, recipe, displayList);
        setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow(
                        "&f機制",
                        "&7這是一個同步貨運機器,屬於[CHIP_SYNC]組",
                        "&7位於同組的機器將共享同一個ticker計數器",
                        "&7出於對時序安全性的保障,&c每1s該計數器+1並讓機器工作一次",
                        "&7機器用該計數器數值順序讀取%s中記錄的01碼".formatted(Language.get("Items.CHIP.Name")),
                        "&7具體地,機器會讀取01碼的第<ticker>(mod32)位",
                        "&7機器根據讀取結果是0/1會執行相應的行為"),
                null,
                AddUtils.getInfoShow(
                        "&f機制",
                        "&7本機器可以選擇與其相鄰的方塊參與傳輸",
                        "&7本機器接受芯片資訊的調控",
                        "&7當讀取結果為1時,執行運輸",
                        "&7當讀取結果為0時,待機",
                        "&c資訊將實時生效"),
                null));
    }

    public void constructMenu(BlockMenuPreset preset) {
        int[] border = BORDER;
        int len = border.length;
        for (int i = 0; i < len; ++i) {
            preset.addItem(border[i], ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addItem(getInfoSlot(), getInfoOffItem(0), ChestMenuUtils.getEmptyClickHandler());
    }

    public void newMenuInstance(BlockMenu inv, Block b) {
        inv.addMenuOpeningHandler((player -> {
            updateMenu(inv, b, Settings.RUN);
        }));
        inv.addMenuCloseHandler(player -> {
            updateMenu(inv, b, Settings.RUN);
        });
        inv.addMenuClickHandler(DIRECTION_SLOT[0], getDirectionHandler("from_dir", inv));
        inv.addMenuClickHandler(DIRECTION_SLOT[1], getDirectionHandler("to_dir", inv));
        updateMenu(inv, b, Settings.INIT);
    }

    public void updateMenu(BlockMenu inv, Block b, Settings mod) {
        loadConfig(inv, b);
        updateDirectionSlots("from_dir", inv, DIRECTION_SLOT[0]);
        updateDirectionSlots("to_dir", inv, DIRECTION_SLOT[1]);
        loadChipCommand(inv);
    }

    public int getInfoSlot() {
        return 4;
    }

    public int getConfigSlot() {

        return 3;
    }

    public int getChipSlot() {
        return 5;
    }

    public ItemStack getInfoItem(int code, int tickCount, int bit) {
        int ticks = tickCount % 32;
        return new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&a資訊",
                "&7左側為貨運配置卡槽",
                "&7右側為芯片槽",
                "&7在左側配置源方方塊鄰接方向",
                "&7在右側配置目標方塊鄰接方向",
                AddUtils.concat("&7當前讀取編碼位數: ", String.valueOf(ticks)),
                AddUtils.concat("&7當前運行狀態: ", bit == 1 ? "運行" : "待機"));
    }

    public ItemStack getInfoOffItem(int tickCount) {
        int ticks = tickCount % 32;
        return new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&a資訊",
                "&7左側為貨運配置卡槽",
                "&7右側為芯片槽",
                "&7在左側配置源方方塊鄰接方向",
                "&7在右側配置目標方塊鄰接方向",
                AddUtils.concat("&7當前運行狀態: ", String.valueOf(ticks)),
                "&e待機中,芯片不存在或已損壞");
    }

    public void syncTick(Block b, BlockMenu inv, SlimefunBlockData data, int synTickCount) {
        if (inv.hasViewer()) updateMenu(inv, b, Settings.RUN);
        String cmd = data.getData(CCODEKEY);
        int code;
        if (cmd == null || cmd.startsWith("nu")) {
            if (cmd == null) data.setData(CCODEKEY, "nu");
            if (inv.hasViewer()) inv.replaceExistingItem(getInfoSlot(), getInfoOffItem(synTickCount));
            return;
        }
        try {
            code = Integer.parseInt(cmd);
        } catch (Throwable e) {
            data.setData(CCODEKEY, "nu");
            if (inv.hasViewer()) inv.replaceExistingItem(getInfoSlot(), getInfoOffItem(synTickCount));
            return;
        }
        synTickCount = synTickCount % 32;
        int bit = MathUtils.getBit(code, synTickCount);
        if (inv.hasViewer()) inv.replaceExistingItem(getInfoSlot(), getInfoItem(code, synTickCount, bit));
        if (bit == 1) {
            super.tick(b, inv, data, synTickCount);
        }
    }

    public void preRegister() {
        super.preRegister();
        // shared ticker
        this.addItemHandler((BlockTicker) AbstractSyncTickCargo.CHIP_SYNC);
    }

    public void onBreak(BlockBreakEvent e, BlockMenu inv) {
        super.onBreak(e, inv);
        if (inv != null) {
            inv.dropItems(inv.getLocation(), getChipSlot());
        }
    }
}
