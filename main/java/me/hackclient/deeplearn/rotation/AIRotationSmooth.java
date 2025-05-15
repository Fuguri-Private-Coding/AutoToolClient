package me.hackclient.deeplearn.rotation;

import ai.djl.translate.TranslateException;
import lombok.experimental.UtilityClass;
import me.hackclient.Client;
import me.hackclient.deeplearn.data.TrainingData;
import me.hackclient.deeplearn.models.MinaraiModel;
import me.hackclient.utils.interfaces.Imports;
import me.hackclient.utils.rotation.Rot;
import me.hackclient.utils.rotation.RotUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.util.vector.Vector2f;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@UtilityClass
public class AIRotationSmooth implements Imports {

    public String currentModelName = "";

    MinaraiModel model;

    Vec3 prevVec = Vec3.ZERO;
    Rot prevRotation = Rot.ZERO;

    public Rot compute(Rot startsFrom, Rot targetRot, EntityLivingBase target, float yawMultiplier, float pitchMultiplier, boolean correct, float yawCorrectSpeed, float pitchCorrectSpeed) {
        Vec3 currentVec = mc.thePlayer.getLookVec();
        Vec3 targetVec = mc.thePlayer.getVectorForRotation(targetRot.getPitch(), targetRot.getYaw());

        Rot correctRot = RotUtils.getDelta(startsFrom, targetRot);

        TrainingData input = new TrainingData(
                currentVec,
                prevVec,
                targetVec,
                new Vector2f(startsFrom.getYaw() - prevRotation.getYaw(), startsFrom.getPitch() - prevRotation.getPitch()),

                mc.thePlayer.getPositionVector().subtract(mc.thePlayer.getPrevPositionVector()),
                target.getPositionVector().subtract(target.getPrevPositionVector()),

                mc.thePlayer.getDistanceToEntity(target),
                target.hurtTime,
                0
        );

        prevVec = currentVec;
        prevRotation = startsFrom;

        try {
            float[] output = model.predict(input.getAsInput());

            float yawOutputDelta = output[0];
            float pitchOutputDelta = output[1];

            yawOutputDelta *= yawMultiplier;
            pitchOutputDelta *= pitchMultiplier;

            correctRot.setYaw(MathHelper.clamp(correctRot.getYaw(), -yawCorrectSpeed, yawCorrectSpeed));
            correctRot.setPitch(MathHelper.clamp(correctRot.getPitch() , -pitchCorrectSpeed, pitchCorrectSpeed));

            if (!correct) correctRot = Rot.ZERO;

            return startsFrom.add(new Rot(yawOutputDelta, pitchOutputDelta)).add(correctRot);
        } catch (TranslateException e) {
            e.printStackTrace(System.out);
        }

        return startsFrom;
    }

    public void changeModel(String modelName) {
        currentModelName = modelName;
        model = new MinaraiModel(modelName + ".params");
        try {
            InputStream inputStream = new FileInputStream(new File(Client.INST.getModelsDirectory(), modelName + ".params"));
            model.load(inputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
