package net.jarjar.jarjarmod.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.jarjar.jarjarmod.component.ModPlayerData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;

public class HudRender implements HudRenderCallback {

    // Change "tutorial" to your mod ID.
    // Assumes texture is at: assets/tutorial/textures/ui/mana_ui.png
    private static final Identifier MANA_UI_TEXTURE = new Identifier("jarjarmod", "textures/ui/resource_bar_ui.png");

    // Config constants (Move these to a config class if needed)
    private static final int MAX_DOMAIN = 100;
    private static final int BAR_OFFSET = 0; // Vertical offset from default position

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            client.getProfiler().swap("domain");
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            int x = width / 2;
            int y = height;

            if (client.player != null && !client.player.isSpectator()) {
                ClientPlayerEntity player = client.player;

                // --- MODIFIED SECTION ---
                // Removed the Item check. The bar will always render.
                // If you want it only when holding an item, add that check back here.

                int yshift = 53 + BAR_OFFSET;
                int maxAir = player.getMaxAir();
                int playerAir = Math.min(player.getAir(), maxAir);

                // Adjust position for Creative Mode
                if (player.getAbilities().creativeMode) yshift -= 17;

                // Adjust position for Water/Air bubbles
                if (player.isSubmergedIn(FluidTags.WATER) || playerAir < maxAir) yshift += 10;

                // Adjust position for Mounts (Horses, Pigs, etc.)
                LivingEntity livingEntity = this.getRiddenEntity();
                if (livingEntity != null) {
                    int i = this.getHeartCount(livingEntity);
                    if (i > 10) {
                        yshift += 10;
                    }
                    if (player.getAbilities().creativeMode) yshift += 17;
                }

                if (!client.options.hudHidden) {
                    // --- COMPONENT ACCESS ---
                    ModPlayerData domain = (ModPlayerData) player;
                    float domainPercent = (float) domain.getInkflowCurrent() / MAX_DOMAIN;

                    // 1. Draw Background Frame
                    // UV: 0,0 | Size: 81x13
                    drawContext.drawTexture(MANA_UI_TEXTURE, x + 10, y - yshift, 0, 0, 81, 13);

                    // 2. Draw Mana Fill
                    // UV: 0,15 | Height: 5px
                    // Calculates width based on percentage (max 81px)
                    int fillWidth = (int) (81f * domainPercent);
                    drawContext.drawTexture(MANA_UI_TEXTURE, x + 10, y - yshift + 5, 0, 15, (int) (81f * ((float) domain.getInkflowCurrent() / MAX_DOMAIN)), 5);
                }
            }
            client.getProfiler().pop();
        }

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private LivingEntity getRiddenEntity() {
        PlayerEntity playerEntity = this.getCameraPlayer();
        if (playerEntity != null) {
            Entity entity = playerEntity.getVehicle();
            if (entity == null) {
                return null;
            }
            if (entity instanceof LivingEntity) {
                return (LivingEntity) entity;
            }
        }
        return null;
    }

    private int getHeartCount(LivingEntity entity) {
        if (entity == null || !entity.isLiving()) {
            return 0;
        }
        float f = entity.getMaxHealth();
        int i = (int) (f + 0.5f) / 2;
        if (i > 30) {
            i = 30;
        }
        return i;
    }

    private PlayerEntity getCameraPlayer() {
        if (!(MinecraftClient.getInstance().getCameraEntity() instanceof PlayerEntity)) {
            return null;
        }
        return (PlayerEntity) MinecraftClient.getInstance().getCameraEntity();
    }
}
