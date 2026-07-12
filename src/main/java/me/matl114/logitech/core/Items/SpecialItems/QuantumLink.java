package me.matl114.logitech.core.Items.SpecialItems;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import java.util.ArrayList;
import java.util.Optional;
import me.matl114.logitech.Language;
import me.matl114.logitech.core.Cargo.Links.StorageLink;
import me.matl114.logitech.core.Items.Abstracts.DataRecordedItem;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.DataCache;
import me.matl114.logitech.utils.Utils;
import me.matl114.logitech.utils.WorldUtils;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class QuantumLink extends DataRecordedItem {
    public QuantumLink(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe, new ArrayList<>());
        this.setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow(
                        "&f機制 - &c終極合成",
                        "&c特性 ",
                        "&7終極合成是本附屬某些終極機器中的機制",
                        "&c具體説明請看\"版本與説明\"分類中的信息",
                        "&7直白了説是讓物品存儲中的物品直接參與合成/進程",
                        "&7從而打破槽位限制",
                        "&7該物品是終極機器支持的物品之一",
                        "&a建議:推薦使用該物品綁定存儲性方塊放入終極機器,方便且會降低卡頓!"),
                null,
                AddUtils.getInfoShow(
                        "&7機制 -&c 代理存儲",
                        "&7本物品可以類似%s一樣綁定某個方塊".formatted(Language.get("Items.HYPER_LINK.Name")),
                        "&7若該方塊是支持的存儲性方塊,則該物品會與此存儲建立糾纏鏈接",
                        "&7當該物品被放入終極機器的槽位中時,",
                        "&7可以代理綁定的存儲中的物品&e直接參與合成!",
                        "&7產出的物品也可以通過它&e直接進入綁定的存儲!",
                        "&7支持的存儲性方塊:",
                        "&7邏輯工藝 存儲接口系列",
                        "&7網絡(拓展) 量子存儲系列"),
                null,
                AddUtils.getInfoShow(
                        "&7機制 -&c 糾纏記錄",
                        "&7當綁定了存儲後",
                        "&7該物品的描述上會顯示該綁定存儲中的物品類型",
                        "&7當你右鍵該物品時,該顯示將被更新為當前的綁定存儲的物品類型",
                        "&7除此之外,該物品描述將不會更新,也不會影響其參與合成",
                        "&7參與合成時所代理的存儲只與存儲位置有關,與描述無關"),
                null,
                AddUtils.getInfoShow(
                        "&7機制 -&c 報錯",
                        "&e如果網絡版本不兼容或使用了錯誤的測試版,會造成報錯",
                        "&7如果使用綁定量子存儲的該物品進行合成時,出現合成不成功的情況",
                        "&7很可能這是量子存儲數據不兼容或者錯誤版本導致的問題",
                        "&e請立刻將其移出槽位",
                        "&7並判斷是存儲數據損壞問題還是網絡版本問題",
                        "&7若累計報錯達到一定數目,將會禁用該兼容"),
                null));
    }

    @Override
    public void preRegister() {
        super.preRegister();
        addItemHandler((ItemUseHandler) this::onBindLocation);
    }

    private void onBindLocation(PlayerRightClickEvent event) {
        if (event.getPlayer().isSneaking()) {
            Optional<Block> b = event.getClickedBlock();
            if (b.isPresent()) {
                ItemStack it = event.getItem();
                ItemMeta im = it.getItemMeta();
                if (StorageLink.canLink(im)) {
                    if (WorldUtils.hasPermission(event.getPlayer(), b.get(), Interaction.INTERACT_BLOCK)) {
                        StorageLink.setLink(im, b.get().getLocation());
                        it.setItemMeta(im);
                        AddUtils.sendMessage(event.getPlayer(), "&a成功建立糾纏鏈接,存儲歷史記錄已刷新!");
                    } else {
                        AddUtils.sendMessage(event.getPlayer(), "&c抱歉,但您似乎並不能在該位置使用此物品.");
                    }
                }
            }
        } else {
            // try load
            ItemStack it = event.getItem();
            ItemMeta im = it.getItemMeta();
            if (StorageLink.isLink(im)) {
                Location loc = StorageLink.getLink(im);
                if (loc != null) {
                    StorageLink.updateLinkLocationDisplay(im, loc);
                    it.setItemMeta(im);
                    AddUtils.sendMessage(event.getPlayer(), "&a糾纏鏈接的歷史記錄已經刷新!");
                    if (WorldUtils.hasPermission(event.getPlayer(), loc, Interaction.INTERACT_BLOCK)) {
                        SlimefunBlockData data = DataCache.safeLoadBlock(loc);
                        if (data != null) {
                            BlockMenu menu = data.getBlockMenu();
                            if (menu != null) menu.open(event.getPlayer());
                        }
                    } else {
                        AddUtils.sendMessage(event.getPlayer(), "&c抱歉,但您似乎並沒有訪問該位置的權限.");
                    }
                }
            }
        }
    }
}
