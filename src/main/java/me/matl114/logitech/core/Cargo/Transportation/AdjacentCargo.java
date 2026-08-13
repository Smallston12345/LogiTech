package me.matl114.logitech.core.Cargo.Transportation;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.HashSet;
import java.util.List;
import me.matl114.logitech.Language;
import me.matl114.logitech.core.Cargo.AbstractCargo;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.UtilClass.CargoClass.Directions;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class AdjacentCargo extends AbstractCargo {
    protected final int[] BORDER = new int[] {0, 1, 2, 6, 7, 8, 9, 11, 15, 17, 18, 19, 20, 24, 25, 26};
    protected final int[] BWSLOT = new int[] {12, 13, 14, 21, 22, 23};
    protected final int[] INFO_SLOT = new int[] {3, 5};
    protected final ItemStack[] INFO_ITEM = new ItemStack[] {
        new CustomItemStack(
                Material.ORANGE_STAINED_GLASS_PANE,
                "&b機制",
                "&7在左側配置源方方塊鄰接方向",
                "&7在中間的槽位插入[%s]".formatted(Language.get("Items.CARGO_CONFIG.Name")),
                "&7在下方放入黑/白名單物品",
                "&e機器將從源方方塊向目標方塊進行傳輸",
                "&c警告:當你設置兩方塊相同時,請不要讓他們操作同樣的槽位,否則後果自負"),
        new CustomItemStack(
                Material.ORANGE_STAINED_GLASS_PANE,
                "&b機制",
                "&7在右側配置目標方塊鄰接方向",
                "&7在中間的槽位插入[%s]".formatted(Language.get("Items.CARGO_CONFIG.Name")),
                "&7在下方放入黑/白名單物品",
                "&e機器將從源方方塊向目標方塊進行傳輸",
                "&c警告:當你設置兩方塊相同時,請不要讓他們操作同樣的槽位,否則後果自負")
    };
    protected final int[] DIRECTION_SLOT = new int[] {10, 16};

    public int[] getBWListSlot() {
        return BWSLOT;
    }

    public int[] getInputSlots() {
        return new int[0];
    }

    public int[] getOutputSlots() {
        return new int[0];
    }

    public int getConfigSlot() {
        return 4;
    }

    public AdjacentCargo(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            List<ItemStack> displayList) {
        super(itemGroup, item, recipeType, recipe, displayList);
        setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow(
                        "&f機制",
                        "&7這是一個普通貨運機器",
                        "&7普通貨運機器的行為會包括若干對源方塊和目標方塊",
                        "&7普通貨運機器會進行從源方塊到目標方塊的物流傳輸",
                        "&7普通貨運機器支援較大規模的傳輸量",
                        "&7但是相應的,他會忽視機器對物品的輸入槽位限制"),
                null,
                AddUtils.getInfoShow("&f機制", "&7本機器可以選擇與其相鄰的方塊參與傳輸"),
                null));
    }

    public void constructMenu(BlockMenuPreset preset) {
        int[] border = BORDER;
        int len = border.length;
        for (int i = 0; i < len; ++i) {
            preset.addItem(border[i], ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        border = INFO_SLOT;
        len = border.length;
        for (int i = 0; i < len; ++i) {
            preset.addItem(INFO_SLOT[i], INFO_ITEM[i], ChestMenuUtils.getEmptyClickHandler());
        }
    }

    protected final String[] savedKeys = new String[] {"from_dir", "to_dir"};

    public String[] getSaveKeys() {
        return savedKeys;
    }

    public int[] getDirectionSlots() {
        return DIRECTION_SLOT;
    }

    public void newMenuInstance(BlockMenu inv, Block b) {
        inv.addMenuOpeningHandler((player -> {
            updateMenu(inv, b, Settings.RUN);
        }));
        inv.addMenuCloseHandler(player -> {
            updateMenu(inv, b, Settings.RUN);
        });
        inv.addMenuClickHandler(DIRECTION_SLOT[0], getDirectionHandler(0, inv));
        inv.addMenuClickHandler(DIRECTION_SLOT[1], getDirectionHandler(1, inv));
        updateMenu(inv, b, Settings.INIT);
    }

    public void updateMenu(BlockMenu inv, Block b, Settings mod) {
        loadConfig(inv, b);
        updateDirectionSlots(0, inv);
        updateDirectionSlots(1, inv);
    }

    protected boolean transportSmarter = false;

    public void cargoTask(Block b, BlockMenu menu, SlimefunBlockData data, int configCode) {
        Location loc = menu.getLocation();
        Directions from_dir = getDirection(0, data);
        Directions to_dir = getDirection(1, data);
        if (from_dir != Directions.NONE && to_dir != Directions.NONE) {
            int[] bwslots = getBWListSlot();
            HashSet<ItemStack> bwset = new HashSet<>();
            ItemStack it;
            for (int i = 0; i < bwslots.length; ++i) {
                it = menu.getItemInSlot(bwslots[i]);
                if (it != null) {
                    bwset.add(it);
                }
            }
            ContainerUtils.transferWithContainer(
                    from_dir.relate(loc), to_dir.relate(loc), configCode, bwset, transportSmarter);
        }
    }
}
