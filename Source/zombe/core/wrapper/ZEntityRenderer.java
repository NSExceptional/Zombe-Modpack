package zombe.core.wrapper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.IResourceManager;
import zombe.core.ZHandle;

public final class ZEntityRenderer extends EntityRenderer {

    private Minecraft mc;

    public ZEntityRenderer(Minecraft minecraft, IResourceManager irm) {
        super(minecraft, irm);
        mc = minecraft;
    }

    @Override
    public void updateCameraAndRender(float partialTicks, long nanoTime) {
        super.updateCameraAndRender(partialTicks, nanoTime);
        ZHandle.onUpdateCameraAndRender(partialTicks);
    }

    @Override
    public void renderWorld(float par1, long par2) {
        // ZMod.beginRenderWorld(par1, par2);
        super.renderWorld(par1, par2);
        // ZMod.endRenderWorld(par1, par2);
    }

    @Override
    protected void renderRainSnow(float par) {
        ZHandle.beginRenderRainSnow(par);
        //noinspection ConstantConditions
        if (ZHandle.forwardRenderRainSnow())
            super.renderRainSnow(par);
        ZHandle.endRenderRainSnow(par);
    }

    @Override
    public void updateRenderer() {
        ZHandle.handle("beforeUpdateRenderer");
        try {
            super.updateRenderer();
        } catch (Exception e) {
            ZHandle.handle("catchUpdateRenderer", e);
        }
        ZHandle.handle("afterUpdateRenderer");
    }

    @Override
    public void getMouseOver(float par1) {
        ZHandle.handle("beforeGetMouseOver", par1);
        try {
            super.getMouseOver(par1);
        } catch (Exception e) {
            ZHandle.handle("catchGetMouseOver", e);
        }
        ZHandle.handle("afterGetMouseOver", par1);
    }

}
