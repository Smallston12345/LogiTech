package me.matl114.logitech.core.Cargo.CargoMachine;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import javax.annotation.Nonnull;
import me.matl114.logitech.Language;
import me.matl114.logitech.core.Cargo.Config.ChipControllable;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.UtilClass.CargoClass.CargoConfigs;
import me.matl114.logitech.utils.UtilClass.ItemClass.ItemCounter;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractSorter extends AbstractSyncTickCargo implements ChipControllable {

    public AbstractSorter(
            ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int divideAmount) {
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
                        "&7機器擁有%d個輸入槽位以及%d個輸入白名單槽".formatted(divideAmount, divideAmount),
                        "&7你將會在界面中看見用彩色玻璃板標出的槽位序號",
                        "&7白名單槽會對貨運的輸入流進行限制",
                        "&7其中,白名單槽不放物品代表對應槽位允許任意輸入",
                        "&7其中,白名單槽放物品代表對應槽位運行相同的物品的輸入",
                        "&ePS:網絡附屬和原版貨運會接受該限制"),
                null,
                AddUtils.getInfoShow(
                        "&f機制",
                        "&7機器會選中一個輸入槽",
                        "&7在公共ticker計數器<ticker>(mod32)=0時,機器一定會選擇第0槽位",
                        "&7每個tick機器將選中槽位的物品轉移至輸出槽",
                        "&7玩家可以使用%s控制機器選擇輸入槽".formatted(Language.get("Items.CHIP.Name")),
                        "&e當機器讀取到01碼中的1時會選擇至下一個輸入槽位,若已經為最後一槽則循環至第0槽",
                        "&e當機器讀取到01碼中的0時會保持選擇當前槽位不動"),
                null));
    }

    public void newMenuInstance(@Nonnull BlockMenu blockMenu, @Nonnull Block block) {
        blockMenu.addMenuOpeningHandler(player -> {
            updateMenu(blockMenu, block, Settings.INIT);
        });
        blockMenu.addMenuCloseHandler(player -> {
            updateMenu(blockMenu, block, Settings.INIT);
        });
        updateMenu(blockMenu, block, Settings.INIT);
        blockMenu.replaceExistingItem(getInfoSlot(), getInfoOffItem(0));
        blockMenu.addMenuClickHandler(getInfoSlot(), ChestMenuUtils.getEmptyClickHandler());
    }

    public void updateMenu(BlockMenu blockMenu, Block block, Settings mod) {
        loadChipCommand(blockMenu);
    }

    public abstract int getInfoSlot();

    public ItemStack getInfoItem(int code, int tickCount) {
        int ticks = tickCount % 32;
        return new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&a信息",
                "&7下方為貨運輸出槽",
                "&7上方為芯片槽",
                AddUtils.concat("&7當前讀取編碼位數: ", String.valueOf(ticks)),
                AddUtils.concat(
                        "&7當前選擇的槽位數: ", String.valueOf(MathUtils.bitCount(code, ticks + 1) % getInputSlots().length)));
    }

    public ItemStack getInfoOffItem(int tickCount) {
        int ticks = tickCount % 32;
        return new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&a信息",
                "&7下方為貨運輸出槽",
                "&7上方為芯片槽",
                AddUtils.concat("&7當前讀取編碼位數: ", String.valueOf(ticks)),
                "&e待機中,芯片不存在或已損壞");
    }

    public abstract int[] getInputWLSlot();

    protected int[] blankSlot = new int[0];

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
        if (inv.hasViewer()) inv.replaceExistingItem(getInfoSlot(), getInfoItem(code, synTickCount));
        int[] inputs = getInputSlots();
        int slot = inputs[MathUtils.bitCount(code, (synTickCount % 32) + 1) % inputs.length];
        TransportUtils.transportItem(
                inv,
                new int[] {slot},
                inv,
                getOutputSlots(),
                CargoConfigs.getDefaultConfig(),
                false,
                null,
                CraftUtils.getpusher);
    }

    public int[] getSlotsAccessedByItemTransportPlus(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
        if (flow == ItemTransportFlow.WITHDRAW) {
            return getOutputSlots();
        } else {
            if (item == null || item.getType().isAir()) {
                return getInputSlots();
            }
            int[] slots = getInputSlots();
            int[] wlslots = getInputWLSlot();
            int len = slots.length;
            ItemStack st;
            ItemCounter ct = CraftUtils.getConsumer(item);
            for (int i = 0; i < len; i++) {
                st = menu.getItemInSlot(wlslots[i]);
                if (st == null || (CraftUtils.matchItemStack(st, ct, false))) {
                    return new int[] {slots[i]};
                }
            }
            return blankSlot;
        }
    }

    public void preRegister() {
        super.preRegister();
        // shared ticker
        this.addItemHandler((BlockTicker) CHIP_SYNC);
    }

    public void onBreak(BlockBreakEvent e, BlockMenu inv) {
        super.onBreak(e, inv);
        if (inv != null) {
            inv.dropItems(inv.getLocation(), getInputWLSlot());
            inv.dropItems(inv.getLocation(), getChipSlot());
        }
    }
}
