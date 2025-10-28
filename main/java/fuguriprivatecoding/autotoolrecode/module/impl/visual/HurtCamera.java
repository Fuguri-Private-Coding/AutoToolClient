package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.PacketEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.MathHelper;

@ModuleInfo(name = "HurtCamera", category = Category.VISUAL)
public class HurtCamera extends Module {

    public FloatSetting strength = new FloatSetting("Strength", this, 0, 1, 0, 0.1f);

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof PacketEvent e &&
            e.getPacket() instanceof S12PacketEntityVelocity s12 &&
            s12.getId() == mc.thePlayer.getEntityId()) {
            final double velocityX = s12.getMotionX() / 8000.0D;
            final double velocityZ = s12.getMotionZ() / 8000.0D;

            mc.thePlayer.attackedAtYaw = (float) (MathHelper.atan2(velocityX, velocityZ) * 180.0D / Math.PI - mc.thePlayer.rotationYaw);
        }
    }

    @Override
    public void onDisable() {
        mc.thePlayer.attackedAtYaw = 0;
    }

}
