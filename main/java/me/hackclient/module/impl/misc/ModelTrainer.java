package me.hackclient.module.impl.misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import me.hackclient.Client;
import me.hackclient.deeplearn.data.TrainingData;
import me.hackclient.event.Event;
import me.hackclient.event.EventTarget;
import me.hackclient.event.events.AttackEvent;
import me.hackclient.event.events.TickEvent;
import me.hackclient.module.Category;
import me.hackclient.module.Module;
import me.hackclient.module.ModuleInfo;
import me.hackclient.settings.impl.FloatSetting;
import me.hackclient.settings.impl.Mode;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.distance.DistanceUtils;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.rotation.Rot;
import me.hackclient.utils.rotation.RotUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ModuleInfo(name = "ModelTrainer", category = Category.MISC)
public class ModelTrainer extends Module {

    Mode modeTrainer = new Mode("TrainerMode", this)
            .addModes("Osu", "Combat")
            .setMode("Osu");

    FloatSetting distanceToTarget = new FloatSetting("DistanceToTarget", this,() -> modeTrainer.getMode().equalsIgnoreCase("Combat"), 6, 12, 10, 1);

    private final String name = "modelSamples";
    private final List<TrainingData> packets = new ArrayList<>();

    @Getter
    private final File folder = new File(Client.INST.getClientDirectory(), name);
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    private int fightTicks;
    private EntityLivingBase slime;
    private EntityLivingBase target;
    private float prevYaw, prevPitch;
    private float prevPrevYaw, prevPrevPitch;

    public ModelTrainer() {
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    @Override
    public void onEnable() {
        packets.clear();
        if (modeTrainer.getMode().equalsIgnoreCase("Osu")) slime = spawn();
    }

    @Override
    public void onDisable() {
        if (modeTrainer.getMode().equalsIgnoreCase("Osu")) {
            mc.theWorld.removeEntity(slime);
            slime = null;
        }

        if (packets.isEmpty()) {
            ClientUtils.chatLog("No packets recorder");
            return;
        }

        var baseName = dateFormat.format(new Date());
        var file = new File(folder, baseName + ".json");

        var idx = 0;
        while (file.exists()) {
            file = new File(folder, baseName + idx + ".json");
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            var writer = new BufferedWriter(new FileWriter(file));
            writer.write(gson.toJson(packets));
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @EventTarget
    public void onEvent(Event event) {
        switch (modeTrainer.getMode()) {
            case "Osu" -> {
                if (event instanceof AttackEvent e && e.getHittingEntity() == slime) {
                    e.cancel();
                    ClientUtils.chatLog("Recorded: " + packets.size() + " samples.");
                    mc.theWorld.removeEntity(slime);
                    slime = spawn();
                }
                if (event instanceof TickEvent && slime != null) {
                    var yaw = MathHelper.wrapDegree(mc.thePlayer.rotationYaw);
                    var pitch = MathHelper.wrapDegree(mc.thePlayer.rotationPitch);

                    var next = new Rot(yaw, pitch);
                    var current = new Rot(prevYaw, prevPitch);
                    var prev = new Rot(prevPrevYaw, prevPrevPitch);

                    var distance = (float) mc.thePlayer.getDistanceSqToEntity(slime);

                    packets.add(new TrainingData(
                            current.getVec3d(),
                            prev.getVec3d(),
                            RotUtils.getRotationToPoint(new Vec3(slime.posX, slime.posY + slime.height / 2f, slime.posZ)).getVec3d(),
                            RotUtils.getDelta(current, next).getVec2f(),
                            mc.thePlayer.getPositionVector().subtract(mc.thePlayer.getPrevPositionVector()),
                            slime.getPositionVector().subtract(slime.getPrevPositionVector()),
                            distance,
                            slime.hurtTime,
                            slime.getAge()
                    ));

                    prevPrevYaw = prevYaw;
                    prevPrevPitch = prevPitch;

                    prevYaw = yaw;
                    prevPitch = pitch;
                }
            }

            case "Combat" -> {
                if (event instanceof TickEvent) {
                    target = null;
                    double bestDistance = Double.MAX_VALUE;

                    for (EntityPlayer playerEntity : mc.theWorld.playerEntities) {
                        double distance = DistanceUtils.getDistance(playerEntity);
                        if (distance > distanceToTarget.getValue()) continue;
                        if (distance < bestDistance) {
                            target = playerEntity;
                            bestDistance = distance;
                        }
                    }
                }

                if (target == null || target.hurtResistantTime == 0) {
                    fightTicks = 0;
                    return;
                }
                if (event instanceof TickEvent) {
                    var yaw = MathHelper.wrapDegree(mc.thePlayer.rotationYaw);
                    var pitch = MathHelper.wrapDegree(mc.thePlayer.rotationPitch);

                    var next = new Rot(yaw, pitch);
                    var current = new Rot(prevYaw, prevPitch);
                    var prev = new Rot(prevPrevYaw, prevPrevPitch);

                    var distance = (float) mc.thePlayer.getDistanceSqToEntity(target);

                    packets.add(new TrainingData(
                            current.getVec3d(),
                            prev.getVec3d(),
                            RotUtils.getRotationToPoint(RotUtils.getBestHitVec(target)).getVec3d(),
                            RotUtils.getDelta(current, next).getVec2f(),
                            mc.thePlayer.getPositionVector().subtract(mc.thePlayer.getPrevPositionVector()),
                            target.getPositionVector().subtract(target.getPrevPositionVector()),
                            distance,
                            target.hurtTime,
                            ++fightTicks
                    ));

                    prevPrevYaw = prevYaw;
                    prevPrevPitch = prevPitch;

                    prevYaw = yaw;
                    prevPitch = pitch;
                }
            }
        }
    }

    private EntityLivingBase spawn() {
        var slime = new EntitySlime(mc.theWorld);
        var distance = 2 + RandomUtils.nextDouble(0, 0.9);
        var direction = new Rot(
                mc.thePlayer.rotationYaw + RandomUtils.nextFloat(-60, 60),
                RandomUtils.nextFloat(-35, 35)
        ).getVec3d().multiple(distance);

        var pos = mc.thePlayer.getPositionEyes(1f).add(direction);

        slime.setPosition(pos);

        slime.setEntityBoundingBox(new AxisAlignedBB(
                slime.getPositionVector(),
                slime.getPositionVector()
        ).expand(0.25, 0.25, 0.25).offset(0, 0.25, 0));
        mc.theWorld.addEntityToWorld(slime.getEntityId(), slime);

        mc.theWorld.playSound(
                pos.xCoord,
                pos.yCoord,
                pos.zCoord,
                "random.orb",
                1f,
                1f,
                false
        );

        return slime;
    }

}
