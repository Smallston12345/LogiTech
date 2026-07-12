package me.matl114.logitech.core.Machines.ManualMachines;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import me.matl114.logitech.core.Blocks.AbstractBlock;
import me.matl114.logitech.core.Registries.FinalFeature;
import me.matl114.logitech.core.Registries.RecipeSupporter;
import me.matl114.logitech.manager.PostSetupTasks;
import me.matl114.logitech.manager.Schedules;
import me.matl114.logitech.utils.*;
import me.matl114.logitech.utils.UtilClass.ItemClass.ItemGreedyConsumer;
import me.matl114.logitech.utils.UtilClass.ItemClass.ItemPusherProvider;
import me.matl114.matlib.utils.inventory.itemStacks.CleanItemStack;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class AdvancedManual extends AbstractBlock {
    // ManualCrafterGui arguments
    private int[] BORDER = new int[] {10, 19, 28, 37, 48, 49, 51, 42, 53, 44};
    private int[] rTypeIconSlot = new int[] {0, 9, 18, 27, 36, 45};
    private int[] recipelistDisplaySlot =
            new int[] {2, 3, 4, 5, 11, 12, 13, 14, 20, 21, 22, 23, 29, 30, 31, 32, 38, 39, 40, 41};
    private int[] recipeDisplaySlot = new int[] {6, 7, 8, 15, 16, 17, 24, 25, 26};
    private int outputSlot = 34;
    private int prevRtype = 1;
    private int nextRtype = 46;
    private int prevRecipe = 47;
    private int nextRecipe = 50;
    private int prevSelect = 33;
    private int nextSelect = 35;
    private int craftOne = 43;
    private int craftMul = 52;
    private ItemStack nullRtype = new CustomItemStack(Material.STRUCTURE_VOID, " ", " ");
    private ItemStack nullRecipe = new CustomItemStack(Material.BARRIER, " ", " ");
    private ItemStack nullIngredient = new CustomItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " ", " ");
    private List<RecipeType> supportedRtype = List.of(
            BukkitUtils.VANILLA_CRAFTTABLE,
            BukkitUtils.VANILLA_FURNACE,
            RecipeType.ENHANCED_CRAFTING_TABLE,
            RecipeType.SMELTERY,
            RecipeType.GRIND_STONE,
            RecipeType.ORE_CRUSHER,
            RecipeType.MAGIC_WORKBENCH,
            RecipeType.ANCIENT_ALTAR,
            RecipeType.GOLD_PAN,
            RecipeType.ORE_WASHER,
            RecipeType.COMPRESSOR,
            RecipeType.ARMOR_FORGE,
            RecipeType.JUICER,
            RecipeType.HEATED_PRESSURE_CHAMBER);
    private int rtypeAmount = supportedRtype.size();
    private ItemStack displayBorder = new CustomItemStack(Material.YELLOW_STAINED_GLASS_PANE, " ", " ");
    protected static final ItemStack CRAFT_ONE = new CustomItemStack(
            Material.GREEN_STAINED_GLASS_PANE, "&3單次合成", "&6左鍵&b合成 &d1次 &b當前物品", "&6右鍵&b合成 &d16次 &b當前物品");
    protected static final ItemStack CRAFT_MUL = new CustomItemStack(
            Material.GREEN_STAINED_GLASS_PANE, "&3批量合成", "&6左鍵&b合成 &d64次 &b當前物品", "&6右鍵&b合成 &d3456次 &b當前物品");
    private int[] craftableSlots = IntStream.range(0, 36).toArray();

    public AdvancedManual(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        this.setDisplayRecipes(Utils.list(FinalFeature.MANUAL_CARD_INFO, null));
        PostSetupTasks.addPostRegisterTask(() -> {
            for (RecipeType rtype : supportedRtype) {
                ItemStack icon = RecipeSupporter.RECIPETYPE_ICON.getOrDefault(rtype, nullRtype);
                this.addDisplayRecipe(new CleanItemStack(Material.BOOK, "&f支持的配方類型"));
                this.addDisplayRecipe(icon);
            }
        });
    }

    public void preRegister() {
        super.preRegister();
        this.registerBlockMenu(this);
    }
    // todo speed up refresh rate, update imm when changing recipeType
    public void constructMenu(BlockMenuPreset preset) {
        preset.setSize(9);
    }

    @Override
    public void newMenuInstance(@Nonnull BlockMenu blockMenu, @Nonnull Block block) {
        blockMenu.addMenuOpeningHandler(this::openManualGui);
    }

    private HashMap<UUID, ManualCrafterGui> PLAYER_MANUALGUIS = new HashMap<>();

    public void openManualGui(Player player) {
        PLAYER_MANUALGUIS
                .computeIfAbsent(player.getUniqueId(), (uuid -> new ManualCrafterGui(getItemName())))
                .open(player);
    }

    private ItemStack displayedBook = new ItemStack(Material.WRITTEN_BOOK);

    public void onPlace(BlockPlaceEvent e, Block b) {
        // bedrock version can not interact with booked-lectern
        //        if(b.getState(false) instanceof Lectern lt){
        //            if(lt.getInventory() instanceof LecternInventory ltiv){
        //                ltiv.setItem(0,displayedBook);
        //            }
        //        }
    }

    protected ItemPusherProvider CRAFT_PROVIDER = FinalFeature.MANUAL_CARD_READER;

    public class ManualCrafterGui extends ChestMenu {
        public ManualCrafterGui(String title) {
            super(title);
            setup();
        }
        // 配方類型選擇的圖標偏移
        private int rtypeDisplayOffset = 0;
        // 上次選中的圖標 記錄在MENU中的位置
        private int lastlySelectedTypeSlotIndex = -1;
        // 上次選中的圖標代表的配方類型
        private RecipeType selectedType = null;
        // 上次選中的MACHINERECIPE的圖標,todo 僅用於handler取消附魔
        private int lastlySelectedRecipeSlot = -1;
        // 上次選中的配方類型的全部配方
        private List<MachineRecipe> selectedTypeRecipes = null;
        // 上次搜索時候匹配的配方下標集合，nonnull，重置時候應該重置到下方這樣
        private List<Integer> lastlySearchedRecipeIndex = List.of();
        // 設定的配方類型偏移量
        private int recipeDisplayOffset = 0;
        // 上次選中的MACHINERECIPE的下標索引,記錄在List中的位置
        private int selectedRecipeIndex = -1;
        private Player executor = null;
        AtomicReference<BukkitRunnable> asyncUpdateMenuTask = new AtomicReference<>(null);
        // 上下文切換，點擊icon列的上/下切換
        private void addRtypeOffset(int offset) { // should be +-1 in case of safety
            if (rtypeDisplayOffset + offset >= 0 && rtypeDisplayOffset + offset + rTypeIconSlot.length <= rtypeAmount) {
                // 維護偏移量和選中光標偏移量
                rtypeDisplayOffset += offset;
                int last = lastlySelectedTypeSlotIndex;
                if (last >= 0) {
                    // 相對屏幕往上挪
                    lastlySelectedTypeSlotIndex -= offset;
                    if (!(lastlySelectedTypeSlotIndex >= 0 && lastlySelectedTypeSlotIndex < rTypeIconSlot.length)) {
                        lastlySelectedTypeSlotIndex = -1;
                    }
                }
                // 重新繪製全部rtype icon 內容
                resetTypeSlot();
                // 光標溢出的時候需要重置rtype,並且觸發重置rtype所帶來的影響
                if (last >= 0 && lastlySelectedTypeSlotIndex < 0) {
                    setRecipeType(null);
                }
            }
        }
        // 重置配方選擇icon，一般在初始化或者上下文切換時使用
        private void resetTypeSlot() {
            // 重新繪製icon
            for (int i = 0; i < rTypeIconSlot.length; ++i) {
                if (rtypeDisplayOffset + i < rtypeAmount) {
                    setTypeSlot(i, supportedRtype.get(rtypeDisplayOffset + i));
                } else {
                    setTypeSlot(i, null);
                }
            }
            // 重新繪製光標
            int last = lastlySelectedTypeSlotIndex;
            lastlySelectedTypeSlotIndex = -1;
            changeSelectedTypeSlot(last);
        }
        // 設置配方類型 generally的
        // 切換類型的時候需要做到重置下方的所有內容
        private void setRecipeType(RecipeType type) {
            if (selectedType != type) {
                selectedType = type;
                // 主要觸發修改物品展示界面 切換的時候需要全部刪除
                this.clearRecordOnTypeChange();
                this.reloadRecipeType();
            }
        }

        private void reloadRecipeType() {
            if (this.selectedType != null) {
                this.selectedTypeRecipes = RecipeSupporter.PROVIDED_UNSHAPED_RECIPES.get(this.selectedType);
            } else {
                this.selectedTypeRecipes = null;
            }
        }
        // 僅用於在handler中設置配方類型的光標的及時切換
        private void changeSelectedTypeSlot(int index) {
            if (index != -1 && index < rTypeIconSlot.length) {
                if (index != lastlySelectedTypeSlotIndex) {
                    ItemStack stack = this.getItemInSlot(rTypeIconSlot[index]);
                    if (stack != null) {
                        AddUtils.addGlow(stack);
                    }
                    if (lastlySelectedTypeSlotIndex >= 0) {
                        ItemStack lastlyChosen = this.getItemInSlot(rTypeIconSlot[lastlySelectedTypeSlotIndex]);
                        if (lastlyChosen != null) {
                            AddUtils.removeGlow(lastlyChosen);
                        }
                    }
                    this.lastlySelectedTypeSlotIndex = index;
                }
            } else {
                if (lastlySelectedTypeSlotIndex >= 0) {
                    ItemStack lastlyChosen = this.getItemInSlot(rTypeIconSlot[lastlySelectedTypeSlotIndex]);
                    if (lastlyChosen != null) {
                        AddUtils.removeGlow(lastlyChosen);
                    }
                }
                this.lastlySelectedTypeSlotIndex = -1;
            }
        }
        // 僅用於構造配方類型的icon列
        private void setTypeSlot(int index, RecipeType type) {
            if (index > 6) return;
            if (type != null) {
                this.replaceExistingItem(
                        rTypeIconSlot[index], RecipeSupporter.RECIPETYPE_ICON.getOrDefault(type, nullRtype));
                this.addMenuClickHandler(rTypeIconSlot[index], ((player, i, itemStack, clickAction) -> {
                    // 修改真實選項
                    setRecipeType(type);
                    changeSelectedTypeSlot(index);
                    return false;
                }));
            } else {
                this.replaceExistingItem(rTypeIconSlot[index], nullRtype);
                this.addMenuClickHandler(rTypeIconSlot[index], ChestMenuUtils.getEmptyClickHandler());
            }
        }
        // 顧名思義
        private void clearRecordOnTypeChange() {
            this.lastlySelectedRecipeSlot = -1;
            this.selectedTypeRecipes = null;
            this.lastlySearchedRecipeIndex = List.of();
            this.recipeDisplayOffset = 0;
            // 包括了設置selectedRecipeIndex以及清除材料展示
            setRecipe(-1, true);
            // 刷新配方列表展示
            listMatchedRecipes();
        }

        private void addRecipeOffset(int offset) {
            if (this.recipeDisplayOffset + offset >= 0
                    && this.recipeDisplayOffset + offset + recipelistDisplaySlot.length
                            <= lastlySearchedRecipeIndex.size()) {
                recipeDisplayOffset += offset;
                listMatchedRecipes();
            }
        }
        // 理論上這個是用於刷新所有配方展示的
        // 實際上也會檢測selected是否有效 若無效會自動刪除
        private void listMatchedRecipes() {
            if (this.selectedTypeRecipes == null
                    || this.lastlySearchedRecipeIndex == null
                    || this.lastlySearchedRecipeIndex.isEmpty()) {
                // 需要重置的
                for (int i : recipelistDisplaySlot) {
                    this.replaceExistingItem(i, nullRecipe);
                    this.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler());
                }
                this.setRecipe(-1, true);
            } else {
                boolean matched = false;
                int len = lastlySearchedRecipeIndex.size();
                this.recipeDisplayOffset =
                        Math.max(Math.min(this.recipeDisplayOffset, len - recipelistDisplaySlot.length), 0);
                for (int i = 0; i < recipelistDisplaySlot.length; ++i) {
                    int index = recipelistDisplaySlot[i];
                    if (i + this.recipeDisplayOffset < len) {
                        int recipeIndex = lastlySearchedRecipeIndex.get(i + this.recipeDisplayOffset);
                        boolean result = setRecipeSlot(index, recipeIndex, getRecipe(recipeIndex));
                        matched = matched || result;
                    } else {
                        setRecipeSlot(index, -1, null);
                    }
                }
                if (!matched) {
                    // 默認設置為index最小的
                    this.lastlySelectedRecipeSlot = recipelistDisplaySlot[0];
                    Optional.of(this.getItemInSlot(recipelistDisplaySlot[0])).ifPresent(AddUtils::addGlow);
                    int index = this.lastlySearchedRecipeIndex.get(this.recipeDisplayOffset);
                    setRecipe(index, false);
                }
            }
        }
        // 僅用於點擊handler即時切換配方選擇光標
        private void forceChangeSelectedRecipeSlot(int index) {

            if (lastlySelectedRecipeSlot != -1) {
                // 只有非0且與之前不一樣才需要取消之前的附魔光效
                if (lastlySelectedRecipeSlot != index) {
                    ItemStack stack = this.getItemInSlot(lastlySelectedRecipeSlot);
                    if (stack != null) {
                        AddUtils.removeGlow(stack);
                    }
                }
            }
            // 強制再來一遍附魔光效，用於tick刷新時無需調用
            if (index != -1) {
                ItemStack stack = this.getItemInSlot(index);
                if (stack != null) {
                    AddUtils.addGlow(stack);
                }
            }
            this.lastlySelectedRecipeSlot = index;
        }
        // set Recipe Slot
        // return if this recipeIndex matches selected
        private boolean setRecipeSlot(int index, int recipeIndex, MachineRecipe recipe) {
            if (recipe != null) {
                ItemStack icon = AddUtils.addLore(recipe.getOutput()[0], "&a點擊選擇該配方", "&8配方編號: " + recipeIndex);
                this.replaceExistingItem(index, icon);
                this.addMenuClickHandler(index, ((player, i, itemStack, clickAction) -> {
                    this.forceChangeSelectedRecipeSlot(index);
                    this.setRecipe(recipeIndex, true);
                    return false;
                }));
                if (this.selectedRecipeIndex == recipeIndex) {
                    forceChangeSelectedRecipeSlot(index);
                }
                return this.selectedRecipeIndex == recipeIndex;
            } else {
                this.replaceExistingItem(index, nullRecipe);
                this.addMenuClickHandler(index, ChestMenuUtils.getEmptyClickHandler());
                return false;
            }
        }
        // 該方法用於generally的設置選中的配方項
        private void setRecipe(int index, boolean force) {
            if (index != this.selectedRecipeIndex || force) {
                if (index == -1 || this.selectedTypeRecipes == null || index >= this.selectedTypeRecipes.size()) {
                    // 非法的值或者是null
                    // 強制重置並情空recipe展示
                    this.selectedRecipeIndex = -1;
                    for (int i = 0; i < recipeDisplaySlot.length; ++i) {
                        this.replaceExistingItem(recipeDisplaySlot[i], nullIngredient);
                    }
                    this.replaceExistingItem(outputSlot, nullIngredient);
                } else {
                    // 在合法範圍內設置值
                    // 設置選中值 構造recipe展示
                    this.selectedRecipeIndex = index;
                    MachineRecipe recipe = this.selectedTypeRecipes.get(index);
                    ItemStack outputs =
                            AddUtils.addLore(recipe.getOutput()[0], "&8匹配的產物", "&8若有多輸出則顯示第一個", "&8配方編號: " + index);
                    ItemStack[] inputs = recipe.getInput();
                    for (int i = 0; i < recipeDisplaySlot.length; ++i) {
                        if (i < inputs.length) {
                            this.replaceExistingItem(
                                    recipeDisplaySlot[i], AddUtils.addLore(inputs[i], "", "&a材料 " + i));
                        } else {
                            this.replaceExistingItem(recipeDisplaySlot[i], nullIngredient);
                        }
                    }
                    this.replaceExistingItem(outputSlot, outputs);
                }
            }
        }

        private MachineRecipe getRecipe() {
            return getRecipe(this.selectedRecipeIndex);
        }

        private MachineRecipe getRecipe(int idx) {
            if (this.selectedTypeRecipes == null || idx >= this.selectedTypeRecipes.size() || idx < 0) {
                return null;
            } else {
                return this.selectedTypeRecipes.get(idx);
            }
        }

        private void onMenuRefreshByPlayerAsync(Player player) {
            if (this.selectedType != null) {
                if (this.selectedTypeRecipes == null) {
                    this.reloadRecipeType();
                }
                this.lastlySearchedRecipeIndex =
                        searchAllMatchedIndex(player, this.selectedTypeRecipes, 2 * recipelistDisplaySlot.length);
            }
            this.listMatchedRecipes();
        }
        // todo 好多bug。
        public void setup() {
            this.setEmptySlotsClickable(true);
            this.setPlayerInventoryClickable(true);
            // 設置背景板
            IntStream.range(0, 54).forEach(i -> this.addMenuClickHandler(i, ChestMenuUtils.getEmptyClickHandler()));
            for (int i : BORDER) {
                this.addItem(i, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
            }
            // 重置配方類型選擇列表
            this.resetTypeSlot();
            // 重置配方顯示
            this.setRecipeType(null);
            // 重置原材料顯示
            this.setRecipe(-1, true);
            this.addItem(prevRtype, MenuUtils.getPreviousButton(114, 514), ((player, i, itemStack, clickAction) -> {
                addRtypeOffset(-1);
                return false;
            }));
            this.addItem(nextRtype, MenuUtils.getNextButton(114, 514), ((player, i, itemStack, clickAction) -> {
                addRtypeOffset(1);
                return false;
            }));
            this.addItem(prevRecipe, MenuUtils.getPreviousButton(114, 514), ((player, i, itemStack, clickAction) -> {
                addRecipeOffset(-1);
                return false;
            }));
            this.addItem(nextRecipe, MenuUtils.getNextButton(114, 514), ((player, i, itemStack, clickAction) -> {
                addRecipeOffset(1);
                return false;
            }));

            this.addItem(prevSelect, displayBorder, ChestMenuUtils.getEmptyClickHandler());
            this.addItem(nextSelect, displayBorder, ChestMenuUtils.getEmptyClickHandler());
            this.addItem(craftOne, CRAFT_ONE, ((player, i, itemStack, clickAction) -> {
                craft(player, clickAction.isRightClicked() ? 16 : 1);
                return false;
            }));
            this.addItem(craftMul, CRAFT_MUL, ((player, i, itemStack, clickAction) -> {
                craft(player, clickAction.isRightClicked() ? 3456 : 64);
                return false;
            }));
            this.addMenuOpeningHandler(player -> {
                executor = player;
                BukkitRunnable newTask = new BukkitRunnable() {
                    public void run() {
                        if (getInventory().getViewers().contains(executor)) {
                            onMenuRefreshByPlayerAsync(executor);
                        } else {
                            this.cancel();
                        }
                    }
                };
                newTask.runTaskTimerAsynchronously(Schedules.getPlugin(), 0, 10);
                newTask = asyncUpdateMenuTask.getAndSet(newTask);
                if (newTask != null && !newTask.isCancelled()) {
                    newTask.cancel();
                }
            });
            this.addMenuCloseHandler(player -> {
                executor = null;
                BukkitRunnable newTask = asyncUpdateMenuTask.getAndSet(null);
                if (newTask != null && !newTask.isCancelled()) {
                    newTask.cancel();
                }
            });
        }

        public void craft(Player player, int limit) {
            this.executor = player;
            MachineRecipe recipe = getRecipe();
            if (recipe == null) {
                AddUtils.sendMessage(player, "&6[高級快捷合成台] &c請先選擇一個配方");
            } else {
                BlockMenu inv = ContainerUtils.getPlayerBackPackWrapper(player);
                Pair<ItemGreedyConsumer[], ItemGreedyConsumer[]> results =
                        CraftUtils.countMultiRecipe(inv, craftableSlots, craftableSlots, recipe, limit, CRAFT_PROVIDER);
                long count = 0;
                if (results != null) {
                    count = CraftUtils.calMaxCraftTime(results.getSecondValue(), limit);
                    CraftUtils.multiUpdateInputMenu(results.getFirstValue(), inv);
                    CraftUtils.multiUpdateOutputMenu(results.getSecondValue(), inv);
                    CompletableFuture.supplyAsync(() -> {
                        this.onMenuRefreshByPlayerAsync(player);
                        return null;
                    });
                }
                AddUtils.sendMessage(player, "&6[高級快捷合成台] &a成功合成 %d 次".formatted(count));
            }
        }
    }

    private List<Integer> searchAllMatchedIndex(Player player, List<MachineRecipe> recipeList, int maxAmount) {
        BlockMenu playerContainerImpl = ContainerUtils.getPlayerBackPackWrapper(player);
        return CraftUtils.matchAllRecipe(
                playerContainerImpl,
                playerContainerImpl
                        .getPreset()
                        .getSlotsAccessedByItemTransport(playerContainerImpl, ItemTransportFlow.WITHDRAW, null),
                recipeList,
                CRAFT_PROVIDER,
                maxAmount);
    }
}
