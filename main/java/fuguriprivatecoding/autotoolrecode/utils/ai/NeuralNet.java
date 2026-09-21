package fuguriprivatecoding.autotoolrecode.utils.ai;

import fuguriprivatecoding.autotoolrecode.utils.interfaces.Imports;
import fuguriprivatecoding.autotoolrecode.utils.rotation.Rot;
import fuguriprivatecoding.autotoolrecode.utils.rotation.RotUtils;
import fuguriprivatecoding.autotoolrecode.utils.value.Constants;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import ru.govnoteam.activation.Activation;
import ru.govnoteam.core.NeuralNetwork;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class NeuralNet implements Imports {

    private final int INPUT_LENGTH = 6;
    private final int OUTPUT_LENGTH = 2;

    private final int SEQUENCE_LENGTH = 5;
    private final int SEQUENCED_INPUT_LENGTH = INPUT_LENGTH * SEQUENCE_LENGTH;

    private float[] input(EntityLivingBase target) {
        if (mc.thePlayer == null) return new float[INPUT_LENGTH];
        Rot delta = RotUtils.getDeltaToPoint(mc.thePlayer.getRotation(), RotUtils.getBestHitVec(target));

        Vec3 targetMotion = target.getPositionVector().subtract(target.getLastPositionVector());
        Vec3 playerMotion = mc.thePlayer.getPositionVector().subtract(mc.thePlayer.getLastPositionVector());

        // TODO Тут кароче типа 1.8.9 тупая и нормально увидеть моушн противника нельзя поэтому тут сделано вот так :3
        Vec3 relativeVelocity = targetMotion.subtract(playerMotion);

        return new float[] {
            (float) relativeVelocity.xCoord,
            (float) relativeVelocity.yCoord,
            (float) relativeVelocity.zCoord,
            delta.getYaw() / 180.0f,
            delta.getPitch() / 180.0f,
            (float) Math.clamp(mc.thePlayer.getDistance(target.posX, target.posY, target.posZ) / 6.0f, 0.0f, 1.0f)
        };
    }

    public NeuralNetwork NN = NeuralNetwork.builder()
            .inputSize(SEQUENCED_INPUT_LENGTH)
            .layer(48, Activation.SWISH)
            .layer(32, Activation.SWISH)
            .layer(24, Activation.SWISH)
            .layer(16, Activation.SWISH)
            .layer(OUTPUT_LENGTH, Activation.TANH)
            .create();

    private final List<float[]> history = new ArrayList<>();
    private EntityLivingBase lastTarget;

    public Rot computeDelta(Rot current, EntityLivingBase target) {
        if (target != lastTarget) {
            lastTarget = target;
            history.clear();
        }

        history.add(input(target));
        while (history.size() > SEQUENCE_LENGTH) {
            history.removeFirst();
        }
        if (history.size() != SEQUENCE_LENGTH) return Constants.ROT_ZERO;

        float[] input = new float[SEQUENCED_INPUT_LENGTH];
        int flatIdx = 0;
        for (float[] floats : history) {
            System.arraycopy(floats, 0, input, flatIdx, INPUT_LENGTH);
            flatIdx += INPUT_LENGTH;
        }

        Rot targetRot = RotUtils.getRotationToPoint(target.getExpandedBoundingBox().getCenter());
        Rot delta = current.deltaTo(targetRot);

        float[] rawPrediction = NN.forward(input);
        float[] prediction = {
                rawPrediction[0] * 180,
                rawPrediction[1] * 180
        };

        return new Rot(prediction);
    }
}
