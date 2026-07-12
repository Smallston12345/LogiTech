package me.matl114.logitech.core.Machines.SpecialMachines;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.ArrayList;
import java.util.Arrays;
import javax.annotation.Nonnull;
import me.matl114.logitech.Language;
import me.matl114.logitech.core.AddItem;
import me.matl114.logitech.core.Machines.Abstracts.AbstractProcessor;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.UtilClass.ItemClass.ItemConsumer;
import me.matl114.logitech.utils.UtilClass.RecipeClass.SimpleCraftingOperation;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class LogicReactor extends AbstractProcessor {
    protected final int[] BORDER = new int[] {
        0, 2, 4, 6, 8, 9, 10, 11, 12, 14, 15, 16, 17, 18, 19, 21, 23, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36,
        38, 40, 42, 44
    };

    protected final int[] INPUT_SLOT = new int[] {1, 3, 5, 7, 37, 39, 41, 43, 20};
    protected final int[] OUTPUT_SLOT = new int[] {24};
    protected final int[] PROCESSOR_SLOT = new int[] {22};
    protected final int[] BORDER_IN = new int[] {19, 12, 30, 14, 32, 25};
    protected final int[] BORDER_OUT = new int[] {10, 28, 21, 23, 16, 34};
    protected final int[] BOOL_SLOT = Arrays.copyOfRange(getInputSlots(), 0, 8);
    protected final int[] COMMON_SLOT = Arrays.copyOfRange(getInputSlots(), 8, getInputSlots().length);

    public int[] getInputSlots() {
        return INPUT_SLOT;
    }

    public int[] getOutputSlots() {
        return OUTPUT_SLOT;
    }

    protected final int processorTick;
    protected final int SWITCH_SLOT = 13;
    protected final ItemStack SWITCH_OFF = new CustomItemStack(Material.SOUL_TORCH, "&6反應堆運行狀態: &c關閉", "&7點擊開啓反應堆");
    protected final ItemStack SWITCH_ON =
            new CustomItemStack(Material.REDSTONE_TORCH, "&6反應堆運行狀態: &a開啓", "&7點擊關閉反應堆", "&c注意:關閉反應堆將丟失當前機器進度");

    public LogicReactor(
            ItemGroup category,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            Material progressItem,
            int tick) {
        super(category, item, recipeType, recipe, progressItem, 400, 4000, null);
        this.processorTick = tick;

        this.setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow(
                        "&f機制",
                        "&7該機器擁有四對布爾輸入槽,分別在&c同一列&7由相同顏色(藍色或者黃色)的玻璃板標出",
                        "&7這些槽位用於輸入布爾組件,即 %s 和 %s"
                                .formatted(Language.get("Items.TRUE_.Name"), Language.get("Items.FALSE_.Name")),
                        "&7只有這些槽位的物品滿足一定機制時,合成才會嘗試進行"),
                null,
                AddUtils.getInfoShow(
                        "&f機制",
                        "&7機器還有一個&c正常輸入槽&7和&e正常輸出槽&7,分別位於第三排的&e左側&7和&e右側空格,分別用於輸入所需的 [%s] 和輸出產物 "
                                .formatted(Language.get("Items.LOGIGATE.Name")),
                        "&7機器中間一個開關,用於調控機器是否會進行合成進程,當點擊關閉時,當前進程將被強制終止",
                        "&7當機器為開啓狀態且為空閒,輸出槽非滿,四對布爾輸入槽&c非空&7且滿足右側某個輸入條件時",
                        "&7才會嘗試&e消耗&7正常輸入槽內的 [%s] 進行合成".formatted(Language.get("Items.LOGIGATE.Name")),
                        "&7警告:當機器嘗試匹配輸入條件進行合成時,四對布爾輸入槽內的物品會被&c清空&7!",
                        "&7指示:當機器在合成進程中運行時,四對布爾輸入槽內的物品不會被&c清空!",
                        "&7指示:不過玩家使用普通貨運輸入時,布爾槽位只會被輸入布爾組件",
                        "&7指示:你可以關閉機器來停止布爾組件的消耗"),
                AddItem.LOGIGATE));

        this.machineRecipes = new ArrayList<>() {
            {
                add(MachineRecipeUtils.FromMachine(AddUtils.formatInfoMachineRecipe(
                        Utils.array(AddItem.LOGIC),
                        tick,
                        "&7當四對輸入槽物品均為同樣的布爾組件時",
                        "&7將會嘗試生成 %s".formatted(Language.get("Items.LOGIC.Name")))));
                add(MachineRecipeUtils.FromMachine(AddUtils.formatInfoMachineRecipe(
                        Utils.array(AddItem.NOLOGIC),
                        tick,
                        "&7當四對輸入槽物品均為相反的布爾組件時",
                        "&7將會嘗試生成 %s".formatted(Language.get("Items.NOLOGIC.Name")))));
                add(MachineRecipeUtils.FromMachine(AddUtils.formatInfoMachineRecipe(
                        Utils.array(AddItem.UNIQUE),
                        tick,
                        "&7當四對輸入槽物品中只有一對布爾組件相異",
                        "&7將會嘗試生成 %s".formatted(Language.get("Items.UNIQUE.Name")))));
                add(MachineRecipeUtils.FromMachine(AddUtils.formatInfoMachineRecipe(
                        Utils.array(AddItem.EXISTE),
                        tick,
                        "&7當前三個材料的生成條件均不滿足",
                        "&7將會嘗試生成 %s".formatted(Language.get("Items.EXISTE.Name")))));
            }
        };
    }

    public void constructMenu(BlockMenuPreset preset) {
        // 空白背景 禁止點擊
        int[] border = BORDER;
        int len = border.length;
        for (int var4 = 0; var4 < len; ++var4) {

            preset.addItem(border[var4], ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        // 輸入槽邊框
        border = BORDER_IN;
        len = border.length;
        for (int var4 = 0; var4 < len; ++var4) {

            preset.addItem(border[var4], ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }
        // 輸出槽邊框
        border = BORDER_OUT;
        len = border.length;
        for (int var4 = 0; var4 < len; ++var4) {

            preset.addItem(border[var4], ChestMenuUtils.getOutputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }
        // 空白邊框
        border = PROCESSOR_SLOT;
        len = border.length;
        for (int var4 = 0; var4 < len; ++var4) {
            preset.addItem(border[var4], MenuUtils.PROCESSOR_NULL, ChestMenuUtils.getEmptyClickHandler());
        }
    }

    public void newMenuInstance(@Nonnull BlockMenu inv, @Nonnull Block block) {
        if (inv.getItemInSlot(SWITCH_SLOT) == null) {
            inv.replaceExistingItem(SWITCH_SLOT, SWITCH_OFF);
        }
        inv.addMenuClickHandler(SWITCH_SLOT, ((player, i, itemStack, clickAction) -> {
            if (itemStack != null && itemStack.getType() == Material.SOUL_TORCH) {
                inv.replaceExistingItem(SWITCH_SLOT, SWITCH_ON);
            } else {
                inv.replaceExistingItem(SWITCH_SLOT, SWITCH_OFF);
                this.processor.endOperation(block);
            }
            return false;
        }));
    }

    public int checkLogic(BlockMenu inv, int[] inputs) {
        int code1 = 0;
        int code2 = 0;
        int cnt = 0;
        ItemStack it;
        for (int i = 0; i < 4; ++i) {
            it = inv.getItemInSlot(inputs[i]);
            if (it != null) {

                if (CraftUtils.matchItemStack(it, AddItem.TRUE_, false)) {
                    code1 = 2 * code1 + 1;
                    ++cnt;
                } else if (CraftUtils.matchItemStack(it, AddItem.FALSE_, false)) {
                    code1 = 2 * code1;
                } else {
                    cnt = -64;
                }
                it.setAmount(0);
            } else cnt = -64;
        }
        for (int i = 4; i < 8; ++i) {
            it = inv.getItemInSlot(inputs[i]);
            if (it != null) {
                if (CraftUtils.matchItemStack(it, AddItem.TRUE_, false)) {
                    code2 = 2 * code2 + 1;
                    ++cnt;
                } else if (CraftUtils.matchItemStack(it, AddItem.FALSE_, false)) {
                    code2 = 2 * code2;

                } else {

                    cnt = -64;
                }
                it.setAmount(0);
            } else cnt = -64;
        }
        if (cnt < 0) {
            return -1;
        }
        // 機制判斷
        if (code1 == code2) {
            return 0;
        } else {
            int code = code1 ^ code2;
            if (code == 15) { // 均不同
                return 1;
            } else if (code == 1 || code == 2 || code == 4 || code == 8) { // 僅一位
                return 2;
            } else return 3; // else
        }
    }

    public boolean conditionHandle(Block b, BlockMenu menu) {
        ItemStack switchItem = menu.getItemInSlot(SWITCH_SLOT);
        if (switchItem != null && switchItem.getType() == Material.SOUL_TORCH) {
            return false;
        } else return super.conditionHandle(b, menu);
    }

    protected Pair<MachineRecipe, ItemConsumer[]> findNextRecipe(BlockMenu inv, int[] inputs, int[] outputs) {
        ItemStack iv = inv.getItemInSlot(inputs[0]);
        ItemStack ov = inv.getItemInSlot(outputs[0]);
        if (iv != null
                && CraftUtils.matchItemStack(iv, AddItem.LOGIGATE, false)
                && (ov == null || ov.getAmount() < ov.getMaxStackSize())) {
            int code = checkLogic(inv, BOOL_SLOT);
            if (code == -1) {
                return null;
            } else {
                MachineRecipe result = getMachineRecipes().get(code);
                ItemConsumer[] outCon = CraftUtils.countOneOutput(inv, outputs, result);
                if (outCon == null) {
                    return null;
                }
                iv.setAmount(iv.getAmount() - 1);
                return new Pair<>(result, outCon);
            }
        }
        return null;
    }

    @Override
    public void process(Block b, BlockMenu inv, SlimefunBlockData data) {
        SimpleCraftingOperation currentOperation = (SimpleCraftingOperation) this.processor.getOperation(b);
        ItemConsumer[] fastCraft = null;
        if (currentOperation == null) {

            Pair<MachineRecipe, ItemConsumer[]> nextP = findNextRecipe(inv, COMMON_SLOT, getOutputSlots());
            if (nextP != null) {

                MachineRecipe next = nextP.getFirstValue();
                ItemConsumer[] outputInfo = nextP.getSecondValue();
                if (next.getTicks() > 0) {
                    currentOperation = new SimpleCraftingOperation(outputInfo, next.getTicks());
                    this.processor.startOperation(b, currentOperation);
                } else {
                    fastCraft = nextP.getSecondValue();
                }
            } else { // if currentOperation ==null return  , cant find nextRecipe
                if (inv.hasViewer()) {
                    for (int var4 = 0; var4 < PROCESSOR_SLOT.length; ++var4) {
                        inv.replaceExistingItem(PROCESSOR_SLOT[var4], MenuUtils.PROCESSOR_NULL);
                    }
                }
                return;
            }
        }

        progressorCost(b, inv);
        if (fastCraft != null) {
            CraftUtils.updateOutputMenu(fastCraft, inv);
        } else if (currentOperation.isFinished()) {
            ItemConsumer[] var4 = currentOperation.getResults();
            CraftUtils.forcePush(var4, inv, getOutputSlots());
            if (inv.hasViewer()) {
                for (int var5 = 0; var5 < PROCESSOR_SLOT.length; ++var5) {
                    inv.replaceExistingItem(PROCESSOR_SLOT[var5], MenuUtils.PROCESSOR_NULL);
                }
            }
            this.processor.endOperation(b);
        } else {
            if (inv.hasViewer()) {
                for (int var5 = 0; var5 < PROCESSOR_SLOT.length; ++var5) {
                    this.processor.updateProgressBar(inv, PROCESSOR_SLOT[var5], currentOperation);
                }
            }
            currentOperation.progress(1);
        }
    }

    public int[] getSlotsAccessedByItemTransportPlus(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
        if (flow == ItemTransportFlow.WITHDRAW) return getOutputSlots();
        else if (item == null || item.getType() == Material.AIR) {
            return getInputSlots();
        }
        if (item.getType() == Material.MUSIC_DISC_5) {
            return BOOL_SLOT;
        } else {
            return COMMON_SLOT;
        }
    }
}
