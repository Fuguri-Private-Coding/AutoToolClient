package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import Effekseer.api.EffekseerManager;
import Effekseer.swig.EffekseerEffectCore;
import Effekseer.swig.EffekseerManagerCore;
import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.matrix.MatrixUtils;
import net.minecraft.client.gui.ScaledResolution;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;

@ModuleInfo(name = "Effect", category = Category.VISUAL)
public class Effect extends Module {

    IntegerSetting speed = new IntegerSetting("Speed", this, 1,200,60);

    private int effectHandle = -1;
    EffekseerManagerCore effekseerManagerCore = null;
    EffekseerEffectCore effekseerEffectCore = null;
    private static final float[] CAMERA_TRANSFORM_DATA = new float[16];
    private static final float[] PROJECTION_MATRIX_DATA = new float[16];

    @Override
    public void onEnable() {
        super.onEnable();
        effekseerManagerCore = Client.INST.getLoadNatives().getEffekseerManagerCore();
        effekseerEffectCore = Client.INST.getLoadNatives().getEffekseerEffectCore();
        effekseerManagerCore.Stop(effectHandle);
        effectHandle = effekseerManagerCore.Play(effekseerEffectCore);
        effekseerManagerCore.SetEffectPosition(effectHandle, (float) mc.thePlayer.posX, (float) mc.thePlayer.posY, (float) mc.thePlayer.posZ);
    }

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof Render3DEvent) {
            assert mc.thePlayer != null;
            assert mc.theWorld != null;

            ScaledResolution sc = new ScaledResolution(mc);

            EffekseerManager manager = new EffekseerManager(this.effekseerManagerCore);
            glPushMatrix();

            MatrixUtils.getCameraAndProjectionMatrices(mc.timer.renderPartialTicks, CAMERA_TRANSFORM_DATA, PROJECTION_MATRIX_DATA);
            manager.setViewport(sc.getScaledWidth(), sc.getScaledHeight());
            manager.setCameraMatrix(CAMERA_TRANSFORM_DATA);
            manager.setProjectionMatrix(PROJECTION_MATRIX_DATA);
            manager.update(speed.getValue() * getDeltaTime());
            manager.draw();

            glPopMatrix();
        }
    }

    private static float getDeltaTime() {
        long last = lastDrawTime;
        if (last == 0) {
            lastDrawTime = System.nanoTime();
            return 1f / 60;
        }

        long now = System.nanoTime();
        lastDrawTime = now;
        return (float) ((now - last) * 1e-9);
    }

    private static long lastDrawTime = 0;
}
