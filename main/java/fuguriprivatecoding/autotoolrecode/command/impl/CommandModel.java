package fuguriprivatecoding.autotoolrecode.command.impl;

import fuguriprivatecoding.autotoolrecode.Client;
import fuguriprivatecoding.autotoolrecode.command.Command;
import fuguriprivatecoding.autotoolrecode.deeplearn.data.TrainingData;
import fuguriprivatecoding.autotoolrecode.deeplearn.models.MinaraiModel;
import fuguriprivatecoding.autotoolrecode.module.impl.combat.KillAura;
import fuguriprivatecoding.autotoolrecode.module.impl.misc.ModelTrainer;
import fuguriprivatecoding.autotoolrecode.utils.client.ClientUtils;

import java.io.File;

public class CommandModel extends Command {
    public CommandModel() {
        super("Model", "/model create <name>" , "model");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            addMessage("Not enough arguments!");
            super.usage();
            return;
        }

        if (args.length == 3) {
            if (args[1].equalsIgnoreCase("create")) {
                var samples = TrainingData.parse(Client.INST.getModuleManager().getModule(ModelTrainer.class).getFolder());

                if (samples.isEmpty()) {
                    addMessage("Not enough samples!");
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
                    Client.INST.getConsole().changed = true;
                    model.train(featuresArray, labelsArray);
                    model.save(Client.INST.getModelsDirectory().toPath(), model.getName());
                    new File(Client.INST.getModelsDirectory(), model.getName() + "-0000").renameTo(new File(model.getName()));
                    Client.INST.getModuleManager().getModule(KillAura.class).updateModels();
                    Client.INST.getConsole().changed = false;
                    addMessage("Created model " + args[2] + " in " + (System.currentTimeMillis() - currentMS) / 1000D + " s.");
                }).start();
            }
        }
    }
}
