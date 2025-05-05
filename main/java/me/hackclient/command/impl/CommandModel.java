package me.hackclient.command.impl;

import me.hackclient.Client;
import me.hackclient.command.Command;
import me.hackclient.deeplearn.data.TrainingData;
import me.hackclient.deeplearn.models.MinaraiModel;
import me.hackclient.module.impl.combat.KillAura;
import me.hackclient.module.impl.combat.ModelTrainer;

import java.io.File;

public class CommandModel extends Command {
    public CommandModel() {
        super("model", "/model create <name>" , "model");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 3) {
            return;
        }

        if (!args[1].equalsIgnoreCase("create")) {
            return;
        }

        var samples = TrainingData.parse(Client.INSTANCE.getModuleManager().getModule(ModelTrainer.class).getFolder());

        if (samples.isEmpty()) {
            console.log("Not enough samples!");
            return;
        }

        float[][] featuresArray = new float[samples.size()][6];
        float[][] labelsArray = new float[samples.size()][2];

        for (int i = 0; i < samples.size(); i++) {
            featuresArray[i] = samples.get(i).getAsInput();
            labelsArray[i] = samples.get(i).getAsOutput();
        }

        var model = new MinaraiModel(args[2]);

        new Thread(() -> {
            long currentMS = System.currentTimeMillis();
            Client.INSTANCE.getConsole().changed = true;
            model.train(featuresArray, labelsArray);
            model.save(Client.INSTANCE.getModelsDirectory().toPath(), model.getName());
            Client.INSTANCE.getModuleManager().getModule(KillAura.class).updateModels();
            Client.INSTANCE.getConsole().changed = false;
            console.log("Created model " + args[2] + " in " + (System.currentTimeMillis() - currentMS) / 1000D + " s.");
        }).start();
    }
}
