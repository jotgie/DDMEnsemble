//package moa.classifiers.drift;
//
//import moa.classifiers.AbstractClassifier;
//import moa.classifiers.Classifier;
//import moa.classifiers.core.driftdetection.DriftDetectionMethod;
//import moa.classifiers.meta.WEKAClassifier;
//import moa.core.Measurement;
//import moa.core.Utils;
//import moa.options.ClassOption;
//import samoa.instances.Instance;
//import samoa.instances.SingleLabelInstance;
//
//import java.util.*;
//
//public class EnsembleClassifierDrift extends AbstractClassifier {
//
//    private static final int ATTR_NUMBER = 24;
//    private static final int DET_NUMBER = 10;
//    private static final int ATTR_IN_ENSEMBLE = 16;
//
//    private static final long serialVersionUID = 1L;
//    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l',
//            "Classifier to train.", Classifier.class, "bayes.SelectiveNaiveBayes");
//    public ClassOption driftDetectionMethodOption = new ClassOption("driftDetectionMethod", 'd',
//            "Drift detection method to use.", DriftDetectionMethod.class, "DDM");
//    protected Classifier classifier;
//    protected Classifier newclassifier;
//    protected List<DriftDetectionMethod> driftDetectionMethod;
//    protected boolean newClassifierReset;
//    protected int ddmLevel;
//    protected int changeDetected = 0;
//    protected int warningDetected = 0;
//    //protected int numberInstances = 0;
//    private int[][] attrToClassify = new int[DET_NUMBER][ATTR_IN_ENSEMBLE];
//
//    {
//        setAttrToClassify();
//    }
//
//    @Override
//    public String getPurposeString() {
//        return "Classifier that replaces the current classifier with a new one when a change is detected in accuracy.";
//    }
//
//    public boolean isWarningDetected() {
//        return (this.ddmLevel == DriftDetectionMethod.DDM_WARNING_LEVEL);
//    }
//
//    public boolean isChangeDetected() {
//        return (this.ddmLevel == DriftDetectionMethod.DDM_OUTCONTROL_LEVEL);
//    }
//
//    @Override
//    public void resetLearningImpl() {
//        this.classifier = ((Classifier) getPreparedClassOption(this.baseLearnerOption)).copy();
//        this.newclassifier = this.classifier.copy();
//        this.classifier.resetLearning();
//        this.newclassifier.resetLearning();
//        driftDetectionMethod = new ArrayList<DriftDetectionMethod>();
//        for (int i = 0; i < DET_NUMBER; i++) {
//            driftDetectionMethod.add(((DriftDetectionMethod) getPreparedClassOption(this.driftDetectionMethodOption)).copy());
//        }
//
//        this.newClassifierReset = false;
//    }
//
//    @Override
//    public void trainOnInstanceImpl(Instance inst) {
//        //this.numberInstances++;
//        int trueClass = (int) inst.classValue();
////        boolean prediction;
////        attrToClassify[0] = 1;
////        SelectiveInstance selectiveInst = new SelectiveInstance((SingleLabelInstance) inst, attrToClassify);
//        boolean[] predictions = new boolean[DET_NUMBER];
//        for (int i = 0; i < DET_NUMBER; i++) {
//            boolean pred = Utils.maxIndex(this.classifier.getVotesForInstance(new SelectiveInstance((SingleLabelInstance) inst, attrToClassify[i]))) == trueClass;
//            predictions[i] = pred;
//        }
////        if (Utils.maxIndex(this.classifier.getVotesForInstance(selectiveInst)) == trueClass) {
////            prediction = true;
////        } else {
////            prediction = false;
////        }
//        int outcontrolNumber = 0;
//        int incontrolNumber = 0;
//        int warningNumber = 0;
//        for (int i = 0; i < DET_NUMBER; i++) {
//            switch (driftDetectionMethod.get(i).computeNextVal(predictions[i])) {
//                case DriftDetectionMethod.DDM_OUTCONTROL_LEVEL: outcontrolNumber+= support[i]; break;
//                case DriftDetectionMethod.DDM_INCONTROL_LEVEL: incontrolNumber+= support[i]; break;
//                case DriftDetectionMethod.DDM_WARNING_LEVEL: warningNumber+= support[i]; break;
//            }
//        }
//
//        if (outcontrolNumber != 0 && outcontrolNumber + warningNumber > incontrolNumber) {
//            ddmLevel = DriftDetectionMethod.DDM_OUTCONTROL_LEVEL;
//            System.out.println("D");
//        } else {
//            if (warningNumber > incontrolNumber) {
//                ddmLevel = DriftDetectionMethod.DDM_WARNING_LEVEL;
//                System.out.println("W");
//            } else {
//                ddmLevel = DriftDetectionMethod.DDM_INCONTROL_LEVEL;
//                System.out.println("N");
//            }
//        }
////        if (outcontrolNumber > 0) {
////            ddmLevel = DriftDetectionMethod.DDM_OUTCONTROL_LEVEL;
////        } else {
////            if (incontrolNumber >  this.driftDetectionMethod.size() / 2) {
////                ddmLevel = DriftDetectionMethod.DDM_INCONTROL_LEVEL;
////            } else {
////                ddmLevel = DriftDetectionMethod.DDM_WARNING_LEVEL;
////            }
////        }
//        switch (this.ddmLevel) {
//            case DriftDetectionMethod.DDM_WARNING_LEVEL:
//                //System.out.println("1 0 W");
//                //System.out.println("DDM_WARNING_LEVEL");
//                this.warningDetected++;
//                if (newClassifierReset == true) {
//                    this.newclassifier.resetLearning();
//                    newClassifierReset = false;
//                }
//                this.newclassifier.trainOnInstance(inst);
//                break;
//
//            case DriftDetectionMethod.DDM_OUTCONTROL_LEVEL:
//                //System.out.println("0 1 O");
//                //System.out.println("DDM_OUTCONTROL_LEVEL");
//                this.changeDetected++;
//                this.classifier = null;
//                this.classifier = this.newclassifier;
//                if (this.classifier instanceof WEKAClassifier) {
//                    ((WEKAClassifier) this.classifier).buildClassifier();
//                }
//                this.newclassifier = ((Classifier) getPreparedClassOption(this.baseLearnerOption)).copy();
//                this.newclassifier.resetLearning();
//                break;
//
//            case DriftDetectionMethod.DDM_INCONTROL_LEVEL:
//                //System.out.println("0 0 I");
//                //System.out.println("DDM_INCONTROL_LEVEL");
//                newClassifierReset = true;
//                break;
//            default:
//                //System.out.println("ERROR!");
//
//        }
//
//        this.classifier.trainOnInstance(inst);
//    }
//
//    private void setAttrToClassify() {
//        Random random = new Random();
//        for (int i = 0; i < DET_NUMBER; i++) {
//            for (int j = 0; j < ATTR_IN_ENSEMBLE; j++) {
//                attrToClassify[i][j] = random.nextInt(ATTR_NUMBER);
//            }
//        }
//    }
//
//    public double[] getVotesForInstance(Instance inst) {
//        return this.classifier.getVotesForInstance(inst);
//    }
//
//    @Override
//    public boolean isRandomizable() {
//        return true;
//    }
//
//    @Override
//    public void getModelDescription(StringBuilder out, int indent) {
//        ((AbstractClassifier) this.classifier).getModelDescription(out, indent);
//    }
//
//    @Override
//    protected Measurement[] getModelMeasurementsImpl() {
//        List<Measurement> measurementList = new LinkedList<Measurement>();
//        measurementList.add(new Measurement("Change detected", this.changeDetected));
//        measurementList.add(new Measurement("Warning detected", this.warningDetected));
//        Measurement[] modelMeasurements = ((AbstractClassifier) this.classifier).getModelMeasurements();
//        if (modelMeasurements != null) {
//            for (Measurement measurement : modelMeasurements) {
//                measurementList.add(measurement);
//            }
//        }
//        this.changeDetected = 0;
//        this.warningDetected = 0;
//        return measurementList.toArray(new Measurement[measurementList.size()]);
//    }
//}
