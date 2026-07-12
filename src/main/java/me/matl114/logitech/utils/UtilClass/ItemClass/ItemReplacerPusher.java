package me.matl114.logitech.utils.UtilClass.ItemClass;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemReplacerPusher extends ItemPusher {
    ItemStack source;
    private static ItemReplacerPusher INSTANCE =
            new ItemReplacerPusher(new ItemStack(Material.STONE), new ItemStack(Material.STONE));

    public static ItemPusher get(ItemStack source, ItemStack data) {
        if (source != null && data != null) {
            return new ItemReplacerPusher(source, data);
        } else {
            return ItemPusher.get(source);
        }
    }

    protected ItemReplacerPusher(ItemStack replacer, ItemStack replacement) {
        super(replacement);
        this.source = replacer;
    }

    public void updateItemStack() {
        super.updateItemStack();
        source.setAmount((int) Math.min(cnt, Integer.MAX_VALUE));
    }
    // 修復了setFrom存儲時覆寫maxSize的問題
    public void setFrom(ItemCounter source) {
        super.setFrom(source);
    }
}
