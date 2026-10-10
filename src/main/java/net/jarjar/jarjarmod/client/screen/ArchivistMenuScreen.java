
package net.jarjar.jarjarmod.client.screen;

import net.jarjar.jarjarmod.client.InkflowClientCache;
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

    private int panelX;
    private int panelY;

    public ArchivistMenuScreen() {
        super(Text.literal("The Archivist"));
    }

    @Override
    protected void init() {
        panelX = (this.width - PANEL_WIDTH) / 2;
        panelY = (this.height - PANEL_HEIGHT) / 2;
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        this.renderBackground(context);

        // Main panel and inset border.
        context.fill(
                panelX, panelY,
                panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT,
                0xF0181818
        );

        context.fill(
                panelX + 2, panelY + 2,
                panelX + PANEL_WIDTH - 2, panelY + PANEL_HEIGHT - 2,
                0xFF303030
        );

        context.fill(
                panelX + 5, panelY + 5,
                panelX + PANEL_WIDTH - 5, panelY + PANEL_HEIGHT - 5,
                0xF0181818
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                panelX + PANEL_WIDTH / 2,
                panelY + 12,
                0xFFE6D5B8
        );

        // Inkflow and replenishment.
        int leftX = panelX + 14;
        int rightX = panelX + PANEL_WIDTH - 154;
        int barWidth = 140;

        drawBar(
                context,
                "Inkflow",
                InkflowClientCache.getCurrent(),
                InkflowClientCache.getCapacity(),
                leftX,
                panelY + 38,
                barWidth,
                0xFF58B7D8
        );

        context.drawText(
                this.textRenderer,
                Text.literal(String.format(
                        java.util.Locale.ROOT,
                        "Replenishment: %.2f/s",
                        InkflowClientCache.getReplenishmentPerSecond()
                )),
                leftX,
                panelY + 59,
                0xFFD8D8D8,
                false
        );

        // Four separate Focus bars.
        drawBar(
                context,
                "Script",
                InkflowClientCache.getFocusScript(),
                FOCUS_MAX,
                leftX,
                panelY + 88,
                barWidth,
                0xFF69A9E8
        );

        drawBar(
                context,
                "Inscribe",
                InkflowClientCache.getFocusInscribe(),
                FOCUS_MAX,
                leftX,
                panelY + 128,
                barWidth,
                0xFFBA8CE8
        );

        drawBar(
                context,
                "Redact",
                InkflowClientCache.getFocusRedact(),
                FOCUS_MAX,
                rightX,
                panelY + 88,
                barWidth,
                0xFFE7777
        );

        drawBar(
                context,
                "Manifest",
                InkflowClientCache.getFocusManifest(),
                FOCUS_MAX,
                rightX,
                panelY + 128,
                barWidth,
                0xFF79D49A
        );

        // Render the actual player model in the middle.
        PlayerEntity player = this.client == null
                ? null
                : this.client.player;

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

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Tome slots will be added next"),
                panelX + PANEL_WIDTH / 2,
                panelY + PANEL_HEIGHT - 19,
                0xFFAAAAAA
        );

        super.render(context, mouseX, mouseY, delta);
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
                this.textRenderer,
                Text.literal(label),
                x,
                y,
                0xFFE6D5B8,
                false
        );

        String value = safeCurrent + " / " + safeMaximum;

        context.drawText(
                this.textRenderer,
                Text.literal(value),
                x + width - this.textRenderer.getWidth(value),
                y,
                0xFFD8D8D8,
                false
        );

        // Bar background.
        context.fill(
                x, y + 12,
                x + width, y + 19,
                0xFF090909
        );

        context.fill(
                x + 1, y + 13,
                x + width - 1, y + 18,
                0xFF383838
        );

        // Colored fill.
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
