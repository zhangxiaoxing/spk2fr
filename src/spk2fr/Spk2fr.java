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

    final private FileParser fp;
    private MiceDay miceDay;
    private boolean wellTrainOnly = false;
    private ClassifyType leastFR = ClassifyType.BY_AVERAGE2Hz;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

    public Spk2fr() {
        fp = new FileParser();
    }

    public void spk2fr(double[][] evts, double[][] spk) {
        miceDay = fp.processFile(evts, spk);
        for (Tetrode t : miceDay.getTetrodes()) {
            t.removeSparseFiringUnits(leastFR,
                    miceDay.countCorrectTrialByFirstOdor(EventType.OdorA) + miceDay.countCorrectTrialByFirstOdor(EventType.OdorB));
        }
    }

//    /*
//    For temporary check only
//    */
//    public double[][] getBaselineTS() {
//        ArrayList<double[]> baseTS = new ArrayList<>();
//        for (Tetrode tetrode : miceDay.getTetrodes()) {
//            for (SingleUnit unit : tetrode.getUnits()) {
//                baseTS.add(unit.getBaselineTS(fp.countCorrectTrialByFirstOdor(0) + fp.countCorrectTrialByFirstOdor(1)));
//            }
//        }
//        return baseTS.toArray(new double[baseTS.size()][]);
//    }
    /*
     For temporary check only
     */
    public double[][][][] getTS() {
        if (wellTrainOnly && !miceDay.isWellTrained()) {
            return null;
        }

        ArrayList<double[][][]> TS = new ArrayList<>();
        for (Tetrode tetrode : miceDay.getTetrodes()) {
            for (SingleUnit unit : tetrode.getUnits()) {
                TS.add(unit.getTrialTS(miceDay.countCorrectTrialByFirstOdor(EventType.OdorA), miceDay.countCorrectTrialByFirstOdor(EventType.OdorB)));
            }
        }
        return TS.toArray(new double[TS.size()][][][]);
    }

    public double[][][] getSampleFringRate(String type, float binStart, float binSize, float binEnd, int[][] sampleSize, int repeats) {
        ClassifyType cType;
        int typeATrials;
        int typeBTrials;
        if (type.equalsIgnoreCase("odor")) {
            cType = ClassifyType.BY_ODOR;
            typeATrials = miceDay.countCorrectTrialByFirstOdor(EventType.OdorA);
            typeBTrials = miceDay.countCorrectTrialByFirstOdor(EventType.OdorB);
        } else if (type.equalsIgnoreCase("correctA")) {
            int[] counts = miceDay.countByCorrect(EventType.OdorA);
            typeATrials = counts[0];
            typeBTrials = counts[1];
            cType = ClassifyType.BY_CORRECT_OdorA;
        } else if (type.equalsIgnoreCase("correctB")) {
            int[] counts = miceDay.countByCorrect(EventType.OdorB);
            typeATrials = counts[0];
            typeBTrials = counts[1];
            cType = ClassifyType.BY_CORRECT_OdorB;
        } else {
            System.out.println("Unknown classify type: " + type);
            return null;
        }

        if (wellTrainOnly && !miceDay.isWellTrained()) {
//            System.out.println("Not Well Trained");
            return null;
        }

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
        return miceDay.countCorrectTrialByFirstOdor(odor == 0 ? EventType.OdorA : EventType.OdorB);
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

    public void setWellTrainOnly(boolean wellTrain) {
        this.wellTrainOnly = wellTrain;
    }

    public void setLeastFR(String type) {
        if (type.equalsIgnoreCase("Average2Hz")) {
            leastFR = ClassifyType.BY_AVERAGE2Hz;
        } else if (type.equalsIgnoreCase("Average1Hz")) {
            leastFR = ClassifyType.BY_AVERAGE1Hz;
        } else if (type.equalsIgnoreCase("Peak2Hz")) {
            leastFR = ClassifyType.BY_PEAK2Hz;
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
}
