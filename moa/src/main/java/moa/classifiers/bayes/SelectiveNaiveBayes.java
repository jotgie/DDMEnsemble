package moa.classifiers.bayes;

import moa.classifiers.core.attributeclassobservers.AttributeClassObserver;
import moa.classifiers.drift.SelectiveInstance;
import moa.core.AutoExpandVector;
import moa.core.DoubleVector;
import samoa.instances.Instance;

public class SelectiveNaiveBayes extends NaiveBayes {

    @Override
    public double[] getVotesForInstance(Instance inst) {
        SelectiveInstance instance = (SelectiveInstance) inst;
        int[] attributesToClassify = instance.getAttributesToClassify();
        return doSelectiveNaiveBayesPrediction(inst, this.observedClassDistribution,
                this.attributeObservers, attributesToClassify);
    }

    private static double[] doSelectiveNaiveBayesPrediction(Instance inst,
                                                            DoubleVector observedClassDistribution,
                                                            AutoExpandVector<AttributeClassObserver> attributeObservers,
                                                            int[] attributesToClassify) {
        double[] votes = new double[observedClassDistribution.numValues()];
        double observedClassSum = sumValues(observedClassDistribution, attributesToClassify);
        if (observedClassSum == 0.0) return votes;
        for (int classIndex = 0; classIndex < votes.length; classIndex++) {
            votes[classIndex] = observedClassDistribution.getValue(classIndex)
                    / observedClassSum;
             for (int attIndex : attributesToClassify) {
                int instAttIndex = modelAttIndexToInstanceAttIndex(attIndex,
                        inst);
                AttributeClassObserver obs = attributeObservers.get(attIndex);
                if ((obs != null) && !inst.isMissing(instAttIndex)) {
                    votes[classIndex] *= obs.probabilityOfAttributeValueGivenClass(inst.value(instAttIndex), classIndex);
                }
            }
        }
        // TODO: need logic to prevent underflow?
        return votes;
    }

    private static double sumValues(DoubleVector observedClassDistribution, int[] attributesToClassify) {
        double sum = 0.0;
        for (int index : attributesToClassify) {
            sum += observedClassDistribution.getValue(index);
        }
        return sum;
    }
}
