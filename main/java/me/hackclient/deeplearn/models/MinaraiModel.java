package me.hackclient.deeplearn.models;

import me.hackclient.deeplearn.translators.FloatArrayInAndOutTranslator;

public class MinaraiModel extends ModelWrapper<float[], float[]> {
    public MinaraiModel(String name) {
        super(name, new FloatArrayInAndOutTranslator(), 2);
    }
}
