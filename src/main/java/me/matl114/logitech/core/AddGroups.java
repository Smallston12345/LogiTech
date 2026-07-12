package me.matl114.logitech.core;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.groups.FlexItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import java.util.*;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import me.matl114.logitech.core.Machines.WorkBenchs.BugCrafter;
import me.matl114.logitech.core.Registries.CustomEffects;
import me.matl114.logitech.core.Registries.RecipeSupporter;
import me.matl114.logitech.manager.PostSetupTasks;
import me.matl114.logitech.manager.Schedules;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.MathUtils;
import me.matl114.logitech.utils.MenuUtils;
import me.matl114.logitech.utils.UtilClass.EffectClass.PlayerEffects;
import me.matl114.logitech.utils.UtilClass.MenuClass.*;
import me.matl114.logitech.utils.UtilClass.SpecialItemClass.CustomHead;
import me.matl114.matlib.implement.slimefun.menu.menuClickHandler.GuideClickHandler;
import me.matl114.matlib.implement.slimefun.menu.menuGroup.CustomMenuGroup;
import me.matl114.matlib.utils.inventory.itemStacks.CleanItemStack;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AddGroups {
    public static void registerGroups(SlimefunAddon plugin) {
        ROOT.register(plugin);
        INFO.register(plugin);
        MATERIAL.register(plugin);
        BASIC.register(plugin);
        CARGO.register(plugin);
        SINGULARITY.register(plugin);
        ADVANCED.register(plugin);
        BEYOND.register(plugin);
        VANILLA.register(plugin);
        MANUAL.register(plugin);
        SPECIAL.register(plugin);
        SPACE.register(plugin);
        GENERATORS.register(plugin);
        ENERGY.register(plugin);
        FUNCTIONAL.register(plugin);
        EXPERIMENTAL.register(plugin);
        TOOLS_FUNCTIONAL.register(plugin);
        TOOLS_SUBGROUP_1.register(plugin);
        TOOLS.register(plugin);
        ROOT.setTier(0);
    }
    // 給你的分類提供一個獨一無二的ID
    protected static final NamespacedKey itemGroupId = AddUtils.getNameKey("addon_category");
    protected static final NamespacedKey bugcrafterId = AddUtils.getNameKey("bugcrafter");

    public static final ItemGroup MATERIAL = new DummyItemGroup(AddUtils.getNameKey("material"), AddItem.MATERIAL);

    public static final ItemGroup BASIC = new DummyItemGroup(AddUtils.getNameKey("basic"), AddItem.BASIC);

    public static final ItemGroup CARGO = new DummyItemGroup(AddUtils.getNameKey("cargo"), AddItem.CARGO);

    public static final ItemGroup SINGULARITY =
            new DummyItemGroup(AddUtils.getNameKey("singularity"), AddItem.SINGULARITY);

    public static final ItemGroup ADVANCED = new DummyItemGroup(AddUtils.getNameKey("advanced"), AddItem.ADVANCED);

    public static final ItemGroup BEYOND = new DummyItemGroup(AddUtils.getNameKey("beyond"), AddItem.BEYOND);

    public static final ItemGroup VANILLA = new DummyItemGroup(AddUtils.getNameKey("vanilla"), AddItem.VANILLA);

    public static final ItemGroup MANUAL = new DummyItemGroup(AddUtils.getNameKey("manual"), AddItem.MANUAL);

    public static final ItemGroup SPECIAL = new DummyItemGroup(AddUtils.getNameKey("special"), AddItem.SPECIAL);

    public static final ItemGroup SPACE = new DummyItemGroup(AddUtils.getNameKey("space"), AddItem.SPACE);

    public static final ItemGroup GENERATORS =
            new DummyItemGroup(AddUtils.getNameKey("generators"), AddItem.GENERATORS);

    public static final ItemGroup ENERGY = new DummyItemGroup(AddUtils.getNameKey("energy"), AddItem.ENERGY);

    public static final ItemGroup TOOLS_FUNCTIONAL =
            new DummyItemGroup(AddUtils.getNameKey("tools-functional"), AddItem.TOOLS) {
                @Override
                public void add(@Nonnull SlimefunItem item) {
                    super.add(item);
                }

                @Override
                public void remove(@Nonnull SlimefunItem item) {
                    super.remove(item);
                }
            };
    public static final ItemGroup TOOLS_SUBGROUP_1 =
            new DummyItemGroup(AddUtils.getNameKey("tools-sub1"), AddItem.TOOLS_SUBGROUP_1);
    public static final ItemGroup TOOLS_SUBGROUP_2 =
            new DummyItemGroup(AddUtils.getNameKey("tools-sub2"), AddItem.TOOLS_SUBGROUP_2);

    public static final ItemGroup FUNCTIONAL =
            new DummyItemGroup(AddUtils.getNameKey("functional"), AddItem.FUNCTIONAL);

    public static final ItemGroup EXPERIMENTAL =
            new DummyItemGroup(AddUtils.getNameKey("experimental"), AddItem.EXPERIMENTAL);

    public static ItemGroup MORE2 =
            new CustomItemGroup(
                    AddUtils.getNameKey("more2"),
                    AddUtils.color("宇宙安全説明"),
                    AddItem.MORE2,
                    54,
                    36,
                    new LinkedHashMap<>()) {
                @Override
                protected void init(MenuFactory menuFactory) {
                    menuFactory.addInventory(
                            0,
                            AddUtils.getInfoShow(
                                    "&f宇宙安全説明",
                                    "&7感謝您能點開到這裏,看到這份説明",
                                    "&7本附屬為我開發的首個附屬,無人問津是應該的",
                                    "&7您能點進這一分類已經是對我最大的鼓勵和支持",
                                    "&7本人水平很次,也沒有很好的創作能力創作讓所有玩家滿意的附屬",
                                    "&7還請包容"));
                    menuFactory.addInventory(
                            1,
                            AddUtils.getInfoShow(
                                    "&f宇宙安全説明",
                                    "&7這個附屬在某些方面可以説很爛",
                                    "&7他擁有冗長的機制説明書和昂貴的合成表",
                                    "&7以及作者亂填的,毫無道理的材料消耗",
                                    "&7為什麼呢",
                                    "&7因為作者並不知道怎麼編一個讓玩家滿意的合成表",
                                    "&7很可能有些時候,在這方面給玩家帶來很大的困擾"));
                    menuFactory.addInventory(
                            2,
                            AddUtils.getInfoShow(
                                    "&f宇宙安全説明",
                                    "&7作者也不知道為什麼作者要寫這一個附屬",
                                    "&7作者已經投入很多閒暇時間進行附屬編寫",
                                    "&7作者也&a希望這個附屬能變得更好&7,而不是更讓玩家噁心",
                                    "&7作者&a希望附屬對玩家有幫助,而不是對玩家造成困擾"));

                    menuFactory.addInventory(
                            3,
                            AddUtils.getInfoShow(
                                    "&f宇宙安全説明",
                                    "&7這個附屬還很糟糕,他沒有一個所謂的\"主題\"",
                                    "&7他既沒有亂碼科技的製造台,也沒有亂序技藝的塵埃反應堆",
                                    "&7作者一開始的目標是製作優秀的機器,雖然現在改為了製作高效的機器",
                                    "&7但是並沒有非常超標的機器存在",
                                    "&7還請玩家包容",
                                    "&7如果你只是想找op機器",
                                    "&7那麼本附屬可能並不符合你的心意"));
                    menuFactory.addInventory(
                            4,
                            AddUtils.getInfoShow(
                                    "&f宇宙安全説明",
                                    "&7作者能拿的出手的機制就這幾個",
                                    "&7雖説抱歉,但是作者也不會因此而做op機器和刷物機器",
                                    "&a但是玩家有什麼好的想法或者急切的機器需求",
                                    "&a也是可以和作者提的",
                                    "",
                                    "&7作者真心希望該附屬越做越好,而不是越來越爛",
                                    "&a所以我在此懇請大家主動發表自己的看法"));
                    menuFactory.addInventory(
                            5,
                            AddUtils.getInfoShow(
                                    "&f宇宙安全説明",
                                    "&7所以,作者在此懇請大家",
                                    "&a多多包容作者,並把自己的想法跟作者討論",
                                    "&a你可以在版本與説明中找到作者的qq號和作者進行交流",
                                    "&a也可以上附屬github鏈接提出issue",
                                    "&a不管如何,作者都會聽取玩家的意見,爭取將附屬做得更好"));
                    menuFactory.addInventory(
                            6,
                            AddUtils.getInfoShow(
                                    "&f宇宙安全説明",
                                    "&7雖説致歉,但是作者還是會&a堅定的繼續開發和維護這個附屬",
                                    "&7雖然作者也曾想過放棄該附屬",
                                    "&7但是也只是一時之念,大家不要當真",
                                    "&7請大家放心,作者並不會丟下一堆爛攤子跑路"));
                    menuFactory.addInventory(
                            7,
                            AddUtils.getInfoShow(
                                    "&f宇宙安全説明",
                                    "&7最後,在此對&e所有覺得這個附屬沒救的玩家&7致辭",
                                    "",
                                    "&7黏液圈雖然不大,但是比本附屬好的附屬也是比比皆是",
                                    "&7作者的生活中也不只是有黏液,作者也要有自己的生活,並不能幫你救活這個附屬",
                                    "&7您也不必拘泥於這個附屬,我們好聚好散,對大家都好"));
                    menuFactory.addInventory(
                            8, AddUtils.getInfoShow("&f宇宙安全説明", "&7最後聲明:本説明並不針對某一特定玩家", "&7當您對號入座時,請不要坐岔了,謝謝."));
                }

                @Override
                protected void addGuideRelated(
                        ChestMenu menu, Player p, PlayerProfile profile, SlimefunGuideMode mode, int pages) {}
            };
    public static ItemGroup UPDATELOG =
            new CustomItemGroup(
                    AddUtils.getNameKey("log"),
                    AddUtils.colorful("更新日誌"),
                    AddItem.UPDATELOG,
                    54,
                    36,
                    new LinkedHashMap<>()) {
                @Override
                protected void init(MenuFactory factory) {
                    factory.addInventory(0, AddUtils.getInfoShow("&f更新日誌", "&7此頁為簡易的更新日誌", "&7你或許可以找到你想要的新特性"));
                    factory.addInventory(
                            1,
                            AddUtils.getInfoShow(
                                    "&f2024.8.28",
                                    "&7創建日誌",
                                    "&7創建新的終極機器用於測試",
                                    "&7大型配方展示組增加歷史記錄",
                                    "&7修復模擬合成台不顯示黑名單的bug",
                                    "&7修復原版配方讀取,將礦辭讀為等價物品組"));
                    factory.addInventory(
                            2,
                            AddUtils.getInfoShow(
                                    "&f2024.8.29",
                                    "&7修改部分描述",
                                    "&7修復了自定義菜單歷史記錄bug",
                                    "&7平衡性調整",
                                    "&7修改了等價物品組配置的讀取方式",
                                    "&7修改了語言文件讀取方式,並研發了自動提取字符串腳本",
                                    "&7研發出亂碼科技的英文版\"亂翻科技\"",
                                    "&7我沒有痴呆症"));
                    factory.addInventory(
                            3,
                            AddUtils.getInfoShow(
                                    "&f2024.8.30",
                                    "&7進行平衡性調整",
                                    "&7修復分揀機與糟糕sft導致的衝突",
                                    "&7新增若干貨運機器與其配方",
                                    "&7遇見了芯片最優解",
                                    "&7與玩家交流,獲得反饋",
                                    "&7出現了手殘破壞多方塊結構的玩家",
                                    "&7大家一定要小心謹慎,少得痴呆症"));
                    factory.addInventory(
                            4,
                            AddUtils.getInfoShow(
                                    "&f&m2024.8.30 &r&f2024.8.31",
                                    "&7增加了可調節的貨運機器",
                                    "&7開始研發邏輯奇點部分的機器",
                                    "&7發現遠古測試機器的bug",
                                    "&7中午出門把身份證丟了,晦氣",
                                    "&7下午治療身份證去了,沒更新痴呆症",
                                    "&7大家一定要小心謹慎,少得身份症",
                                    "&7晚上和haiman整了SlimefunEssentials",
                                    "&7JEI十分的強勁,我覺得我都不用寫搜索了"));
                    factory.addInventory(
                            5,
                            AddUtils.getInfoShow(
                                    "&f2024.9.1",
                                    "&7修復薯片核心配方無法原版自動化",
                                    "&7到處轉轉,學會怎麼上傳頭顱",
                                    "&7完成終極機制之遠程接入存儲,但是出現了小的bug",
                                    "&7修一個小bug 花費了我好久。實際上這是我算法架構的問題",
                                    "&7但是我還是嘗試了若干方案之後,最終決定這麼搞,希望不會炸",
                                    "&7經過了億點重構,準備開始測試了,希望人沒事"));
                    factory.addInventory(
                            6,
                            AddUtils.getInfoShow("&f2024.9.1", "&7今天是幾號來着", "&7方案全部無效", "&7新特性不能就這麼寄了", "&7躺下睡覺吧還是"));
                    factory.addInventory(
                            7,
                            AddUtils.getInfoShow(
                                    "&f2024.9.3",
                                    "&7哈哈哈哈哈哈哈哈",
                                    "&7終於想到一個方法了",
                                    "&7既然他提供超過兩個引用會寄,那我就要根據menu和slots為單位控制產生的引用數",
                                    "&7不要再對引用位置做無所謂的控制,這沒有必要",
                                    "&7只要將cache生成器包裝入一個方法 然後在方法裏狠狠注入檢測代碼就可以了!",
                                    "&7我他孃的還真是小機靈鬼",
                                    "&7開幹",
                                    "&7終於,,,修好了!"));
                    factory.addInventory(
                            8,
                            AddUtils.getInfoShow(
                                    "&f2024.9.3",
                                    "&7我一定是記錯日期了,不然為什麼一覺醒來日期沒變呢",
                                    "&7繼續完善存儲鏈接體系",
                                    "&7應廣大玩家要求,修改邏輯反應堆為運行中不銷耗布爾組件,只在搜索階段消耗",
                                    "&7發現配方搜素算法一個cache未及時同步的bug",
                                    "&7好在在新測試員的幫助下很快解決.",
                                    "&7比Tmatsuki_rui能幹多了."));
                    factory.addInventory(
                            9,
                            AddUtils.getInfoShow(
                                    "&f2024.9.4",
                                    "&7一不小心忘了寫了",
                                    "&7還能寫啥呢",
                                    "&7修bug,完善終極科技線",
                                    "&7修復超新星不同步bug",
                                    "&7修復了存儲緩存生成後dirty位錯誤的bug"));
                    factory.addInventory(
                            10,
                            AddUtils.getInfoShow(
                                    "&f2024.9.5",
                                    "&7是時候給附屬加一點強力的生產機器了",
                                    "&7不然都沒人玩,成本太高了",
                                    "&7開發了多線程機器,雖然就是複製粘貼",
                                    "&7研究一下同化方塊機制",
                                    "&7嘗試優化push方法"));
                    factory.addInventory(
                            11,
                            AddUtils.getInfoShow(
                                    "&f2024.9.6", "&7整了一些機器 想必玩家們會喜歡", "&7開發了一些數值比較好看的機器", "&7希望是這樣的", "&7"));
                    factory.addInventory(
                            12,
                            AddUtils.getInfoShow(
                                    "&f2024.9.7",
                                    "&7整了一些機器 想必玩家們會喜歡",
                                    "&7整了一款全新的高端多方塊",
                                    "&7包括兩個等級和一些激發機制",
                                    "&7我覺得那真是太酷啦"));
                    factory.addInventory(
                            13,
                            AddUtils.getInfoShow(
                                    "&f2024.9.7",
                                    "&7開發了LogiTech shell",
                                    "&7一款基於原神(bushi)的shell命令行工具",
                                    "&7可以查詢和修改類/對象的成員等等,支持設置變量",
                                    "&7緊急修復了因dataload導致的卡服",
                                    "&7這就是依託"));
                    factory.addInventory(
                            14,
                            AddUtils.getInfoShow(
                                    "&f2024.9.9",
                                    "&7開學了",
                                    "&7開學第一天沒課",
                                    "&7兼容構思rsc生成器隨機輸出",
                                    "&7我希望開啓一個計劃,讓大家一起完善machine堆疊列表"));
                    factory.addInventory(
                            15,
                            AddUtils.getInfoShow(
                                    "&f2024.9.12",
                                    "&7增加了內置文件",
                                    "&7增加了亂碼的機器列表",
                                    "&7恕我直言,起名真亂",
                                    "&7還有ag科技,一個配方機器都找不到",
                                    "&7純懶狗附屬"));
                    factory.addInventory(
                            16, AddUtils.getInfoShow("&f2024.9.27", "&7顯然 我忘了記錄日誌", "&7這甜蜜確實挺糟糕的", "&7不如之後我就寫更新日誌"));
                    factory.addInventory(
                            18,
                            AddUtils.getInfoShow(
                                    "&f2024.9.27更新",
                                    "&7修復存儲交互接口潛在bug",
                                    "&7修復堆疊機器列表導入重複bug",
                                    "&7修復反概念物質擴散不會繼承玩家權限bug",
                                    "&7增加shell玩家權限檢測",
                                    "&7修復簡易堆疊破爛的bug",
                                    "&7增加星辰金熔爐的槽位推送限制",
                                    "&7修復了未插入貨運配置卡時code讀取異常bug",
                                    "&7還修了啥不太記得了",
                                    "&7增加了烘乾機到快捷綜合機器",
                                    "&7增加赤石科技道具",
                                    "&7減少超頻刷怪籠的合成需求",
                                    "&7修復了堆疊機器無法支持非消耗物品的bug",
                                    "&7準備增加分揀機",
                                    "&7增加堆疊機器配置文件直接導入配方類型的功能",
                                    "&7增加tnt複製機",
                                    "&7增加破基岩棒",
                                    "&7修復邏輯反應堆槽位提供異常",
                                    ""));
                    factory.addInventory(
                            19,
                            AddUtils.getInfoShow(
                                    "&f2024.10.12更新",
                                    "&7修復部分bug,雖然忘了修了啥了",
                                    "&7增加了激光發射器 進階版",
                                    "&7增加桃花飛針,好吃",
                                    "&7增加更多的赤石道具",
                                    "&7增加了超級海綿",
                                    "&7更新啥了忘了",
                                    "&7更新了空間塔",
                                    "&7發掘了moveSfData的真相",
                                    "&7發掘了刷物特性",
                                    "&7修復了大量潛在問題,並增加刷物提示",
                                    "&7空間塔不再支持移動黏液機器",
                                    "&7增加了隨機傳送符文",
                                    "&7增加了物品命名機(物品操作枱)",
                                    "&7忘了",
                                    "&7註冊了只使用原版合成台合成的道具",
                                    "&7記得找個時間補配方",
                                    "&7補啥配方",
                                    "&7修復了無盡礦機配方導入異常",
                                    "&7修復了物品匹配異常",
                                    "&7做好了區塊充電器,按理論講耗時可控,比能源電網要好",
                                    "&7這次更新就先做到這裏吧 剩下的放到下次更新",
                                    "&7更新到了v1.0.3"));
                    factory.addInventory(
                            20,
                            AddUtils.getInfoShow(
                                    "&f2024.?更新",
                                    "&7修復部分bug,雖然忘了修了啥了",
                                    "&7增加了wiki",
                                    "&7好像修了點bug",
                                    "&7重構部分代碼,優化了多方塊結構數據存儲",
                                    "&7限制了區塊充能器",
                                    "&7完善了一個較為完整的電力系統",
                                    "&7完成了電線",
                                    "&7修復了bug",
                                    "&7加入了神金的小道具",
                                    "&7加入了高級快捷機器,我感覺很好用",
                                    "&7加入了貨運管道,玩家一定會喜歡的",
                                    "&7加入了對原版容器的貨運支持",
                                    "&7加入了好多其他的東西,忘了",
                                    "&7加入了黏液替代卡",
                                    "&7加入了配置器",
                                    "&7加入了"));
                }

                @Override
                protected void addGuideRelated(
                        ChestMenu menu, Player p, PlayerProfile profile, SlimefunGuideMode mode, int pages) {}
            };
    public static ItemGroup BUGCRAFTER =
            new CustomItemGroup(
                    bugcrafterId,
                    AddUtils.colorful(AddUtils.ADDON_NAME),
                    AddItem.ALLBIGRECIPES,
                    54,
                    36,
                    new LinkedHashMap<>()) {
                public PlayerHistoryRecord<CustomMenu> historyHandler = new SimplePlayerHistoryRecord<CustomMenu>();
                MenuFactory subRecipes = null;

                public void initCustomMenus() {
                    subRecipes = MenuUtils.createItemListDisplay(
                            BugCrafter.TYPE,
                            RecipeSupporter.RECIPETYPE_TO_ITEMS.get(BugCrafter.TYPE),
                            null,
                            historyHandler,
                            MenuUtils::createMRecipeDisplay,
                            true);
                }

                public MenuFactory getSubRecipes() {
                    if (subRecipes == null) {
                        initCustomMenus();
                    }
                    return subRecipes;
                }

                protected void init(MenuFactory factory) {
                    ItemStack menuBackGround = new CustomItemStack(Material.PURPLE_STAINED_GLASS_PANE, "");
                    factory.addOverrides(
                            4, AddItem.URL, (Player player1, int i1, ItemStack itemstack1, ClickAction clickAction) -> {
                                final TextComponent link = new TextComponent("點擊此處訪問Github");
                                link.setColor(ChatColor.YELLOW);
                                link.setClickEvent(new ClickEvent(
                                        ClickEvent.Action.OPEN_URL, "https://github.com/m1919810/LogiTech"));
                                player1.spigot().sendMessage(link);
                                return false;
                            });
                    factory.setBackGround(menuBackGround);
                    PostSetupTasks.addPostRegisterTask(this::initCustomMenus);
                }

                public void openPage(Player var1, PlayerProfile var2, SlimefunGuideMode var3, int page) {

                    var2.getGuideHistory().add(this, 1);
                    CustomMenu menu = historyHandler.getRecord(var1);
                    if (menu != null) {
                        ChestMenu historyMenu = menu.constructPage(-1);
                        historyMenu.open(var1);
                        return;
                    }
                    CustomMenu menu2 = getSubRecipes().build();
                    menu2.setBackSlot(1);
                    menu2.setBackHandler(((player, i, itemStack, clickAction) -> {
                        historyHandler.deleteAllRecords(player);
                        var2.getGuideHistory().goBack(Slimefun.getRegistry().getSlimefunGuide(var3));
                        return false;
                    }));
                    menu2.openPages(var1, 1);
                }

                @Override
                protected void addGuideRelated(
                        ChestMenu menu, Player p, PlayerProfile profile, SlimefunGuideMode mode, int pages) {}
            };

    public static final ItemGroup INFO =
            new CustomItemGroup(AddUtils.getNameKey("info"), null, AddItem.INFO, 54, 36, new LinkedHashMap<>()) {
                @Override
                protected void init(MenuFactory factory) {
                    this.setVisble(false);
                    factory.addInventory(1, AddItem.INFO1);
                    factory.addInventory(11, AddItem.INFO2);
                    factory.addInventory(21, AddItem.INFO3);
                    factory.addInventory(7, AddItem.INFO4);
                    factory.addInventory(15, AddItem.INFO5);
                    factory.addInventory(23, AddItem.INFO6);
                    factory.addInventory(
                            13,
                            AddItem.WIKI,
                            (Player player1, int i1, ItemStack itemstack1, ClickAction clickAction) -> {
                                final TextComponent link = new TextComponent("點擊此處訪問附屬wiki");
                                link.setColor(ChatColor.YELLOW);
                                link.setClickEvent(new ClickEvent(
                                        ClickEvent.Action.OPEN_URL, "https://github.com/m1919810/LogiTech/wiki"));
                                player1.spigot().sendMessage(link);
                                return false;
                            });
                    factory.addInventory(27, AddItem.FEAT1);
                    factory.addInventory(28, AddItem.FEAT2);
                    factory.addInventory(29, AddItem.FEAT3);
                    factory.addInventory(30, AddItem.FEAT4);
                    factory.addInventory(31, AddItem.FEAT5);
                    factory.addInventory(32, AddItem.FEAT6);
                    factory.addInventory(33, AddItem.FEAT7);
                    factory.addInventory(34, AddItem.FEAT8);
                    factory.addInventory(35, AddItem.FEAT9);
                    factory.addOverrides(4, AddItem.MATL114);
                }

                @Override
                protected void addGuideRelated(
                        ChestMenu menu, Player p, PlayerProfile profile, SlimefunGuideMode mode, int pages) {
                    for (int s = 0; s < 2; ++s) {
                        int rand = AddUtils.random(36);
                        if (menu.getItemInSlot(rand + 9) == null) {
                            menu.replaceExistingItem(rand + 9, AddUtils.getWithoutId(AddItem.BUG));
                            menu.addMenuClickHandler(rand + 9, ((player, i, itemStack, clickAction) -> {
                                AddUtils.forceGive(player, AddItem.BUG, 1);
                                menu.replaceExistingItem(rand + 9, null);
                                menu.addMenuClickHandler(rand + 9, ChestMenuUtils.getEmptyClickHandler());
                                return false;
                            }));
                            break;
                        }
                    }
                }
            };

    public static final ItemGroup TOOLS = new me.matl114.matlib.implement.slimefun.menu.guideMenu.CustomItemGroup(
                    AddUtils.getNameKey("tools"), AddItem.TOOLS, true)
            .setLoader(
                    new CustomMenuGroup(AddItem.TOOLS.getItemMeta().getDisplayName(), 54, 1) {
                        {
                            // initialize
                            enablePresets((i) -> {
                                        ChestMenu menu =
                                                this.getDefaultGenerator().apply(i);
                                        IntStream.range(0, 9)
                                                .forEach(t -> menu.addItem(
                                                        t,
                                                        ChestMenuUtils.getBackground(),
                                                        ChestMenuUtils.getEmptyClickHandler()));
                                        IntStream.range(45, 54)
                                                .forEach(t -> menu.addItem(
                                                        t,
                                                        ChestMenuUtils.getBackground(),
                                                        ChestMenuUtils.getEmptyClickHandler()));
                                        return menu;
                                    })
                                    .setPageNextSlots(52)
                                    .setPagePrevSlots(46)
                                    .enableContentPlace(IntStream.range(9, 45).toArray())
                                    .addItem(0, AddItem.TOOLS_RECIPES, (cm) -> (GuideClickHandler)
                                            (Player var1,
                                                    int var2,
                                                    ItemStack var3,
                                                    ClickAction var4,
                                                    PlayerProfile var5,
                                                    SlimefunGuideMode var6,
                                                    FlexItemGroup var7,
                                                    int var8) -> {
                                                // left as not completed yet
                                                return false;
                                            });
                        }
                    },
                    () -> {
                        int itemSize = TOOLS_FUNCTIONAL.getItems().size();
                        HashMap<Integer, ItemGroup> items = new HashMap<>();
                        items.put(itemSize + 2, TOOLS_SUBGROUP_1);
                        items.put(itemSize + 1, TOOLS_SUBGROUP_2);
                        return items;
                    },
                    () -> {
                        var re = TOOLS_FUNCTIONAL.getItems();
                        var returned = new HashMap<Integer, SlimefunItem>();
                        for (int i = 0; i < re.size(); ++i) {
                            returned.put(i + 1, re.get(i));
                        }
                        return returned;
                    })
            .setBackButton(1)
            .setSearchButton(7);

    public static final ItemGroup ROOT =
            new CustomItemGroup(
                    itemGroupId, AddUtils.colorful(AddUtils.ADDON_NAME), AddItem.ROOT, 54, 144, new LinkedHashMap<>() {
                        {
                            put(2, MATERIAL);
                            put(0, INFO);
                            put(27, BUGCRAFTER);
                            put(4, BASIC);
                            put(3, MANUAL);
                            put(6, ENERGY);
                            put(5, GENERATORS);
                            put(22, SPACE);
                            put(21, VANILLA);
                            put(17, SPECIAL);
                            put(20, CARGO);
                            put(23, ADVANCED);
                            put(24, SINGULARITY);
                            put(31, BEYOND);
                            put(36, UPDATELOG);
                            put(26, MORE2);
                            put(35, EXPERIMENTAL);

                            put(8, TOOLS);
                        }
                    }) {
                public MenuFactory MACHINEMENU = null;
                public MenuFactory RECIPEMENU = null;

                public void initCustomMenus() {
                    MACHINEMENU = MenuUtils.createMachineListDisplay(
                                    RecipeSupporter.MACHINE_RECIPELIST.keySet().stream()
                                            .toList(),
                                    null)
                            .setBack(1);
                    RECIPEMENU = MenuUtils.createRecipeTypeDisplay(
                                    RecipeSupporter.RECIPE_TYPES.stream().toList(), null)
                            .setBack(1);
                    //
                    // BUGCRAFTER=MenuUtils.createMRecipeListDisplay(AddItem.BUG_CRAFTER,RecipeSupporter.PROVIDED_SHAPED_RECIPES.get(BugCrafter.TYPE),
                    //                    null,MenuUtils::createMRecipeDisplay);
                }

                public MenuFactory getMachineMenu() {
                    if (MACHINEMENU == null) {
                        initCustomMenus();
                    }
                    return MACHINEMENU;
                }

                public MenuFactory getRecipeMenu() {
                    if (RECIPEMENU == null) {
                        initCustomMenus();
                    }
                    return RECIPEMENU;
                }
                //        public MenuFactory getBugCrafterMenu(){
                //            if(BUGCRAFTER==null){
                //                initCustomMenus();
                //            }
                //            return BUGCRAFTER;
                //        }
                // used to set common handlers and common params
                public void init(MenuFactory factory) {
                    this.setVisble(true);
                    // 對模板進行最高級別的覆寫
                    ItemStack menuBackGround = new CustomItemStack(Material.PURPLE_STAINED_GLASS_PANE, "");
                    factory.addOverrides(
                            4, AddItem.URL, (Player player1, int i1, ItemStack itemstack1, ClickAction clickAction) -> {
                                final TextComponent link = new TextComponent("點擊此處訪問Github");
                                link.setColor(ChatColor.YELLOW);
                                link.setClickEvent(new ClickEvent(
                                        ClickEvent.Action.OPEN_URL, "https://github.com/m1919810/LogiTech"));
                                player1.spigot().sendMessage(link);
                                final TextComponent link2 = new TextComponent("點擊此處訪問附屬wiki");
                                link2.setColor(ChatColor.YELLOW);
                                link2.setClickEvent(new ClickEvent(
                                        ClickEvent.Action.OPEN_URL, "https://github.com/m1919810/LogiTech/wiki"));
                                player1.spigot().sendMessage(link2);
                                return false;
                            });
                    factory.setBackGround(menuBackGround);
                    final int[] BORDER = new int[] {1, 7, 10, 11, 12, 13, 14, 15, 16, 19, 25, 28, 34};
                    int len = BORDER.length;
                    for (int i = 0; i < len; ++i) {
                        factory.addInventory(BORDER[i], menuBackGround);
                    }
                    // 對模板內的填充物進行修改
                    factory.addInventory(9, AddItem.ALLMACHINE);
                    factory.addInventory(18, AddItem.ALLRECIPE);
                    factory.addInventory(27, AddItem.ALLBIGRECIPES);
                    //            factory.addInventory(35,AddItem.TOBECONTINUE);
                    factory.addInventory(72, new CleanItemStack(Material.COMMAND_BLOCK, "&6別看了", "&7這裏真的啥都沒有了"));
                    PostSetupTasks.addPostRegisterTask(this::initCustomMenus);
                    // 對自動生成的物品組位置進行修改(如果使用hashmap指定物品組則不用修改
                    // 彩蛋
                    ItemStack info1 =
                            new CustomItemStack(Material.CHERRY_HANGING_SIGN, AddUtils.color("請選擇你的英雄"), "&8可不要走錯方向哦~");
                    ItemStack h1 = new CustomItemStack(
                            CustomHead.getHead("fd524332cdb381c9e51f77d4cec9bc6d4d1c5bdec1499d206d8383e9176bdfb0"),
                            AddUtils.color("haiman"),
                            "&7點擊查看詳情");
                    ItemStack h2 = new CustomItemStack(
                            CustomHead.getHead("a9b046531a6182de634d6fed1f3b4f885ee99bfe2bc0c1684f7b97d396c2059f"),
                            AddUtils.color("mmmjjkx"),
                            "&7點擊查看詳情");
                    ItemStack h3 = new CustomItemStack(
                            CustomHead.getHead("7e224b7fb1e9dc78b2abb6a3fbf726b1d7159d08599b2c02b7bf1ad396285da6"),
                            AddUtils.color("tinalness"),
                            "&7點擊查看詳情");
                    ItemStack h4 = new CustomItemStack(
                            CustomHead.SUPPORTER2.getItem(), AddUtils.color("HgTlPbBi"), "&7點擊查看詳情");
                    ItemStack h5 = new CustomItemStack(
                            CustomHead.getHead("8e434215b5616bf37dccbacdb240bd16de59507e62a5371ceca80327b398e65"),
                            AddUtils.color("Tmatsuki_rui"),
                            "&7點擊查看詳情");
                    ItemStack h6 = new CustomItemStack(AddItem.MATL114, "  ", "&8..?");
                    ItemStack h7 =
                            new CustomItemStack(CustomHead.TOURUYA.getItem(), AddUtils.color("TouRuya"), "&7點擊查看詳情");
                    ItemStack h8 = new CustomItemStack(
                            CustomHead.BIG_SNAKE.getItem(), AddUtils.color("某不知名大蛇"), "&7年輕人,你想刷物麼");
                    ItemStack h9 =
                            new CustomItemStack(CustomHead.MOYU.getItem(), AddUtils.color("xiaoUI1"), "&7點擊查看詳情");

                    factory.addInventory(40, info1);
                    factory.addInventory(56, h1, (cm) -> new ChestMenu.MenuClickHandler() {
                        boolean has = false;

                        @Override
                        public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                            if (!has && itemStack != null) {
                                has = true;
                                itemStack.setItemMeta(s1);
                            }
                            return false;
                        }
                    });
                    factory.addInventory(57, h2, (cm) -> new ChestMenu.MenuClickHandler() {
                        boolean has = false;

                        @Override
                        public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                            if (!has && itemStack != null) {
                                has = true;
                                itemStack.setItemMeta(s2);
                            }
                            return false;
                        }
                    });
                    factory.addInventory(58, h3, (cm) -> new ChestMenu.MenuClickHandler() {
                        boolean has = false;

                        @Override
                        public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                            if (!has && itemStack != null) {
                                has = true;
                                itemStack.setType(Material.REPEATING_COMMAND_BLOCK);
                                itemStack.setItemMeta(s3);
                            }
                            return false;
                        }
                    });
                    factory.addInventory(59, h4, (cm) -> new ChestMenu.MenuClickHandler() {
                        boolean has = false;

                        @Override
                        public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                            if (!has && itemStack != null) {
                                has = true;
                                itemStack.setItemMeta(s4);
                            }
                            return false;
                        }
                    });
                    factory.addInventory(60, h5, (cm) -> new ChestMenu.MenuClickHandler() {
                        boolean has = false;

                        @Override
                        public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                            if (!has && itemStack != null) {
                                has = true;
                                itemStack.setItemMeta(s5);
                                AddUtils.sendMessage(player, "&eTmatsuki_rui就是純純的____");
                                if (AddUtils.standardRandom() < 0.3) {
                                    AddUtils.forceGive(player, head.clone(), AddUtils.random(4));
                                }
                                PlayerEffects.grantEffect(CustomEffects.WRONG_BUTTON, player, 1, 1);
                            }
                            return false;
                        }
                    });
                    factory.addInventory(63, h6, (cm) -> new ChestMenu.MenuClickHandler() {
                        boolean has = false;

                        @Override
                        public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                            if (!has && itemStack != null) {
                                has = true;
                                itemStack.setItemMeta(s6);
                            }
                            return false;
                        }
                    });
                    factory.addInventory(66, h7, (cm) -> new ChestMenu.MenuClickHandler() {
                        boolean has = false;

                        public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                            if (!has && itemStack != null) {
                                has = true;
                                itemStack.setItemMeta(s7);
                            }
                            return false;
                        }
                    });
                    factory.addInventory(67, h8, (cm) -> new ChestMenu.MenuClickHandler() {
                        boolean has = false;

                        @Override
                        public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                            if (!has && itemStack != null) {
                                has = true;
                                itemStack.setItemMeta(s8);
                                Schedules.launchSchedules(
                                        () -> {
                                            PlayerEffects.grantEffect(CustomEffects.WRONG_BUTTON, player, 2, 1);
                                        },
                                        100,
                                        true,
                                        0);
                            }
                            return false;
                        }
                    });
                    factory.addInventory(68, h9, (cm -> new ChestMenu.MenuClickHandler() {
                        boolean has = false;

                        @Override
                        public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                            if (!has && itemStack != null) {
                                has = true;
                                itemStack.setType(Material.CHORUS_PLANT);
                                itemStack.setItemMeta(s9);
                            }
                            return false;
                        }
                    }));
                    this.smetas = new ItemMeta[6];
                    String continueStr = "&7點擊繼續";
                    ItemStack it = new CustomItemStack(AddItem.MATL114, AddUtils.color("你覺得..."), continueStr);
                    factory.addInventory(71, it, (cm) -> new ChestMenu.MenuClickHandler() {
                        int index = 0;

                        @Override
                        public boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
                            if (itemStack != null) itemStack.setItemMeta(smetas[index]);
                            index = (index + 1) % 5;
                            return false;
                        }
                    });
                    it = new CustomItemStack(AddItem.MATL114, AddUtils.color("我能在患有痴呆症的情況下完成邏輯工藝的編寫嗎?"), continueStr);
                    smetas[0] = it.getItemMeta();
                    it = new CustomItemStack(AddItem.MATL114, AddUtils.color("這能被完成麼,這可能完成麼?"), continueStr);
                    smetas[1] = it.getItemMeta();
                    it = new CustomItemStack(AddItem.MATL114, AddUtils.color("我知道這有點難"), continueStr);
                    smetas[2] = it.getItemMeta();
                    it = new CustomItemStack(AddItem.MATL114, AddUtils.color("所以,我想"), continueStr);
                    smetas[3] = it.getItemMeta();
                    it = new CustomItemStack(AddItem.MATL114, AddUtils.color("今年我要進行一項從未有人達成過的挑戰"), continueStr);
                    smetas[4] = it.getItemMeta();
                    final String PREFIX1 = AddUtils.resolveColor("&6今日運勢:");
                    final String PREFIX2 = AddUtils.resolveColor(AddUtils.color("財運: ") + "&7");
                    final String PREFIX3 = AddUtils.resolveColor(AddUtils.color("桃花運: ") + "&7");
                    final String PREFIX4 = AddUtils.resolveColor(AddUtils.color("事業運: ") + "&7");
                    final ItemStack luckButton =
                            new CustomItemStack(CustomHead.LUCKY_BOX.getItem(), "&6每日運勢", "&7看看你的運勢如何?");
                    final int MAX_SIZE = 101;
                    factory.addInventory(44, luckButton, ((player, i, itemStack, clickAction) -> {
                        if (itemStack != null) {
                            ItemMeta meta = itemStack.getItemMeta();
                            List<String> lores = meta.getLore();
                            if (lores == null) lores = new ArrayList<>();
                            lores.subList(1, lores.size()).clear();
                            lores.add(PREFIX1);
                            String dateString = AddUtils.getDateString();
                            String name = player.getName();
                            String token = AddUtils.concat(dateString, name, "abcdiaeifg");
                            int code1 = Math.abs(MathUtils.sha256(token, 4)) % MAX_SIZE;
                            int code2 = Math.abs(MathUtils.sha512(token, 4)) % MAX_SIZE;
                            int code3 = Math.abs(MathUtils.sha384(token, 4)) % MAX_SIZE;
                            if ("matl114".equals(name)) {
                                // byd
                                code1 = 100;
                                code2 = 100;
                                code3 = 100;
                            }
                            lores.add(PREFIX2 + code1);
                            lores.add(PREFIX3 + code2);
                            lores.add(PREFIX4 + code3);
                            String date = LASTLY_CALCUATED_DATE.getOrDefault(player.getUniqueId(), "");
                            if (dateString.equals(date)) {
                                AddUtils.sendMessage(player, "&e你今天已經測過一次了,不再進行公告");
                            } else {
                                AddUtils.broadCast("&6玩家 %s 今日的運勢為".formatted(player.getName()));
                                AddUtils.broadCast(PREFIX2 + code1);
                                AddUtils.broadCast(PREFIX3 + code2);
                                AddUtils.broadCast(PREFIX4 + code3);
                                LASTLY_CALCUATED_DATE.put(player.getUniqueId(), dateString);
                            }
                            meta.setLore(lores);
                            itemStack.setItemMeta(meta);
                        }
                        return false;
                    }));
                }

                final ItemMeta s1 = new CustomItemStack(
                                CustomHead.getHead("fd524332cdb381c9e51f77d4cec9bc6d4d1c5bdec1499d206d8383e9176bdfb0"),
                                AddUtils.color("haiman"),
                                "&7haiman科技作者",
                                "&7海曼科技會為你提供足夠的物質支持")
                        .getItemMeta();
                final ItemMeta s2 = new CustomItemStack(
                                CustomHead.getHead("a9b046531a6182de634d6fed1f3b4f885ee99bfe2bc0c1684f7b97d396c2059f"),
                                AddUtils.color("mmmjjkx"),
                                "&7rsc開發者",
                                "&3純大蛇\uD83D\uDE0B")
                        .getItemMeta();
                final ItemMeta s3 = new CustomItemStack(
                                Material.REPEATING_COMMAND_BLOCK,
                                AddUtils.color("tinalness"),
                                "&7大香蕉的本質是命令方塊\uD83D\uDE0B(確信",
                                "&7但是你或許會需要他的網絡拓展")
                        .getItemMeta();
                final ItemMeta s4 = new CustomItemStack(
                                CustomHead.SUPPORTER2.getItem(),
                                AddUtils.color("HgTlPbBi"),
                                "&7邏輯工藝的支持者",
                                "&7提出了很多有用的點子")
                        .getItemMeta();
                final ItemMeta s5 = new CustomItemStack(
                                CustomHead.getHead("8e434215b5616bf37dccbacdb240bd16de59507e62a5371ceca80327b398e65"),
                                AddUtils.color("Tmatsuki_rui"),
                                "&7涼城服的禍源",
                                "&7純傻逼,給爺死啊\uD83D\uDE21")
                        .getItemMeta();
                final ItemMeta s6 = new CustomItemStack(
                                CustomHead.BUSHIGEMEN.getItem(), "  ", "&7看得出你的視力很好", "&7所以一定要仔細閲讀", "&7版本説明與機器説明哦")
                        .getItemMeta();
                final ItemStack head = new CustomItemStack(Material.PLAYER_HEAD, AddUtils.color("逝者的頭顱"));
                final ItemMeta s7 = new CustomItemStack(
                                CustomHead.TOURUYA.getItem(),
                                AddUtils.color("TouRuya"),
                                "&7人機服腐竹",
                                "&7純人跡",
                                "&3\"堆疊開局 怎麼輸?\uD83D\uDE0B\"")
                        .getItemMeta();
                final ItemMeta s8 = new CustomItemStack(
                                CustomHead.BIG_SNAKE.getItem(), AddUtils.color("taitaia"), "&7刷物?你在想屁吃\uD83D\uDE0B")
                        .getItemMeta();
                final ItemMeta s9 = new CustomItemStack(
                                Material.CHORUS_PLANT, AddUtils.color("xiaoUI1"), "&7知名服務器-&b魔芋&7 的腐竹", "&7感謝UI對本項目的支持")
                        .getItemMeta();
                ItemMeta[] smetas;
                final HashMap<UUID, String> LASTLY_CALCUATED_DATE = new HashMap<>();
                // used to set GUIDE based handlers,an interface to adapt CustomMenu menus
                public void addGuideRelated(
                        ChestMenu menu, Player p, PlayerProfile profile, SlimefunGuideMode mode, int page) {
                    // 加入實例化之後的handler和item,同打開玩家等數據有關的handler
                    if (page == 1) {
                        menu.addMenuClickHandler(
                                27, ((Player player1, int i1, ItemStack itemstack1, ClickAction clickAction) -> {
                                    getRecipeMenu()
                                            .build()
                                            .setBackHandler(((player, i, itemStack, clickAction1) -> {
                                                this.openPage(player1, profile, mode, page);
                                                return false;
                                            }))
                                            .open(player1);
                                    return false;
                                }));
                        menu.addMenuClickHandler(
                                18, ((Player player1, int i1, ItemStack itemstack1, ClickAction clickAction) -> {
                                    getMachineMenu()
                                            .build()
                                            .setBackHandler(((player, i, itemStack, clickAction1) -> {
                                                this.openPage(player1, profile, mode, page);
                                                return false;
                                            }))
                                            .open(player1);
                                    return false;
                                }));
                    } else if (page == 2) {

                    }
                }
            };
}
