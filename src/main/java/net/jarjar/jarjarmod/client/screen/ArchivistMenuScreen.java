
package net.jarjar.jarjarmod.client.screen;

import net.jarjar.jarjarmod.client.InkflowClientCache;
import net.jarjar.jarjarmod.client.TomeClientCache;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ArchivistMenuScreen extends Screen {

    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 240;
    private static final int FOCUS_MAX = 7000;

    private static final int TOME_SLOT_SIZE = 24;
    private static final int TOME_SLOT_GAP = 8;

    private static final String[] ROMAN_NUMERALS = {"I", "II", "III"};

    private int panelX;
    private int panelY;

    public ArchivistMenuScreen() {
        super(Text.literal("Inkflow Stats"));
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_WIDTH) / 2;
        panelY = (height - PANEL_HEIGHT) / 2;
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
                0xF0181818
        );

        context.fill(
                panelX + 2, panelY + 2,
                panelX + PANEL_WIDTH - 2,
                panelY + PANEL_HEIGHT - 2,
                0xFF303030
        );

        context.fill(
                panelX + 5, panelY + 5,
                panelX + PANEL_WIDTH - 5,
                panelY + PANEL_HEIGHT - 5,
                0xF0181818
        );

        context.drawCenteredTextWithShadow(
                textRenderer,
                title,
                panelX + PANEL_WIDTH / 2,
                panelY + 12,
                0xFFE6D5B8
        );

        int leftX = panelX + 14;
        int rightX = panelX + PANEL_WIDTH - 154;
        int barWidth = 140;

        drawBar(
                context, "Inkflow",
                InkflowClientCache.getCurrent(),
                InkflowClientCache.getCapacity(),
                leftX, panelY + 38, barWidth, 0xFF58B7D8
        );

        context.drawText(
                textRenderer,
                Text.literal(String.format(
                        java.util.Locale.ROOT,
                        "Replenishment: %.2f/s",
                        InkflowClientCache.getReplenishmentPerSecond()
                )),
                leftX, panelY + 59, 0xFFD8D8D8, false
        );

        drawBar(
                context, "Script",
                InkflowClientCache.getFocusScript(),
                FOCUS_MAX,
                leftX, panelY + 88, barWidth, 0xFF69A9E8
        );

        drawBar(
                context, "Inscribe",
                InkflowClientCache.getFocusInscribe(),
                FOCUS_MAX,
                leftX, panelY + 128, barWidth, 0xFFBA8CE8
        );

        drawBar(
                context, "Redact",
                InkflowClientCache.getFocusRedact(),
                FOCUS_MAX,
                rightX, panelY + 88, barWidth, 0xFFE77777
        );

        drawBar(
                context, "Manifest",
                InkflowClientCache.getFocusManifest(),
                FOCUS_MAX,
                rightX, panelY + 128, barWidth, 0xFF79D49A
        );

        PlayerEntity player = client == null ? null : client.player;

        if (player != null) {
            InventoryScreen.drawEntity(
                    context,
                    panelX + PANEL_WIDTH / 2,
                    panelY + 166,
                    42,
                    panelX + (float) PANEL_WIDTH / 2 - mouseX,
                    panelY + 115 - mouseY,
                    player
            );
        }

        drawTomeSlots(context, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawTomeSlots(
            DrawContext context,
            int mouseX,
            int mouseY
    ) {
        int totalWidth =
                3 * TOME_SLOT_SIZE + 2 * TOME_SLOT_GAP;

        int startX = panelX + (PANEL_WIDTH - totalWidth) / 2;
        int slotY = panelY + 184;

        for (int slot = 0; slot < 3; slot++) {
            int x = startX + slot * (TOME_SLOT_SIZE + TOME_SLOT_GAP);

            boolean hovered =
                    mouseX >= x && mouseX < x + TOME_SLOT_SIZE
                            && mouseY >= slotY
                            && mouseY < slotY + TOME_SLOT_SIZE;

            context.fill(
                    x - 1, slotY - 1,
                    x + TOME_SLOT_SIZE + 1,
                    slotY + TOME_SLOT_SIZE + 1,
                    hovered ? 0xFFE6D5B8 : 0xFF080808
            );

            context.fill(
                    x, slotY,
                    x + TOME_SLOT_SIZE,
                    slotY + TOME_SLOT_SIZE,
                    0xFF383838
            );

            String tomeId = TomeClientCache.getEquippedTome(slot);

            if (tomeId != null) {
                context.drawItem(
                        TomeDisplay.getIcon(tomeId),
                        x + 4,
                        slotY + 4
                );
            }

            context.drawCenteredTextWithShadow(
                    textRenderer,
                    Text.literal(ROMAN_NUMERALS[slot]),
                    x + TOME_SLOT_SIZE / 2,
                    slotY + TOME_SLOT_SIZE + 3,
                    0xFFE6D5B8
            );

            if (hovered && tomeId != null) {
                context.drawTooltip(
                        textRenderer,
                        TomeDisplay.getName(tomeId),
                        mouseX,
                        mouseY
                );
            }
        }
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button == 0) {
            int totalWidth =
                    3 * TOME_SLOT_SIZE + 2 * TOME_SLOT_GAP;

            int startX = panelX + (PANEL_WIDTH - totalWidth) / 2;
            int slotY = panelY + 184;

            for (int slot = 0; slot < 3; slot++) {
                int x = startX
                        + slot * (TOME_SLOT_SIZE + TOME_SLOT_GAP);

                if (mouseX >= x && mouseX < x + TOME_SLOT_SIZE
                        && mouseY >= slotY
                        && mouseY < slotY + TOME_SLOT_SIZE) {

                    if (client != null) {
                        client.setScreen(
                                new TomeSelectionScreen(this, slot)
                        );
                    }

                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawBar(
            DrawContext context,
            String label,
            int current,
            int maximum,
            int x,
            int y,
            int width,
            int color
    ) {
        int safeMaximum = Math.max(1, maximum);
        int safeCurrent = MathHelper.clamp(current, 0, safeMaximum);

        int fillWidth = Math.round(
                width * (safeCurrent / (float) safeMaximum)
        );

        context.drawText(
                textRenderer,
                Text.literal(label),
                x, y, 0xFFE6D5B8, false
        );

        String value = safeCurrent + " / " + safeMaximum;

        context.drawText(
                textRenderer,
                Text.literal(value),
                x + width - textRenderer.getWidth(value),
                y,
                0xFFD8D8D8,
                false
        );

        context.fill(x, y + 12, x + width, y + 19, 0xFF090909);
        context.fill(x + 1, y + 13, x + width - 1, y + 18, 0xFF383838);

        if (fillWidth > 0) {
            context.fill(
                    x + 1, y + 13,
                    x + Math.min(width - 1, fillWidth),
                    y + 18,
                    color
            );
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
