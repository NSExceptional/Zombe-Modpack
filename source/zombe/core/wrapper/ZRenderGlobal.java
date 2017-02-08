package zombe.core.wrapper;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import zombe.core.ZHandle;

import javax.annotation.Nonnull;

public class ZRenderGlobal extends RenderGlobal {

    private Minecraft mc;

    public ZRenderGlobal(@Nonnull Minecraft minecraft) {
        super(minecraft);
        this.mc = minecraft;
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
    public void setupTerrain(@Nonnull Entity viewEntity,
                             double partialTicks,
                             @Nonnull ICamera camera,
                             int frameCount,
                             boolean playerSpectator) {
        this.sortAndRender();
        super.setupTerrain(viewEntity, partialTicks, camera, frameCount, playerSpectator);
    }

    @Override
    public void renderClouds(float partialTicks, int pass) {
        //if (ZHandle.handle("beforeRenderClouds", (Float) partialTicks, true))
        super.renderClouds(partialTicks, pass);
        //ZHandle.handle("afterRenderClouds", partialTicks);
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
