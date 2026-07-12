package me.matl114.logitech.core.Machines.Electrics;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import me.matl114.logitech.core.AddItem;
import me.matl114.logitech.core.Interface.MenuTogglableBlock;
import me.matl114.logitech.core.Machines.Abstracts.AbstractEnergyMachine;
import me.matl114.logitech.core.Machines.Abstracts.AbstractEnergyProvider;
import me.matl114.logitech.core.Registries.FinalFeature;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.UtilClass.ItemClass.ItemPusher;
import me.matl114.logitech.utils.UtilClass.ItemClass.ItemPusherProvider;
import me.matl114.logitech.utils.UtilClass.MenuClass.DataMenuClickHandler;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class EnergyAmplifier extends AbstractEnergyProvider implements MenuTogglableBlock {
    protected final int[] NULL_SLOT = new int[0];

    public int[] getInputSlots() {
        return NULL_SLOT;
    }

    public int[] getOutputSlots() {
        return NULL_SLOT;
    }

    protected final int[] BORDER =
            new int[] {0, 1, 2, 3, 5, 6, 7, 9, 10, 11, 12, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
    protected final int RUNWHENFULL_SLOT = 8;
    protected final int STATUS_SLOT = 4;
    protected final int MACHINE_SLOT = 13;
    protected final ItemStack STATUS_ITEM_OFF = new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&c當前狀態: 未發電");

    protected ItemPusherProvider MACHINE_PROVIDER = FinalFeature.STORAGE_READER;
    protected double multiply;
    protected final ItemStack RUNWHENFULL_OFF = new CustomItemStack(
            Material.RED_STAINED_GLASS_PANE, "&6過載模式: &c關", "&7點擊切換過載模式", "&7當過載模式啓用時候,即使發電機存電量已滿仍舊會發電");
    protected final ItemStack RUNWHENFULL_ON = new CustomItemStack(
            Material.GREEN_STAINED_GLASS_PANE, "&6過載模式: &a開", "&7點擊切換過載模式", "&7當過載模式啓用時候,即使發電機存電量已滿仍舊會發電");

    public EnergyAmplifier(
            ItemGroup category,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int energyBuffer,
            double multiply) {
        super(category, item, recipeType, recipe, energyBuffer, 1);
        this.multiply = multiply;
        this.setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow(
                        "&f機制 - &c終極堆疊發電",
                        "&7該機器可以模擬其上方的發電機發電",
                        "&7在槽位中放入與&e該機器方塊上方方塊&7的&a發電機&7類型相同的粘液物品",
                        "&a注意:你可以將發電機裝入存儲類物品放入該槽位",
                        "&a機器將讀取存儲中的發電機類型和數目",
                        "&7該機器將會發電<槽位中讀取的機器數>*%.2f*<上方環境中發電機的發電量>J".formatted(this.multiply),
                        "&a該槽位中支持的存儲類物品與&c\"終極合成\"&a特性中的存儲類物品相同"),
                null,
                AddUtils.getInfoShow("&f示例", "&7右側是一個示意圖,講述了該如何使用該機器"),
                AddUtils.resolveItem("ENERGY_REGULATOR"),
                AddItem.OPPO_GEN,
                item,
                null,
                AddUtils.getInfoShow("&f説明", "&7你將會看見下方的機器發電")));
    }

    public void constructMenu(BlockMenuPreset preset) {
        int[] border = BORDER;
        int len = border.length;
        for (int i = 0; i < len; ++i) {
            preset.addItem(border[i], ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addItem(STATUS_SLOT, getStatusItem(0, 0, 0, "0"), ChestMenuUtils.getEmptyClickHandler());
    }

    public ItemStack getStatusItem(long energyProvide, int charge, int stackNum, String workingAmount) {
        if (stackNum == 0) {
            return STATUS_ITEM_OFF;
        } else {
            if (charge > this.energybuffer) {
                return new CustomItemStack(
                        Material.ORANGE_STAINED_GLASS_PANE,
                        "&a當前狀態: 發電中",
                        "&7當前單體發電: 電力緩存已滿!",
                        "&7當前電機數目: %dx".formatted(stackNum),
                        "&7當前工作效率: %.3f".formatted(multiply),
                        "&7當前發電速率: 電力緩存已滿!",
                        "&7當前電力存儲: %dJ".formatted(charge));
            } else {
                return new CustomItemStack(
                        Material.ORANGE_STAINED_GLASS_PANE,
                        "&a當前狀態: 發電中",
                        "&7當前單體發電: %dJ/t".formatted(energyProvide),
                        "&7當前電機數目: %dx".formatted(stackNum),
                        "&7當前工作效率: %.3f".formatted(multiply),
                        "&7當前發電速率: %sJ/t".formatted(workingAmount),
                        "&7當前電力存儲: %dJ".formatted(charge));
            }
        }
    }

    public void registerTick(SlimefunItem item) {
        item.addItemHandler(new BlockTicker() {
            public boolean isSynchronized() {
                return false;
            }

            @ParametersAreNonnullByDefault
            public void tick(Block b, SlimefunItem item, SlimefunBlockData data) {
                BlockMenu menu = data.getBlockMenu();
                if (menu != null) {
                    if (menu.hasViewer()) {
                        updateMenu(menu, b, Settings.RUN);
                    }
                }
            }
        });
    }

    public int getGeneratedOutput(@Nonnull Location l, @Nonnull SlimefunBlockData data) {
        BlockMenu inv = data.getBlockMenu();
        if (inv == null) return 0;
        DataMenuClickHandler dh = getDataHolder(null, inv);
        Object sf = dh.getObject(0);
        if (sf instanceof EnergyNetProvider ep
                && !(sf instanceof EnergyAmplifier)
                && !(sf instanceof AbstractEnergyMachine)) {
            Location loc2 = l.clone().add(0, 1, 0);
            if (DataCache.getSfItem(loc2) == ep) {
                SlimefunBlockData data2 = DataCache.safeLoadBlock(loc2);
                if (data2 == null) {
                    return 0;
                }
                int charge = this.getCharge(l);
                int amplify = dh.getInt(0);
                if (!getStatus(inv)[0] && charge >= this.energybuffer) {
                    if (inv.hasViewer()) {
                        inv.replaceExistingItem(STATUS_SLOT, getStatusItem(0, charge, amplify, "0"));
                    }
                    return 0;
                } else {

                    long result = ep.getGeneratedOutput(loc2, data2);
                    long result0 = (long) (result * amplify * multiply);
                    int ret = MathUtils.fromLong(result0);
                    if (inv.hasViewer()) {
                        inv.replaceExistingItem(
                                STATUS_SLOT, getStatusItem(result, charge, amplify, String.valueOf(ret)));
                    }
                    return ret;
                }
            }
        }
        if (inv.hasViewer()) {
            int charge = this.getCharge(l);
            inv.replaceExistingItem(STATUS_SLOT, getStatusItem(0, charge, 0, "0"));
        }
        return 0;
    }

    public long getGeneratedOutputLong(@Nonnull Location l, @Nonnull SlimefunBlockData data) {
        BlockMenu inv = data.getBlockMenu();
        if (inv == null) return 0;
        DataMenuClickHandler dh = getDataHolder(null, inv);
        Object sf = dh.getObject(0);
        if (sf instanceof EnergyNetProvider ep
                && !(sf instanceof EnergyAmplifier)
                && !(sf instanceof AbstractEnergyMachine)) {
            Location loc2 = l.clone().add(0, 1, 0);
            if (DataCache.getSfItem(loc2) == ep) {
                SlimefunBlockData data2 = DataCache.safeLoadBlock(loc2);
                if (data2 == null) {
                    return 0;
                }
                int charge = this.getCharge(l);
                int amplify = dh.getInt(0);
                if (!getStatus(inv)[0] && charge >= this.energybuffer) {
                    if (inv.hasViewer()) {
                        inv.replaceExistingItem(STATUS_SLOT, getStatusItem(0, charge, amplify, "0"));
                    }
                    return 0;
                } else {

                    long result = ep.getGeneratedOutput(loc2, data2);
                    long result0 = (long) (result * ((long) amplify) * multiply);
                    if (inv.hasViewer()) {
                        inv.replaceExistingItem(
                                STATUS_SLOT, getStatusItem(result, charge, amplify, String.valueOf(result0)));
                    }
                    return result0;
                }
            }
        }
        if (inv.hasViewer()) {
            int charge = this.getCharge(l);
            inv.replaceExistingItem(STATUS_SLOT, getStatusItem(0, charge, 0, "0"));
        }
        return 0;
    }

    public final int DATA_SLOT = 0;

    public DataMenuClickHandler createDataHolder() {
        return new DataMenuClickHandler() {
            // 0 為 數量 1 為 電力
            Object it = null;
            int t;

            public void setInt(int val1, int val2) {
                t = val2;
            }

            public int getInt(int val1) {
                return t;
            }

            public void setObject(int val1, Object val2) {
                it = val2;
            }

            public Object getObject(int val2) {
                return it;
            }

            @Override
            public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                return false;
            }
        };
    }

    public DataMenuClickHandler getDataHolder(Block b, BlockMenu inv) {
        ChestMenu.MenuClickHandler handler = inv.getMenuClickHandler(DATA_SLOT);
        if (handler instanceof DataMenuClickHandler dh) {
            return dh;
        } else {
            DataMenuClickHandler dh = createDataHolder();
            inv.addMenuClickHandler(DATA_SLOT, dh);
            updateMenu(inv, b, Settings.RUN);
            return dh;
        }
    }

    public void newMenuInstance(BlockMenu inv, Block b) {
        inv.addMenuOpeningHandler(player -> {
            updateMenu(inv, b, Settings.RUN);
        });
        inv.addMenuCloseHandler(player -> {
            updateMenu(inv, b, Settings.RUN);
        });
        ItemStack stack = inv.getItemInSlot(RUNWHENFULL_SLOT);
        inv.addMenuClickHandler(RUNWHENFULL_SLOT, ((player, i, itemStack, clickAction) -> {
            boolean t = getStatus(inv)[0];
            toggleStatus(inv, !t);
            return false;
        }));

        if (stack == null || stack.getType() != Material.RED_STAINED_GLASS_PANE) {
            inv.replaceExistingItem(RUNWHENFULL_SLOT, RUNWHENFULL_ON);
        } else {
            inv.replaceExistingItem(RUNWHENFULL_SLOT, RUNWHENFULL_OFF);
        }
        updateMenu(inv, b, Settings.INIT);
    }

    public void updateMenu(BlockMenu inv, Block b, Settings mod) {
        ItemPusher pusher = MACHINE_PROVIDER.getPusher(Settings.INPUT, inv, MACHINE_SLOT);
        DataMenuClickHandler dh = getDataHolder(b, inv);
        if (pusher != null) {
            SlimefunItem sfit = SlimefunItem.getByItem(pusher.getItem());
            if (sfit != null) {
                if (sfit instanceof EnergyNetProvider ep) {
                    dh.setInt(0, pusher.getAmount());
                    dh.setObject(0, ep);
                    return;
                }
            }
        }
        dh.setInt(0, 0);
        dh.setObject(0, null);
    }

    public void onBreak(BlockBreakEvent e, BlockMenu inv) {
        super.onBreak(e, inv);
        if (inv != null) {
            inv.dropItems(inv.getLocation(), MACHINE_SLOT);
        }
    }

    @Override
    public boolean[] getStatus(BlockMenu inv) {
        ItemStack itemStack = inv.getItemInSlot(RUNWHENFULL_SLOT);
        if (itemStack == null || itemStack.getType() != Material.RED_STAINED_GLASS_PANE) {
            return new boolean[] {true};
        } else {
            return new boolean[] {false};
        }
    }

    @Override
    public void toggleStatus(BlockMenu inv, boolean... result) {
        if (result[0]) {
            inv.replaceExistingItem(RUNWHENFULL_SLOT, RUNWHENFULL_ON);
        } else {
            inv.replaceExistingItem(RUNWHENFULL_SLOT, RUNWHENFULL_OFF);
        }
    }
}
