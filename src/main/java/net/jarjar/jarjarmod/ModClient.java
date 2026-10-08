package net.jarjar.jarjarmod;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.api.ClientModInitializer;
import net.jarjar.jarjarmod.client.InkflowHudOverlay;
import net.jarjar.jarjarmod.client.screen.ArchivistMenuScreen;
import net.jarjar.jarjarmod.component.handling.ArchivistPackets;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModClient implements ClientModInitializer {

    private static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        ArchivistPackets.registerGlobalReceiversS2C();
        HudRenderCallback.EVENT.register(new InkflowHudOverlay());

        openMenuKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.jajarmod.open_archivist_menu",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_M,
                        "category.jajarmod.archivist"
                )
        );
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (openMenuKey.wasPressed()) {

                if (client.currentScreen == null) {
                    client.setScreen(new ArchivistMenuScreen());
                }
            }
        });
    }
}
