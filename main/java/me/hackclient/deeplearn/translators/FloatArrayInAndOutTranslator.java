package me.hackclient.deeplearn.translators;

import ai.djl.ndarray.NDList;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

public class FloatArrayInAndOutTranslator implements Translator<float[], float[]> {
    @Override
    public NDList processInput(TranslatorContext translatorContext, float[] floats) {
        return new NDList(translatorContext.getNDManager().create(floats));
    }

    @Override
    public float[] processOutput(TranslatorContext translatorContext, NDList ndList) {
        return ndList.getFirst().toFloatArray();
    }
}
