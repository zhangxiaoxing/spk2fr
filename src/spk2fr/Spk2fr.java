/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr;

import spk2fr.FP.FileParser;
import spk2fr.FP.FileParserDNMS;
import spk2fr.FP.FileParserWJ;
import java.io.File;
import java.util.ArrayList;
import spk2fr.SU.SingleUnit;

/**
 *
 * @author Libra
 */
public class Spk2fr {

//    final protected FileParserDNMS fp;
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
//    public Spk2fr(String format) {
//
//    }
//
//    public void spk2fr(double[][] evts, double[][] spk) {
//
//    }
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

    /*
     For temporary check only
     */
//    public double[][][][] getTS(String type) {
//        if (miceDay == null
//                || (wellTrainOnly != 2 && (wellTrainOnly == 1) != miceDay.isWellTrained())) {
//            return null;
//        }
//
////        int[] params = getTrialNum(type);
////        ClassifyType cType = ClassifyType.values()[params[0]];
////        int typeATrials = params[1];
////        int typeBTrials = params[2];
//        ArrayList<double[][][]> TS = new ArrayList<>();
//        for (Tetrode tetrode : miceDay.getTetrodes()) {
//            for (SingleUnit unit : tetrode.getUnits()) {
//                TS.add(unit.getTrialTS(miceDay.countCorrectTrialByOdor(0, EventType.OdorA), miceDay.countCorrectTrialByOdor(0, EventType.OdorB)));
//            }
//        }
//        return TS.toArray(new double[TS.size()][][][]);
//    }
    MiceDay parseEvts(Data data) {
        FileParser fp;
        switch (data.getFormat().toLowerCase()) {
            case "wj":
                fp = new FileParserWJ();
                break;
            case "opsuppress":
                fp = new spk2fr.FP.FileParserOpGeneSuppression();
                break;
            default:
                System.out.println("Unknown format:[" + data.getFormat() + "] , using default.");
                fp = new FileParserDNMS();
        }

        MiceDay miceDay = fp.processFile(data.getEvts(), data.getSpk()).removeSparseFiringUnits(leastFR, refracRatio);

        return miceDay;
    }

    public float[] setBin(float binStart, float binSize, float binEnd) {
        return (new float[]{binStart, binSize, binEnd});
    }

    public Data buildData(double[][] evts, double[][] spk, String format) {
        return new Data(evts, spk, format);
    }

    public double[][][] getSampleFringRate(Data data, String type, float[] bin, int[][] sampleSize, int repeats) {
        MiceDay miceDay = parseEvts(data);
        if ((wellTrainOnly != 2 && ((wellTrainOnly == 1) != miceDay.isWellTrained()))) {
            return null;
        }
        ArrayList<double[][]> frs = new ArrayList<>();
        for (Tetrode tetrode : miceDay.getTetrodes()) {
            for (SingleUnit unit : tetrode.getUnits()) {
                double[][] rtn = unit.getSampleFR(miceDay, type, bin, sampleSize, repeats);
                if (null != rtn) {
                    frs.add(rtn);
                }
            }
        }
        return frs.toArray(new double[frs.size()][][]);
    }

//    public int getTrialCountByFirstOdor(int odor) {
//        return miceDay.countCorrectTrialByOdor(0, odor == 0 ? EventType.OdorA : EventType.OdorB);
//    }
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

//    public int[][] getBehaviorTrials() {
//
//        ArrayList<EventType[]> pool = new ArrayList<>();
//        for (ArrayList<EventType[]> sess : miceDay.getBehaviorSessions()) {
//            pool.addAll(sess);
//        }
//        int[][] rtn = new int[pool.size()][];
//        for (int i = 0; i < rtn.length; i++) {
//            rtn[i] = new int[]{pool.get(i)[0].ordinal(), pool.get(i)[1].ordinal(), pool.get(i)[2].ordinal()};
//        }
//        return rtn;
//    }
    public double[][] rebuild(double[][] evts, double delay) {
        return RebuildEventFile.rebulid(evts, delay);
    }

    public void setRefracRatio(double refracRatio) {
        this.refracRatio = refracRatio;
    }

    public class Data {

        final double[][] evts;
        final double[][] spk;
        final String format;

        public Data(double[][] evts, double[][] spk, String format) {
            this.evts = evts;
            this.spk = spk;
            this.format = format;
        }

        public double[][] getEvts() {
            return evts;
        }

        public double[][] getSpk() {
            return spk;
        }

        public String getFormat() {
            return format;
        }

    }

}
