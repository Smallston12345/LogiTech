package me.matl114.logitech.core.Items.SpecialItems;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.matl114.logitech.core.Items.Abstracts.DataRecordedItem;
import me.matl114.logitech.utils.AddUtils;
import me.matl114.logitech.utils.Utils;
import org.bukkit.inventory.ItemStack;

public class Singularity extends DataRecordedItem {
    public Singularity(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        this.setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow(
                        "&f機制 - &c終極合成",
                        "&c特性 ",
                        "&7終極合成是本附屬某些終極機器中的機制",
                        "&c具體說明請看\"版本與說明\"分類中的資訊",
                        "&7直白了説是讓物品存儲中的物品直接參與合成/進程",
                        "&7從而打破槽位限制",
                        "&7該物品是終極機器支援的物品之一",
                        "&a建議:不推薦直接將存儲奇點放入終極機器,不方便且會有更高卡頓!"),
                null,
                AddUtils.getInfoShow(
                        "&7機制 -&c 物品存儲",
                        "&7本物品可以存儲某種物品",
                        "&7本物品的最大存儲量為9223372036854775807(long上限)",
                        "&7當該物品被放入終極機器的槽位中時,",
                        "&7可以代理其內部存儲的物品&e直接參與合成!",
                        "&7產出的物品也可以&e直接進入內部存儲的物品中!",
                        "&e本物品需要奇點交互接口才可以進行數量操作和種類設置"),
                null));
    }
}
