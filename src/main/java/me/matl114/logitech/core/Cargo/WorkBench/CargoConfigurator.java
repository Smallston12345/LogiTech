package me.matl114.logitech.core.Cargo.WorkBench;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.matl114.logitech.Language;
import me.matl114.logitech.core.AddItem;
import me.matl114.logitech.core.Blocks.AbstractBlock;
import me.matl114.logitech.core.Cargo.Config.CargoConfigCard;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.Algorithms.PairList;
import me.matl114.logitech.utils.CraftUtils;
import me.matl114.logitech.utils.UtilClass.CargoClass.CargoConfigs;
import me.matl114.logitech.utils.UtilClass.ItemClass.ItemCounter;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CargoConfigurator extends AbstractBlock {
    protected final int[] BORDER = new int[] {0, 1, 2, 3, 5, 6, 7, 8, 27, 28, 34, 35};
    protected final int[] INFO_SLOTS = new int[] {4, 9, 10, 11, 12, 13, 14, 15, 16, 17, 30, 32};
    protected final int[] CONFIG_SLOTS = new int[] {18, 19, 20, 21, 22, 23, 24, 25, 26, 29, 33};
    protected final int OUTPUT_SLOT = 31;
    protected final ItemStack[] INFO_ITEMS = new ItemStack[] {
        new CustomItemStack(
                Material.ORANGE_STAINED_GLASS_PANE,
                "&6點擊構建貨運配置卡",
                "&e將任意貨運配置卡置於下方槽位",
                "&7並在對應的配置槽位中放入指定物品",
                "&a若配置卡上已有配置,只有非空配置槽位的配置會覆蓋原有配置項!",
                "&c若配置卡上沒有配置, 留空將默認false/0",
                "&a點擊這個按鈕,即可進行配置",
                "&e支持一次配置一組配置卡"),
        new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&6配置 強對稱 的配置槽位",
                "&e將[%s]或[%s]置於下方".formatted(Language.get("Items.TRUE_.Name"), Language.get("Items.FALSE_.Name")),
                "&7配置是否使用強對稱傳輸",
                "&7即是否將物品按槽位進行對應的運輸"),
        new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&6配置 僅空運輸 的配置槽位",
                "&e將[%s]或[%s]置於下方".formatted(Language.get("Items.TRUE_.Name"), Language.get("Items.FALSE_.Name")),
                "&7配置是否使用僅空傳輸",
                "&7即是否只傳向空槽位"),
        new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&6配置 懶惰模式 的配置槽位",
                "&e將[%s]或[%s]置於下方".formatted(Language.get("Items.TRUE_.Name"), Language.get("Items.FALSE_.Name")),
                "&7配置是否使用懶惰模式",
                "&7即是否在傳輸一次後停止"),
        new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&6配置 白名單 的配置槽位",
                "&e將[%s]或[%s]置於下方".formatted(Language.get("Items.TRUE_.Name"), Language.get("Items.FALSE_.Name")),
                "&7配置是否使用白名單",
                "&7即是否將物品列表視為白名單,默認黑名單"),
        new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&6配置 逆向傳輸 的配置槽位",
                "&e將[%s]或[%s]置於下方".formatted(Language.get("Items.TRUE_.Name"), Language.get("Items.FALSE_.Name")),
                "&7配置是否使用逆向傳輸",
                "&7交換原有的目標方塊和源方塊!即:",
                "&7⇨ 使用目標方塊模式&e從目標方塊抓取",
                "&7⇨ 使用源方塊模式向源方塊推送",
                "&7⇨ &e運輸模式會隨着方塊轉換"),
        // Modified
        new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&6配置 源方塊槽位優先級 的配置槽位",
                "&e將[%s]或[%s]置於下方".formatted(Language.get("Items.TRUE_.Name"), Language.get("Items.FALSE_.Name")),
                "&7配置源方塊模式(抓取)",
                "&7若該項為True,則優先從源方塊輸入槽進行貨運",
                "&7藴含最終源方塊模式 &e輸入槽 &7和 &e輸入槽+輸出槽",
                "&7若該項為False,則優先從源方塊輸出槽進行貨運",
                "&7藴含最終源方塊模式 &e輸出槽 &7和 &e輸出槽+輸入槽"),
        new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&6配置 目標方塊槽位優先級 的配置槽位",
                "&e將[%s]或[%s]置於下方".formatted(Language.get("Items.TRUE_.Name"), Language.get("Items.FALSE_.Name")),
                "&7配置目標方塊模式(推送)",
                "&7若該項為True,則優先從目標方塊輸出槽進行貨運",
                "&7藴含最終目標方塊模式 &e輸出槽 &7和 &e輸出槽+輸入槽",
                "&7若該項為False,則優先從目標方塊輸入槽進行貨運",
                "&7藴含最終目標方塊模式 &e輸入槽 &7和 &e輸入槽+輸出槽"),
        new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&6配置 源方塊++ 的配置槽位",
                "&e將[%s]或[%s]置於下方".formatted(Language.get("Items.TRUE_.Name"), Language.get("Items.FALSE_.Name")),
                "&7配置是否啓用源方塊++模式",
                "&7該模式為True時,貨運會同時涉及輸入槽和輸出槽",
                "&7++模式且抓取輸入槽優先:True時,最終源方塊模式為 &e輸入槽+輸出槽",
                "&7++模式且抓取輸入槽優先:False,最終源方塊模式為 &e輸出槽+輸入槽"),
        new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&6配置 目標方塊++ 的配置槽位",
                "&e將[%s]或[%s]置於下方".formatted(Language.get("Items.TRUE_.Name"), Language.get("Items.FALSE_.Name")),
                "&7配置是否啓用目標方塊++模式",
                "&7該模式為True時,貨運會同時涉及輸入槽和輸出槽",
                "&7++模式且推送輸出槽優先:True時,最終目標方塊模式為 &e輸出槽+輸入槽",
                "&7++模式且推送輸出槽優先:False,最終目標方塊模式為 &e輸入槽+輸出槽"),
        new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&6配置 傳輸數量限制x64 的配置槽位",
                "&e將[%s]置於左側".formatted(Language.get("Items.CARGO_PART.Name")),
                "&7配置傳輸數量",
                "&a傳輸數量增加<物品數量>*64"),
        new CustomItemStack(
                Material.GREEN_STAINED_GLASS_PANE,
                "&6配置 傳輸數量限制x1 的配置槽位",
                "&e將[%s]置於右側".formatted(Language.get("Items.CARGO_PART.Name")),
                "&7配置傳輸數量",
                "&a傳輸數量增加<物品數量>"),
    };
    protected final ItemStack TIPS_ITEM = AddUtils.randItemStackFactory(new PairList() {
        {
            put(new CustomItemStack(Material.KNOWLEDGE_BOOK, "&a小tips", "&7將僅空模式設置為true可以減少機器運行開銷"), 400);
            put(new CustomItemStack(Material.KNOWLEDGE_BOOK, "&a小tips", "&7強對稱模式很強大,或許你會很好的利用它"), 200);
            put(new CustomItemStack(Material.KNOWLEDGE_BOOK, "&a小tips", "&7懶惰模式會在首次傳輸某槽位後終止傳輸"), 200);
            put(new CustomItemStack(Material.KNOWLEDGE_BOOK, "&a小tips", "&7請不要讓傳輸雙方的目標槽位重疊,除非你想丟物品!"), 800);
            put(new CustomItemStack(Material.KNOWLEDGE_BOOK, "&a小tips", "&7若無特殊需求,儘量不要放置黑白名單,這有助於服務器健康"), 200);
            put(new CustomItemStack(Material.KNOWLEDGE_BOOK, "&a小tips", "&7將僅空模式設置為true可以減少機器運行開銷"), 200);
            put(new CustomItemStack(Material.KNOWLEDGE_BOOK, "&a小tips", "&7貨運配置卡可以同時配置一組!"), 200);
            put(new CustomItemStack(Material.KNOWLEDGE_BOOK, "&a小tips", "&7已經配置好的配置卡可以放入配置槽重配,這將覆蓋之前的設置"), 200);
            put(new CustomItemStack(Material.KNOWLEDGE_BOOK, "&a小tips", "&7將僅空模式設置為true可以減少機器運行開銷"), 200);
            put(new CustomItemStack(Material.KNOWLEDGE_BOOK, "&a小tips", "&7逆向模式看起來似乎沒什麼用,不過你可以在鏈式運輸中使用它"), 200);
            put(new CustomItemStack(Material.KNOWLEDGE_BOOK, "&a小tips", "&7本附屬通用貨運方法由matl114設計", "若有疑問可以尋找作者"), 100);
            put(new CustomItemStack(Material.KNOWLEDGE_BOOK, "&a小tips", "&7最有效的配置是除了僅空模式以外全設置false,同時調製合適的傳輸數量"), 100);
            put(
                    AddUtils.addGlow(new CustomItemStack(
                            Material.KNOWLEDGE_BOOK, "&x&E&B&3&3&E&B小彩蛋", "&b恭喜您抽中了1/3600概率的彩蛋", "&b想來您今天的運勢一定很好!")),
                    1);
        }
    });
    protected final int TIPS_SLOT = 35;
    protected ItemCounter MATCHED_CARGO = CraftUtils.getConsumer(AddItem.CARGO_PART);

    public CargoConfigurator(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    public int[] getConfigureSlots() {
        return CONFIG_SLOTS;
    }

    public int getConfigCardSlot() {
        return OUTPUT_SLOT;
    }

    public void constructMenu(BlockMenuPreset preset) {
        int[] slot = BORDER;
        int len = slot.length;
        for (int i = 0; i < len; i++) {
            preset.addItem(slot[i], ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        slot = INFO_SLOTS;
        len = slot.length;
        for (int i = 0; i < len; ++i) {
            preset.addItem(slot[i], INFO_ITEMS[i], ChestMenuUtils.getEmptyClickHandler());
        }
    }

    public boolean craft(BlockMenu inv, Player player) {
        ItemStack it = inv.getItemInSlot(getConfigCardSlot());
        if (it == null) {
            AddUtils.sendMessage(player, "&c配置失敗,請放入貨運配置卡");
            return false;
        }
        int[] slots = getConfigureSlots();
        ItemMeta meta = it.getItemMeta();
        if (CargoConfigCard.canConfig(meta)) {
            int code = 0;
            if (CargoConfigCard.isConfig(meta)) {
                code = CargoConfigCard.getConfig(meta);
            }
            ItemStack stack;
            CargoConfigs[] configs = CargoConfigs.values();
            for (int i = 0; i < 9; ++i) {
                stack = inv.getItemInSlot(slots[i]);
                if (stack == null) {
                    continue;
                }
                if (stack.getType() == Material.MUSIC_DISC_5 && stack.hasItemMeta()) {
                    // 有附魔是true 沒是false
                    int icode = stack.getEnchantments().isEmpty() ? 0 : 1;
                    code = configs[i].setConfig(code, icode); // CargoConfigs.setConfigBit(code,icode,i);
                } else {
                    AddUtils.sendMessage(player, "&c配置失敗,這不是布爾組件");
                    return false;
                }
            }
            stack = inv.getItemInSlot(slots[9]);
            if (stack != null && stack.getType() != Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE) {
                AddUtils.sendMessage(player, "&c配置失敗,配置傳輸數量限制的物品不符");
                return false;
            }
            int stackAmount = 0;
            if (stack != null) {
                stackAmount += (stack.getAmount()) * 64;
            }
            stack = inv.getItemInSlot(slots[10]);
            if (stack != null && stack.getType() != Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE) {
                AddUtils.sendMessage(player, "&c配置失敗,配置傳輸數量限制的物品不符");
                return false;
            }
            if (stack != null) {
                stackAmount += stack.getAmount();
            }
            if (stackAmount != 0) {
                code = CargoConfigs.TRANSLIMIT.setConfig(code, stackAmount);
            }
            if (CargoConfigs.TRANSLIMIT.getConfigInt(code) <= 0) {
                AddUtils.sendMessage(player, "&c配置失敗,傳輸數量限制不能為0!");
                return false;
            }
            CargoConfigCard.setConfig(meta, code);
            it.setItemMeta(meta);
            for (int slot : slots) {
                inv.replaceExistingItem(slot, null);
            }
            return true;
        }
        AddUtils.sendMessage(player, "&c配置失敗,請放入貨運配置卡");
        return false;
    }

    public void newMenuInstance(BlockMenu inv, Block block) {
        inv.addMenuOpeningHandler(player -> {
            inv.replaceExistingItem(TIPS_SLOT, TIPS_ITEM.clone());
        });
        inv.addMenuClickHandler(INFO_SLOTS[0], ((player, i, itemStack, clickAction) -> {
            if (craft(inv, player)) {
                AddUtils.sendMessage(player, "&a配置成功!");
            }
            return false;
        }));
    }

    public void onBreak(BlockBreakEvent event, BlockMenu menu) {
        super.onBreak(event, menu);
        if (menu != null) {
            Location loc = menu.getLocation();
            menu.dropItems(loc, getConfigureSlots());
            menu.dropItems(loc, getConfigCardSlot());
        }
    }

    public void preRegister() {
        super.preRegister();
        this.registerBlockMenu(this);
    }
}
