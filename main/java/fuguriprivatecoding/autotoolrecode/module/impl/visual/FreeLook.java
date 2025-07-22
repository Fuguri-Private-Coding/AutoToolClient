package fuguriprivatecoding.autotoolrecode.module.impl.visual;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.KillAura;
import fuguriprivatecoding.autotoolrecode.module.impl.player.Scaffold;
import net.minecraft.util.MathHelper;

@ModuleInfo(name = "FreeLook", category = Category.VISUAL, description = "Позволяет осматриватся во круг.")
public class FreeLook extends Module {

    public float rotYaw = 0;
    public float rotPitch = 0;

    @Override
    public void onDisable() {
        mc.gameSettings.thirdPersonView = 0;
        mc.thePlayer.rotationYaw = mc.thePlayer.rotationYawHead;
        mc.thePlayer.rotationPitch = mc.thePlayer.rotationPitchHead;
    }

    @Override
    public void onEnable() {
        mc.gameSettings.thirdPersonView = 1;
        rotYaw = mc.thePlayer.rotationYaw;
        rotPitch = mc.thePlayer.rotationPitch;
    }

    private void setRots() {
        mc.thePlayer.renderYawOffset = calculateCorrectYawOffset(rotYaw);
        mc.thePlayer.rotationYawHead = rotYaw;
        mc.thePlayer.rotationPitchHead = rotPitch;
    }

    @EventTarget
    public void onEvent(Event event) {


        if (event instanceof TickEvent) {
            if ((Client.INST.getModuleManager().getModule(KillAura.class).isToggled() && Client.INST.getCombatManager().getTarget() != null) || Client.INST.getModuleManager().getModule(Scaffold.class).isToggled()) return;
            mc.gameSettings.thirdPersonView = 1;
            setRots();
        }

        if (event instanceof JumpEvent e) {
            if ((Client.INST.getModuleManager().getModule(KillAura.class).isToggled() && Client.INST.getCombatManager().getTarget() != null) || Client.INST.getModuleManager().getModule(Scaffold.class).isToggled()) return;
            setRots();
            e.setYaw(rotYaw);
        }

        if (event instanceof MotionEvent e) {
            if ((Client.INST.getModuleManager().getModule(KillAura.class).isToggled() && Client.INST.getCombatManager().getTarget() != null) || Client.INST.getModuleManager().getModule(Scaffold.class).isToggled()) return;
            setRots();
            e.setPitch(rotPitch);
            e.setYaw(rotYaw);
        }

        if (event instanceof LookEvent e) {
            if ((Client.INST.getModuleManager().getModule(KillAura.class).isToggled() && Client.INST.getCombatManager().getTarget() != null) || Client.INST.getModuleManager().getModule(Scaffold.class).isToggled()) return;
            setRots();
            e.setYaw(rotYaw);
            e.setPitch(rotPitch);
        }
        if (event instanceof ChangeHeadRotationEvent e) {
            if ((Client.INST.getModuleManager().getModule(KillAura.class).isToggled() && Client.INST.getCombatManager().getTarget() != null) || Client.INST.getModuleManager().getModule(Scaffold.class).isToggled()) return;
            setRots();
            e.setYaw(rotYaw);
            e.setPitch(rotPitch);
        }
        if (event instanceof UpdateBodyRotationEvent e) {
            if ((Client.INST.getModuleManager().getModule(KillAura.class).isToggled() && Client.INST.getCombatManager().getTarget() != null) || Client.INST.getModuleManager().getModule(Scaffold.class).isToggled()) return;
            setRots();
            e.setYaw(rotYaw);
        }
        if (event instanceof MoveFlyingEvent e) {
            if ((Client.INST.getModuleManager().getModule(KillAura.class).isToggled() && Client.INST.getCombatManager().getTarget() != null) || Client.INST.getModuleManager().getModule(Scaffold.class).isToggled()) return;
            setRots();
            e.setYaw(rotYaw);
        }
    }

    public static float calculateCorrectYawOffset(float yaw) {
        // Инициализация переменных
        double xDiff = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double zDiff = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        float distSquared = (float) (xDiff * xDiff + zDiff * zDiff);
        float renderYawOffset = mc.thePlayer.prevRenderYawOffset;
        float offset = renderYawOffset;
        float yawOffsetDiff;

        // Вычисление смещения, если расстояние больше порогового значения
        if (distSquared > 0.0025000002f) {
            offset = (float) MathHelper.atan2(zDiff, xDiff) * 180.0f / (float) Math.PI - 90.0f;
        }

        // Установка смещения равным углу поворота, если игрок машет рукой
        if (mc.thePlayer != null && mc.thePlayer.swingProgress > 0.0f) {
            offset = yaw;
        }

        // Ограничение разницы смещений
        yawOffsetDiff = MathHelper.wrapDegree(yaw - (renderYawOffset + MathHelper.wrapDegree(offset - renderYawOffset) * 0.3f));
        yawOffsetDiff = MathHelper.clamp(yawOffsetDiff, -75.0f, 75.0f);

        // Вычисление итогового смещения
        renderYawOffset = yaw - yawOffsetDiff;
        if (yawOffsetDiff * yawOffsetDiff > 2500.0f) {
            renderYawOffset += yawOffsetDiff * 0.2f;
        }

        return renderYawOffset;
    }

}