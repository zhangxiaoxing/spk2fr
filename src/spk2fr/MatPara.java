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
    final int wellTrainOnly;
    final double refracRatio;

    public MatPara() {
        this(2, 0.0015);
    }

    public MatPara(int wellTrain, double refracRatio) {
        int cpus = Runtime.getRuntime().availableProcessors();
        if (Runtime.getRuntime().maxMemory() / 1024 / 1024 < 8192) {
            cpus = 1;
        }
        System.out.println("cpus " + cpus);
        pool = Executors.newFixedThreadPool(cpus);
        this.wellTrainOnly = wellTrain;
        this.refracRatio = refracRatio;
    }

    synchronized public Future<double[][][]> parGetSampleFR(double[][] evt, double[][] spk,
            String classify, String type, float binStart, float binSize, float binEnd, int[][] sampleSize, int repeats) {

        return pool.submit(new ParSpk2fr(evt, spk, classify, type, binStart, binSize, binEnd, sampleSize, repeats));
    }

    synchronized public Future<double[][][]> parGetSampleFR(String  trialF, 
            String classify, String type, float binStart, float binSize, float binEnd, int[][] sampleSize, int repeats) {

        return pool.submit(new ParSpk2fr(trialF, classify, type, binStart, binSize, binEnd, sampleSize, repeats));
    }

    public void setFormat(String format) {
        this.format = format;
    }

    class ParSpk2fr implements Callable<double[][][]> {

        double[][] evt;
        double[][] spk;
        final String classify;
        final String type;
        final float binStart;
        final float binSize;
        final float binEnd;
        final int[][] sampleSize;
        final int repeats;
        String trialF="";
        
        public ParSpk2fr(double[][] evt, double[][] spk,
                String classify, String type, float binStart, float binSize, float binEnd, int[][] sampleSize, int repeats) {
            this.evt = evt;
            this.spk = spk;
            this.classify = classify;
            this.type = type;
            this.binStart = binStart;
            this.binSize = binSize;
            this.binEnd = binEnd;
            this.sampleSize = sampleSize;
            this.repeats = repeats;
            
        }

        public ParSpk2fr(String trialF,
                String classify, String type, float binStart, float binSize, float binEnd, int[][] sampleSize, int repeats) {
            this(new double[0][0], new double[0][0], classify, type, binStart, binSize, binEnd, sampleSize, repeats);
            this.trialF=trialF;
        }

        @Override
        public double[][][] call() {
            if (this.trialF.length()>5){
                spk=MatFile.getFile(trialF, "SPK");
                evt=MatFile.getFile(trialF, "EVT");
            }
//            System.out.println("SPK "+spk.length+"x"+spk[0].length);
//            System.out.println("EVT "+evt.length+"x"+evt[0].length);
            
            Spk2fr s2f = new Spk2fr();
            s2f.setWellTrainOnly(wellTrainOnly);
            s2f.setRefracRatio(refracRatio);
            s2f.setLeastFR(classify);
            
            return s2f.getSampleFringRate(s2f.buildData(evt, spk, format),
                    type, s2f.setBin(binStart, binSize, binEnd), sampleSize, repeats);
        }

    }
}
