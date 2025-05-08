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
import me.hackclient.settings.impl.ModeSetting;
import me.hackclient.utils.client.ClientUtils;
import me.hackclient.utils.math.RandomUtils;
import me.hackclient.utils.rotation.Rotation;
import me.hackclient.utils.rotation.RotationUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySlime;
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

    ModeSetting modeTrainer = new ModeSetting("TrainerMode", this)
            .addModes("Osu", "Combat")
            .setMode("Osu");

    private final String name = "modelSamples";
    private final List<TrainingData> packets = new ArrayList<>();

    @Getter private final File folder = new File(Client.INSTANCE.getClientDirectory(), name);
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

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
        if (modeTrainer.getMode().equalsIgnoreCase("Osu")) target = spawn();
    }

    @Override
    public void onDisable() {
        if (modeTrainer.getMode().equalsIgnoreCase("Osu")){
            mc.theWorld.removeEntity(target);
            target = null;
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
                if (event instanceof AttackEvent e && e.getHittingEntity() == target) {
                    e.cancel();
                    ClientUtils.chatLog("Recorded: " + packets.size() + " samples.");
                    mc.theWorld.removeEntity(target);
                    target = spawn();
                }
                if (event instanceof TickEvent && target != null) {
                    var yaw = MathHelper.wrapDegree(mc.thePlayer.rotationYaw);
                    var pitch = MathHelper.wrapDegree(mc.thePlayer.rotationPitch);

                    var next = new Rotation(yaw, pitch);
                    var current = new Rotation(prevYaw, prevPitch);
                    var prev = new Rotation(prevPrevYaw, prevPrevPitch);

                    var distance = (float) mc.thePlayer.getDistanceSqToEntity(target);

                    packets.add(new TrainingData(
                            current.getVec3d(),
                            prev.getVec3d(),
                            RotationUtils.getRotationToPoint(new Vec3(target.posX, target.posY + target.height / 2f, target.posZ)).getVec3d(),
                            RotationUtils.getDelta(current, next).getVec2f(),
                            mc.thePlayer.getPositionVector().subtract(mc.thePlayer.getPrevPositionVector()),
                            target.getPositionVector().subtract(target.getPrevPositionVector()),
                            distance,
                            target.hurtTime,
                            target.getAge()
                    ));

                    prevPrevYaw = prevYaw;
                    prevPrevPitch = prevPitch;

                    prevYaw = yaw;
                    prevPitch = pitch;
                }
            }

            case "Combat" -> {

            }
        }
    }

    private EntityLivingBase spawn() {
        var slime = new EntitySlime(mc.theWorld);
        var distance = 2 + RandomUtils.nextDouble(0, 0.9);
        var direction = new Rotation(
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

    class Fight {
        int ticks = 0;
    }
}
