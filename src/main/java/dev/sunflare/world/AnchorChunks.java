package dev.sunflare.world;

import dev.sunflare.SunflareConfig;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 记录每个维度里由锚定节点强制加载的方块位置，用于限制数量。
 */
public class AnchorChunks extends SavedData {

    private static final String NAME = "sunflare_anchor_chunks";

    private final Set<Long> positions = new HashSet<>();

    public static AnchorChunks get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(AnchorChunks::new, AnchorChunks::load, null),
                NAME);
    }

    /** 超过配置上限时返回 false。 */
    public boolean add(BlockPos pos) {
        if (this.positions.contains(pos.asLong())) {
            return true;
        }
        if (this.positions.size() >= SunflareConfig.ANCHOR_MAX_CHUNKS.get()) {
            return false;
        }
        this.positions.add(pos.asLong());
        this.setDirty();
        return true;
    }

    public int size() {
        return this.positions.size();
    }

    public void remove(BlockPos pos) {
        if (this.positions.remove(pos.asLong())) {
            this.setDirty();
        }
    }

    public static AnchorChunks load(CompoundTag tag, HolderLookup.Provider registries) {
        AnchorChunks data = new AnchorChunks();
        ListTag list = tag.getList("Positions", Tag.TAG_LONG);
        for (Tag entry : list) {
            data.positions.add(((LongTag) entry).getAsLong());
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (long pos : this.positions) {
            list.add(LongTag.valueOf(pos));
        }
        tag.put("Positions", list);
        return tag;
    }
}
