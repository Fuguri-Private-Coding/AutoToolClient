package me.hackclient.deeplearn.listener;

import ai.djl.training.Trainer;
import ai.djl.training.listener.TrainingListenerAdapter;
import me.hackclient.Client;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

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
        updateProgress(epoch, maxEpoch, progress);
    }

    private void updateProgress(int epoch, int maxEpoch, int progress) {
        String newMessage = "§7[§2DJL§7] " + getAnimatedSpinner() + " " +
                getFormattedEpoch(epoch, maxEpoch) + " - " +
                buildProgressBar(progress) + " " +
                getColoredPercentage(progress);

        List<String> history = Client.INSTANCE.getConsole().history;

        if (!history.isEmpty()) {
            String last = history.getLast();
            if (last.contains("/") && last.contains("%") && last.contains("░") && last.contains("█")) {
                history.set(history.size() - 1, newMessage);
                return;
            }
        }

        history.add(newMessage);
    }

    private String getAnimatedSpinner() {
        String[] spinnerFrames = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
        int frame = (int)(System.currentTimeMillis() / 100) % spinnerFrames.length;
        return EnumChatFormatting.AQUA + spinnerFrames[frame];
    }

    private String getFormattedEpoch(int epoch, int maxEpoch) {
        return EnumChatFormatting.GOLD + String.valueOf(epoch) +
                EnumChatFormatting.DARK_GRAY + "/" +
                EnumChatFormatting.YELLOW + maxEpoch;
    }

    private static String buildProgressBar(int progress) {
        int filled = progress * 25 / 100;
        int remaining = 25 - filled;

        EnumChatFormatting progressColor = progress < 30 ? EnumChatFormatting.RED :
                progress < 70 ? EnumChatFormatting.YELLOW :
                        EnumChatFormatting.GREEN;

        String filledChar = "░";
        String remainingChar = "░";

        return progressColor + filledChar.repeat(filled) +
                "█" +
                EnumChatFormatting.DARK_GRAY + remainingChar.repeat(remaining);
    }

    private String getColoredPercentage(int progress) {
        EnumChatFormatting color = progress < 30 ? EnumChatFormatting.RED :
                progress < 70 ? EnumChatFormatting.YELLOW :
                        EnumChatFormatting.GREEN;
        return color.toString() + progress + "%";
    }
}
