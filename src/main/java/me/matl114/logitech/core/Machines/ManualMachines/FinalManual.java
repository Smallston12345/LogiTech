package me.matl114.logitech.core.Machines.ManualMachines;

import com.google.common.collect.Streams;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;
import me.matl114.logitech.core.Interface.MultiCraftType;
import me.matl114.logitech.core.Interface.RecipeDisplay;
import me.matl114.logitech.core.Machines.Abstracts.AbstractManual;
import me.matl114.logitech.core.Machines.WorkBenchs.BugCrafter;
import me.matl114.logitech.core.Registries.FinalFeature;
import me.matl114.logitech.core.Registries.RecipeSupporter;
import me.matl114.logitech.manager.PostSetupTasks;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.UtilClass.ItemClass.ItemPusherProvider;
import me.matl114.logitech.utils.UtilClass.MenuClass.MenuFactory;
import me.matl114.logitech.utils.UtilClass.RecipeClass.ImportRecipes;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class FinalManual extends AbstractManual implements MultiCraftType, ImportRecipes {
    protected final int[] INPUT_SLOT = new int[] {
        3, 4, 5, 6, 7, 8, 12, 13, 14, 15, 16, 17, 21, 22, 23, 24, 25, 26, 30, 31, 32, 33, 34, 35, 39, 40, 41, 42, 43,
        44, 48, 49, 50, 51, 52, 53
    };
    protected final int[] OUTPUT_SLOT = new int[] {
        30, 31, 32, 33, 34, 35, 39, 40, 41, 42, 43, 44, 48, 49, 50, 51, 52, 53, 3, 4, 5, 6, 7, 8, 12, 13, 14, 15, 16,
        17, 21, 22, 23, 24, 25, 26
    };
    protected static final int RECIPE_ITEM_SLOT = 0;
    protected static final ItemStack INFO_ITEM = new CustomItemStack(
            Material.ORANGE_STAINED_GLASS_PANE,
            "&6配方匹配機制:",
            " ",
            "&b點擊左邊圖標切換合成類型",
            "&b點擊右邊書本查看當前合成類型資訊",
            "&b在輸入欄放入物品進行快捷合成");
    protected static final int INFO_SLOT = 1;
    protected static final int RECIPEBOOK_SHOW_SLOT = 2;
    protected static final ItemStack PREV = new CustomItemStack(Material.YELLOW_STAINED_GLASS_PANE, "&3搜索上一個配方");
    protected static final int PREV_SLOT = 9;
    protected static final int DISPLAYEITEM_SLOT = 10;
    protected static final ItemStack NEXT = new CustomItemStack(Material.YELLOW_STAINED_GLASS_PANE, "&3搜索下一個配方");
    protected static final int NEXT_SLOT = 11;
    protected static final ItemStack CRAFT_ONE = new CustomItemStack(
            Material.GREEN_STAINED_GLASS_PANE, "&3單次合成", "&6左鍵&b合成 &d1次 &b當前物品", "&6右鍵&b合成 &d64次 &b當前物品");
    protected static final int ONE_SLOT = 18;
    protected static final ItemStack CRAFT_MUL = new CustomItemStack(
            Material.GREEN_STAINED_GLASS_PANE, "&3批量合成", "&6左鍵&b合成 &d3456次 &b當前物品", "&6右鍵&b合成 &d9,999,999次 &b當前物品");
    protected static final int MUL_SLOT = 20;
    protected static final ItemStack INFO2_ITEM = new CustomItemStack(
            Material.BEACON,
            "&6合成機制: " + AddUtils.colorful("終極合成"),
            "",
            "&b該快捷機器可直接消耗存儲類物品或者連結綁定的實體存儲內的物品參與合成",
            "&b產出的物品也可以直接地存入存儲類物品或者連結綁定的實體存儲",
            "&c配方總輸出數量最大會被限制至9223372036854775807(long上限)",
            "",
            "&b當前支援的物品:",
            "&7邏輯工藝 概念奇點存儲(存儲類物品)",
            "&7邏輯工藝 量子糾纏奇點(存儲連結物品)",
            "&7網絡(拓展) 量子存儲系列(存儲類物品)");
    protected static final int INFO2_SLOT = 19;
    protected static final ItemStack DISPLAY_BKGROUND = new CustomItemStack(Material.GREEN_STAINED_GLASS_PANE, " ");
    protected static final ItemStack DISPLAY_DEFAULT_BKGROUND =
            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, " ");
    protected static final ItemStack RECIPEBOOK_SHOW_ITEM =
            new CustomItemStack(Material.BOOK, "&6點擊查看配方", "", "&6但是並沒有一鍵放置配方的功能");
    protected static final int[] RECIPE_DISPLAY = {27, 28, 29, 36, 37, 38, 45, 46, 47};

    public FinalManual(
            ItemGroup category,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            int energybuffer,
            int energyConsumption) {
        super(category, item, recipeType, recipe, 0, 0, null);
        this.BW_LIST = new ArrayList<>() {
            {
                add(RecipeType.ENHANCED_CRAFTING_TABLE);
                add(RecipeType.SMELTERY);
                add(RecipeType.MAGIC_WORKBENCH);
                add(RecipeType.ANCIENT_ALTAR);
                add(BugCrafter.TYPE);
                add(RecipeType.GRIND_STONE);
                add(RecipeType.ORE_CRUSHER);
                add(RecipeType.ORE_WASHER);
                add(RecipeType.GOLD_PAN);
                add(RecipeType.COMPRESSOR);
                add(RecipeType.PRESSURE_CHAMBER);
                add(RecipeType.ARMOR_FORGE);
                add(RecipeType.HEATED_PRESSURE_CHAMBER);
                add(BukkitUtils.VANILLA_CRAFTTABLE);
                add(BukkitUtils.VANILLA_FURNACE);
            }
        };
        PostSetupTasks.addPostRegisterTask(() -> {
            registerRecipeList();
            initMenuFactory();
        });
        // 啟動奇點合成的支援
        this.CRAFT_PROVIDER = SINGULARITY_PROVIDER;
        setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow(
                        "&f機制 - &c終極合成",
                        "&7機器的輸入槽,輸出槽均支援終極合成的特性",
                        "&e可以直接消耗存儲類物品或者連結綁定的實體存儲內的物品參與合成",
                        "&e產出的物品也可以直接地存入存儲類物品或者連結綁定的實體存儲",
                        "&7當前支援的物品:",
                        "&7邏輯工藝 概念奇點存儲(存儲類物品)",
                        "&7邏輯工藝 量子糾纏奇點(存儲連結物品)",
                        "&7網絡(拓展) 量子存儲系列(存儲類物品)",
                        "&a建議:多使用量子糾纏奇點,可以大幅度減少卡頓!"),
                null,
                AddUtils.getInfoShow(
                        "&f機制 - &c無限級一鍵合成", "&7本機器最多支援一鍵合成9,999,999次", "&7\"終極合成\"的特性讓它不再是空談", "&7是時候讓存儲裏的大批物品動起來了")));
    }

    public static final ItemPusherProvider SINGULARITY_PROVIDER = FinalFeature.STORAGE_AND_LOCPROXY_READER;
    // after start,load recipeType from config
    public void registerRecipeList() {
        craftType = BW_LIST.toArray(new RecipeType[BW_LIST.size()]);
    }

    public final List<RecipeType> BW_LIST;
    public RecipeType[] craftType = new RecipeType[] {RecipeType.ENHANCED_CRAFTING_TABLE};
    protected boolean isWhiteList = true;

    public List<MachineRecipe> getMachineRecipes(Block b, BlockMenu inv) {
        Location loc = inv.getLocation();
        int index = MultiCraftType.getRecipeTypeIndex(loc);
        if (index >= 0 && index < getCraftTypes().length) {
            return RecipeSupporter.PROVIDED_UNSHAPED_RECIPES.get(getCraftTypes()[index]);
        } else if (index > 0) {
            MultiCraftType.forceSetRecipeTypeIndex(loc, 0);
            setNowRecordRecipe(loc, -1);
        }
        return null;
    }

    public RecipeType[] getCraftTypes() {
        return craftType;
    }

    public MachineRecipe getRecordRecipe(SlimefunBlockData data) {
        int index = MultiCraftType.getRecipeTypeIndex(data);
        if (index >= 0 && index < getCraftTypes().length) {
            int index2 = getNowRecordRecipe(data);
            if (index2 >= 0) { // I think that's safe
                return RecipeSupporter.PROVIDED_UNSHAPED_RECIPES
                        .get(getCraftTypes()[index])
                        .get(index2);
            }
        } else {
            MultiCraftType.forceSetRecipeTypeIndex(data, 0);
        }
        return null;
    }

    public void constructMenu(BlockMenuPreset preset) {
        // 空白背景 禁止點擊
        preset.addItem(DISPLAYEITEM_SLOT, DISPLAY_DEFAULT_BKGROUND, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(INFO_SLOT, INFO_ITEM, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(RECIPE_ITEM_SLOT, DISPLAY_NOMATCH, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(INFO2_SLOT, INFO2_ITEM, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(PREV_SLOT, PREV);
        preset.addItem(NEXT_SLOT, NEXT);
        preset.addItem(ONE_SLOT, CRAFT_ONE);
        preset.addItem(MUL_SLOT, CRAFT_MUL);
        preset.addItem(RECIPEBOOK_SHOW_SLOT, RECIPEBOOK_SHOW_ITEM);
        int len = RECIPE_DISPLAY.length;
        for (int i = 0; i < len; i++) {
            preset.addItem(RECIPE_DISPLAY[i], DISPLAY_DEFAULT_BKGROUND, ChestMenuUtils.getEmptyClickHandler());
        }
    }

    protected HashMap<RecipeType, MenuFactory> RECIPEMENU = null;

    protected void initMenuFactory() {
        if (RECIPEMENU == null) {
            RECIPEMENU = new HashMap<>();
            int len = craftType.length;
            RecipeType rp;
            for (int i = 0; i < len; i++) {
                rp = craftType[i];
                RECIPEMENU.put(
                        rp,
                        MenuUtils.createMRecipeListDisplay(
                                RecipeSupporter.RECIPETYPE_ICON.get(rp),
                                RecipeSupporter.PROVIDED_UNSHAPED_RECIPES.get(rp),
                                null));
            }
        }
    }

    protected MenuFactory getMenuFactory(RecipeType r) {
        if (RECIPEMENU == null) {
            initMenuFactory();
        }
        return RECIPEMENU.get(r);
    }

    public void newMenuInstance(BlockMenu menu, Block block) {
        int ind = MultiCraftType.getRecipeTypeIndex(menu.getLocation());
        // 清除非法記錄
        if (!(ind >= 0 && ind < getCraftTypes().length)) {
            MultiCraftType.setRecipeTypeIndex(menu.getLocation(), 0);
        }
        menu.addMenuOpeningHandler((player -> {
            FinalManual.this.updateMenu(menu, block, Settings.RUN);
        }));
        menu.addMenuClickHandler(RECIPE_ITEM_SLOT, ((player, i, itemStack, clickAction) -> {
            Location loc = menu.getLocation();
            int index = MultiCraftType.getRecipeTypeIndex(loc);
            index += clickAction.isRightClicked() ? -1 : 1;
            if (index >= 0 && index <= getCraftTypes().length - 1) {
                MultiCraftType.forceSetRecipeTypeIndex(loc, index);
            } else if (index < 0) {
                MultiCraftType.forceSetRecipeTypeIndex(loc, getCraftTypes().length - 1);
            } else {
                MultiCraftType.forceSetRecipeTypeIndex(loc, 0);
            }
            FinalManual.this.updateMenu(menu, block, Settings.RUN);
            return false;
        }));
        menu.addMenuClickHandler(PREV_SLOT, (player, i, itemStack, clickAction) -> {
            if (getNowRecordRecipe(menu.getLocation()) != -1) {
                orderSearchRecipe(menu, Settings.REVERSE);
                FinalManual.this.updateMenu(menu, block, Settings.RUN);
            }
            return false;
        });
        menu.addMenuClickHandler(NEXT_SLOT, (player, i, itemStack, clickAction) -> {
            if (getNowRecordRecipe(menu.getLocation()) != -1) {
                orderSearchRecipe(menu, Settings.SEQUNTIAL);
                FinalManual.this.updateMenu(menu, block, Settings.RUN);
            }
            return false;
        });
        menu.addMenuClickHandler(ONE_SLOT, (player, i, itemStack, clickAction) -> {
            int limit = 1;
            if (clickAction.isRightClicked()) {
                limit = 64;
            }
            // FinalManual.this.updateMenu(menu,block,Settings.RUN);
            craft(player, menu, limit);
            // FinalManual.this.updateMenu(menu,block,Settings.RUN);

            return false;
        });
        menu.addMenuClickHandler(MUL_SLOT, (player, i, itemStack, clickAction) -> {
            int limit = 3456;
            if (clickAction.isRightClicked()) {
                limit = 9_999_999;
            }
            // do not refresh since some of them
            // FinalManual.this.updateMenu(menu,block,Settings.RUN);
            craft(player, menu, limit);
            // FinalManual.this.updateMenu(menu,block,Settings.RUN);
            return false;
        });
        menu.addMenuClickHandler(RECIPEBOOK_SHOW_SLOT, ((player, i, itemStack, clickAction) -> {
            int index = MultiCraftType.getRecipeTypeIndex(menu.getLocation());
            if (index >= 0) {
                RecipeType rp = getCraftTypes()[index];
                getMenuFactory(rp)
                        .build()
                        .setBackHandler(((player1, i1, itemStack1, clickAction1) -> {
                            menu.open(player1);
                            return false;
                        }))
                        .open(player);
            } else {
                player.sendMessage(ChatColors.color("&e配方類型獲取錯誤"));
                // player.sendMessage();
            }
            return false;
        }));
        updateMenu(menu, block, Settings.INIT);
    }

    public void updateMenu(BlockMenu inv, Block block, Settings mod) {
        if (mod == Settings.INIT) {
            orderSearchRecipe(inv, Settings.SEQUNTIAL);
        } else {
            Location loc = inv.getLocation();
            MachineRecipe getRecipe = CraftUtils.matchNextRecipe(
                    inv,
                    getInputSlots(),
                    getMachineRecipes(block, inv),
                    true,
                    Settings.SEQUNTIAL,
                    SINGULARITY_PROVIDER);
            if (getRecipe == null) {
                DataCache.setLastRecipe(loc, -1);
            }
        }
        Location loc = inv.getLocation();
        int index_ = MultiCraftType.getRecipeTypeIndex(loc);
        if (index_ >= 0) {
            RecipeType rp = getCraftTypes()[index_];
            List<MachineRecipe> mRecipe = RecipeSupporter.PROVIDED_UNSHAPED_RECIPES.get(rp);
            inv.replaceExistingItem(
                    RECIPE_ITEM_SLOT,
                    AddUtils.addLore(RecipeSupporter.RECIPETYPE_ICON.get(rp), "&8點擊切換配方類型", "&8左鍵順時針切換", "&8右鍵逆時針切換"));

            int index = DataCache.getLastRecipe(loc);
            int indexRecord = getNowRecordRecipe(loc);
            if (index != -1) {
                MachineRecipe getRecipe = mRecipe.get(index);
                inv.replaceExistingItem(
                        DISPLAYEITEM_SLOT,
                        AddUtils.addLore(getRecipe.getOutput()[0], "&8匹配的產物", "&8若有多輸出則顯示第一個", "&8配方編號: " + index));
                ItemStack[] opt = getRecipe.getInput();
                int len = opt.length;
                len = Math.min(len, RECIPE_DISPLAY.length);
                for (int i = 0; i < len; i++) {
                    inv.replaceExistingItem(
                            RECIPE_DISPLAY[i], RecipeDisplay.addRecipeInfo(opt[i], Settings.INPUT, i, 1.0, 0));
                }
                for (int i = len; i < RECIPE_DISPLAY.length; i++) {
                    inv.replaceExistingItem(RECIPE_DISPLAY[i], DISPLAY_BKGROUND);
                }
                if (index != indexRecord || mod == Settings.INIT) {

                    setNowRecordRecipe(loc, index);
                    return;
                }
                return;
            } else {

                if (indexRecord != -1 || mod == Settings.INIT) {
                    inv.replaceExistingItem(DISPLAYEITEM_SLOT, DISPLAY_NOMATCH);
                    setNowRecordRecipe(loc, -1);
                    for (int i = 0; i < RECIPE_DISPLAY.length; i++) {
                        inv.replaceExistingItem(RECIPE_DISPLAY[i], DISPLAY_DEFAULT_BKGROUND);
                    }
                }
            }
        } else {
            MultiCraftType.forceSetRecipeTypeIndex(loc, 0);
        }
    }

    public int[] getInputSlots() {
        return INPUT_SLOT;
    }

    public int[] getOutputSlots() {
        return OUTPUT_SLOT;
    }

    public void orderSearchRecipe(BlockMenu inv, Settings order) {
        if (inv != null) {
            int delta;
            switch (order) {
                case REVERSE:
                    delta = -1;
                    break;
                case SEQUNTIAL:
                default:
                    delta = 1;
                    break;
            }

            Location loc = inv.getLocation();
            int index = DataCache.getLastRecipe(loc);
            if (index < 0) {
                return;
            }
            List<MachineRecipe> mRecipe = getMachineRecipes(null, inv);
            if (mRecipe == null) return;
            index = index + delta;
            if (index < 0) {
                index = mRecipe.size() - 1;
            } else if (index >= mRecipe.size()) {
                index = 0;
            }
            DataCache.setLastRecipe(loc, index);
            if (CraftUtils.matchNextRecipe(inv, getInputSlots(), mRecipe, true, order, SINGULARITY_PROVIDER) == null) {
                DataCache.setLastRecipe(loc, -1);
            }
        }
    }
    //

    public void process(Block b, BlockMenu inv, SlimefunBlockData data) {
        // only works when has viewer.
        if (inv != null && (inv.hasViewer())) {

            updateMenu(inv, b, Settings.RUN);
        }
    }

    public void registerTick(SlimefunItem item) {
        this.addItemHandler(FinalFeature.FINAL_SYNC_TICKER);
    }

    protected final IntSet NOT_INTERACTABLE_SLOTS = new IntOpenHashSet(
            Streams.concat(Arrays.stream(RECIPE_DISPLAY), IntStream.of(PREV_SLOT, DISPLAYEITEM_SLOT, NEXT_SLOT))
                    .iterator());

    public IntSet getSensitiveInteractSlots() {
        return NOT_INTERACTABLE_SLOTS;
    }
}
