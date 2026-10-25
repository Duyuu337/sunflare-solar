package dev.sunflare.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 把 FE 存在物品的 {@code CUSTOM_DATA} 组件里的小工具。
 */
public final class ItemEnergy {

    private static final String KEY_ENERGY = "Fe";
    private static final String KEY_LINK_DIM = "LinkDim";
    private static final String KEY_LINK_POS = "LinkPos";
    private static final String KEY_LINK_OWNER = "LinkOwner";

    private ItemEnergy() {
    }

    public static CompoundTag tag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static void write(ItemStack stack, CompoundTag tag) {
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }

    public static int get(ItemStack stack) {
        return tag(stack).getInt(KEY_ENERGY);
    }

    public static void set(ItemStack stack, int value) {
        CompoundTag tag = tag(stack);
        tag.putInt(KEY_ENERGY, Math.max(0, value));
        write(stack, tag);
    }

    public static void add(ItemStack stack, int delta, int capacity) {
        set(stack, Math.min(capacity, get(stack) + delta));
    }

    public static String linkDim(ItemStack stack) {
        return tag(stack).getString(KEY_LINK_DIM);
    }

    public static long linkPos(ItemStack stack) {
        return tag(stack).getLong(KEY_LINK_POS);
    }

    public static String linkOwner(ItemStack stack) {
        return tag(stack).getString(KEY_LINK_OWNER);
    }

    public static boolean hasLink(ItemStack stack) {
        return !linkDim(stack).isEmpty();
    }

    public static void setLink(ItemStack stack, String dim, long pos, String owner) {
        CompoundTag tag = tag(stack);
        tag.putString(KEY_LINK_DIM, dim);
        tag.putLong(KEY_LINK_POS, pos);
        tag.putString(KEY_LINK_OWNER, owner);
        write(stack, tag);
    }

    public static void clearLink(ItemStack stack) {
        CompoundTag tag = tag(stack);
        tag.remove(KEY_LINK_DIM);
        tag.remove(KEY_LINK_POS);
        tag.remove(KEY_LINK_OWNER);
        write(stack, tag);
    }
}
