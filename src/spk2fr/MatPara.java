/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author Libra
 */
public class MatPara {

    String format = "";

    final ExecutorService pool;
    final boolean wellTrainOnly;
    final double refracRatio;

    public MatPara() {
        this(false, 0.0015);
    }

    public MatPara(boolean wellTrain, double refracRatio) {
        int cpus = Runtime.getRuntime().availableProcessors();
        if (Runtime.getRuntime().maxMemory() / 1024 / 1024 < 8192) {
            cpus = 1;
        }
        System.out.println("cpus " + cpus);
        pool = Executors.newFixedThreadPool(cpus);
        this.wellTrainOnly = wellTrain;
        this.refracRatio = refracRatio;
    }

    synchronized public Future<ComboReturnType> parGetSampleFR(double[][] evt, double[][] spk,
            String classify, String type, float binStart, float binSize, float binEnd, int[][] sampleSize, int repeats) {

        return pool.submit(new ParSpk2fr(evt, spk, classify, type, binStart, binSize, binEnd, sampleSize, repeats));
    }

    synchronized public Future<ComboReturnType> parGetSampleFR(String trialF,
            String classify, String type, float binStart, float binSize, float binEnd, int[][] sampleSize, int repeats) {

        return pool.submit(new ParSpk2fr(trialF, classify, type, binStart, binSize, binEnd, sampleSize, repeats));
    }

    synchronized public Future<ComboReturnType> parGetAllFR(String trialFile,
            String criteria, String groupBy, float binStart, float binSize, float binEnd, boolean isS1) {//S1: true=odor1, false=odorb
        return pool.submit(new ParSpk2fr(trialFile, criteria, groupBy, binStart, binSize, binEnd, isS1));
    }

    public void setFormat(String format) {
        this.format = format.toLowerCase();
    }

    class ParSpk2fr implements Callable<ComboReturnType> {

        double[][] evt;
        double[][] spk;
        final String criteria;
        final String groupBy;
        final float binStart;
        final float binSize;
        final float binEnd;
        final int[][] sampleSize;
        final int repeats;
        boolean isS1;
        String trialF = "";

        public ParSpk2fr(double[][] evt, double[][] spk,
                String classify, String type, float binStart, float binSize, float binEnd, int[][] sampleSize, int repeats) {
            this.evt = evt;
            this.spk = spk;
            this.criteria = classify;
            this.groupBy = type;
            this.binStart = binStart;
            this.binSize = binSize;
            this.binEnd = binEnd;
            this.sampleSize = sampleSize;
            this.repeats = repeats;

        }

        public ParSpk2fr(String trialF,
                String classify, String type, float binStart, float binSize, float binEnd, int[][] sampleSize, int repeats) {
            this(null, null, classify, type, binStart, binSize, binEnd, sampleSize, repeats);
            this.trialF = trialF;
        }

        public ParSpk2fr(String trialF,
                String criteria, String groupBy, float binStart, float binSize, float binEnd, boolean isS1) {

            this.criteria = criteria;
            this.groupBy = groupBy;
            this.binStart = binStart;
            this.binSize = binSize;
            this.binEnd = binEnd;
            this.sampleSize = null;
            this.repeats = 0;
            this.isS1 = isS1;
            this.trialF = trialF;
        }

        @Override
        public ComboReturnType call() {
            if (this.trialF.length() > 5) {
                if (format.startsWith("dual") || format.startsWith("op")) {
                    spk = MatFile.getFile(trialF, "SPK");
                    evt = MatFile.getFile(trialF, "EVT");
                } else {
                    spk = MatFile.getFile(trialF, "Spk");
                    evt = MatFile.getFile(trialF, "TrialInfo");
                }
            }

            evt = MatFile.wellTrainedTrials(evt, MatPara.this.wellTrainOnly);

            if (spk.length < 100 || evt.length < 10) {
                System.out.println("Error Parsing File " + trialF);
                return new ComboReturnType(new double[0][0][0], new int[0][0],new double[0][0]);
            }

            Spk2fr s2f = new Spk2fr();
//            s2f.setWellTrainOnly(wellTrainOnly);
            s2f.setRefracRatio(refracRatio);
            s2f.setLeastFR(criteria);

            if (format.toLowerCase().endsWith("allfr")) {
                return s2f.getAllFiringRate(s2f.buildData(evt, spk, format.substring(0, format.length() - 5)),
                        groupBy, s2f.setBin(binStart, binSize, binEnd), isS1);
            }
            return s2f.getSampleFringRate(s2f.buildData(evt, spk, format),
                    groupBy, s2f.setBin(binStart, binSize, binEnd), sampleSize, repeats);

        }

    }

}
