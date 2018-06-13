package moa.classifiers.drift;

import moa.classifiers.AbstractClassifier;
import moa.classifiers.Classifier;
import moa.classifiers.core.driftdetection.DriftDetectionMethod;
import moa.classifiers.core.driftdetection.DriftEnsembleDetectionMethod;
import moa.classifiers.core.driftdetection.MultipleDDM;
import moa.classifiers.meta.WEKAClassifier;
import moa.core.Measurement;
import moa.core.Utils;
import moa.options.ClassOption;
import samoa.instances.Instance;
import samoa.instances.SingleLabelInstance;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MultipleClassifierDrift extends AbstractClassifier {

    private static final int ATTR_NUMBER = 20;
    private int detNumber;
    private static final int ATTR_IN_ENSEMBLE = 2;

    private static final long serialVersionUID = 1L;
    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l',
            "Classifier to train.", Classifier.class, "bayes.SelectiveNaiveBayes");
    public ClassOption driftDetectionMethodOption = new ClassOption("driftDetectionMethod", 'd',
            "Drift detection method to use.", DriftEnsembleDetectionMethod.class, "MultipleDDM");
    protected Classifier classifier;
    protected Classifier newclassifier;
    protected MultipleDDM driftDetectionMethod;
    protected boolean newClassifierReset;
    protected int ddmLevel;
    protected int changeDetected = 0;
    protected int warningDetected = 0;
    //protected int numberInstances = 0;
    private int[][] attrToClassify;

    {
        detNumber = (ATTR_NUMBER - 1) * ATTR_NUMBER / 2;
        setBrute();
    }

    @Override
    public String getPurposeString() {
        return "Classifier that replaces the current classifier with a new one when a change is detected in accuracy.";
    }

    public boolean isWarningDetected() {
        return (this.ddmLevel == DriftDetectionMethod.DDM_WARNING_LEVEL);
    }

    public boolean isChangeDetected() {
        return (this.ddmLevel == DriftDetectionMethod.DDM_OUTCONTROL_LEVEL);
    }

    @Override
    public void resetLearningImpl() {
        this.classifier = ((Classifier) getPreparedClassOption(this.baseLearnerOption)).copy();
        this.newclassifier = this.classifier.copy();
        this.classifier.resetLearning();
        this.newclassifier.resetLearning();
        driftDetectionMethod = new MultipleDDM();

        this.newClassifierReset = false;
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        //this.numberInstances++;
        int trueClass = (int) inst.classValue();
//        boolean prediction;
//        attrToClassify[0] = 1;
//        SelectiveInstance selectiveInst = new SelectiveInstance((SingleLabelInstance) inst, attrToClassify);
        boolean[] predictions = new boolean[detNumber];
        for (int i = 0; i < detNumber; i++) {
            boolean pred = Utils.maxIndex(this.classifier.getVotesForInstance(new SelectiveInstance((SingleLabelInstance) inst, attrToClassify[i]))) == trueClass;
            predictions[i] = pred;
        }
//        if (Utils.maxIndex(this.classifier.getVotesForInstance(selectiveInst)) == trueClass) {
//            prediction = true;
//        } else {
//            prediction = false;
//        }
        int outcontrolNumber = 0;
        int incontrolNumber = 0;
        int warningNumber = 0;

        ddmLevel = driftDetectionMethod.computeNextVal(predictions);

        switch (this.ddmLevel) {
            case DriftDetectionMethod.DDM_WARNING_LEVEL:
                //System.out.println("1 0 W");
                //System.out.println("DDM_WARNING_LEVEL");
                this.warningDetected++;
                if (newClassifierReset == true) {
                    this.newclassifier.resetLearning();
                    newClassifierReset = false;
                }
                this.newclassifier.trainOnInstance(inst);
                break;

            case DriftDetectionMethod.DDM_OUTCONTROL_LEVEL:
                //System.out.println("0 1 O");
                //System.out.println("DDM_OUTCONTROL_LEVEL");
                this.changeDetected++;
                this.classifier = null;
                this.classifier = this.newclassifier;
                if (this.classifier instanceof WEKAClassifier) {
                    ((WEKAClassifier) this.classifier).buildClassifier();
                }
                this.newclassifier = ((Classifier) getPreparedClassOption(this.baseLearnerOption)).copy();
                this.newclassifier.resetLearning();
                break;

            case DriftDetectionMethod.DDM_INCONTROL_LEVEL:
                //System.out.println("0 0 I");
                //System.out.println("DDM_INCONTROL_LEVEL");
                newClassifierReset = true;
                break;
            default:
                //System.out.println("ERROR!");

        }

        this.classifier.trainOnInstance(inst);
    }

    private void setBrute() {
        attrToClassify = new int[detNumber][ATTR_IN_ENSEMBLE];
        permute(0, 1, 0);
    }

    private void setRandom() {
        attrToClassify = new int[detNumber][ATTR_IN_ENSEMBLE];
        Random random = new Random();
        detNumber /= 2;
        putRandom(random, 0);
    }

    private void putRandom(Random random, int i) {
        if (i >= detNumber) return;
        int a = random.nextInt(detNumber);
        int b = a;
        while (a == b) {
            b = random.nextInt(detNumber);
        }
        attrToClassify[i][0] = a;
        attrToClassify[i][0] = b;
        putRandom(random, i + 1);
    }

    private void permute(int a, int b, int i) {
        if (i >= detNumber) return;
        int[] permutation = new int[ATTR_IN_ENSEMBLE];
        permutation[0] = a;
        permutation[1] = b;
        attrToClassify[i] = permutation;
        int nA = a, nB = b + 1;
        if (++b == ATTR_NUMBER) {
            nA = a + 1;
            nB = nA + 1;
        }
        permute(nA, nB, i + 1);
    }

    public double[] getVotesForInstance(Instance inst) {
        return this.classifier.getVotesForInstance(inst);
    }

    @Override
    public boolean isRandomizable() {
        return true;
    }

    @Override
    public void getModelDescription(StringBuilder out, int indent) {
        ((AbstractClassifier) this.classifier).getModelDescription(out, indent);
    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        List<Measurement> measurementList = new LinkedList<Measurement>();
        measurementList.add(new Measurement("Change detected", this.changeDetected));
        measurementList.add(new Measurement("Warning detected", this.warningDetected));
        Measurement[] modelMeasurements = ((AbstractClassifier) this.classifier).getModelMeasurements();
        if (modelMeasurements != null) {
            for (Measurement measurement : modelMeasurements) {
                measurementList.add(measurement);
            }
        }
        this.changeDetected = 0;
        this.warningDetected = 0;
        return measurementList.toArray(new Measurement[measurementList.size()]);
    }
}
