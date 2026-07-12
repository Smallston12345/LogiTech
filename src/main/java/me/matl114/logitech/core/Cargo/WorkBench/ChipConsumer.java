package me.matl114.logitech.core.Cargo.WorkBench;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.matl114.logitech.Language;
import me.matl114.logitech.core.AddItem;
import me.matl114.logitech.core.Cargo.Config.ChipCardCode;
import me.matl114.logitech.core.Machines.Abstracts.AbstractMachine;
import me.matl114.logitech.manager.Schedules;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.CraftUtils;
import me.matl114.logitech.utils.UtilClass.ItemClass.ItemCounter;
import me.matl114.logitech.utils.Utils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ChipConsumer extends AbstractMachine {
    protected int[] BORDER = new int[] {0, 2, 3, 5, 6, 8, 9, 11, 12, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
    protected int[] INPUT_SLOT = new int[] {10, 13};
    protected int[] OUTPUT_SLOT = new int[] {16};

    public int[] getInputSlots() {
        return INPUT_SLOT;
    }

    public int[] getOutputSlots() {
        return OUTPUT_SLOT;
    }

    protected int[] INFO_SLOT = new int[] {1, 4, 7};
    protected ItemStack[] INFO_ITEM = new ItemStack[] {
        new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&6芯片輸入槽", "&7將待操作的芯片放入該槽"),
        new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, "&6邏輯物質輸入槽", "&7將位運算對應的物品放入該槽"),
        new CustomItemStack(Material.GREEN_STAINED_GLASS_PANE, "&a輸出槽", "&7只有輸入槽為空時才會輸出")
    };

    public ChipConsumer(
            ItemGroup category,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int energybuffer,
            int energyConsumption) {
        super(category, item, recipeType, recipe, energybuffer, energyConsumption);
        setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow("&f機制", "&7機器可以進行取反,左移一位,右移一位 三種位運算", "&7將所需物品插入指定槽位即可消耗電力運算"), null,
                AddUtils.getInfoShow(
                                "&f取反",
                                "&7插入%s進行取反操作".formatted(Language.get("Items.NOLOGIC.Name")),
                                "&7運算規則:",
                                "&7依次對輸入芯片的01位進行'~'運算",
                                "&8⇨ &7~0=1",
                                "&8⇨ &7~1=0"),
                        AddItem.NOLOGIC,
                AddUtils.getInfoShow(
                                "&f左移",
                                "&7插入%s進行左移操作".formatted(Language.get("Items.EXISTE.Name")),
                                "&7運算規則:",
                                "&7對01碼的首位加入0,丟棄最末尾"),
                        AddItem.EXISTE,
                AddUtils.getInfoShow(
                                "&f右移",
                                "&7插入%s進行右移操作".formatted(Language.get("Items.UNIQUE.Name")),
                                "&7運算規則:",
                                "&7當最末位是1時,向01碼最末尾部加1,並丟掉首位",
                                "&7當最末位是0時,向01碼最末尾部加0,並丟掉首位"),
                        AddItem.UNIQUE));
    }

    protected final ItemCounter[] MATCH_ITEM = new ItemCounter[] {
        CraftUtils.getConsumer(AddItem.NOLOGIC),
        CraftUtils.getConsumer(AddItem.EXISTE),
        CraftUtils.getConsumer(AddItem.UNIQUE),
    };

    protected Material ChipMaterial = AddItem.CHIP.getType();

    public void constructMenu(BlockMenuPreset preset) {
        int[] border = BORDER;
        int len = border.length;
        for (int i = 0; i < len; i++) {
            preset.addItem(border[i], ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        border = INFO_SLOT;
        len = border.length;
        for (int i = 0; i < len; i++) {
            preset.addItem(INFO_SLOT[i], INFO_ITEM[i], ChestMenuUtils.getEmptyClickHandler());
        }
    }

    public void process(Block b, BlockMenu inv, SlimefunBlockData data) {
        ItemStack it2 = inv.getItemInSlot(INPUT_SLOT[1]);
        ItemStack it = inv.getItemInSlot(INPUT_SLOT[0]);
        ItemStack it3 = inv.getItemInSlot(OUTPUT_SLOT[0]);
        if (it2 == null || it == null || it3 != null) {
            return;
        }
        int index = -1;
        for (index = 0; index < 3; index++) {
            if (CraftUtils.matchItemStack(it2, MATCH_ITEM[index], false)) {
                break;
            }
        }
        if (index >= 3) {
            return;
        }
        ItemMeta meta = it.getItemMeta();
        if (ChipCardCode.isConfig(meta)) {
            it.setAmount(it.getAmount() - 1);
            it2.setAmount(it2.getAmount() - 1);
            final int indexer = index;
            progressorCost(b, inv);
            Schedules.launchSchedules(
                    () -> {
                        ItemStack itout = AddItem.CHIP.clone();
                        int code = ChipCardCode.getConfig(meta);
                        switch (indexer) {
                            case 0:
                                code = ~code;
                                break;
                            case 1:
                                code = code << 1;
                                break;
                            case 2:
                                code = code >> 1;
                                break;
                        }
                        itout.setItemMeta(ChipCardCode.getCard(code));
                        if (inv.getItemInSlot(OUTPUT_SLOT[0]) == null) {
                            inv.replaceExistingItem(OUTPUT_SLOT[0], itout, false);
                        }
                    },
                    0,
                    false,
                    0);
        }
    }

    int[] CHIP_SLOT = new int[] {
        INPUT_SLOT[0],
    };
    int[] NOCHIP_SLOT = new int[] {
        INPUT_SLOT[1],
    };

    @Override
    public int[] getSlotsAccessedByItemTransportPlus(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
        if (flow == ItemTransportFlow.WITHDRAW) return getOutputSlots();
        if (item == null || item.getType().isAir()) {
            return getInputSlots();
        }
        if (item.getType() == ChipMaterial) {
            return CHIP_SLOT;
        }
        return NOCHIP_SLOT;
    }
}
