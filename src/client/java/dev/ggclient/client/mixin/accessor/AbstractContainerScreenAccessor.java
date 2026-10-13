package dev.ggclient.client.mixin.accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("hoveredSlot")
    Slot ggclient$getHoveredSlot();

    @Accessor("leftPos")
    int ggclient$getLeftPos();

    @Accessor("topPos")
    int ggclient$getTopPos();

    @Invoker("slotClicked")
    void ggclient$slotClicked(Slot slot, int index, int button, ContainerInput input);
}
