package org.dockbox.hartshorn.headdatabase;

import org.dockbox.hartshorn.util.HartshornUtils;

import java.util.Locale;

public enum HeadDatabaseCategory {
    ALPHABET,
    ANIMALS,
    BLOCKS,
    DECORATION,
    FOOD_DRINKS,
    HUMANS,
    HUMANOID,
    MISCELLANEOUS,
    MONSTERS,
    PLANTS,
    ;

    public String displayName() {
        return HartshornUtils.capitalize(this.name().toLowerCase(Locale.ROOT));
    }
}
