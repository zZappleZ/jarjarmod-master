package net.jarjar.jarjarmod.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import io.github.apace100.apoli.power.Active;

import java.util.Set;

public interface KeyPressComponent extends Component {

    Set<Active.Key> getCurrentlyUsedKeys();

    Set<Active.Key> getPreviouslyUsedKeys();

    void setPreviouslyUsedKeys();

    Set<Active.Key> getKeysToCheck();

    void addKeyToCheck(Active.Key key);

    void addKey(Active.Key key);

    void removeKey(Active.Key key);
}
