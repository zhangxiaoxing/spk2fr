/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author Libra
 */
public class Spk2fr {

    final protected FileParserDNMS fp;
    protected MiceDay miceDay;
    protected int wellTrainOnly = 0;//0=not well-trained; 1=well-trained; 2=include all;
    protected ClassifyType leastFR = ClassifyType.BY_AVERAGE2Hz;
    protected double refracRatio = 0.0015;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        System.out.println(Runtime.getRuntime().maxMemory() / 1024 / 1024);
    }

//    public Spk2fr() {
//        fp = new FileParserDNMS();
//    }
    public Spk2fr(String format) {
        switch (format.toLowerCase()) {
            case "wj":
                fp = new FileParserWJ();
                break;
            case "opsuppress":
                fp = new spk2fr.OpSupress.FileParserOpGeneSuppression();
                break;
            default:
                System.out.println("Unknown format:["+format+"] , using default.");
                fp = new FileParserDNMS();
        }

    }

    public void spk2fr(double[][] evts, double[][] spk) {
        if (evts != null && spk != null) {
            miceDay = fp.processFile(evts, spk);
            for (Tetrode t : miceDay.getTetrodes()) {
                t.removeSparseFiringUnits(leastFR,
                        miceDay.countByCorrect(EventType.OdorA)[0] + miceDay.countByCorrect(EventType.OdorA)[1]
                        + miceDay.countByCorrect(EventType.OdorB)[0] + miceDay.countByCorrect(EventType.OdorB)[1], refracRatio);
            }
        }
    }

//    /*
//    For temporary check only
//    */
//    public double[][] getBaselineTS() {
//        ArrayList<double[]> baseTS = new ArrayList<>();
//        for (Tetrode tetrode : miceDay.getTetrodes()) {
//            for (SingleUnit unit : tetrode.getUnits()) {
//                baseTS.add(unit.getBaselineTS(fp.countCorrectTrialByOdor(0) + fp.countCorrectTrialByOdor(1)));
//            }
//        }
//        return baseTS.toArray(new double[baseTS.size()][]);
//    }
    protected int[] getTrialNum(String type) {
        ClassifyType cType;
        int typeATrials;
        int typeBTrials;
        int[] counts;
        switch (type.toLowerCase()) {
            case "odor":
                cType = ClassifyType.BY_ODOR;
                typeATrials = miceDay.countCorrectTrialByOdor(0, EventType.OdorA);
                typeBTrials = miceDay.countCorrectTrialByOdor(0, EventType.OdorB);
                break;
            case "secondodor":
                cType = ClassifyType.BY_SECOND_ODOR;
                typeATrials = miceDay.countCorrectTrialByOdor(1, EventType.OdorA);
                typeBTrials = miceDay.countCorrectTrialByOdor(1, EventType.OdorB);
                break;
            case "odorwithinmeantrial":
                cType = ClassifyType.BY_ODOR_WITHIN_MEAN_TRIAL;
                typeATrials = miceDay.countCorrectTrialByOdor(0, EventType.OdorA);
                typeBTrials = miceDay.countCorrectTrialByOdor(0, EventType.OdorB);
                break;
            case "odorz":
                cType = ClassifyType.BY_ODOR_Z;
                typeATrials = miceDay.countCorrectTrialByOdor(0, EventType.OdorA);
                typeBTrials = miceDay.countCorrectTrialByOdor(0, EventType.OdorB);
                break;
            case "odorwithinmeantrialz":
                cType = ClassifyType.BY_ODOR_WITHIN_MEAN_TRIAL_Z;
                typeATrials = miceDay.countCorrectTrialByOdor(0, EventType.OdorA);
                typeBTrials = miceDay.countCorrectTrialByOdor(0, EventType.OdorB);
                break;
            case "correcta":
                counts = miceDay.countByCorrect(EventType.OdorA);
                typeATrials = counts[0];
                typeBTrials = counts[1];
                System.out.println("A ," + typeATrials + "\t" + typeBTrials);
                cType = ClassifyType.BY_CORRECT_OdorA;
                break;
            case "alla":
                counts = miceDay.countByCorrect(EventType.OdorA);
                typeATrials = counts[0] + counts[1];
                typeBTrials = counts[0] + counts[1];
                cType = ClassifyType.ALL_ODORA;
                break;
            case "correctza":
                counts = miceDay.countByCorrect(EventType.OdorA);
                typeATrials = counts[0];
                typeBTrials = counts[1];
                cType = ClassifyType.BY_CORRECT_OdorA_Z;
                break;
            case "correctb":
                counts = miceDay.countByCorrect(EventType.OdorB);
                typeATrials = counts[0];
                typeBTrials = counts[1];
                System.out.println("B ," + typeATrials + "\t" + typeBTrials);
                cType = ClassifyType.BY_CORRECT_OdorB;
                break;
            case "allb":
                counts = miceDay.countByCorrect(EventType.OdorB);
                typeATrials = counts[0] + counts[1];
                typeBTrials = counts[0] + counts[1];
                cType = ClassifyType.ALL_ODORB;
                break;
            case "correctzb":
                counts = miceDay.countByCorrect(EventType.OdorB);
                typeATrials = counts[0];
                typeBTrials = counts[1];
                cType = ClassifyType.BY_CORRECT_OdorB_Z;
                break;
            case "match":
                cType = ClassifyType.BY_MATCH;
                typeATrials = miceDay.countCorrectTrialByMatch(EventType.MATCH, true);
                typeBTrials = miceDay.countCorrectTrialByMatch(EventType.NONMATCH, true);
                break;
            case "matchincincorr":
                cType = ClassifyType.BY_MATCH;
                typeATrials = miceDay.countCorrectTrialByMatch(EventType.MATCH, false);
                typeBTrials = miceDay.countCorrectTrialByMatch(EventType.NONMATCH, false);
                break;
            case "opsuppress":
                cType = ClassifyType.BY_OP_SUPPRESS;
                typeATrials = miceDay.getBehaviorSessions().get(0).size();
                typeBTrials = miceDay.getBehaviorSessions().get(0).size();
                break;
            default:
                System.out.println("Unknown classify type: " + type);
                return null;
        }
        return new int[]{cType.ordinal(), typeATrials, typeBTrials};
    }

    /*
     For temporary check only
     */
    public double[][][][] getTS(String type) {
        if (miceDay == null
                || (wellTrainOnly != 2 && (wellTrainOnly == 1) != miceDay.isWellTrained())) {
            return null;
        }

//        int[] params = getTrialNum(type);
//        ClassifyType cType = ClassifyType.values()[params[0]];
//        int typeATrials = params[1];
//        int typeBTrials = params[2];
        ArrayList<double[][][]> TS = new ArrayList<>();
        for (Tetrode tetrode : miceDay.getTetrodes()) {
            for (SingleUnit unit : tetrode.getUnits()) {
                TS.add(unit.getTrialTS(miceDay.countCorrectTrialByOdor(0, EventType.OdorA), miceDay.countCorrectTrialByOdor(0, EventType.OdorB)));
            }
        }
        return TS.toArray(new double[TS.size()][][][]);
    }

    public double[][][] getSampleFringRate(String type, float binStart, float binSize, float binEnd, int[][] sampleSize, int repeats) {
        if (miceDay == null
                || (wellTrainOnly != 2 && ((wellTrainOnly == 1) != miceDay.isWellTrained()))) {
            return null;
        }
        int[] params = getTrialNum(type);
        ClassifyType cType = ClassifyType.values()[params[0]];
        int typeATrials = params[1];
        int typeBTrials = params[2];
        ArrayList<double[][]> frs = new ArrayList<>();
        for (Tetrode tetrode : miceDay.getTetrodes()) {
            for (SingleUnit unit : tetrode.getUnits()) {
                double[][] rtn = unit.getSampleFR(cType, typeATrials, typeBTrials, binStart, binSize, binEnd, sampleSize, repeats);
                if (null != rtn) {
                    frs.add(rtn);
                }
            }
        }
        return frs.toArray(new double[frs.size()][][]);
    }

    public int getTrialCountByFirstOdor(int odor) {
        return miceDay.countCorrectTrialByOdor(0, odor == 0 ? EventType.OdorA : EventType.OdorB);
    }

    public ArrayList<String> listFiles(String rootPath, String[] elements) {
        ArrayList<String> fileList = new ArrayList<>();
        if (rootPath == null) {
            return null;
        }
        File root = new File(rootPath);
        if (!root.exists()) {
            return null;
        }
        File[] list = root.listFiles();

        if (list != null) {
            for (File f : list) {
                if (f.isDirectory()) {
                    fileList.addAll(listFiles(f.getAbsolutePath(), elements));
                } else {
                    String fileName = f.getName();
                    boolean add = true;

                    if (elements.length > 0) {
                        for (String element : elements) {

                            if (element.startsWith("-")
                                    ? fileName.contains(element.substring(1))
                                    : !fileName.contains(element)) {
                                add = false;
                            }
                        }
                    }
                    if (add) {
                        fileList.add(f.getPath());
                    }
                }
            }
        }
        return fileList;
    }

    public void setWellTrainOnly(int wellTrain) {
        this.wellTrainOnly = wellTrain;
    }

    public void setLeastFR(String type) {
        if (type.equalsIgnoreCase("Average2Hz")) {
            leastFR = ClassifyType.BY_AVERAGE2Hz;
        } else if (type.equalsIgnoreCase("Average2HzWhole")) {
            leastFR = ClassifyType.BY_AVERAGE2Hz_WHOLETRIAL;
        } else if (type.equalsIgnoreCase("Average1Hz")) {
            leastFR = ClassifyType.BY_AVERAGE1Hz;
        } else if (type.equalsIgnoreCase("Peak2Hz")) {
            leastFR = ClassifyType.BY_PEAK2Hz;
        } else if (type.equalsIgnoreCase("Peak2HzWhole")) {
            leastFR = ClassifyType.BY_PEAK2Hz_WHOLETRIAL;
        } else {
            System.out.println("Wrong Least FR type, using Average 2Hz");
        }

    }

    public int[][] getBehaviorTrials() {

        ArrayList<EventType[]> pool = new ArrayList<>();
        for (ArrayList<EventType[]> sess : miceDay.getBehaviorSessions()) {
            pool.addAll(sess);
        }
        int[][] rtn = new int[pool.size()][];
        for (int i = 0; i < rtn.length; i++) {
            rtn[i] = new int[]{pool.get(i)[0].ordinal(), pool.get(i)[1].ordinal(), pool.get(i)[2].ordinal()};
        }
        return rtn;
    }

    public double[][] rebuild(double[][] evts, double delay) {
        return RebuildEventFile.rebulid(evts, delay);
    }

    public void setRefracRatio(double refracRatio) {
        this.refracRatio = refracRatio;
    }

}
