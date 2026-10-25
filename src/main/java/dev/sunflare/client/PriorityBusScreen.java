package dev.sunflare.client;

import dev.sunflare.menu.PriorityBusMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 优先级总线节点：优先级 ±1、强制通断、缓冲条。
 */
public class PriorityBusScreen extends AbstractContainerScreen<PriorityBusMenu> {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 112;

    private static final int BG = 0xFFC6C6C6;
    private static final int BORDER = 0xFF555555;
    private static final int SEPARATOR = 0xFFA8A8A8;
    private static final int TITLE = 0xFF3F3F3F;
    private static final int LABEL = 0xFF5A5A5A;
    private static final int VALUE = 0xFF2B2B2B;
    private static final int ACCENT = 0xFF1E5B8C;
    private static final int BUTTON = 0xFFAAAAAA;
    private static final int BUTTON_HOVER = 0xFFBFBFBF;
    private static final int TRACK = 0xFF767676;
    private static final int BAR = 0xFF3A7BBF;
    private static final int BAR_TOP = 0xFF7DB6E4;

    private static final int BUTTON_SIZE = 12;

    public PriorityBusScreen(PriorityBusMenu menu, Inventory inventory, Component title) {
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

    private int closeX() {
        return this.leftPos + WIDTH - BUTTON_SIZE - 6;
    }

    private int closeY() {
        return this.topPos + 4;
    }

    private int minusX() {
        return this.leftPos + 100;
    }

    private int plusX() {
        return this.leftPos + 140;
    }

    private int priorityY() {
        return this.topPos + 20;
    }

    private int toggleX() {
        return this.leftPos + 14;
    }

    private int toggleY() {
        return this.topPos + 78;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
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
        graphics.fill(x + 8, y + 40, x + WIDTH - 8, y + 41, SEPARATOR);

        // 缓冲条
        int barX = x + 14;
        int barY = y + 58;
        int barW = WIDTH - 28;
        int barH = 8;
        graphics.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF373737);
        graphics.fill(barX, barY, barX + barW, barY + barH, TRACK);
        int filled = (int) Math.min(barW, (long) this.menu.energy() * barW / this.menu.capacity());
        if (filled > 0) {
            graphics.fill(barX, barY, barX + filled, barY + barH, BAR);
            graphics.fill(barX, barY, barX + filled, barY + 1, BAR_TOP);
        }

        // 关闭
        int bx = this.closeX();
        int by = this.closeY();
        boolean hoverClose = inside(mouseX, mouseY, bx, by, BUTTON_SIZE, BUTTON_SIZE);
        graphics.fill(bx, by, bx + BUTTON_SIZE, by + BUTTON_SIZE, hoverClose ? BUTTON_HOVER : BUTTON);
        graphics.fill(bx, by, bx + BUTTON_SIZE, by + 1, 0xFFFFFFFF);
        graphics.fill(bx, by + BUTTON_SIZE - 1, bx + BUTTON_SIZE, by + BUTTON_SIZE, BORDER);
        int cross = hoverClose ? 0xFF2B2B2B : 0xFF4A4A4A;
        for (int i = 0; i < 6; i++) {
            graphics.fill(bx + 3 + i, by + 3 + i, bx + 4 + i, by + 4 + i, cross);
            graphics.fill(bx + 3 + i, by + 8 - i, bx + 4 + i, by + 9 - i, cross);
        }

        // 优先级 ±
        drawSmallButton(graphics, this.minusX(), this.priorityY(), mouseX, mouseY, '-');
        drawSmallButton(graphics, this.plusX(), this.priorityY(), mouseX, mouseY, '+');

        // 强制通断
        int tx = this.toggleX();
        int ty = this.toggleY();
        boolean hoverToggle = inside(mouseX, mouseY, tx, ty, WIDTH - 28, 14);
        graphics.fill(tx, ty, tx + WIDTH - 28, ty + 14, hoverToggle ? BUTTON_HOVER : BUTTON);
        graphics.fill(tx, ty, tx + WIDTH - 28, ty + 1, 0xFFFFFFFF);
        graphics.fill(tx, ty + 13, tx + WIDTH - 28, ty + 14, BORDER);
    }

    private void drawSmallButton(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, char glyph) {
        boolean hover = inside(mouseX, mouseY, x, y, BUTTON_SIZE, BUTTON_SIZE);
        graphics.fill(x, y, x + BUTTON_SIZE, y + BUTTON_SIZE, hover ? BUTTON_HOVER : BUTTON);
        graphics.fill(x, y, x + BUTTON_SIZE, y + 1, 0xFFFFFFFF);
        graphics.fill(x, y + BUTTON_SIZE - 1, x + BUTTON_SIZE, y + BUTTON_SIZE, BORDER);
        int colour = hover ? 0xFF2B2B2B : 0xFF4A4A4A;
        if (glyph == '-') {
            graphics.fill(x + 3, y + 5, x + 9, y + 6, colour);
        } else {
            graphics.fill(x + 3, y + 5, x + 9, y + 6, colour);
            graphics.fill(x + 5, y + 3, x + 6, y + 8, colour);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 5, TITLE, false);

        graphics.drawString(this.font, Component.translatable("sunflare.gui.bus.priority"), 14, 24, LABEL, false);
        graphics.drawString(this.font, Component.literal(String.valueOf(this.menu.priority())),
                126 - this.font.width(Component.literal(String.valueOf(this.menu.priority()))) / 2, 24, VALUE, false);

        Component status = switch (this.menu.forcedOff() == 1 ? 2 : (this.menu.suspended() == 1 ? 1 : 0)) {
            case 2 -> Component.translatable("sunflare.gui.bus.status.off");
            case 1 -> Component.translatable("sunflare.gui.bus.status.suspended");
            default -> Component.translatable("sunflare.gui.bus.status.active");
        };
        graphics.drawString(this.font, Component.translatable("sunflare.gui.bus.status"), 14, 44, LABEL, false);
        graphics.drawString(this.font, status, 162 - this.font.width(status), 44, ACCENT, false);

        graphics.drawString(this.font, Component.translatable("sunflare.gui.bus.energy",
                this.menu.energy() / 1000, this.menu.capacity() / 1000), 14, 68, LABEL, false);

        Component toggle = Component.translatable(this.menu.forcedOff() == 1
                ? "sunflare.gui.bus.toggle.on"
                : "sunflare.gui.bus.toggle.off");
        graphics.drawString(this.font, toggle,
                this.leftPos + WIDTH / 2 - this.font.width(toggle) / 2 - this.leftPos,
                this.toggleY() + 3 - this.topPos, VALUE, false);
    }

    private void send(int id) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (inside(mouseX, mouseY, this.closeX(), this.closeY(), BUTTON_SIZE, BUTTON_SIZE)) {
                this.onClose();
                return true;
            }
            if (inside(mouseX, mouseY, this.minusX(), this.priorityY(), BUTTON_SIZE, BUTTON_SIZE)) {
                this.send(PriorityBusMenu.BUTTON_PRIORITY_DOWN);
                return true;
            }
            if (inside(mouseX, mouseY, this.plusX(), this.priorityY(), BUTTON_SIZE, BUTTON_SIZE)) {
                this.send(PriorityBusMenu.BUTTON_PRIORITY_UP);
                return true;
            }
            if (inside(mouseX, mouseY, this.toggleX(), this.toggleY(), WIDTH - 28, 14)) {
                this.send(PriorityBusMenu.BUTTON_TOGGLE);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
