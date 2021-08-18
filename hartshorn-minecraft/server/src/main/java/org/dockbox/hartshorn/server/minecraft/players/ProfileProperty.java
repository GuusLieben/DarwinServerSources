package org.dockbox.hartshorn.server.minecraft.players;

import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ProfileProperty {

    private final String name;
    private final String value;
    private final @Nullable String signature;

    public static ProfileProperty of(final String name, final String value) {
        return new ProfileProperty(name, value, null);
    }

    public static ProfileProperty of(final String name, final String value, final String signature) {
        return new ProfileProperty(name, value, signature);
    }

}
