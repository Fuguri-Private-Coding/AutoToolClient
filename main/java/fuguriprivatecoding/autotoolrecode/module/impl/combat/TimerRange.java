package fuguriprivatecoding.autotoolrecode.module.impl.combat;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.impl.connection.BackTrack;
import fuguriprivatecoding.autotoolrecode.settings.impl.*;
import fuguriprivatecoding.autotoolrecode.utils.color.ColorUtils;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.predict.SimulatedPlayer;
import fuguriprivatecoding.autotoolrecode.utils.raytrace.RayCastUtils;
import fuguriprivatecoding.autotoolrecode.utils.render.RenderUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.function.BooleanSupplier;

@ModuleInfo(name = "TimerRange", category = Category.COMBAT, description = "Телепортирует вас к противнику чтобы вы ударили его первее.")
public class TimerRange extends Module {

    IntegerSetting maxTicks = new IntegerSetting("Max Ticks", this, 0, 20, 2);

    IntegerSetting maxTargetHurtTime = new IntegerSetting("Max Target Hurt Time", this, 0, 10, 0);
    FloatSetting partialTicks = new FloatSetting("Partial Ticks", this, 0, 2.5f, 1, 0.1f);

    IntegerSetting additionalTicks = new IntegerSetting("Additional Ticks", this, 0,5,1);

    FloatSetting minDistanceToSkipTick = new FloatSetting("Min Distance To Skip Tick", this, 2.5f, 6, 3, 0.1f);

    CheckBox renderRealPlayerPosition = new CheckBox("Render Real Player Position", this);

    Mode render = new Mode("Render", this, renderRealPlayerPosition::isToggled)
            .addModes("Player", "HitBox", "Box")
            .setMode("Player");

    BooleanSupplier renderBox = () -> (render.getMode().equalsIgnoreCase("Box") || render.getMode().equalsIgnoreCase("HitBox")) && renderRealPlayerPosition.isToggled();

    final ColorSetting color = new ColorSetting("Color", this, renderBox);

    FloatSetting lineWidth = new FloatSetting("Line Width", this, () -> renderRealPlayerPosition.isToggled() && renderBox.getAsBoolean(), 0, 5f, 1, 0.1f);

    Color fadeColor;

    boolean teleporting, click = false;
    int teleportTicks, balance, posRotIncrement = 0;

    Vec3 targetPos, pos;

    @EventTarget
    public void onEvent(Event event) {
        if (event instanceof RunGameLoopEvent && balance > 0) mc.timer.renderPartialTicks = partialTicks.getValue();
        if (event instanceof LegitClickTimingEvent && click) {
            mc.clickMouse();
            click = false;
        }

        if (teleporting) return;

        if (event instanceof TickEvent e) {
            EntityLivingBase target = Client.INST.getCombatManager().getTarget();

            if (balance > 0) {
                e.cancel();
                balance--;
                return;
            }

            if (target == null) return;

            SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput, Rot.getServerRotation().getYaw());

            pos = new Vec3(target.posX, target.posY, target.posZ);
            targetPos = new Vec3(target.newPosX, target.newPosY, target.newPosZ);
            posRotIncrement = target.newPosRotationIncrements;

            teleportTicks = 0;
            for (int i = 0; i < maxTicks.getValue(); i++) {
                updateCashedIncrementPos();

                Vec3 position = new Vec3(pos.xCoord, pos.yCoord, pos.zCoord);
                Vec3 newPos = new Vec3(target.nx, target.ny, target.nz);

                AxisAlignedBB targetBox = getFixedCashedBB(target, newPos, position);

                boolean skipTickDistance = DistanceUtils.getDistance(simulatedPlayer, targetBox) > minDistanceToSkipTick.getValue();

                if (skipTickDistance) {
                    simulatedPlayer.tick();
                } else {
                    teleportTicks = i;
                    break;
                }
            }

            if (target.hurtTime > maxTargetHurtTime.getValue()) return;

            teleporting = true;
            for (int i = 0; i < teleportTicks; i++) {
                try {
                    mc.runTick();
                    balance++;
                    if (i == teleportTicks - 1) balance += additionalTicks.getValue();
                    if (RayCastUtils.rayCast(3.0, 0, Rot.getServerRotation()).typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                        click = true;
                        break;
                    }
                } catch (Exception ignored) {}
            }
            teleporting = false;
        }

        if (event instanceof Render3DEvent && renderRealPlayerPosition.isToggled()) {
            EntityLivingBase target = Client.INST.getCombatManager().getTarget();

            if (target == null) return;

            Vec3 realPositon = new Vec3(
                    target.lrx + (target.rx - target.lrx) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosX,
                    target.lry + (target.ry - target.lry) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosY,
                    target.lrz + (target.rz - target.lrz) * mc.timer.renderPartialTicks - mc.getRenderManager().viewerPosZ
            );

            AxisAlignedBB bb = target.getEntityBoundingBox().offset(realPositon.xCoord - target.posX, realPositon.yCoord - target.posY, realPositon.zCoord - target.posZ);

            if (!render.getMode().equalsIgnoreCase("Player")) updateColors();

            switch (render.getMode()) {
                case "Player" -> renderPlayer(realPositon, target, target.rotationYawHead, mc.timer.renderPartialTicks);
                case "Box" -> renderBox(bb, fadeColor);
                case "HitBox" -> renderHitBox(bb, fadeColor, lineWidth.getValue());
            }
        }
    }

    private void updateColors() {
        fadeColor = color.isFade() ?
                ColorUtils.fadeColor(color.getColor(), color.getFadeColor(), color.getSpeed())
                : color.getColor();
    }

    private void renderHitBox(AxisAlignedBB bb, Color color, float lineWidth) {
        RenderUtils.start3D();
        RenderUtils.drawHitBox(bb,color, lineWidth);
        RenderUtils.stop3D();
    }

    private void renderBox(AxisAlignedBB bb, Color color) {
        RenderUtils.start3D();
        RenderUtils.drawBoundingBox(bb,color);
        RenderUtils.stop3D();
    }

    private void renderPlayer(Vec3 pos, Entity target, float rotationYawHead, float partialTicks) {
        mc.getRenderManager().doRenderEntity(target, pos.xCoord, pos.yCoord, pos.zCoord, rotationYawHead, partialTicks, true);
        mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
    }

    private void updateCashedIncrementPos() {
        if (posRotIncrement > 0) {
            Vec3 cashingPos = new Vec3(
                    (targetPos.xCoord - pos.xCoord) / posRotIncrement,
                    (targetPos.yCoord - pos.yCoord) / posRotIncrement,
                    (targetPos.zCoord - pos.zCoord) / posRotIncrement
            );
            pos.add(cashingPos);
            posRotIncrement--;
        }
    }

    private AxisAlignedBB getFixedCashedBB(EntityLivingBase target, Vec3 newPos, Vec3 pos) {
        BackTrack backTrack = Client.INST.getModuleManager().getModule(BackTrack.class);
        if (backTrack.isToggled() && !backTrack.packetBuffer.isEmpty()) {
            return target.getEntityBoundingBox().offset(
                    pos.xCoord - target.posX, pos.yCoord - target.posY, pos.zCoord - target.posZ
            ).expand(
                    target.getCollisionBorderSize(),
                    target.getCollisionBorderSize(),
                    target.getCollisionBorderSize()
            );
        }
        return target.getEntityBoundingBox().offset(
                newPos.xCoord - target.posX, newPos.yCoord - target.posY, newPos.zCoord - target.posZ
        ).expand(
                target.getCollisionBorderSize(),
                target.getCollisionBorderSize(),
                target.getCollisionBorderSize()
        );
    }
}