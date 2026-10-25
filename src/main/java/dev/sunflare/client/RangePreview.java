package dev.sunflare.client;

import dev.sunflare.Sunflare;
import dev.sunflare.SunflareConfig;
import dev.sunflare.SunflareRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * 手持调控器/总线节点时，在瞄准位置画出工作范围。
 */
@EventBusSubscriber(modid = Sunflare.MOD_ID, value = Dist.CLIENT)
public final class RangePreview {

    private RangePreview() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (!SunflareConfig.RANGE_PREVIEW.get()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        ItemStack held = minecraft.player.getMainHandItem();
        boolean governor = held.is(SunflareRegistry.FLUX_GOVERNOR.get().asItem());
        boolean bus = held.is(SunflareRegistry.PRIORITY_BUS.get().asItem());
        if (!governor && !bus) {
            return;
        }
        HitResult hit = minecraft.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) {
            return;
        }
        BlockPos target = blockHit.getBlockPos().relative(blockHit.getDirection());
        int radius = SunflareConfig.GOVERNOR_RADIUS.get();

        double r = radius + 1.0D;
        AABB box = new AABB(
                target.getX() - radius, target.getY() - radius, target.getZ() - radius,
                target.getX() + r, target.getY() + r, target.getZ() + r);

        PoseStack pose = event.getPoseStack();
        var camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        float red = governor ? 0.35F : 0.90F;
        float green = governor ? 0.85F : 0.65F;
        float blue = governor ? 0.95F : 0.25F;
        LevelRenderer.renderLineBox(pose, lines, box, red, green, blue, 0.75F);
        pose.popPose();
        buffers.endBatch(RenderType.lines());
    }
}
