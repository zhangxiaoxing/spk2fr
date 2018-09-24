/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr;

import spk2fr.FP.FileParser;
import spk2fr.FP.FileParserDNMS;
import spk2fr.FP.FileParserWJDNMS;
import java.io.File;
import java.util.ArrayList;
import spk2fr.SU.SingleUnit;

/**
 *
 * @author Libra
 */
public class Spk2fr {

//    protected boolean wellTrainOnly;//used to be 0=not well-trained; 1=well-trained; 2=include all;now only boolean
    protected ClassifyType leastFR = ClassifyType.BY_AVERAGE2Hz;
    protected double refracRatio = 0.0015;
    private ArrayList<int[]> keyIdx;
    private ArrayList<double[]> suCriteria;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println(Runtime.getRuntime().maxMemory() / 1024 / 1024);
    }

    public double[][][][] getTS(double[][] evts, double[][] spk, String type, boolean byLick, boolean isCorrect) {
        keyIdx = new ArrayList<>();
        suCriteria=new ArrayList<>();
        MiceDay miceDay = parseEvts(buildData(evts, spk, type));
        if (miceDay.getTetrodes().size() < 1 /* || (wellTrainOnly !=  && (wellTrainOnly == 1) != miceDay.isWellTrained()) */) {
            return null;
        }
//        System.out.println("DBG tetrode counts " + miceDay.getTetrodeKeys().size());
        ArrayList<double[][][]> TS = new ArrayList<>();
        for (Integer tetKey : miceDay.getTetrodeKeys()) {
//            System.out.println("DBG tetKEY " + tetKey);

            Tetrode tetrode = miceDay.getTetrode(tetKey);
//            System.out.println("DBG unitCnt "+tetrode.getUnitKeys().size());
            for (Integer unitKey : tetrode.getUnitKeys()) {
//                System.out.println("DBG unitKEY " + unitKey);
                SingleUnit unit = tetrode.getSingleUnit(unitKey);
                this.keyIdx.add(new int[]{tetKey, unitKey});
                this.suCriteria.add(unit.getCriteria());
                int a_trailNum, b_trailNum;
                if (byLick) {
                    a_trailNum = miceDay.countTrialByLick(true);
                    b_trailNum = miceDay.countTrialByLick(false);
                } else {
                    int idx = isCorrect ? 0 : 1;
                    a_trailNum = miceDay.countCorrectErrorTrialByOdor(0, EventType.OdorA)[idx];
                    b_trailNum = miceDay.countCorrectErrorTrialByOdor(0, EventType.OdorB)[idx];
                }
//                System.out.println("DBG trial num " + a_trailNum + ", " + b_trailNum);
                TS.add(unit.getTrialTS(a_trailNum, b_trailNum, byLick, isCorrect));
            }
        }
        return TS.toArray(new double[TS.size()][][][]);
    }

    public int[][] getKeyIdx() {
        return keyIdx.toArray(new int[keyIdx.size()][]);
    }
    public double[][] getCriteria() {
        return this.suCriteria.toArray(new double[suCriteria.size()][]);
    }
    
    

    MiceDay parseEvts(Data data) {
        FileParser fp;
        switch (data.getFormat().toLowerCase()) {
            case "wj":
            case "wjdnms":
                fp = new FileParserWJDNMS();
                break;

            case "opsuppress":
            case "opsuppressallfr":
                fp = new spk2fr.FP.FileParserOpGeneSuppression();
                break;
            case "dual":
                fp = new spk2fr.FP.FileParserDual();
                break;
            case "dnms":
                fp = new FileParserDNMS();
                break;
            default:
                System.out.println("Unknown format:[" + data.getFormat() + "] , using default.");
                fp = new FileParserDNMS();
        }
        
//        System.out.println("DBG FR "+leastFR+", ISI "+refracRatio);
        MiceDay miceDay = fp.processFile(data.getEvts(), data.getSpk()).removeSparseFiringUnits(leastFR, refracRatio);
        
//        MiceDay miceDay = fp.processFile(data.getEvts(), data.getSpk());
        return miceDay;
    }

    public float[] setBin(float binStart, float binSize, float binEnd) {
        return (new float[]{binStart, binSize, binEnd});
    }

    public Data buildData(double[][] evts, double[][] spk, String format) {
        return new Data(evts, spk, format);
    }

    public ComboReturnType getSampleFringRate(Data data, String type, float[] bin, int[][] sampleSize, int repeats) {
        MiceDay miceDay = parseEvts(data);
        if (miceDay.getTetrodes().size() < 1) {
//                || (wellTrainOnly != 2 && ((wellTrainOnly == 1) != miceDay.isWellTrained()))) {
            return null;
        }
        ArrayList<double[][]> frs = new ArrayList<>();
        keyIdx = new ArrayList<>();
        suCriteria=new ArrayList<>();
        for (Integer tetKey : miceDay.getTetrodeKeys()) {
            Tetrode tetrode = miceDay.getTetrode(tetKey);
            for (Integer unitKey : tetrode.getUnitKeys()) {
                SingleUnit unit = tetrode.getSingleUnit(unitKey);

                double[][] rtn = unit.getSampleFR(miceDay, type, bin, sampleSize, repeats);
                if (null != rtn && rtn.length > 0) {
                    keyIdx.add(new int[]{tetKey, unitKey});
                    this.suCriteria.add(unit.getCriteria());
                    frs.add(rtn);
                }
            }
        }
        return new ComboReturnType(frs.toArray(new double[frs.size()][][]), getKeyIdx(), data.getEvts());
    }

    public ComboReturnType getAllFiringRate(Data data, String groupBy, float[] bin, boolean isS1) {
        MiceDay miceDay = parseEvts(data);
        if (miceDay.getTetrodes().size() < 1) {
//                || (wellTrainOnly != 2 && ((wellTrainOnly == 1) != miceDay.isWellTrained()))) {
            return null;
        }
        ArrayList<double[][]> frs = new ArrayList<>();
        keyIdx = new ArrayList<>();
        suCriteria=new ArrayList<>();
        for (Integer tetKey : miceDay.getTetrodeKeys()) {
            Tetrode tetrode = miceDay.getTetrode(tetKey);
            for (Integer unitKey : tetrode.getUnitKeys()) {
                SingleUnit unit = tetrode.getSingleUnit(unitKey);
                double[][] rtn = unit.getAllFR(miceDay, groupBy, bin, isS1);
                if (null != rtn && rtn.length > 0) {
                    keyIdx.add(new int[]{tetKey, unitKey});
                    this.suCriteria.add(unit.getCriteria());
                    frs.add(rtn);
                }
            }
        }
        return new ComboReturnType(frs.toArray(new double[frs.size()][][]), getKeyIdx(), data.getEvts());
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

//    public void setWellTrainOnly(boolean wellTrain) {
//        this.wellTrainOnly = wellTrain;
//    }
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
        } else if (type.equalsIgnoreCase("all")) {
            leastFR = ClassifyType.ALL;
        } else {
            System.out.println("Wrong Least FR type, using Average 2Hz");
        }

    }

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
