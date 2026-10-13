package dev.ggclient.client.render.state;

import dev.ggclient.client.mixin.accessor.GuiGraphicsExtractorAccessor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Submit {
    public static void submit(GuiGraphicsExtractor graphics, GuiElementRenderState state) {
        ((GuiGraphicsExtractorAccessor) graphics).ggclient$getGuiRenderState().addGuiElement(state);
    }
}
