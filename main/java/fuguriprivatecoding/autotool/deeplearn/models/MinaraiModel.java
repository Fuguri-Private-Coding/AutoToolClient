package fuguriprivatecoding.autotool.deeplearn.models;

import fuguriprivatecoding.autotool.deeplearn.translators.FloatArrayInAndOutTranslator;

public class MinaraiModel extends ModelWrapper<float[], float[]> {
    public MinaraiModel(String name) {
        super(name, new FloatArrayInAndOutTranslator(), 2);
    }
}
