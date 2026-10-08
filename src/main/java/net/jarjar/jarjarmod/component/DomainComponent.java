package net.jarjar.jarjarmod.component;

import dev.onyxstudios.cca.api.v3.component.Component;

public interface DomainComponent extends Component {
    int getValue();
    void setValue(int value);

    // Helper methods
    default void addMana(int amount) {
        setValue(getValue() + amount);
    }

    default void subtractMana(int amount) {
        setValue(Math.max(0, getValue() - amount));
    }
}