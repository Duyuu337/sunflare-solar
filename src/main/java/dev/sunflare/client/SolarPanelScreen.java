package dev.sunflare.client;

import dev.sunflare.Sunflare;
import dev.sunflare.menu.SolarPanelMenu;
import dev.sunflare.util.Numbers;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 参考 AE2 的机械面板风格：斜面边框、内凹读数区、能量条、状态指示灯、关闭按钮。
 *
 * <p>注意坐标空间：{@code renderBg} 在未平移的绝对坐标里调用，
 * {@code renderLabels} 在 {@code translate(leftPos, topPos)} 之后调用，必须用相对坐标。</p>
 */
public class SolarPanelScreen extends AbstractContainerScreen<SolarPanelMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Sunflare.MOD_ID, "textures/gui/solar_panel.png");

    private static final int WIDTH = 176;
    private static final int HEIGHT = 112;

    private static final int TITLE = 0xFF3F3F3F;
    private static final int LABEL = 0xFF5A5A5A;
    private static final int VALUE = 0xFF2B2B2B;
    private static final int ACCENT = 0xFF1E5B8C;

    private static final int TRACK = 0xFF767676;
    private static final int BAR_MAIN = 0xFF3A7BBF;
    private static final int BAR_TOP = 0xFF7DB6E4;

    private static final int LAMP_ON_DARK = 0xFF3E8F5E;
    private static final int LAMP_ON = 0xFF7FE0A6;
    private static final int LAMP_OFF_DARK = 0xFF8B8B8B;
    private static final int LAMP_OFF = 0xFFB4B4B4;

    private static final int BUTTON_SIZE = 12;

    public SolarPanelScreen(SolarPanelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = (this.height - HEIGHT) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private int buttonX() {
        return this.leftPos + WIDTH - BUTTON_SIZE - 6;
    }

    private int buttonY() {
        return this.topPos + 4;
    }

    private boolean isOverButton(double mouseX, double mouseY) {
        int bx = this.buttonX();
        int by = this.buttonY();
        return mouseX >= bx && mouseX < bx + BUTTON_SIZE && mouseY >= by && mouseY < by + BUTTON_SIZE;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, WIDTH, HEIGHT);

        int barX = this.leftPos + 14;
        int barY = this.topPos + 40;
        int barW = 147;
        int barH = 8;
        graphics.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF373737);
        graphics.fill(barX, barY, barX + barW, barY + barH, TRACK);
        int filled = (int) Math.min(barW, (long) this.menu.energy() * barW / this.menu.capacity());
        if (filled > 0) {
            graphics.fill(barX, barY, barX + filled, barY + barH, BAR_MAIN);
            graphics.fill(barX, barY, barX + filled, barY + 1, BAR_TOP);
        }

        boolean active = this.menu.output() > 0;
        int lampX = this.leftPos + WIDTH - BUTTON_SIZE - 6 - 10;
        int lampY = this.topPos + 6;
        graphics.fill(lampX, lampY, lampX + 7, lampY + 7, active ? LAMP_ON_DARK : LAMP_OFF_DARK);
        graphics.fill(lampX + 1, lampY + 1, lampX + 6, lampY + 6, active ? LAMP_ON : LAMP_OFF);

        // 关闭按钮
        int bx = this.buttonX();
        int by = this.buttonY();
        boolean hover = this.isOverButton(mouseX, mouseY);
        graphics.fill(bx, by, bx + BUTTON_SIZE, by + BUTTON_SIZE, hover ? 0xFFBFBFBF : 0xFFAAAAAA);
        graphics.fill(bx, by, bx + BUTTON_SIZE, by + 1, 0xFFFFFFFF);
        graphics.fill(bx, by + BUTTON_SIZE - 1, bx + BUTTON_SIZE, by + BUTTON_SIZE, 0xFF555555);
        int cx = bx + 3;
        int cy = by + 3;
        int cross = hover ? 0xFF2B2B2B : 0xFF4A4A4A;
        for (int i = 0; i < 6; i++) {
            graphics.fill(cx + i, cy + i, cx + i + 1, cy + i + 1, cross);
            graphics.fill(cx + i, cy + 5 - i, cx + i + 1, cy + 5 - i + 1, cross);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 相对坐标（0,0 = 面板左上角）
        graphics.drawString(this.font, this.title, 8, 5, TITLE, false);

        row(graphics, Component.translatable("sunflare.gui.label.buffer"),
                Component.translatable("sunflare.gui.value.buffer",
                        Numbers.compact(this.menu.energy()), Numbers.compact(this.menu.capacity())),
                25, VALUE);

        row(graphics, Component.translatable("sunflare.gui.label.rated"),
                Component.translatable("sunflare.gui.value.rate", Numbers.compact(this.menu.rated())),
                68, VALUE);

        row(graphics, Component.translatable("sunflare.gui.label.output"),
                Component.translatable("sunflare.gui.value.rate", Numbers.compact(this.menu.output())),
                82, VALUE);

        row(graphics, Component.translatable("sunflare.gui.label.convert"),
                Component.translatable("sunflare.gui.value.convert",
                        Numbers.compact(this.menu.rated() / 2), Numbers.compact(this.menu.rated() * 2L / 5L)),
                96, ACCENT);
    }

    private void row(GuiGraphics graphics, Component label, Component value, int y, int valueColor) {
        graphics.drawString(this.font, label, 14, y, LABEL, false);
        graphics.drawString(this.font, value, 162 - this.font.width(value), y, valueColor, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isOverButton(mouseX, mouseY)) {
            this.onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
