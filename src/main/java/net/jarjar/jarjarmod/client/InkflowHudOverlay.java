package net.jarjar.jarjarmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.HudRendered;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.jarjar.jarjarmod.component.ModPlayerData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;

public class InkflowHudOverlay implements HudRenderCallback {

    private static final Identifier INKFLOW_UI_ATLAS = new Identifier("jarjarmod", "textures/ui/resource_bar_ui.png");

    private static final int BAR_WIDTH = 81;
    private static final int BAR_HEIGHT = 13;
    private static final int FILL_HEIGHT = 5;
    private static final int FILL_V_OFFSET = 15;

    //Origins stack
    private static final int ORIGINS_ROW_STEP = 8;
    private static final int EXTRA_GAP = 4;

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.getProfiler().swap("inkflow");

            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            int x = width / 2;
            int y = height;

            if (client.player != null && !client.player.isSpectator()) {
                ClientPlayerEntity player = client.player;

                // TODO: once origin-selection exists, gate this behind an
                // "is this player an Archivist" check, similar to how the
                // shield HUD checks for the item being held.

                int yShift = 53; // base offset above the hotbar

                int maxAir = player.getMaxAir();
                int playerAir = Math.min(player.getAir(), maxAir);
                if (player.getAbilities().creativeMode) yShift -= 17;
                if (player.isSubmergedIn(FluidTags.WATER) || playerAir < maxAir) yShift += 10;

                LivingEntity riddenEntity = getRiddenEntity();
                if (riddenEntity != null) {
                    if (getHeartCount(riddenEntity) > 10) yShift += 10;
                    if (player.getAbilities().creativeMode) yShift += 17;
                }

                if (!client.options.hudHidden) {
                    ModPlayerData data = (ModPlayerData) player;
                    long activeOriginsBars = PowerHolderComponent.KEY.get(player).getPowers().stream()
                            .filter(p -> p instanceof HudRendered)
                            .map(p -> (HudRendered) p)
                            .filter(HudRendered::shouldRender)
                            .count();

                    // Position ourselves directly above the current top of their stack
                    int baseX = x;
                    int baseY = y - (int) (activeOriginsBars * ORIGINS_ROW_STEP);

                    int current = InkflowClientCache.getCurrent();
                    int capacity = Math.max(1, InkflowClientCache.getCapacity()); // guard div-by-zero
                    float percent = current / (float) capacity;

                    drawContext.drawTexture(INKFLOW_UI_ATLAS, baseX + 10, baseY - yShift, 0, 0, BAR_WIDTH, BAR_HEIGHT);
                    drawContext.drawTexture(INKFLOW_UI_ATLAS, baseX + 10, baseY - yShift + 5, 0, FILL_V_OFFSET,
                            (int) (BAR_WIDTH * percent), FILL_HEIGHT);
                }
            }

            client.getProfiler().pop();
        }

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private LivingEntity getRiddenEntity() {
        PlayerEntity playerEntity = getCameraPlayer();
        if (playerEntity == null) return null;
        Entity vehicle = playerEntity.getVehicle();
        return vehicle instanceof LivingEntity living ? living : null;
    }

    private int getHeartCount(LivingEntity entity) {
        if (entity == null || !entity.isLiving()) return 0;
        int hearts = (int) (entity.getMaxHealth() + 0.5f) / 2;
        return Math.min(hearts, 30);
    }

    private PlayerEntity getCameraPlayer() {
        return MinecraftClient.getInstance().getCameraEntity() instanceof PlayerEntity p ? p : null;
    }
}
