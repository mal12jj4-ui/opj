package dev.ggclient.client;

import dev.ggclient.client.util.Clicks;
import dev.ggclient.client.util.Time;
import dev.ggclient.client.event.Priority;
import dev.ggclient.client.backtrack.Backtracker;
import dev.ggclient.client.config.StateStore;
import dev.ggclient.client.event.EventBus;
import dev.ggclient.client.event.events.PreTickEvent;
import dev.ggclient.client.event.events.TickEvent;
import dev.ggclient.client.input.Keybinds;
import dev.ggclient.client.module.ModuleManager;
import dev.ggclient.client.render.screen.ScreenRenderer;
import dev.ggclient.client.render.blocks.BlockIndex;
import dev.ggclient.client.render.blocks.BlockRenderer;
import dev.ggclient.client.render.world.WorldRenderer;
import dev.ggclient.client.rotation.RotationManager;
import dev.ggclient.client.ui.HudEditor;
import dev.ggclient.client.ui.preview.EntityPreview;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class GGClient implements ClientModInitializer {
    public static final EventBus EVENTS = new EventBus();
    public static final ModuleManager MODULES = new ModuleManager();
    public static final RotationManager ROTATIONS = new RotationManager();

    @Override
    public void onInitializeClient() {
        StateStore.init();
        Backtracker.init();
        Keybinds.register();
        ROTATIONS.init();
        HudEditor.init();
        WorldRenderer.init();
        BlockIndex.init(BlockRenderer::onSectionChanged);
        BlockRenderer.init();
        ScreenRenderer.init();
        EVENTS.subscribe(PreTickEvent.class, Priority.HIGHEST, event -> Time.tick());
        EVENTS.subscribe(PreTickEvent.class, event -> MODULES.tickKeybinds(event.client()));
        ClientTickEvents.START_CLIENT_TICK.register(client -> EVENTS.post(new PreTickEvent(client)));
        ClientTickEvents.END_CLIENT_TICK.register(client -> EVENTS.post(new TickEvent(client)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> EntityPreview.clear());
        Clicks.init();
    }
}
