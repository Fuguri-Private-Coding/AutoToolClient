package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.EventTarget;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.settings.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.settings.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.settings.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.math.RandomUtils;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.timer.StopWatch;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

import java.util.function.BooleanSupplier;

@ModuleInfo(name = "AntiFireball", category = Category.PLAYER, description = "Автоматически отбивает фаербол противника.")
public class AntiFireball extends Module {

    final CheckBox rotate = new CheckBox("Rotate", this);

    final BooleanSupplier rotateVisible = rotate::isToggled;
    final IntegerSetting horizontalHitBoxSize = new IntegerSetting("HorizontalHitBoxSize", this, rotateVisible, 1, 100, 100);
    final IntegerSetting verticalHitBoxSize = new IntegerSetting("VerticalHitBoxSize", this, rotateVisible, 1, 100, 100);

    final IntegerSetting minYawSpeed = new IntegerSetting("MinYawSpeed", this,rotateVisible, 0, 180, 90) {
        @Override
        public int getValue() {
            if (maxYawSpeed.value < value) { value = maxYawSpeed.value; }
            return value;
        }
    };
    final IntegerSetting maxYawSpeed = new IntegerSetting("MaxYawSpeed", this,rotateVisible, 0, 180, 30) {
        @Override
        public int getValue() {
            if (minYawSpeed.value > value) { value = minYawSpeed.value; }
            return value;
        }
    };
    final IntegerSetting minPitchSpeed = new IntegerSetting("MinPitchSpeed", this,rotateVisible, 0, 180, 90) {
        @Override
        public int getValue() {
            if (maxPitchSpeed.value < value) { value = maxPitchSpeed.value; }
            return value;
        }
    };
    final IntegerSetting maxPitchSpeed = new IntegerSetting("MaxPitchSpeed", this,rotateVisible, 0, 180, 30) {
        @Override
        public int getValue() {
            if (minPitchSpeed.value > value) { value = minPitchSpeed.value; }
            return value;
        }
    };

    final FloatSetting smooth = new FloatSetting("Smooth", this,rotateVisible, 1f,5f,2f, 0.1f);

    final CheckBox lockView = new CheckBox("LockView", this,rotateVisible, false);

    final IntegerSetting delay = new IntegerSetting("Delay", this, 0, 500, 0);
    final FloatSetting distance = new FloatSetting("Distance", this, 3f, 12f, 4.5f, 0.1f);
    final CheckBox debug = new CheckBox("Debug", this, false);

    final StopWatch stopWatch = new StopWatch();

    public boolean rotating = false;

    @EventTarget
    public void onEvent(Event event) {
        if (mc.thePlayer.ticksExisted < 40
                || (Client.INST.getModuleManager().getModule(Scaffold.class).isToggled()
                && rotate.isToggled())) return;

        if (rotate.isToggled()) {
            for (Entity target : mc.theWorld.loadedEntityList) {
                if (!(target instanceof EntityFireball entityFireball) || entityFireball.shootingEntity == mc.thePlayer || DistanceUtils.getDistance(target) > distance.getValue() + 3.5f) {
                    rotating = false;
                    continue;
                }

                Rot lr = Rot.getServerRotation().copy();
                if (event instanceof MotionEvent e) {
                    e.setYaw(lr.getYaw());
                    e.setPitch(lr.getPitch());
                    AxisAlignedBB box = getHitBox(entityFireball);

                    Rot needRotation = RotUtils.getBestRotation(box.expand(0.1f, 0.1f, 0.1f));
                    Rot delta = RotUtils.getDelta(lr, needRotation);

                    Rot speed = new Rot(
                            RandomUtils.nextFloat(minYawSpeed.getValue(), maxYawSpeed.getValue()),
                            RandomUtils.nextFloat(minPitchSpeed.getValue(), maxPitchSpeed.getValue())
                    );

                    RotUtils.limitDelta(delta, speed);

                    delta.setYaw(MathHelper.wrapDegree(delta.getYaw() / smooth.getValue()));
                    delta.setPitch(MathHelper.wrapDegree(delta.getPitch() / smooth.getValue()));

                    delta = RotUtils.fixDelta(delta);
                    lr = lr.add(delta);
                    lr.setPitch(Math.clamp(lr.getPitch(), -90, 90));
                    Rot.setServerRotation(lr);

                    if (lockView.isToggled()) {
                        mc.thePlayer.rotationYaw = Rot.getServerRotation().getYaw();
                        mc.thePlayer.rotationPitch = Rot.getServerRotation().getPitch();
                    }

                    rotating = true;
                }

                if (event instanceof LookEvent e) {
                    e.setYaw(lr.getYaw());
                    e.setPitch(lr.getPitch());
                }

                if (event instanceof ChangeHeadRotationEvent e) {
                    e.setYaw(lr.getYaw());
                    e.setPitch(lr.getPitch());
                }

                if (event instanceof UpdateBodyRotationEvent e) {
                    e.setYaw(lr.getYaw());
                }

                if (event instanceof MoveFlyingEvent e) e.setYaw(lr.getYaw());
                if (event instanceof JumpEvent e) e.setYaw(lr.getYaw());

                if (event instanceof MoveEvent e) {
                    MoveUtils.moveFix(e, MoveUtils.getDirection(mc.thePlayer.rotationYaw, e.getForward(), e.getStrafe()));
                }
            }
        }

        if (event instanceof TickEvent) {
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (!(entity instanceof EntityFireball entityFireball) || entityFireball.shootingEntity == mc.thePlayer || DistanceUtils.getDistance(entity) > distance.getValue() || !stopWatch.reachedMS(delay.getValue()))
                    continue;
                if (rotate.isToggled()) {
                    mc.clickMouse();
                } else {
                    mc.playerController.attackEntity(mc.thePlayer, entity);
                }
                stopWatch.reset();
                if (debug.isToggled()) ClientUtils.chatLog("Fireball detected.");
            }
        }
    }

    private AxisAlignedBB getHitBox(Entity target) {
        AxisAlignedBB box = target.getEntityBoundingBox();

        double horizontalPercent = 0.5 + horizontalHitBoxSize.getValue() / 200d;
        double verticalPercent = 0.5 + verticalHitBoxSize.getValue() / 200d;

        double invertHorizontalPercent = 1 - horizontalPercent;
        double invertVerticalPercent = 1 - verticalPercent;

        box = new AxisAlignedBB(
                box.minX + box.getLengthX() * invertHorizontalPercent,
                box.minY + box.getLengthY() * invertVerticalPercent,
                box.minZ + box.getLengthZ() * invertHorizontalPercent,
                box.minX + box.getLengthX() * horizontalPercent,
                box.minY + box.getLengthY() * verticalPercent,
                box.minZ + box.getLengthZ() * horizontalPercent
        );
        return box;
    }
}
