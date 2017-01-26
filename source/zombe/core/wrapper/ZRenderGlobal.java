package zombe.core.wrapper;

import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.*;
import net.minecraft.entity.*;
import zombe.core.*;

public class ZRenderGlobal extends RenderGlobal {

    private Minecraft mc;

    public ZRenderGlobal(Minecraft minecraft) {
        super(minecraft);
        mc = minecraft;
    }

    /*
    @Override
    public void forwardRenderClouds(float f) {
        super.renderClouds(f);
    }

    @Override
    public void updateClouds() {
        super.updateClouds();
    }
    */

    @Override
    public void renderClouds(float partialTicks, int pass) {
        //if (ZHandle.handle("beforeRenderClouds", (Float) partialTicks, true))
        super.renderClouds(partialTicks, pass);
        //ZHandle.handle("afterRenderClouds", partialTicks);
    }

    @Override
    public void setupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
        sortAndRender();
        super.setupTerrain(viewEntity, partialTicks, camera, frameCount, playerSpectator);
    }
    public void sortAndRender() {
        ZHandle.handle("onSortAndRender");
    }

    /*
    @Override
    public void loadRenderers() {
        ZHandle.onLoadRenderers(mc.gameSettings.fancyGraphics);
        super.loadRenderers();
    }
    */

}
