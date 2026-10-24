package me.matl114.logitech.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class Utils {
    public static void log(String... message) {
        Debug.logger(String.join(" ", message));
    }

    public static <T extends Object> List<T> list(T... objs) {
        return Arrays.stream(objs).collect(Collectors.toCollection(ArrayList::new));
    }

    public static <T extends Object> T[] array(T... objs) {
        return objs;
    }

    public static ItemStack[] toArray(List<ItemStack> list) {
        return list.toArray(new ItemStack[list.size()]);
    }

    public static ItemStack[] recipe(Object... v) {
        return Arrays.stream(v).map(AddUtils::resolveItem).toArray(ItemStack[]::new);
    }

    // === 1.21.x cross-version compat helpers (ported from LogiTech2) ===
    // Material.CHAIN was renamed IRON_CHAIN in 1.21.9
    public static Material chainMaterial() {
        try {
            return Material.valueOf("IRON_CHAIN");
        } catch (IllegalArgumentException ignored) {
        }
        try {
            return Material.valueOf("CHAIN");
        } catch (IllegalArgumentException ignored) {
        }
        throw new IllegalStateException("CHAIN/IRON_CHAIN material not found");
    }

    // EntityType.BOAT / CHEST_BOAT were split into OAK_BOAT / OAK_CHEST_BOAT etc. in 1.21.4
    public static EntityType boatEntity() {
        try {
            return EntityType.valueOf("OAK_BOAT");
        } catch (IllegalArgumentException ignored) {
        }
        try {
            return EntityType.valueOf("BOAT");
        } catch (IllegalArgumentException ignored) {
        }
        throw new IllegalStateException("OAK_BOAT/BOAT entity type not found");
    }

    public static EntityType chestBoatEntity() {
        try {
            return EntityType.valueOf("OAK_CHEST_BOAT");
        } catch (IllegalArgumentException ignored) {
        }
        try {
            return EntityType.valueOf("CHEST_BOAT");
        } catch (IllegalArgumentException ignored) {
        }
        throw new IllegalStateException("OAK_CHEST_BOAT/CHEST_BOAT entity type not found");
    }
}
