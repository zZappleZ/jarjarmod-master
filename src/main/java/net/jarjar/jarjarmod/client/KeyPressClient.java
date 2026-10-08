package net.jarjar.jarjarmod.client;

import io.github.apace100.apoli.power.Active;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.jarjar.jarjarmod.component.KeyPressComponent;
import net.jarjar.jarjarmod.component.ModEntityComponents;
import net.jarjar.jarjarmod.network.KeyPressNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeyPressClient {

    private static final Map<String, Boolean> LAST_KEY_STATES =
            new HashMap<>();

    private static final Map<String, KeyBinding> KEY_BINDINGS =
            new HashMap<>();

    private static boolean keyBindingsInitialized = false;

    public static void register() {

        ClientTickEvents.START_CLIENT_TICK.register(
                KeyPressClient::tick
        );
    }

    private static void tick(MinecraftClient client) {

        PlayerEntity player = client.player;

        if (player == null) {
            return;
        }

        KeyPressComponent component =
                ModEntityComponents.KEY_PRESS_COMPONENT.get(player);

        if (component.getKeysToCheck().isEmpty()) {
            LAST_KEY_STATES.clear();
            return;
        }

        initializeKeyBindings(client);

        Set<Active.Key> addedKeys = new HashSet<>();
        Set<Active.Key> removedKeys = new HashSet<>();

        Map<String, Boolean> currentKeyStates =
                new HashMap<>();

        for (Active.Key key : component.getKeysToCheck()) {

            KeyBinding keyBinding =
                    KEY_BINDINGS.get(key.key);

            if (keyBinding == null) {
                continue;
            }

            boolean pressed = keyBinding.isPressed();

            boolean previouslyPressed =
                    LAST_KEY_STATES.getOrDefault(
                            key.key,
                            false
                    );

            currentKeyStates.put(
                    key.key,
                    pressed
            );

            if (pressed) {

                if (key.continuous || !previouslyPressed) {

                    component.addKey(key);

                    if (!previouslyPressed) {
                        addedKeys.add(key);
                    }
                }

            } else if (previouslyPressed) {

                component.removeKey(key);

                removedKeys.add(key);
            }
        }

        if (!addedKeys.isEmpty() || !removedKeys.isEmpty()) {

            KeyPressNetworking.sendUpdatedKeys(
                    addedKeys,
                    removedKeys
            );
        }

        LAST_KEY_STATES.clear();
        LAST_KEY_STATES.putAll(currentKeyStates);
    }

    private static void initializeKeyBindings(
            MinecraftClient client
    ) {

        if (keyBindingsInitialized) {
            return;
        }

        for (KeyBinding keyBinding : client.options.allKeys) {

            KEY_BINDINGS.put(
                    keyBinding.getTranslationKey(),
                    keyBinding
            );
        }

        keyBindingsInitialized = true;
    }
}
