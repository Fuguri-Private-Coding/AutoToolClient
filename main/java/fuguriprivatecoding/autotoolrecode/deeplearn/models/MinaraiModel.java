package fuguriprivatecoding.autotoolrecode.deeplearn.models;

import fuguriprivatecoding.autotoolrecode.deeplearn.translators.FloatArrayInAndOutTranslator;

public class MinaraiModel extends ModelWrapper<float[], float[]> {
    public MinaraiModel(String name) {
        super(name, new FloatArrayInAndOutTranslator(), 2);
    }
}
