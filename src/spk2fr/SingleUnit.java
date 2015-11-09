/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.StatUtils;

/**
 *
 * @author Libra
 */
public class SingleUnit {

    final private HashMap<Integer, HashMap<Integer, Trial>> sessions;
    private ArrayList<Trial> trialPool;
//    HashMap<Integer, Trial> trials;

    public SingleUnit() {
        sessions = new HashMap<>();
    }

    public Trial getTrial(int sessionIdx, int trialIdx) {
        if (!sessions.containsKey(sessionIdx)) {
            sessions.put(sessionIdx, new HashMap<Integer, Trial>());
        }
        if (!sessions.get(sessionIdx).containsKey(trialIdx)) {
            sessions.get(sessionIdx).put(trialIdx, new Trial());
        }
        return sessions.get(sessionIdx).get(trialIdx);
    }
//    public double[] getAvgFireRate(int firstOdor, int response){}

    public void removeSession(int sessionIdx) {
        sessions.remove(sessionIdx);
    }

// 4 removal    
//    public Double[][] getFiringTimesByOdor(int odor) {
//        ArrayList<Double[]> firingTS = new ArrayList<>();//Time Stamp
//        for (HashMap<Integer, Trial> session : sessions.values()) {
//            for (Trial trial : session.values()) {
//                if (trial.firstOdorIs(odor)) {
//                    firingTS.add(trial.getSpikes());
//                }
//            }
//        }
//        return firingTS.toArray(new Double[firingTS.size()][]);
//    }
//    public Double[]getFiringTimesByOdor(int odor) {
//        ArrayList<Double> firingRates = new ArrayList<>();
//        for (HashMap<Integer, Trial> session : sessions.values()) {
//            for (Trial trial : session.values()) {
//                if (trial.firstOdorIs(odor)) {
//                    firingRates.addAll(Arrays.asList(trial.getSpikes()));
//                }
//            }
//        }
//        return firingRates.toArray(new Double[firingRates.size()]);
//    }
    public boolean isSparseFiring() {
        for (Trial trial : trialPool) {
            ArrayList<Double> ts = trial.getSpikesList();
            if (ts.size() > 1) {
                for (int i = 1; i < ts.size(); i++) {
                    if (ts.get(i) - ts.get(i - 1) < 1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isStrictSparseFiring(int trialCount) {
        int spkCount = 0;
        int firedTrial = 0;
        double firedTrialLengthSum = 0;
        for (Trial trial : trialPool) {
            spkCount += trial.getSpikesList().size();
            firedTrial++;
            firedTrialLengthSum += trial.getLength();
        }
        double avgLength = firedTrialLengthSum / firedTrial;
//        System.out.println("Length "+avgLength+", spkCount "+spkCount);
        return !(spkCount > trialCount * avgLength * 2d); //2Hz

    }

//    public double[] getFRbyOdor(int odorATrialCount, int odorBTrialCount, float binStart, float binSize, float binEnd) {  //firing rate, baseline assumed to be 1s
//
//        ArrayList<Double> firingTSOdorA = new ArrayList<>();//Time Stamp
//        ArrayList<Double> firingTSOdorB = new ArrayList<>();//Time Stamp
//
//        for (Trial trial : trialPool) {
//            if (trial.firstOdorIs(0) && trial.isCorrect()) {
//                firingTSOdorA.addAll(trial.getSpikesList());
//            } else if (trial.firstOdorIs(1) && trial.isCorrect()) {
//                firingTSOdorB.addAll(trial.getSpikesList());
//            }
//        }
////        Collections.sort(firingTS);
//
//        int[] binedTSOdorA = new int[Math.round((binEnd - binStart) / binSize)];//time stamp
//        int[] binedTSOdorB = new int[Math.round((binEnd - binStart) / binSize)];//time stamp
//
//        for (Double d : firingTSOdorA) {
//            binedTSOdorA[(int) ((d - binStart) / binSize)]++;
//        }
//
//        for (Double d : firingTSOdorB) {
//            binedTSOdorB[(int) ((d - binStart) / binSize)]++;
//        }
//
//        double[] normalized = new double[Math.round((binEnd - binStart) / binSize) * 2];
//        double[] stats = getBaselineStats(this.trialPool, odorATrialCount + odorBTrialCount);
//        double meanBaseFR = stats[0];
//        double stdBaseFR = stats[1];
//        for (int i = 0; i < binedTSOdorA.length; i++) {
//            normalized[i] = (((double) binedTSOdorA[i] / odorATrialCount * (1 / binSize)) - meanBaseFR) / stdBaseFR;
//        }
//        int odorALength = binedTSOdorA.length;
//        for (int i = 0; i < binedTSOdorB.length; i++) {
//            normalized[i + odorALength] = (((double) binedTSOdorB[i] / odorBTrialCount * (1 / binSize)) - meanBaseFR) / stdBaseFR;
//        }
//        return normalized;
//    }
    /*
     SampleSize=[PFSampleSize1,PFSampleSize2;BNSampleSize1,BNSampleSize2];
     */

    public double[][] getSampleFRbyOdor(int odorATrialCount, int odorBTrialCount, float binStart, float binSize, float binEnd, int[][] sampleCount, int repeatCount) {  //firing rate, baseline assumed to be 1s

        double[] stats = getBaselineStats(this.trialPool, odorATrialCount + odorBTrialCount);
        double meanBaseFR = stats[0];
        double stdBaseFR = stats[1];
        ArrayList<Trial> odorAPool = new ArrayList<>();
        ArrayList<Trial> odorBPool = new ArrayList<>();

        for (Trial trial : this.trialPool) {
            if (trial.firstOdorIs(0) && trial.isCorrect()) {
                odorAPool.add(trial);
            } else if (trial.firstOdorIs(1) && trial.isCorrect()) {
                odorBPool.add(trial);
            }
        }

        //////////////////////////////TODO//////////////////////////////////
        RandomDataGenerator rng = new RandomDataGenerator();
        double[][] samples = new double[repeatCount][];
        if (sampleCount[0][0] + sampleCount[0][1] > odorATrialCount) {
            if (sampleCount[0][1] < 2) {
                sampleCount[0][0] = odorATrialCount - sampleCount[0][1];
            } else {
                sampleCount[0][0] = odorATrialCount / 2;
                sampleCount[0][1] = odorATrialCount / 2;
            }
        }
        if (sampleCount[1][0] + sampleCount[1][1] > odorBTrialCount) {
            if (sampleCount[1][1] < 2) {
                sampleCount[1][0] = odorBTrialCount - sampleCount[1][1];
            } else {
                sampleCount[1][0] = odorBTrialCount / 2;
                sampleCount[1][1] = odorBTrialCount / 2;
            }
        }

        for (int repeat = 0; repeat < repeatCount; repeat++) {

            int[] aPerm = rng.nextPermutation(odorATrialCount, sampleCount[0][0] + sampleCount[0][1]);
            int[] bPerm = rng.nextPermutation(odorBTrialCount, sampleCount[1][0] + sampleCount[1][1]);

            ArrayList<Double> firingTSOdorA1 = new ArrayList<>();//Time Stamp
            ArrayList<Double> firingTSOdorA2 = new ArrayList<>();//Time Stamp
            ArrayList<Double> firingTSOdorB1 = new ArrayList<>();//Time Stamp
            ArrayList<Double> firingTSOdorB2 = new ArrayList<>();//Time Stamp

            for (int i = 0; i < sampleCount[0][0]; i++) {
                if (aPerm[i] < odorAPool.size()) {
                    firingTSOdorA1.addAll(odorAPool.get(aPerm[i]).getSpikesList());
                }
            }
            for (int i = sampleCount[0][0]; i < sampleCount[0][0] + sampleCount[0][1]; i++) {
                if (aPerm[i] < odorAPool.size()) {
                    firingTSOdorA2.addAll(odorAPool.get(aPerm[i]).getSpikesList());
                }
            }

            for (int i = 0; i < sampleCount[1][0]; i++) {
                if (bPerm[i] < odorBPool.size()) {
                    firingTSOdorB1.addAll(odorBPool.get(bPerm[i]).getSpikesList());
                }
            }

            for (int i = sampleCount[1][0]; i < sampleCount[1][0] + sampleCount[1][1]; i++) {
                if (bPerm[i] < odorBPool.size()) {
                    firingTSOdorB2.addAll(odorBPool.get(bPerm[i]).getSpikesList());
                }
            }
            int binCount = Math.round((binEnd - binStart) / binSize);
            int[] binedTSOdorA1 = new int[binCount];//time stamp
            int[] binedTSOdorA2 = new int[binCount];//time stamp
            int[] binedTSOdorB1 = new int[binCount];//time stamp
            int[] binedTSOdorB2 = new int[binCount];//time stamp

            for (Double d : firingTSOdorA1) {
                binedTSOdorA1[(int) ((d - binStart) / binSize)]++;
            }

            for (Double d : firingTSOdorA2) {
                binedTSOdorA2[(int) ((d - binStart) / binSize)]++;
            }

            for (Double d : firingTSOdorB1) {
                binedTSOdorB1[(int) ((d - binStart) / binSize)]++;
            }
            for (Double d : firingTSOdorB2) {
                binedTSOdorB2[(int) ((d - binStart) / binSize)]++;
            }

            double[] normalized = new double[binCount * 4];

            for (int i = 0; i < binedTSOdorA1.length; i++) {
                normalized[i] = (((double) binedTSOdorA1[i] / sampleCount[0][0] / binSize) - meanBaseFR) / stdBaseFR;
            }

            for (int i = 0; i < binedTSOdorA2.length; i++) {
                normalized[i + binCount] = (((double) binedTSOdorA2[i] / sampleCount[0][1] / binSize) - meanBaseFR) / stdBaseFR;
            }

            for (int i = 0; i < binedTSOdorB1.length; i++) {
                normalized[i + binCount * 2] = (((double) binedTSOdorB1[i] / sampleCount[1][0] * (1 / binSize)) - meanBaseFR) / stdBaseFR;
            }

            for (int i = 0; i < binedTSOdorB2.length; i++) {
                normalized[i + binCount * 3] = (((double) binedTSOdorB2[i] / sampleCount[1][1] * (1 / binSize)) - meanBaseFR) / stdBaseFR;
            }

            samples[repeat] = normalized;
        }
        return samples;
    }

    public void poolTrials() {
        this.trialPool = new ArrayList<>();
        for (HashMap<Integer, Trial> sess : this.sessions.values()) {
            for (Trial t : sess.values()) {
                trialPool.add(t);
            }
        }
    }

    private double[] getBaselineStats(ArrayList<Trial> trialPool, int totalTrialCount) {
        double[] baselineTSCount = new double[totalTrialCount];
        int trialIdx = 0;
        for (Trial trial : trialPool) {
            if (trial.isCorrect()) {
                for (Double d : trial.getSpikesList()) {
                    if (d < 0) {
                        baselineTSCount[trialIdx]++;
                    } else {
                        break;
                    }
                }
                trialIdx++;
            }
        }
        return new double[]{StatUtils.mean(baselineTSCount), Math.sqrt(StatUtils.variance(baselineTSCount))};
    }

//    /*
//     For Temporary check only
//     */
//    public double[] getBaselineTS(int totalTrialCount) {
//        double[] baselineTSCount = new double[totalTrialCount+2];
//        int trialIdx = 2;
//        for (Trial trial : trialPool) {
//            if (trial.isCorrect()) {
//                for (Double d : trial.getSpikesList()) {
//                    if (d < 0) {
//                        baselineTSCount[trialIdx]++;
//                    } else {
//                        break;
//                    }
//                }
//                trialIdx++;
//            }
//        }
//        
//        double[] temp=new double[totalTrialCount];
//        System.arraycopy(baselineTSCount, 2, temp, 0, totalTrialCount);
//        baselineTSCount[0]=StatUtils.mean(temp);
//        baselineTSCount[1]=Math.sqrt(StatUtils.variance(temp));
//        
//        return baselineTSCount;
//    }

    /*
     For temporary check only
     */
    public double[][][] getTrialTS(int odorATrialCount, int odorBTrialCount) {

        double[][] firingTSOdorA = new double[odorATrialCount][];//Time Stamp
        double[][] firingTSOdorB = new double[odorBTrialCount][];//Time Stamp

        int trialAIdx = 0;
        int trialBIdx = 0;

        for (Trial trial : trialPool) {

            if (trial.firstOdorIs(0) && trial.isCorrect()) {
                double[] temp = new double[trial.getSpikesList().size()];
                int idx = 0;
                for (Double d : trial.getSpikesList()) {
                    temp[idx] = d;
                    idx++;
                }
                firingTSOdorA[trialAIdx] = temp;
                trialAIdx++;

            } else if (trial.firstOdorIs(1) && trial.isCorrect()) {
                double[] temp = new double[trial.getSpikesList().size()];
                int idx = 0;
                for (Double d : trial.getSpikesList()) {
                    temp[idx] = d;
                    idx++;
                }
                firingTSOdorB[trialBIdx] = temp;
                trialBIdx++;
            }
        }
//        Collections.sort(firingTS);

        return new double[][][]{firingTSOdorA, firingTSOdorB};
    }
}
