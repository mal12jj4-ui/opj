package dev.ggclient.client.module.impl.donut;

import dev.ggclient.client.module.Module;
import dev.ggclient.client.ui.DonutWheelScreen;

/** Enable it, bind a key, and the key opens a radial menu of server commands. */
public final class DonutWheel extends Module {
    public DonutWheel() {
        super("donutWheel");
    }

    @Override
    public boolean activatable() {
        return true;
    }

    @Override
    protected void onActivate() {
        mc.setScreenAndShow(new DonutWheelScreen());
    }
}
