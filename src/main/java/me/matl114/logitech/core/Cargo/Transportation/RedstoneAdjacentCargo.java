package me.matl114.logitech.core.Cargo.Transportation;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import java.util.List;
import me.matl114.logitech.utils.*;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.inventory.ItemStack;

public class RedstoneAdjacentCargo extends AdjacentCargo {
    public RedstoneAdjacentCargo(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            List<ItemStack> displayList) {
        super(itemGroup, item, recipeType, recipe, displayList);
        setDisplayRecipes(Utils.list(
                AddUtils.getInfoShow(
                        "&f機制",
                        "&7這是一個可調控貨運機器",
                        "&7可調控貨運機器的行為會包括若干對源方塊和目標方塊",
                        "&7可調控貨運機器會進行從源方塊到目標方塊的物流傳輸",
                        "&7可調控貨運機器會根據某些特定條件決定運輸行為",
                        "&7可調節貨運機器支援目標方塊設置的輸入槽限制",
                        "&7但是相應的,其最大傳輸量會被限制為min(576,配置數)"),
                null,
                AddUtils.getInfoShow(
                        "&f機制",
                        "&7本機器可以選擇與其相鄰的方塊參與傳輸",
                        "&7本機器接受紅石信號的調控",
                        "&7當該方塊被充能時,進行正常推送",
                        "&7當該方塊不被充能時,待機",
                        "&c紅石資訊的調控將有1sft(0.5s)延遲,當前刻更改的充能狀態將在下一刻生效!"),
                null));
    }

    public String POWERED_KEY = "po";

    public void cargoTask(Block b, BlockMenu menu, SlimefunBlockData data, int configCode) {
        BlockData data1 = b.getBlockData();
        if (data1 instanceof Lightable la && la.isLit()) {
            super.cargoTask(b, menu, data, configCode);
            // DataCache.setCustomData(data,POWERED_KEY,la.isLit()?1:0);
        }
    }

    public boolean isSync() {
        // run in sync thread can be faster
        return true;
    }
}
