package net.jarjar.jarjarmod;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.api.ClientModInitializer;
import net.jarjar.jarjarmod.client.InkflowHudOverlay;
import net.jarjar.jarjarmod.component.handling.ArchivistPackets;

public class ModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ArchivistPackets.registerGlobalReceiversS2C();
        HudRenderCallback.EVENT.register(new InkflowHudOverlay());
    }
}
