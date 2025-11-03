package fuguriprivatecoding.autotoolrecode.module.impl.player;

import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.event.events.*;
import fuguriprivatecoding.autotoolrecode.module.Category;
import fuguriprivatecoding.autotoolrecode.module.Module;
import fuguriprivatecoding.autotoolrecode.module.ModuleInfo;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.setting.impl.CheckBox;
import fuguriprivatecoding.autotoolrecode.setting.impl.DoubleSlider;
import fuguriprivatecoding.autotoolrecode.setting.impl.FloatSetting;
import fuguriprivatecoding.autotoolrecode.setting.impl.IntegerSetting;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;
import fuguriprivatecoding.autotoolrecode.utils.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.move.MoveUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.time.StopWatch;
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

    DoubleSlider yawSpeed = new DoubleSlider("YawSpeed", this, rotateVisible, 0, 180, 90, 1);
    DoubleSlider pitchSpeed = new DoubleSlider("PitchSpeed", this, rotateVisible, 0, 180, 90, 1);

    final FloatSetting smooth = new FloatSetting("Smooth", this, rotateVisible, 1f, 5f, 2f, 0.1f);

    final CheckBox lockView = new CheckBox("LockView", this, rotateVisible, false);

    final IntegerSetting delay = new IntegerSetting("Delay", this, 0, 500, 0);
    final FloatSetting distance = new FloatSetting("Distance", this, 3f, 12f, 4.5f, 0.1f);
    final CheckBox debug = new CheckBox("Debug", this, false);

    final StopWatch stopWatch = new StopWatch();

    public EntityFireball target;

    @Override
    public void onEvent(Event event) {
        if (mc.thePlayer.ticksExisted < 40 || (Modules.getModule(Scaffold.class).isToggled() && rotate.isToggled()))
            return;

        if (event instanceof TickEvent) {
            if (target != null && (target.hurtResistantTime > 0 || target.isDead || !mc.theWorld.getLoadedEntityList().contains(target) || DistanceUtils.getDistance(target) > distance.getValue() + 3.5f)) target = null;

            for (Entity target : mc.theWorld.loadedEntityList) {
                if (!(target instanceof EntityFireball entityFireball) || entityFireball.shootingEntity == mc.thePlayer || DistanceUtils.getDistance(target) > distance.getValue() + 3.5f) {
                    continue;
                }

                this.target = entityFireball;
                if (debug.isToggled()) ClientUtils.chatLog("Fireball detected.");
            }
        }

        if (rotate.isToggled()) {
            if (target == null) return;

            Rot lr = Rot.getServerRotation().copy();
            if (event instanceof MotionEvent e) {
                e.setYaw(lr.getYaw());
                e.setPitch(lr.getPitch());
                AxisAlignedBB box = getHitBox(target);

                Rot needRotation = RotUtils.getBestRotation(box.expand(0.1f, 0.1f, 0.1f));
                Rot delta = RotUtils.getDelta(lr, needRotation);

                Rot speed = new Rot(
                        yawSpeed.getRandomizedIntValue(),
                        pitchSpeed.getRandomizedIntValue()
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

            if (event instanceof TickEvent) {
                if (target == null) return;

                if (stopWatch.reachedMS(delay.getValue())) {
                    if (rotate.isToggled()) {
                        mc.clickMouse();
                    } else {
                        mc.playerController.attackEntity(mc.thePlayer, target);
                    }
                    stopWatch.reset();
                }
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
