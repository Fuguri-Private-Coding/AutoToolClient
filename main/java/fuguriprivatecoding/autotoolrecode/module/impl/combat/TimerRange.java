package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.event.events.player.LegitClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.event.events.render.Render3DEvent;
import fuguriprivatecoding.autotoolrecode.event.events.world.TickEvent;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.connect.BackTrack;
import fuguriprivatecoding.autotoolrecode.setting.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.predict.SimulatedPlayer;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.util.function.BooleanSupplier;

@ModuleInfo(name = "TimerRange", category = Category.COMBAT, description = "Телепортирует вас к противнику чтобы вы ударили его первее.")
public class TimerRange extends Module {

    IntegerSetting maxTicks = new IntegerSetting("MaxTicks", this, 0, 20, 4);
    IntegerSetting maxTargetHurtTime = new IntegerSetting("MaxTargetHurtTime", this, 0, 10, 4);
    FloatSetting partialTicks = new FloatSetting("PartialTicks", this, -2.5f, 2.5f, 1, 0.1f);

    IntegerSetting additionalTicks = new IntegerSetting("AdditionalTicks", this, -5,5,1);

    CheckBox renderRealPlayerPosition = new CheckBox("RenderRealPlayerPosition", this);
    Mode render = new Mode("Render", this, renderRealPlayerPosition::isToggled)
            .addModes("Player", "HitBox", "Box")
            .setMode("Player");

    BooleanSupplier renderBox = () -> (render.getMode().equalsIgnoreCase("Box") || render.getMode().equalsIgnoreCase("HitBox")) && renderRealPlayerPosition.isToggled();

    final ColorSetting color = new ColorSetting("Color", this, renderBox);
    FloatSetting lineWidth = new FloatSetting("LineWidth", this, () -> renderRealPlayerPosition.isToggled() && renderBox.getAsBoolean(), 0, 5f, 1, 0.1f);

    public static boolean teleporting = false, click = false;
    int teleportTicks, posRotIncrement = 0;

    public static int balance = 0;
    Vec3 targetPos, pos;

    @Override
    public void onEvent(Event event) {
        EntityLivingBase target = TargetStorage.getTarget();
        if (event instanceof RunGameLoopEvent && balance > 0) {
            mc.timer.renderPartialTicks = partialTicks.getValue();
        }

        if (event instanceof LegitClickTimingEvent && click) {
            mc.clickMouse();
            click = false;
        }

        if (event instanceof TickEvent e && !teleporting) {
            if (balance > 0) {
                e.cancel();
                balance--;
                return;
            }

            AxisAlignedBB box = RotUtils.getHitBox(target, 100, 100);

            float yaw = RotUtils.getBestRotation(box).getYaw();

            SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput, yaw);

            pos = new Vec3(target.posX, target.posY, target.posZ);
            targetPos = new Vec3(target.newPosX, target.newPosY, target.newPosZ);
            posRotIncrement = target.newPosRotationIncrements;

            teleportTicks = 0;
            for (int i = 0; i < maxTicks.getValue(); i++) {
                updateCashedIncrementPos();

                Vec3 position = new Vec3(pos.xCoord, pos.yCoord, pos.zCoord);
                Vec3 newPos = new Vec3(target.nx, target.ny, target.nz);

                AxisAlignedBB targetBox = getFixedCashedBB(target, newPos, position);

                boolean skipTickDistance = DistanceUtils.getDistance(simulatedPlayer, targetBox) > 3.0D;

                if (skipTickDistance) {
                    simulatedPlayer.tick();
                    continue;
                }

                teleportTicks = i;
                break;
            }

            if (target.hurtTime > maxTargetHurtTime.getValue()) return;

            teleporting = true;
            for (int i = 0; i < teleportTicks; i++) {
                try {
                    mc.runTick();
                    balance++;
                    if (i == teleportTicks - 1) {
                        click = true;
                        balance += additionalTicks.getValue();
                    }
                } catch (Exception ignored) {}
            }
            teleporting = false;
        }

        if (event instanceof Render3DEvent && renderRealPlayerPosition.isToggled() && target != null) {
            Vec3 realPositon = new Vec3(
                    target.lrx + (target.rx - target.lrx) * mc.timer.renderPartialTicks - RenderManager.renderPosX,
                    target.lry + (target.ry - target.lry) * mc.timer.renderPartialTicks - RenderManager.renderPosY,
                    target.lrz + (target.rz - target.lrz) * mc.timer.renderPartialTicks - RenderManager.renderPosZ
            );

            AxisAlignedBB bb = target.getEntityBoundingBox().offset(realPositon.xCoord - target.posX, realPositon.yCoord - target.posY, realPositon.zCoord - target.posZ);

            switch (render.getMode()) {
                case "Player" -> RenderUtils.renderPlayer(target, realPositon, target.rotationYawHead, mc.timer.renderPartialTicks);
                case "Box" -> {
                    RenderUtils.start3D();
                    RenderUtils.drawBoundingBox(bb, color.getFadedColor());
                    RenderUtils.stop3D();
                }

                case "HitBox" -> {
                    RenderUtils.start3D();
                    RenderUtils.drawHitBox(bb, color.getFadedColor(), lineWidth.getValue());
                    RenderUtils.stop3D();
                }
            }
        }
    }

    private void updateCashedIncrementPos() {
        if (posRotIncrement > 0) {
            Vec3 cashingPos = new Vec3(
                    (targetPos.xCoord - pos.xCoord) / posRotIncrement,
                    (targetPos.yCoord - pos.yCoord) / posRotIncrement,
                    (targetPos.zCoord - pos.zCoord) / posRotIncrement
            );
            pos = pos.add(cashingPos);
            posRotIncrement--;
        }
    }

    private AxisAlignedBB getFixedCashedBB(EntityLivingBase target, Vec3 newPos, Vec3 pos) {
        BackTrack backTrack = Modules.getModule(BackTrack.class);
        if (backTrack.isToggled() && !backTrack.packetBuffer.isEmpty()) {
            return target.getEntityBoundingBox().offset(
                    pos.xCoord - target.posX, pos.yCoord - target.posY, pos.zCoord - target.posZ
            );
        }
        return target.getEntityBoundingBox().offset(
                newPos.xCoord - target.posX, newPos.yCoord - target.posY, newPos.zCoord - target.posZ
        );
    }
}