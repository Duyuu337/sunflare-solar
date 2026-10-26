package dev.sunflare.client;

import dev.sunflare.menu.SubspaceRelayMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 中继信标界面。
 */
public class SubspaceRelayScreen extends AbstractContainerScreen<SubspaceRelayMenu> {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 112;

    private static final int BG = 0xFFC6C6C6;
    private static final int BORDER = 0xFF555555;
    private static final int SEPARATOR = 0xFFA8A8A8;
    private static final int TITLE = 0xFF3F3F3F;
    private static final int LABEL = 0xFF5A5A5A;
    private static final int VALUE = 0xFF2B2B2B;
    private static final int ACCENT = 0xFF1E5B8C;
    private static final int TRACK = 0xFF767676;
    private static final int BAR = 0xFF3A7BBF;
    private static final int BAR_TOP = 0xFF7DB6E4;
    private static final int BUTTON = 0xFFAAAAAA;
    private static final int BUTTON_HOVER = 0xFFBFBFBF;

    private static final int BUTTON_SIZE = 12;

    public SubspaceRelayScreen(SubspaceRelayMenu menu, Inventory inventory, Component title) {
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

    private int primaryX() {
        return this.leftPos + 14;
    }

    private int primaryY() {
        return this.topPos + 72;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        g.fill(x, y, x + WIDTH, y + HEIGHT, BG);
        g.fill(x, y, x + WIDTH, y + 1, BORDER);
        g.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, BORDER);
        g.fill(x, y, x + 1, y + HEIGHT, BORDER);
        g.fill(x + WIDTH - 1, y, x + WIDTH, y + HEIGHT, BORDER);
        g.fill(x + 1, y + 17, x + WIDTH - 1, y + 18, SEPARATOR);
        g.fill(x + 8, y + 40, x + WIDTH - 8, y + 41, SEPARATOR);

        int barX = x + 14;
        int barY = y + 28;
        int barW = WIDTH - 28;
        int barH = 8;
        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF373737);
        g.fill(barX, barY, barX + barW, barY + barH, TRACK);
        int filled = (int) Math.min(barW, (long) this.menu.energy() * barW / this.menu.capacity());
        if (filled > 0) {
            g.fill(barX, barY, barX + filled, barY + barH, BAR);
            g.fill(barX, barY, barX + filled, barY + 1, BAR_TOP);
        }

        boolean canInsert = this.menu.hasCard() == 0;
        drawButton(g, this.primaryX(), this.primaryY(),
                Component.translatable(canInsert ? "sunflare.gui.relay.insert" : "sunflare.gui.relay.eject"),
                mouseX, mouseY);

        int bx = this.closeX();
        int by = this.closeY();
        boolean hover = inside(mouseX, mouseY, bx, by, BUTTON_SIZE, BUTTON_SIZE);
        g.fill(bx, by, bx + BUTTON_SIZE, by + BUTTON_SIZE, hover ? BUTTON_HOVER : BUTTON);
        g.fill(bx, by, bx + BUTTON_SIZE, by + 1, 0xFFFFFFFF);
        g.fill(bx, by + BUTTON_SIZE - 1, bx + BUTTON_SIZE, by + BUTTON_SIZE, BORDER);
        int cross = hover ? 0xFF2B2B2B : 0xFF4A4A4A;
        for (int i = 0; i < 6; i++) {
            g.fill(bx + 3 + i, by + 3 + i, bx + 4 + i, by + 4 + i, cross);
            g.fill(bx + 3 + i, by + 8 - i, bx + 4 + i, by + 9 - i, cross);
        }
    }

    private void drawButton(GuiGraphics g, int x, int y, Component label, int mouseX, int mouseY) {
        int w = WIDTH - 28;
        boolean hover = inside(mouseX, mouseY, x, y, w, 14);
        g.fill(x, y, x + w, y + 14, hover ? BUTTON_HOVER : BUTTON);
        g.fill(x, y, x + w, y + 1, 0xFFFFFFFF);
        g.fill(x, y + 13, x + w, y + 14, BORDER);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, 8, 5, TITLE, false);

        Component buffer = Component.translatable("sunflare.gui.relay.buffer",
                this.menu.energy() / 1000, this.menu.capacity() / 1000);
        g.drawString(this.font, buffer, 14, 18, LABEL, false);

        Component link = this.menu.hasCard() == 1
                ? Component.translatable("sunflare.gui.relay.linked")
                : Component.translatable("sunflare.gui.relay.nolink");
        g.drawString(this.font, link, 14, 46, ACCENT, false);
        g.drawString(this.font, Component.translatable("sunflare.gui.relay.hint"), 14, 58, LABEL, false);

        boolean canInsert = this.menu.hasCard() == 0;
        Component label = Component.translatable(canInsert ? "sunflare.gui.relay.insert" : "sunflare.gui.relay.eject");
        g.drawString(this.font, label, WIDTH / 2 - this.font.width(label) / 2, this.primaryY() + 3 - this.topPos, VALUE, false);
    }

    private void send(int id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null) {
            mc.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (inside(mouseX, mouseY, this.closeX(), this.closeY(), BUTTON_SIZE, BUTTON_SIZE)) {
                this.onClose();
                return true;
            }
            if (inside(mouseX, mouseY, this.primaryX(), this.primaryY(), WIDTH - 28, 14)) {
                this.send(this.menu.hasCard() == 0 ? SubspaceRelayMenu.BUTTON_INSERT : SubspaceRelayMenu.BUTTON_EJECT);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
