package dev.ggclient.client.mixin.render;

import dev.ggclient.client.render.entity.EntityFill;
import dev.ggclient.client.render.entity.EntityOutline;
import dev.ggclient.client.render.entity.Filled;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements Filled {
    @Unique
    private EntityFill ggclient$fill;
    @Unique
    private EntityOutline ggclient$outline;

    @Override
    public EntityFill ggclient$fill() {
        return ggclient$fill;
    }

    @Override
    public void ggclient$setFill(EntityFill fill) {
        ggclient$fill = fill;
    }

    @Override
    public EntityOutline ggclient$outline() {
        return ggclient$outline;
    }

    @Override
    public void ggclient$setOutline(EntityOutline outline) {
        ggclient$outline = outline;
    }
}
