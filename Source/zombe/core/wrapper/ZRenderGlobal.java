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
    public void func_180447_b(float p_180447_1_, int p_180447_2_) {
        renderClouds(p_180447_1_, p_180447_2_);
    }
    public void superRenderClouds(float p_180447_1_, int p_180447_2_) {
        super.func_180447_b(p_180447_1_, p_180447_2_);
    }
    public void renderClouds(float delta, int arg2) {
        //if (ZHandle.handle("beforeRenderClouds", (Float) delta, true))
        superRenderClouds(delta, arg2);
        //ZHandle.handle("afterRenderClouds", delta);
    }

    @Override
    public void func_174970_a(Entity p1, double p2, ICamera p3, int p4, boolean p5) {
        sortAndRender();
        super.func_174970_a(p1, p2, p3, p4, p5);
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
