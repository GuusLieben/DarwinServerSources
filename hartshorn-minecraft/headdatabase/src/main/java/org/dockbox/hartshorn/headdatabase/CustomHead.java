package org.dockbox.hartshorn.headdatabase;

import org.dockbox.hartshorn.server.minecraft.players.ProfileProperty;
import org.dockbox.hartshorn.util.HartshornUtils;

import java.util.List;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class CustomHead {

    @Getter private String name;
    @Getter private String value;
    private String uuid;
    private String tags;

    public List<String> tags() {
        if (this.tags == null) return HartshornUtils.emptyList();
        return HartshornUtils.asUnmodifiableList(this.tags.split(","));
    }

    public ProfileProperty property() {
        return ProfileProperty.of("textures", this.value());
    }

    public UUID uuid() {
        return UUID.fromString(this.uuid);
    }

}
