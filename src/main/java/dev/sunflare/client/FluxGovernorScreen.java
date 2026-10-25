package dev.sunflare.client;

import dev.sunflare.menu.FluxGovernorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 通量调控器：只读状态面板。
 */
public class FluxGovernorScreen extends AbstractContainerScreen<FluxGovernorMenu> {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 96;

    private static final int BG = 0xFFC6C6C6;
    private static final int BORDER = 0xFF555555;
    private static final int SEPARATOR = 0xFFA8A8A8;
    private static final int TITLE = 0xFF3F3F3F;
    private static final int LABEL = 0xFF5A5A5A;
    private static final int VALUE = 0xFF2B2B2B;
    private static final int ACCENT = 0xFF1E5B8C;
    private static final int BUTTON = 0xFFAAAAAA;
    private static final int BUTTON_HOVER = 0xFFBFBFBF;

    private static final int BUTTON_SIZE = 12;

    public FluxGovernorScreen(FluxGovernorMenu menu, Inventory inventory, Component title) {
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

    private int buttonX() {
        return this.leftPos + WIDTH - BUTTON_SIZE - 6;
    }

    private int buttonY() {
        return this.topPos + 4;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, BG);
        graphics.fill(x, y, x + WIDTH, y + 1, BORDER);
        graphics.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, BORDER);
        graphics.fill(x, y, x + 1, y + HEIGHT, BORDER);
        graphics.fill(x + WIDTH - 1, y, x + WIDTH, y + HEIGHT, BORDER);
        graphics.fill(x + 1, y + 17, x + WIDTH - 1, y + 18, SEPARATOR);
        for (int ly : new int[]{34, 50, 66}) {
            graphics.fill(x + 8, y + ly, x + WIDTH - 8, y + ly, SEPARATOR);
        }

        drawClose(graphics, mouseX, mouseY);
    }

    private void drawClose(GuiGraphics graphics, int mouseX, int mouseY) {
        int bx = this.buttonX();
        int by = this.buttonY();
        boolean hover = mouseX >= bx && mouseX < bx + BUTTON_SIZE && mouseY >= by && mouseY < by + BUTTON_SIZE;
        graphics.fill(bx, by, bx + BUTTON_SIZE, by + BUTTON_SIZE, hover ? BUTTON_HOVER : BUTTON);
        graphics.fill(bx, by, bx + BUTTON_SIZE, by + 1, 0xFFFFFFFF);
        graphics.fill(bx, by + BUTTON_SIZE - 1, bx + BUTTON_SIZE, by + BUTTON_SIZE, BORDER);
        int colour = hover ? 0xFF2B2B2B : 0xFF4A4A4A;
        int cx = bx + 3;
        int cy = by + 3;
        for (int i = 0; i < 6; i++) {
            graphics.fill(cx + i, cy + i, cx + i + 1, cy + i + 1, colour);
            graphics.fill(cx + i, cy + 5 - i, cx + i + 1, cy + 5 - i + 1, colour);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 5, TITLE, false);

        row(graphics, Component.translatable("sunflare.gui.governor.panels"),
                Component.literal(String.valueOf(this.menu.panelCount())), 22, VALUE);
        row(graphics, Component.translatable("sunflare.gui.governor.average"),
                Component.literal(this.menu.averagePermille() / 10 + "%"), 38, VALUE);
        row(graphics, Component.translatable("sunflare.gui.governor.scale"),
                Component.literal(this.menu.scalePercent() + "%"), 54, ACCENT);
        row(graphics, Component.translatable("sunflare.gui.governor.suspended"),
                Component.literal(String.valueOf(this.menu.suspendedCount())), 70, VALUE);
    }

    private void row(GuiGraphics graphics, Component label, Component value, int y, int valueColor) {
        graphics.drawString(this.font, label, 14, y, LABEL, false);
        graphics.drawString(this.font, value, 162 - this.font.width(value), y, valueColor, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int bx = this.buttonX();
        int by = this.buttonY();
        if (button == 0 && mouseX >= bx && mouseX < bx + BUTTON_SIZE && mouseY >= by && mouseY < by + BUTTON_SIZE) {
            this.onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
