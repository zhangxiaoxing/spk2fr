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
            t.removeSparseFiringUnits(miceDay.countCorrectTrialByFirstOdor(0) + miceDay.countCorrectTrialByFirstOdor(1));
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
                TS.add(unit.getTrialTS(miceDay.countCorrectTrialByFirstOdor(0), miceDay.countCorrectTrialByFirstOdor(1)));
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
            typeATrials = miceDay.countCorrectTrialByFirstOdor(0);
            typeBTrials = miceDay.countCorrectTrialByFirstOdor(1);
        } else if (type.equalsIgnoreCase("correct")) {
            int[] counts=miceDay.countByCorrect();
            typeATrials=counts[0];
            typeBTrials=counts[1];
            cType = ClassifyType.BY_CORRECT;
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
                frs.add(unit.getSampleFR(cType, typeATrials, typeBTrials, binStart, binSize, binEnd, sampleSize, repeats));
            }
        }
        return frs.toArray(new double[frs.size()][][]);
    }

    public int getTrialCountByFirstOdor(int odor) {
        return miceDay.countCorrectTrialByFirstOdor(odor);
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

}
