package me.hackclient.deeplearn.listener;

import ai.djl.training.Trainer;
import ai.djl.training.listener.TrainingListenerAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;

public class OverlayTrainingListener extends TrainingListenerAdapter {
    private int epoch;
    private final int maxEpoch;

    public OverlayTrainingListener(int maxEpoch) {
        this.maxEpoch = maxEpoch;
    }

    @Override
    public void onEpoch(Trainer trainer) {
        epoch++;
        super.onEpoch(trainer);
    }

    @Override
    public void onTrainingBatch(Trainer trainer, BatchData batchData) {
        reportBatchData(batchData);
        super.onTrainingBatch(trainer, batchData);
    }

    @Override
    public void onValidationBatch(Trainer trainer, BatchData batchData) {
        reportBatchData(batchData);
        super.onValidationBatch(trainer, batchData);
    }

    private void reportBatchData(BatchData batchData) {
        var batch = batchData.getBatch();
        var progressCurrent = batch.getProgress();
        var progressTotal = batch.getProgressTotal();
        var progress = (int) ((float) progressCurrent / (float) progressTotal * 100);

        Minecraft.getMinecraft().ingameGUI.setRecordPlaying(
                epoch + "/" + maxEpoch
                + " - "
                + EnumChatFormatting.GREEN + "█".repeat(progress / 4)
                + EnumChatFormatting.DARK_GRAY + "░".repeat(25 - progress / 4),
                false
        );
    }
}
