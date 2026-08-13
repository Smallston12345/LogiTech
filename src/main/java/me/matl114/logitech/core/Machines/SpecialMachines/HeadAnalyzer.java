package me.matl114.logitech.core.Machines.SpecialMachines;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.chat.ChatInput;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import javax.annotation.Nonnull;
import me.matl114.logitech.core.AddItem;
import me.matl114.logitech.core.Machines.Abstracts.AbstractMachine;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.CraftUtils;
import me.matl114.logitech.utils.UtilClass.SpecialItemClass.CustomHead;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
// import net.kyori.adventure.text.Component;
// import net.kyori.adventure.text.event.ClickEvent;
// import net.kyori.adventure.text.format.NamedTextColor;

public class HeadAnalyzer extends AbstractMachine {
    protected final int[] BORDER =
            new int[] {0, 2, 3, 5, 6, 8, 9, 11, 12, 14, 15, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
    protected final int ENCODE_SLOT = 7;
    protected final int CHANGE_SLOT = 4;
    protected final int CHANGE_INPUT = 13;
    protected final int DECODE_SLOT = 1;
    protected final int ENCODE_DISPLAY = 16;
    protected final int DECODE_INPUTSLOT = 10;
    protected final ItemStack CHANGE_ITEM = new CustomItemStack(
            Material.CRAFTING_TABLE, "&e點擊轉換頭顱", "&7當下方為默認頭顱時,點擊輸入hash,可以更改頭顱樣式", "&7當下方為展示示例頭顱或非黏液物品時,將頭顱轉為默認頭顱");
    protected final ItemStack ENCODE_ITEM = new CustomItemStack(Material.NAME_TAG, "&e點擊輸入hash", "&7將在下方顯示頭顱樣式");
    protected final ItemStack ENCODE_DISPLAY_NULL =
            new CustomItemStack(Material.RED_STAINED_GLASS_PANE, "&c空", "&c暫未輸入hash或者不是有效的hash");
    protected final ItemStack DECODE_ITEM =
            new CustomItemStack(Material.KNOWLEDGE_BOOK, "&e點擊解析下方頭顱", "&7若下方為有效物品,則顯示hash", "&3hash: &f空");
    protected final ItemMeta BLANK_SKULL = new ItemStack(Material.PLAYER_HEAD).getItemMeta();

    private String getHash(ItemStack item) {
        String st = CustomHead.getHash(item);
        if (st != null) return "&3hash: &f" + st;
        else return "&3hash: &f空";
    }

    private ItemStack getSkull(String string) {
        try {
            return new CustomItemStack(
                    SlimefunUtils.getCustomHead(string), "&a頭顱樣式展示品", "&7這是一個展示頭顱", "&3hash: &f" + string);
        } catch (Throwable t) {
            return ENCODE_DISPLAY_NULL.clone();
        }
    }

    public int[] getInputSlots() {
        return new int[0];
    }

    public int[] getOutputSlots() {
        return new int[0];
    }

    public HeadAnalyzer(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(category, item, recipeType, recipe, 0, 0);
    }

    public void addInfo(ItemStack item) {}

    public void constructMenu(BlockMenuPreset preset) {
        int[] border = BORDER;
        int len = border.length;
        for (int var4 = 0; var4 < len; ++var4) {

            preset.addItem(border[var4], ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        preset.addItem(ENCODE_DISPLAY, ENCODE_DISPLAY_NULL, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(DECODE_SLOT, DECODE_ITEM, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(ENCODE_SLOT, ENCODE_ITEM, ChestMenuUtils.getEmptyClickHandler());
        preset.addItem(CHANGE_SLOT, CHANGE_ITEM, ChestMenuUtils.getEmptyClickHandler());
    }

    public void process(Block b, BlockMenu preset, SlimefunBlockData data) {}

    public void registerTick(SlimefunItem item) {
        // no ticker
    }

    public void newMenuInstance(@Nonnull BlockMenu blockMenu, @Nonnull Block block) {
        blockMenu.addMenuClickHandler(ENCODE_SLOT, ((player, i, itemStack, clickAction) -> {
            player.closeInventory();

            player.sendMessage(ChatColors.color("&e請輸入頭顱的hash碼"));
            ChatInput.waitForPlayer(Slimefun.instance(), player, msg -> {
                blockMenu.replaceExistingItem(ENCODE_DISPLAY, getSkull(msg));
                blockMenu.open(player);
            });

            return false;
        }));
        blockMenu.addMenuClickHandler(DECODE_SLOT, ((player, i, itemStack, clickAction) -> {
            String st = CustomHead.getHash(blockMenu.getItemInSlot(DECODE_INPUTSLOT));
            if (st != null) {
                String res = "&3hash: &f" + st;
                ItemStack it = blockMenu.getItemInSlot(DECODE_SLOT);
                AddUtils.setLore(it, 1, res);
                AddUtils.displayCopyString(player, "點擊此處拷貝hash碼", "點擊複製到剪貼板", st);

            } else {
                blockMenu.replaceExistingItem(DECODE_SLOT, DECODE_ITEM);
            }
            return false;
        }));
        blockMenu.addMenuClickHandler(CHANGE_SLOT, ((player, i, itemStack, clickAction) -> {
            ItemStack it = blockMenu.getItemInSlot(CHANGE_INPUT);
            if (it != null && it.getType() == Material.PLAYER_HEAD) {

                if (!it.hasItemMeta()) {
                    blockMenu.replaceExistingItem(CHANGE_INPUT, null);
                    AddUtils.sendMessage(player, "&e請輸入頭顱的hash碼");
                    player.closeInventory();
                    ChatInput.waitForPlayer(Slimefun.instance(), player, msg -> {
                        ItemStack it2 = AddItem.SAMPLE_HEAD.clone();
                        ItemMeta meta2 = it2.getItemMeta();
                        ItemMeta target_meta = CustomHead.getHead(msg).getItemMeta();

                        if (meta2 instanceof SkullMeta skmeta && target_meta instanceof SkullMeta skmeta2) {
                            skmeta.setOwnerProfile(skmeta2.getOwnerProfile());
                        }
                        it2.setItemMeta(meta2);
                        blockMenu.replaceExistingItem(CHANGE_INPUT, it2);
                        blockMenu.open(player);
                        return;
                    });
                } else {
                    ItemMeta meta = it.getItemMeta();
                    String id = CraftUtils.parseSfId(meta);
                    if (id == null || id.equals(AddItem.SAMPLE_HEAD.getItemId())) {
                        it.setItemMeta(null);
                    } else {
                        AddUtils.sendMessage(player, "&c不是合法物品,無法清除頭顱資料!");
                    }
                }
            }
            return false;
        }));
    }
}
