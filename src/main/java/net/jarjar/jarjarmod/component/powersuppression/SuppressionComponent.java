package net.jarjar.jarjarmod.component.powersuppression;

import dev.onyxstudios.cca.api.v3.component.Component;

public interface SuppressionComponent extends Component {

    boolean isSuppressed();
    void setSuppressed(boolean suppressed);
}
