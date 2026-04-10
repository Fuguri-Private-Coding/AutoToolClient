package fuguriprivatecoding.autotoolrecode.handle;

import de.florianmichael.viamcp.fixes.AttackOrder;
import fuguriprivatecoding.autotoolrecode.event.EventListener;
import fuguriprivatecoding.autotoolrecode.event.Events;
import fuguriprivatecoding.autotoolrecode.event.events.player.ClickEvent;
import fuguriprivatecoding.autotoolrecode.event.events.player.LegitClickTimingEvent;
import fuguriprivatecoding.autotoolrecode.module.Modules;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.TimerRange;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.CustomCamera;
import fuguriprivatecoding.autotoolrecode.module.impl.visual.CustomCrosshair;
import fuguriprivatecoding.autotoolrecode.utils.Utils;
import fuguriprivatecoding.autotoolrecode.utils.player.distance.DistanceUtils;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.target.TargetStorage;
import fuguriprivatecoding.autotoolrecode.event.Event;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.ClickSettings;
import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.rotation.raytrace.RayCastUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import lombok.Getter;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RayTrace;

public class Clicks implements Imports, EventListener {

    public Clicks() {
        Events.register(this);
    }

    @Getter static int clicks;

    private static final ClickSettings clickSettings = Modules.getModule(ClickSettings.class);

    @Override public boolean listen() {
        return Utils.isWorldLoaded();
    }

    @Override
    public void onEvent(Event event) {
        EntityLivingBase target = TargetStorage.getTargetOrSelectedEntity();
        boolean clicking = needClick(target);

        if (event instanceof LegitClickTimingEvent) {
            int iters = clicks;
            clicks = 0;

            EntityPlayer rayCast = (EntityPlayer) RayCastUtils.raycastEntity(3.0, entity -> entity instanceof EntityPlayer);

            if (rayCast != null && (rayCast.isFriend() || rayCast.isTeam() || rayCast.isBot()) || !clicking) {
                return;
            }

            for (int i = 0; i < iters; i++) {
                click(target);
            }
        }

        if (event instanceof ClickEvent e) {
            EntityPlayer rayCast = (EntityPlayer) RayCastUtils.raycastEntity(3.0, entity -> entity instanceof EntityPlayer);

            if (rayCast != null && (rayCast.isFriend() || rayCast.isTeam() || rayCast.isBot()) || !clicking) {
                e.cancel();
            }
        }
    }

    public boolean needClick(EntityLivingBase target) {
        if (target == null || !clickSettings.isToggled()) {
            return true;
        }

        if (clickSettings.forceClickReduce.isToggled()) {
            float forceClickToReduce = MathHelper.wrapDegree(mc.thePlayer.rotationYaw - RotUtils.getRotationFromDiff(mc.thePlayer.getMotionVector()).getYaw());

            if (Math.abs(forceClickToReduce) > clickSettings.minDiffToForce.getValue()) {
                return true;
            }
        }

        int startHurtTime = clickSettings.startHurtTime.getRandomizedIntValue();
        int endHurtTime = clickSettings.endHurtTime.getRandomizedIntValue();

        return target.hurtTime <= startHurtTime || mc.thePlayer.hurtTime >= endHurtTime;
    }

    public static void addClick() {
        if (TimerRange.isTeleporting()) return;
        clicks++;

        if (clickSettings.simulateDoubleClick.isToggled() && clicks > 0) {
            float chance = clickSettings.chanceDoubleClick.getValue() / 100f;

            if (Math.random() <= chance) {
                clicks++;
            }
        }
    }

    public static void click(EntityLivingBase target) {
        if (clickSettings.ignoreWalls.isToggled() && target != null) {
            RayTrace hit = RayCastUtils.rayCast(mc.thePlayer.getRotation(), 8, 0);
            RayTrace hits = RayCastUtils.rayCast(DistanceUtils.getDistance(target) + 0.2f, 0, mc.thePlayer.getRotation());

            if (shouldWallAttack(target, hit, hits)) {
                AttackOrder.sendFixedAttack(mc.thePlayer, target);
                return;
            }

            mc.clickMouse();
        } else {
            mc.clickMouse();
        }
    }

    public static boolean shouldWallAttack(EntityLivingBase target, RayTrace hit, RayTrace hits) {
        double distance = DistanceUtils.getDistance(target);

        return distance <= 3 && hit.typeOfHit == RayTrace.RayType.BLOCK && hits != null && hits.typeOfHit == RayTrace.RayType.ENTITY;
    }
}
