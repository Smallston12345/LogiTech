package me.matl114.logitech.core.Cargo.WorkBench;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.concurrent.CompletableFuture;
import me.matl114.logitech.Language;
import me.matl114.logitech.core.Cargo.CargoMachine.AbstractSyncTickCargo;
import me.matl114.logitech.core.Cargo.Config.ChipCardCode;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.MathUtils;
import me.matl114.logitech.utils.Settings;
import me.matl114.logitech.utils.Utils;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ChipCopier extends AbstractSyncTickCargo {
    protected final int[] BORDER = new int[] {3, 4, 5, 12, 14, 21, 22, 23};
    protected final int[] INPUT_BORDER = new int[] {0, 2, 9, 11, 18, 19, 20};
    protected final int[] OUTPUT_BORDER = new int[] {6, 8, 15, 17, 24, 25, 26};
    protected final int[] SAMPLE_SLOT = new int[] {10};

    protected final int[] INFO_SLOT = new int[] {1, 7};
    protected final ItemStack[] INFO_ITEMS = new ItemStack[] {
        new CustomItemStack(Material.GREEN_STAINED_GLASS_PANE, "&6樣板芯片槽"),
        new CustomItemStack(Material.GREEN_STAINED_GLASS_PANE, "&a輸入/輸出槽")
    };
    protected final int STATUS_SLOT = 13;
    protected int[] SLOTS = {16};

    public int[] getInputSlots() {
        return SLOTS;
    }

    public int[] getOutputSlots() {
        return SLOTS;
    }

    public ChipCopier(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
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
                        "&7機器擁有一個樣板芯片槽和一個輸入/輸出槽",
                        "&7機器會嘗試讀取樣本槽中的01碼",
                        "&7機器會從輸入/輸出槽中讀取目標芯片",
                        "&7機器會嘗試復刻當前讀取的數據到目標芯片的對應01位上",
                        "&a機器可以同時向一組芯片拷貝數據"),
                null));
    }

    public ItemStack getInfoItem(int tickCount) {
        int ticks = tickCount % 32;
        return new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE, "&a信息", AddUtils.concat("&7當前讀取編碼位數: ", String.valueOf(ticks)));
    }

    public void constructMenu(BlockMenuPreset preset) {
        int[] border = BORDER;
        int len = border.length;
        for (int i = 0; i < len; i++) {
            preset.addItem(border[i], ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        border = INPUT_BORDER;
        len = border.length;
        for (int i = 0; i < len; i++) {
            preset.addItem(border[i], ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }
        border = OUTPUT_BORDER;
        len = border.length;
        for (int i = 0; i < len; i++) {
            preset.addItem(border[i], ChestMenuUtils.getOutputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }
        border = INFO_SLOT;
        len = border.length;
        for (int i = 0; i < len; i++) {
            preset.addItem(border[i], INFO_ITEMS[i], ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addItem(STATUS_SLOT, getInfoItem(0), ChestMenuUtils.getEmptyClickHandler());
    }

    public void newMenuInstance(BlockMenu inv, Block b) {}

    public void updateMenu(BlockMenu inv, Block b, Settings mod) {}

    public void syncTick(Block b, BlockMenu inv, SlimefunBlockData data, int synTickCount) {
        if (inv.hasViewer()) {
            inv.replaceExistingItem(STATUS_SLOT, getInfoItem(synTickCount % 32));
            updateMenu(inv, b, Settings.RUN);
        }
        ItemStack it1 = inv.getItemInSlot(SAMPLE_SLOT[0]);
        if (it1 == null) return;
        ItemStack it2 = inv.getItemInSlot(SLOTS[0]);
        if (it2 == null) return;
        ItemMeta meta1 = it1.getItemMeta();
        if (meta1 != null && ChipCardCode.isConfig(meta1)) {
            ItemMeta meta2 = it2.getItemMeta();
            final int syncTick = synTickCount % 32;
            // 獲取meta2副本之後直接開啓異步計算
            CompletableFuture.runAsync(() -> {
                if (meta2 != null && ChipCardCode.isConfig(meta2)) {
                    int code1 = ChipCardCode.getConfig(meta1);
                    int code2 = ChipCardCode.getConfig(meta2);
                    int pos = MathUtils.getBitPos(syncTick);
                    int pos2 = code1 & pos;
                    int tar;
                    if (pos2 == 0) {
                        tar = code2 & (~pos);
                    } else {
                        tar = code2 | pos;
                    }
                    if (tar != code2) {
                        ChipCardCode.setConfigCode(meta2, tar);
                        ChipCardCode.updateConfigCodeDisplay(meta2, tar);
                        // 防止異步導致數量不符 如果有數量更改則直接刪除
                        it2.setItemMeta(meta2);
                    }
                }
            });
        }
    }

    public void onBreak(BlockBreakEvent e, BlockMenu inv) {
        super.onBreak(e, inv);
        if (inv != null) {
            inv.dropItems(inv.getLocation(), SAMPLE_SLOT);
        }
    }

    public void preRegister() {
        super.preRegister();
        this.addItemHandler((BlockTicker) CHIP_SYNC);
    }
}
