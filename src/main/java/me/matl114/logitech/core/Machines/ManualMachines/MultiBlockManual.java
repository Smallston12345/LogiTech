package me.matl114.logitech.core.Machines.ManualMachines;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.multiblocks.MultiBlockMachine;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import me.matl114.logitech.core.AddSlimefunItems;
import me.matl114.logitech.core.Interface.MultiCraftType;
import me.matl114.logitech.core.Machines.Abstracts.AbstractManual;
import me.matl114.logitech.core.Registries.RecipeSupporter;
import me.matl114.logitech.manager.PostSetupTasks;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.UtilClass.ItemClass.DisplayItemStack;
import me.matl114.logitech.utils.UtilClass.MenuClass.MenuFactory;
import me.matl114.logitech.utils.UtilClass.RecipeClass.ImportRecipes;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class MultiBlockManual extends AbstractManual implements MultiCraftType, ImportRecipes {

    protected final int[] INPUT_SLOT = new int[] {
        4, 5, 6, 7, 8, 13, 14, 15, 16, 17, 22, 23, 24, 25, 26, 31, 32, 33, 34, 35, 40, 41, 42, 43, 44, 49, 50, 51, 52,
        53
    };
    protected final int[] OUTPUT_SLOT = new int[] {
        31, 32, 33, 34, 35, 40, 41, 42, 43, 44, 49, 50, 51, 52, 53, 4, 5, 6, 7, 8, 13, 14, 15, 16, 17, 22, 23, 24, 25,
        26
    };
    protected final int[] BORDER = new int[] {3, 12, 21, 30, 39, 48, 46};
    protected static final int[] MACHINEITEM_SLOT = new int[] {0, 1, 2, 9, 10, 11, 18, 19, 20};
    public MultiBlockMachine[] craftType;
    protected static final ItemStack DISPLAY_DEFAULT_BKGROUND =
            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c沒有匹配的多方塊機器");
    protected static final int DISPLAYEITEM_SLOT = 27;
    protected static final ItemStack INFO_ITEM = new CustomItemStack(
            Material.ORANGE_STAINED_GLASS_PANE,
            "&6機制:",
            " ",
            "&6在上方的3x3區域擺入多方塊機器構造",
            "&6即可加載配方",
            "&a可以使用槽位偽裝板填充空餘的槽位!",
            "&6在右邊放入物品進行快捷合成");
    protected static final int INFO_SLOT = 28;
    protected static final ItemStack PREV = new CustomItemStack(Material.YELLOW_STAINED_GLASS_PANE, "&3搜索上一個配方");
    protected static final int PREV_SLOT = 36;
    protected static final ItemStack NEXT = new CustomItemStack(Material.YELLOW_STAINED_GLASS_PANE, "&3搜索下一個配方");
    protected static final int NEXT_SLOT = 38;
    protected static final ItemStack CRAFT_ONE = new CustomItemStack(
            Material.GREEN_STAINED_GLASS_PANE, "&3單次合成", "&6左鍵&b合成 &d1次 &b當前物品", "&6右鍵&b合成 &d16次 &b當前物品");
    protected static final int ONE_SLOT = 45;
    protected static final ItemStack CRAFT_MUL = new CustomItemStack(
            Material.GREEN_STAINED_GLASS_PANE, "&3批量合成", "&6左鍵&b合成 &d64次 &b當前物品", "&6右鍵&b合成 &d3456次 &b當前物品");
    protected static final int MUL_SLOT = 47;
    protected static final ItemStack DISPLAY_NOMATCH =
            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c沒有匹配的配方");
    protected static final int RECIPE_ITEM_SLOT = 37;
    protected static final int RECIPEBOOK_SHOW_SLOT = 29;
    protected static final ItemStack RECIPEBOOK_SHOW_ITEM =
            new CustomItemStack(Material.BOOK, "&6點擊查看配方", "", "&6但是並沒有一鍵放置配方的功能");

    public MultiBlockManual(
            ItemGroup category,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int energybuffer,
            int energyConsumption,
            RecipeType... craftType) {
        super(category, item, recipeType, recipe, energybuffer, energyConsumption, null);
        PostSetupTasks.addPostRegisterTask(() -> {
            getDisplayRecipes();
            initMenuFactory();
        });
    }

    public MultiBlockMachine[] getCraftTypes() {
        if (craftType == null || craftType.length <= 0) {
            HashMap<MultiBlockMachine, List<MachineRecipe>> types = RecipeSupporter.MULTIBLOCK_RECIPES;
            if (types == null || types.size() <= 0) {
                return craftType;
            }
            craftType = new MultiBlockMachine[types.size()];
            int cnt = 0;
            for (MultiBlockMachine e : types.keySet()) {
                craftType[cnt] = e;
                cnt++;
            }
        }
        return craftType;
    }

    public MachineRecipe getRecordRecipe(Location loc) {
        int index = MultiCraftType.getRecipeTypeIndex(loc);

        if (index >= 0 && index < getCraftTypes().length) {
            int index2 = getNowRecordRecipe(loc);
            if (index2 >= 0) {
                return RecipeSupporter.MULTIBLOCK_RECIPES
                        .get(getCraftTypes()[index])
                        .get(index2);
            }
        } else if (index > 0) {
            MultiCraftType.setRecipeTypeIndex(loc, -1);
            setNowRecordRecipe(loc, -1);
        }
        return null;
    }

    public List<MachineRecipe> getMachineRecipes(Block b, BlockMenu inv) {
        Location loc = inv.getLocation();
        int index = MultiCraftType.getRecipeTypeIndex(loc);
        if (index >= 0 && index < getCraftTypes().length) {
            return RecipeSupporter.MULTIBLOCK_RECIPES.get(getCraftTypes()[index]);
        } else if (index > 0) {
            MultiCraftType.setRecipeTypeIndex(loc, -1);
            setNowRecordRecipe(loc, -1);
        }
        return null;
    }

    protected HashMap<MultiBlockMachine, MenuFactory> RECIPEMENU = null;

    protected void initMenuFactory() {
        if (RECIPEMENU == null) {
            RECIPEMENU = new HashMap<>();
            MultiBlockMachine[] list = getCraftTypes();
            int len = list.length;
            for (int i = 0; i < len; i++) {
                RECIPEMENU.put(
                        list[i],
                        MenuUtils.createMRecipeListDisplay(
                                list[i].getItem(), RecipeSupporter.MULTIBLOCK_RECIPES.get(list[i]), null));
            }
        }
    }

    protected MenuFactory getMenuFactory(MultiBlockMachine r) {
        if (RECIPEMENU == null) {
            initMenuFactory();
        }
        return RECIPEMENU.get(r);
    }

    public void constructMenu(BlockMenuPreset preset) {
        // 空白背景 禁止點擊
        int[] border = BORDER;
        int len = border.length;
        for (int var4 = 0; var4 < len; ++var4) {
            preset.addItem(border[var4], ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        preset.addItem(DISPLAYEITEM_SLOT, DISPLAY_DEFAULT_BKGROUND, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(INFO_SLOT, INFO_ITEM, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(RECIPE_ITEM_SLOT, DISPLAY_NOMATCH, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(PREV_SLOT, PREV);
        preset.addItem(NEXT_SLOT, NEXT);
        preset.addItem(ONE_SLOT, CRAFT_ONE);
        preset.addItem(MUL_SLOT, CRAFT_MUL);
        preset.addItem(RECIPEBOOK_SHOW_SLOT, RECIPEBOOK_SHOW_ITEM);
    }

    public void newMenuInstance(BlockMenu menu, Block block) {
        menu.addMenuOpeningHandler((player -> {
            MultiBlockManual.this.updateMenu(menu, block, Settings.RUN);
        }));
        menu.addMenuClickHandler(PREV_SLOT, (player, i, itemStack, clickAction) -> {
            if (getNowRecordRecipe(menu.getLocation()) != -1) {
                orderSearchRecipe(menu, Settings.REVERSE, true);
                MultiBlockManual.this.updateMenu(menu, block, Settings.RUN);
            }
            return false;
        });
        menu.addMenuClickHandler(NEXT_SLOT, (player, i, itemStack, clickAction) -> {
            if (getNowRecordRecipe(menu.getLocation()) != -1) {
                orderSearchRecipe(menu, Settings.SEQUNTIAL, true);
                MultiBlockManual.this.updateMenu(menu, block, Settings.RUN);
            }
            return false;
        });
        menu.addMenuClickHandler(ONE_SLOT, (player, i, itemStack, clickAction) -> {
            int limit = 1;
            if (clickAction.isRightClicked()) {
                limit = 16;
            }
            if (this.energyConsumption > 0) {
                int charge = this.getCharge(menu.getLocation()) / this.energyConsumption;
                limit = Math.min(limit, charge);
                if (limit == 0) {
                    AddUtils.sendMessage(player, "&c電力不足!無法進行合成");
                }
            }
            MultiBlockManual.this.updateMenu(menu, block, Settings.RUN);
            craft(player, menu, limit);
            MultiBlockManual.this.updateMenu(menu, block, Settings.RUN);
            return false;
        });
        menu.addMenuClickHandler(MUL_SLOT, (player, i, itemStack, clickAction) -> {
            int limit = 64;
            if (clickAction.isRightClicked()) {
                limit = 3456;
            }
            if (this.energyConsumption > 0) {
                int charge = this.getCharge(menu.getLocation()) / this.energyConsumption;
                limit = Math.min(limit, charge);
                if (limit == 0) {
                    AddUtils.sendMessage(player, "&c電力不足!無法進行合成");
                }
            }
            MultiBlockManual.this.updateMenu(menu, block, Settings.RUN);
            craft(player, menu, limit);
            MultiBlockManual.this.updateMenu(menu, block, Settings.RUN);
            return false;
        });
        menu.addMenuClickHandler(RECIPEBOOK_SHOW_SLOT, ((player, i, itemStack, clickAction) -> {
            int index = MultiCraftType.getRecipeTypeIndex(menu.getLocation());
            if (index >= 0) {
                MultiBlockMachine multiblock = getCraftTypes()[index];
                getMenuFactory(multiblock)
                        .build()
                        .setBackHandler(((player1, i1, itemStack1, clickAction1) -> {
                            menu.open(player1);
                            return false;
                        }))
                        .open(player);
            } else {

                player.sendMessage(ChatColors.color("&e您所擺放的方式並不構成多方塊機器"));

                // player.sendMessage();
            }
            return false;
        }));
        updateMenu(menu, block, Settings.INIT);
    }

    public void updateMenu(BlockMenu inv, Block block, Settings mod) {
        if (mod == Settings.INIT) {
            parseRecipe(inv);
            orderSearchRecipe(inv, Settings.SEQUNTIAL, true);
        } else {
            if (parseRecipe(inv)) {
                Location loc = inv.getLocation();
                MachineRecipe getRecipe = CraftUtils.matchNextRecipe(
                        inv, getInputSlots(), getMachineRecipes(block, inv), true, Settings.SEQUNTIAL, CRAFT_PROVIDER);

                if (getRecipe == null) {
                    DataCache.setLastRecipe(loc, -1);
                }
            }
        }
        Location loc = inv.getLocation();
        int index_ = MultiCraftType.getRecipeTypeIndex(loc);
        if (index_ >= 0) {
            MultiBlockMachine nowMachine = getCraftTypes()[index_];
            List<MachineRecipe> mRecipe = RecipeSupporter.MULTIBLOCK_RECIPES.get(nowMachine);
            inv.replaceExistingItem(DISPLAYEITEM_SLOT, AddUtils.addLore(nowMachine.getItem(), "&8檢測出的多方塊機器"));

            int index = DataCache.getLastRecipe(loc);
            int indexRecord = getNowRecordRecipe(loc);
            if (index != -1) {
                MachineRecipe getRecipe = mRecipe.get(index);
                inv.replaceExistingItem(
                        RECIPE_ITEM_SLOT,
                        AddUtils.addLore(getRecipe.getOutput()[0], "&8匹配的產物", "&8若有多輸出則顯示第一個", "&8配方編號: " + index));
                if (index != indexRecord || mod == Settings.INIT) {

                    setNowRecordRecipe(loc, index);
                    return;
                }
            } else {

                if (indexRecord != -1 || mod == Settings.INIT) {
                    inv.replaceExistingItem(RECIPE_ITEM_SLOT, DISPLAY_NOMATCH);
                    setNowRecordRecipe(loc, -1);
                }
            }
        } else {
            inv.replaceExistingItem(DISPLAYEITEM_SLOT, DISPLAY_DEFAULT_BKGROUND);
            inv.replaceExistingItem(RECIPE_ITEM_SLOT, DISPLAY_NOMATCH);
            setNowRecordRecipe(loc, -1);
        }
    }

    @Override
    public void onBreak(BlockBreakEvent e, BlockMenu inv) {
        super.onBreak(e, inv);
        Location l = inv.getLocation();
        inv.dropItems(l, MACHINEITEM_SLOT);
    }

    public boolean parseRecipe(BlockMenu menu) {
        Location loc = menu.getLocation();
        ItemStack[] multimachineRecipe = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            multimachineRecipe[i] = menu.getItemInSlot(MACHINEITEM_SLOT[i]);
        }
        MultiBlockMachine[] mulM = getCraftTypes();
        int offset = MultiCraftType.getRecipeTypeIndex(loc);
        if (offset <= 0) {
            offset = 0;
        }
        int index;
        for (int i = 0; i < mulM.length; i++) {
            index = (i + offset) % mulM.length;
            ItemStack[] recipe = mulM[index].getRecipe();
            boolean match = true;
            if (recipe != null && recipe.length >= 9) {
                for (int j = 0; j < 9; j++) {
                    if (recipe[j] == null) {
                        if (multimachineRecipe[j] != null
                                && SlimefunItem.getByItem(multimachineRecipe[j]) != AddSlimefunItems.FAKE_UI) {
                            match = false;
                        }
                    } else {
                        if (multimachineRecipe[j] == null || recipe[j].getType() != multimachineRecipe[j].getType()) {
                            match = false;
                            break;
                        }
                    }
                }
            } else {
                match = false;
            }
            if (match) {
                MultiCraftType.setRecipeTypeIndex(loc, index);
                return true;
            }
        }
        MultiCraftType.setRecipeTypeIndex(loc, -1);
        // 清空當前搜索緩存
        return false;
    }

    public void process(Block b, BlockMenu inv, SlimefunBlockData data) {
        // only works when has viewer.
        if (inv != null && inv.hasViewer()) {

            updateMenu(inv, b, Settings.RUN);
        }
    }

    public int[] getInputSlots() {
        return INPUT_SLOT;
    }

    public int[] getOutputSlots() {
        return OUTPUT_SLOT;
    }

    public List<ItemStack> _getDisplayRecipes(List<ItemStack> re) {
        List<ItemStack> its = new ArrayList<>() {
            {
                for (SlimefunItem item : RecipeSupporter.MULTIBLOCK_RECIPES.keySet()) {
                    add(AddUtils.getInfoShow("&f支持的多方塊機器", "&7將機器配方置於指定槽位以進行合成"));
                    add(new DisplayItemStack(item.getItem()));
                }
            }
        };
        re.addAll(its);
        return re;
    }

    protected final IntSet NOT_INTERACTABLE_SLOTS = new IntOpenHashSet();

    public IntSet getSensitiveInteractSlots() {
        return NOT_INTERACTABLE_SLOTS;
    }
}
