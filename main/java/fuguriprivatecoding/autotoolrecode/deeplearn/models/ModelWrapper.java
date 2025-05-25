package fuguriprivatecoding.autotoolrecode.deeplearn.models;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Block;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.nn.norm.BatchNorm;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.ArrayDataset;
import ai.djl.training.initializer.XavierInitializer;
import ai.djl.training.listener.LoggingTrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Adam;
import ai.djl.training.tracker.Tracker;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import lombok.Getter;
import fuguriprivatecoding.autotoolrecode.deeplearn.listener.OverlayTrainingListener;
import org.apache.commons.lang3.Validate;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class ModelWrapper<I, O> implements Closeable {

    @Getter private String name;
    private Model model;
    private Predictor<I, O> predictor;

    public ModelWrapper(String name, Translator<I, O> translator, long outputs) {
        this.name = name;
        model = Model.newInstance(name);
        model.setBlock(createMlpBlock(outputs));
        predictor = model.newPredictor(translator);
    }

    public O predict(I input) throws TranslateException {
        return predictor.predict(input);
    }

    public void train(float[][] features, float[][] labels) {
        Validate.isTrue(features.length == labels.length, "Features and labels must have the same size");
        Validate.isTrue(features.length != 0, "Features and labels must not be empty");

        long inputs = features[0].length;

        DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.l2Loss())
                .optInitializer(new XavierInitializer(), "weight")
                .optOptimizer(
                        Adam.builder()
                                .optLearningRateTracker(Tracker.fixed(0.001f))
                                .build()
                )
                .addTrainingListeners(new LoggingTrainingListener(), new OverlayTrainingListener(100));
        Trainer trainer = model.newTrainer(config);

        NDManager manager = NDManager.newBaseManager();
        var trainingSet = new ArrayDataset.Builder()
                .setData(manager.create(features))
                .optLabels(manager.create(labels))
                .setSampling(32, true)
                .build();
        trainer.initialize(new Shape(32, inputs));
        try {
            EasyTrain.fit(trainer, 100, trainingSet, null);
        } catch (IOException | TranslateException e) {
            throw new RuntimeException(e);
        }
    }

    public void load(InputStream is) {
        try {
            model.load(is);
        } catch (IOException | MalformedModelException e) {
            throw new RuntimeException(e);
        }
    }

    public void load(Path path) {
        try {
            model.load(path);
        } catch (IOException | MalformedModelException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(Path path, String name) {
        try {
            model.save(path, name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        predictor.close();
        model.close();
    }

    private Block createMlpBlock(long outputs) {
        return new SequentialBlock()
                .add(Linear.builder()
                        .setUnits(128)
                        .build())
                .add(Blocks.batchFlattenBlock())
                .add(BatchNorm.builder().build())
                .add(Activation.reluBlock())

                .add(Linear.builder()
                        .setUnits(64)
                        .build())
                .add(Blocks.batchFlattenBlock())
                .add(BatchNorm.builder().build())
                .add(Activation.reluBlock())

                .add(Linear.builder()
                        .setUnits(32)
                        .build())
                .add(Blocks.batchFlattenBlock())
                .add(BatchNorm.builder().build())
                .add(Activation.reluBlock())

                .add(Linear.builder()
                        .setUnits(outputs)
                        .build());
    }
}
