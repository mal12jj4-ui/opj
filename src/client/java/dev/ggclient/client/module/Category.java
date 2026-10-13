package dev.ggclient.client.module;

import dev.ggclient.client.render.GGIcon;
import dev.ggclient.client.util.Lang;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true)
public enum Category {
    COMBAT("combat", GGIcon.COMBAT),
    MOVEMENT("movement", GGIcon.MOVEMENT),
    RENDER("render", GGIcon.RENDER),
    HUD("hud", GGIcon.HUD),
    PLAYER("player", GGIcon.PLAYER),
    MISC("misc", GGIcon.MISC),
    DONUT("donut", GGIcon.FLY);

    @Getter
    private final String id;
    @Getter
    private final GGIcon icon;

    public String label() {
        return Lang.get("category." + id);
    }
}
