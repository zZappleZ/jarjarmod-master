
package net.jarjar.jarjarmod.client.screen;

import net.jarjar.jarjarmod.client.TomeClientCache;
import net.jarjar.jarjarmod.component.handling.TomePackets;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class TomeSelectionScreen extends Screen {

    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 220;
    private static final int ROW_HEIGHT = 25;
    private static final int VISIBLE_ROWS = 6;

    private final ArchivistMenuScreen parent;
    private final int targetSlot;

    private int panelX;
    private int panelY;
    private int scrollOffset = 0;

    public TomeSelectionScreen(
            ArchivistMenuScreen parent,
            int targetSlot
    ) {
        super(Text.literal("Select a Tome"));
        this.parent = parent;
        this.targetSlot = targetSlot;
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_WIDTH) / 2;
        panelY = (height - PANEL_HEIGHT) / 2;
    }

    private int getOtherEquippedSlot(String tomeId) {
        for (int slot = 0; slot < 3; slot++) {
            if (slot == targetSlot) {
                continue;
            }

            String equipped = TomeClientCache.getEquippedTome(slot);

            if (tomeId.equals(equipped)) {
                return slot;
            }
        }

        return -1;
    }

    private String getRomanNumeral(int slot) {
        return switch (slot) {
            case 0 -> "I";
            case 1 -> "II";
            case 2 -> "III";
            default -> "?";
        };
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        renderBackground(context);

        context.fill(
                panelX, panelY,
                panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT,
                0xFF171717
        );

        context.fill(
                panelX + 2, panelY + 2,
                panelX + PANEL_WIDTH - 2,
                panelY + PANEL_HEIGHT - 2,
                0xFF353535
        );

        context.fill(
                panelX + 4, panelY + 4,
                panelX + PANEL_WIDTH - 4,
                panelY + PANEL_HEIGHT - 4,
                0xFF171717
        );

        context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("Select Tome — Slot " + (targetSlot + 1)),
                panelX + PANEL_WIDTH / 2,
                panelY + 10,
                0xFFE6D5B8
        );

        // Row zero always clears the selected slot.
        drawRow(
                context,
                panelY + 32,
                Text.literal("Clear Slot"),
                ItemStack.EMPTY,
                mouseX,
                mouseY,
                0xFFFFFFFF
        );

        List<String> tomes = TomeClientCache.getUnlockedTomes();

        if (tomes.isEmpty()) {
            context.drawCenteredTextWithShadow(
                    textRenderer,
                    Text.literal("No tomes unlocked"),
                    panelX + PANEL_WIDTH / 2,
                    panelY + 82,
                    0xFFAAAAAA
            );
        }

        int listTop = panelY + 60;

        for (int visibleIndex = 0;
             visibleIndex < VISIBLE_ROWS;
             visibleIndex++) {

            int tomeIndex = scrollOffset + visibleIndex;

            if (tomeIndex >= tomes.size()) {
                break;
            }

            String tomeId = tomes.get(tomeIndex);

            int otherSlot = getOtherEquippedSlot(tomeId);

            String displayName = TomeDisplay.getName(tomeId).getString();

            if (otherSlot != -1) {
                displayName += " (Equipped in "
                        + getRomanNumeral(otherSlot) + ")";
            }

            int textColor = otherSlot != -1
                    ? 0xFF777777
                    : 0xFFFFFFFF;

            drawRow(
                    context,
                    listTop + visibleIndex * ROW_HEIGHT,
                    Text.literal(displayName),
                    TomeDisplay.getIcon(tomeId),
                    mouseX,
                    mouseY,
                    textColor
            );
        }

        context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("Scroll to see more"),
                panelX + PANEL_WIDTH / 2,
                panelY + PANEL_HEIGHT - 15,
                0xFF888888
        );

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawRow(
            DrawContext context,
            int y,
            Text label,
            ItemStack icon,
            int mouseX,
            int mouseY,
            int textColor
    ) {
        int x = panelX + 8;
        int width = PANEL_WIDTH - 16;

        boolean hovered =
                mouseX >= x && mouseX < x + width
                        && mouseY >= y && mouseY < y + ROW_HEIGHT;

        context.fill(
                x, y,
                x + width, y + ROW_HEIGHT - 1,
                hovered ? 0xFF62513A : 0xFF292929
        );

        if (!icon.isEmpty()) {
            context.drawItem(icon, x + 4, y + 4);
        }

        context.drawText(
                textRenderer,
                label,
                x + 28,
                y + 8,
                textColor,
                false
        );
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int rowX = panelX + 8;
        int rowWidth = PANEL_WIDTH - 16;

        // Clear Slot row.
        int clearY = panelY + 32;

        if (mouseX >= rowX && mouseX < rowX + rowWidth
                && mouseY >= clearY
                && mouseY < clearY + ROW_HEIGHT) {
            TomePackets.selectTome(targetSlot, null);
            client.setScreen(parent);
            return true;
        }

        // Unlocked tome rows.
        int listTop = panelY + 60;

        if (mouseX >= rowX && mouseX < rowX + rowWidth
                && mouseY >= listTop
                && mouseY < listTop + VISIBLE_ROWS * ROW_HEIGHT) {

            int visibleIndex =
                    (int) ((mouseY - listTop) / ROW_HEIGHT);

            int tomeIndex = scrollOffset + visibleIndex;

            List<String> tomes = TomeClientCache.getUnlockedTomes();

            if (tomeIndex >= 0 && tomeIndex < tomes.size()) {
                String tomeId = tomes.get(tomeIndex);

                if (getOtherEquippedSlot(tomeId) != -1) {
                    return true;
                }

                TomePackets.selectTome(
                        targetSlot,
                        tomeId
                );

                client.setScreen(parent);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        List<String> tomes = TomeClientCache.getUnlockedTomes();

        int maxOffset = Math.max(0, tomes.size() - VISIBLE_ROWS);

        scrollOffset -= (int) Math.signum(amount);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxOffset));

        return true;
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
