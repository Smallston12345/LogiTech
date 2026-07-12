package me.matl114.logitech.core.Cargo.CargoMachine;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.function.IntFunction;
import me.matl114.logitech.core.Machines.Abstracts.AbstractMachine;
import me.matl114.logitech.core.Registries.FinalFeature;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.CraftUtils;
import me.matl114.logitech.utils.Settings;
import me.matl114.logitech.utils.UtilClass.ItemClass.ItemPusher;
import me.matl114.logitech.utils.Utils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class StorageOperator extends AbstractMachine {
    protected final int[] BORDER = new int[] {
        0, 1, 3, 4, 5, 7, 8, 9, 10, 12, 14, 16, 17, 18, 19, 21, 22, 23, 25, 26, 27, 28, 30, 31, 32, 34, 35, 36, 37, 39,
        40, 41, 43, 44
    };
    protected final int[] INPUT_BORDER = new int[] {20, 24};
    protected final int[] INPUT_SLOT = new int[] {11, 15};
    protected final int[] OUTPUT_SLOT = new int[] {29, 33};
    protected final int[] INFO_SLOTS = new int[] {2, 6, 38, 42, 13};

    public int[] getInputSlots() {
        return INPUT_SLOT;
    }

    public int[] getOutputSlots() {
        return OUTPUT_SLOT;
    }

    protected final ItemStack[] INFO_ITEMS = new ItemStack[] {
        new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&a輸入槽1"),
        new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&a輸入槽2"),
        new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&a輸出槽1"),
        new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&a輸出槽2"),
        new CustomItemStack(Material.BOOK, "&f機制", "&7將存儲類物品放入兩個輸入槽進行轉移", "&7轉移結果將輸入至輸出槽", "&7操作之前請保證輸出槽內不存在物品")
    };
    protected final int BOTTON_SLOT = 22;
    protected final ItemStack BOTTON_ITEM = new CustomItemStack(
            Material.GREEN_STAINED_GLASS_PANE,
            "&a操作按鈕",
            "&7點擊按鈕 將會試圖將輸入槽2中存儲的物品轉移至輸入槽1中的存儲",
            "&7shift點擊按鈕 將會試圖將輸入槽1,2中的存儲物品數量調至其平均數",
            "&7轉移的結果數量會受到存儲的最大數量限制",
            "&7轉移之後,輸入槽1,2中的存儲將會分別輸出至輸出槽1,2");

    public StorageOperator(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe, 0, 0);
        setDisplayRecipes(Utils.list(AddUtils.getInfoShow(
                "&f機制",
                "&7將存儲類物品放入兩個輸入槽進行轉移",
                "&7點擊操作按鈕 將會試圖將輸入的第二個個存儲的物品轉移至第一個中的存儲",
                "&7shift點擊操作按鈕 將會試圖將兩個存儲內的存儲物品數量調至其平均數",
                "&7轉移的結果數量會受到存儲的最大數量限制",
                "&7轉移結果將輸入至輸出槽")));
    }

    public void constructMenu(BlockMenuPreset preset) {
        int[] slot = BORDER;
        int len = slot.length;
        for (int i = 0; i < len; i++) {
            preset.addItem(slot[i], ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        slot = INPUT_BORDER;
        len = slot.length;
        for (int i = 0; i < len; ++i) {
            preset.addItem(slot[i], ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }
        slot = INFO_SLOTS;
        len = slot.length;
        for (int i = 0; i < len; ++i) {
            preset.addItem(slot[i], INFO_ITEMS[i], ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addItem(BOTTON_SLOT, BOTTON_ITEM);
    }

    public void newMenuInstance(BlockMenu inv, Block block) {

        inv.addMenuClickHandler(BOTTON_SLOT, ((player, i, itemStack, clickAction) -> {
            ItemStack it1, it2;
            it1 = inv.getItemInSlot(INPUT_SLOT[0]);
            it2 = inv.getItemInSlot(INPUT_SLOT[1]);
            if (it1 == null || it2 == null || it1.getAmount() != 1 || it2.getAmount() != 1) {
                AddUtils.sendMessage(player, "&c存儲不能為空或者超過2個");
                return false;
            }
            if (inv.getItemInSlot(OUTPUT_SLOT[0]) != null || inv.getItemInSlot(OUTPUT_SLOT[1]) != null) {
                AddUtils.sendMessage(player, "&c輸出槽必須為空!");
                return false;
            }
            IntFunction<ItemPusher> pusherIntFunction = FinalFeature.STORAGE_READER.getMenuInstance(
                    Settings.INPUT, inv, new ItemStack[] {it1, it2}, INPUT_SLOT);
            ItemPusher pusher1 =
                    pusherIntFunction.apply(0); // FinalFeature.STORAGE_READER.get(Settings.INPUT,it1,INPUT_SLOT[0]);
            if (pusher1 == null) {
                AddUtils.sendMessage(player, "&c輸入槽1的物品為無效存儲");
                return false;
            }
            ItemPusher pusher2 =
                    pusherIntFunction.apply(1); // FinalFeature.STORAGE_READER.get(Settings.INPUT,it2,INPUT_SLOT[1]);
            if (pusher2 == null) {
                AddUtils.sendMessage(player, "&c輸入槽2的物品為無效存儲");
                return false;
            }
            if (!CraftUtils.matchItemCounter(pusher1, pusher2, true)) {
                AddUtils.sendMessage(player, "&c存儲內的物品類型不匹配!");
                return false;
            }
            if (clickAction.isShiftClicked()) {
                long amount1 = pusher1.getAmountLong();
                long amount2 = pusher2.getAmountLong();
                long middleAmount = (amount1 / 2) + (amount2 / 2);
                long diff1 = middleAmount - amount1;
                if (diff1 != 0) {
                    if (diff1 > 0) {
                        diff1 = Math.min(diff1, pusher1.getMaxStackCnt() - pusher1.getAmountLong());
                    } else {
                        diff1 = Math.max(diff1, pusher2.getAmountLong() - pusher2.getMaxStackCnt());
                    }
                    pusher1.addAmount(diff1);
                    pusher2.addAmount(-diff1);
                    pusher1.updateMenu(inv);
                    pusher2.updateMenu(inv);
                }
            } else {
                pusher1.grab(pusher2);
                pusher1.updateMenu(inv);
                pusher2.updateMenu(inv);
            }
            it1 = inv.getItemInSlot(INPUT_SLOT[0]);
            inv.replaceExistingItem(INPUT_SLOT[0], null);
            inv.replaceExistingItem(OUTPUT_SLOT[0], it1);
            it2 = inv.getItemInSlot(INPUT_SLOT[1]);
            inv.replaceExistingItem(INPUT_SLOT[1], null);
            inv.replaceExistingItem(OUTPUT_SLOT[1], it2);
            return false;
        }));
    }

    public void registerTick(SlimefunItem it) {
        // doing nothing
    }

    public void process(Block b, BlockMenu preset, SlimefunBlockData data) {
        // doing nothings
    }
}
