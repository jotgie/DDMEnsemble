package moa.classifiers.drift;

import samoa.instances.SingleLabelInstance;

public class SelectiveInstance extends SingleLabelInstance {

    public int[] getAttributesToClassify() {
        return attributesToClassify;
    }

    private int[] attributesToClassify;

    SelectiveInstance(SingleLabelInstance instance, int[] attributesToClassify) {
        super(instance);
        this.attributesToClassify = attributesToClassify;
    }
}
