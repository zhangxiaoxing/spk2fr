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
    private ArrayList<Double> spkPool = new ArrayList<>();
//    HashMap<Integer, Trial> trials;

    public SingleUnit() {
        sessions = new HashMap<>();
    }

    public void addspk(Double d) {
        spkPool.add(d);
    }

    public SingleUnit(HashMap<Integer, HashMap<Integer, Trial>> sessions) {
        this.sessions = sessions;
//        spkPool = new ArrayList<>();
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

    public void removeSession(int sessionIdx) {
        sessions.remove(sessionIdx);
    }

    public boolean isSparseFiring(ClassifyType type, int trialCount, double refracRatio) {
        int totalSpike = 0;
        int lowRefracSpike = 0;
        for (Trial trial : trialPool) {
            ArrayList<Double> ts = trial.getSpikesList();
            totalSpike += ts.size();
            if (ts.size() > 1) {
                for (int i = 1; i < ts.size(); i++) {
                    if (ts.get(i) - ts.get(i - 1) < 0.002) {
                        lowRefracSpike++;
                    }
                }
            }
        }
        if (totalSpike < 1 || (double) lowRefracSpike / totalSpike > refracRatio) {
            return true;
        }
        switch (type) {
            case BY_PEAK2Hz:
                for (Trial trial : trialPool) {
                    ArrayList<Double> ts = trial.getSpikesList();
                    if (ts.size() > 1) {
                        for (int i = 1; i < ts.size(); i++) {
                            if (ts.get(i) - ts.get(i - 1) < 0.5) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            case BY_AVERAGE2Hz:
            case BY_AVERAGE1Hz:
                int spkCount = 0;
                int firedTrial = 0;
                double firedTrialLengthSum = 0;
                for (Trial trial : trialPool) {
                    spkCount += trial.getSpikesList().size();
                    firedTrial++;
                    firedTrialLengthSum += trial.getLength();
                }
                double avgLength = firedTrialLengthSum / firedTrial;
                double fr = type == ClassifyType.BY_AVERAGE1Hz ? 1d : 2d;
                return !(spkCount / trialCount / avgLength > fr); //2Hz
            case BY_AVERAGE2Hz_WHOLETRIAL:
                return spkPool.size() / (spkPool.get(spkPool.size() - 1) - spkPool.get(0)) < 2;
            case BY_PEAK2Hz_WHOLETRIAL:
                if (spkPool.size() > 1) {
                    for (int i = 1; i < spkPool.size(); i++) {
                        if (spkPool.get(i) - spkPool.get(i - 1) < 0.5) {
                            return false;
                        }
                    }
                }
                return true;
        }

        return true;
    }

    /*
     SampleSize=[PFSampleSize1,PFSampleSize2;BNSampleSize1,BNSampleSize2];
     */
    public double[][] getSampleFR(ClassifyType cType, int typeATrialCount, int typeBTrialCount, float binStart, float binSize, float binEnd, int[][] sampleCount, int repeatCount) {  //firing rate, baseline assumed to be 1s

        double meanBaseFR;
        double stdBaseFR;
        if (cType == ClassifyType.BY_ODOR_Z || cType == ClassifyType.BY_ODOR_WITHIN_MEAN_TRIAL_Z
                || cType == ClassifyType.BY_CORRECT_OdorA_Z || cType == ClassifyType.BY_CORRECT_OdorB_Z
                || cType == ClassifyType.BY_OP_SUPPRESS) {
            double[] stats = getBaselineStats(cType, this.trialPool, typeATrialCount + typeBTrialCount);
            meanBaseFR = stats[0];
            stdBaseFR = stats[1];
        } else {
            meanBaseFR = 0;
            stdBaseFR = 1;
        }

//        System.out.println(meanBaseFR+", "+stdBaseFR);
        ArrayList<Trial> typeAPool = new ArrayList<>();
        ArrayList<Trial> typeBPool = new ArrayList<>();
        switch (cType) {
            case BY_ODOR:
            case BY_ODOR_WITHIN_MEAN_TRIAL:
            case BY_ODOR_Z:
            case BY_ODOR_WITHIN_MEAN_TRIAL_Z:
                for (Trial trial : this.trialPool) {
                    if (trial.firstOdorIs(EventType.OdorA) && trial.isCorrect()) {
                        typeAPool.add(trial);
                    } else if (trial.firstOdorIs(EventType.OdorB) && trial.isCorrect()) {
                        typeBPool.add(trial);
                    }
                }
                break;
            case BY_OP_SUPPRESS:
                for (Trial trial : this.trialPool) {
                    typeAPool.add(trial);
                    typeBPool.add(trial);
                }
                break;
            case BY_SECOND_ODOR:
                for (Trial trial : this.trialPool) {
                    if (trial.secondOdorIs(EventType.OdorA) && trial.isCorrect()) {
                        typeAPool.add(trial);
                    } else if (trial.secondOdorIs(EventType.OdorB) && trial.isCorrect()) {
                        typeBPool.add(trial);
                    }
                }
                break;
            case BY_MATCH:
                for (Trial trial : this.trialPool) {
                    if (trial.isMatch() && trial.isCorrect()) {
                        typeAPool.add(trial);
                    } else if (trial.isCorrect() && !trial.isMatch()) {
                        typeBPool.add(trial);
                    }
                }
                break;
            case BY_CORRECT_OdorA:
            case BY_CORRECT_OdorA_Z:
                for (Trial trial : this.trialPool) {
                    if (trial.firstOdorIs(EventType.OdorA) && trial.isCorrect()) {
                        typeAPool.add(trial);
                    } else if (trial.firstOdorIs(EventType.OdorA) && !trial.isCorrect()) {
                        typeBPool.add(trial);
                    }
                }
                break;
            case BY_CORRECT_OdorB:
            case BY_CORRECT_OdorB_Z:
                for (Trial trial : this.trialPool) {
                    if (trial.firstOdorIs(EventType.OdorB) && trial.isCorrect()) {
                        typeAPool.add(trial);
                    } else if (trial.firstOdorIs(EventType.OdorB) && !trial.isCorrect()) {
                        typeBPool.add(trial);
                    }
                }
                break;
            case ALL_ODORA:
                for (Trial trial : this.trialPool) {
                    if (trial.firstOdorIs(EventType.OdorA)) {
                        typeAPool.add(trial);
                        typeBPool.add(trial);
                    }
                }
                break;
            case ALL_ODORB:
                for (Trial trial : this.trialPool) {
                    if (trial.firstOdorIs(EventType.OdorB)) {
                        typeAPool.add(trial);
                        typeBPool.add(trial);
                    }
                }
                break;
        }

        RandomDataGenerator rng = new RandomDataGenerator();
        double[][] samples = new double[repeatCount][];
        if (sampleCount[0][0] + sampleCount[0][1] > typeATrialCount) {
            if (sampleCount[0][1] < 2) {
                sampleCount[0][0] = typeATrialCount - sampleCount[0][1];
            } else {
                sampleCount[0][0] = typeATrialCount / 2;
                sampleCount[0][1] = typeATrialCount / 2;
            }
        }
        if (sampleCount[1][0] + sampleCount[1][1] > typeBTrialCount) {
            if (sampleCount[1][1] < 2) {
                sampleCount[1][0] = typeBTrialCount - sampleCount[1][1];
            } else {
                sampleCount[1][0] = typeBTrialCount / 2;
                sampleCount[1][1] = typeBTrialCount / 2;
            }
        }

        if (sampleCount[0][0] < 1 || sampleCount[1][0] < 1) {
//            System.out.print(sampleCount[0][0]+", "+sampleCount[0][1]+", "+sampleCount[1][0]+", "+sampleCount[1][1]+", ");
            System.out.println("Not Enough Trial!");
            return null;
        }

        for (int repeat = 0; repeat < repeatCount; repeat++) {

            int[] aPerm = rng.nextPermutation(typeATrialCount, sampleCount[0][0] + sampleCount[0][1]);
            int[] bPerm = rng.nextPermutation(typeBTrialCount, sampleCount[1][0] + sampleCount[1][1]);

            ArrayList<Double> psthTypeA1 = new ArrayList<>();//Time Stamp
            ArrayList<Double> psthTypeA2 = new ArrayList<>();//Time Stamp
            ArrayList<Double> psthTypeB1 = new ArrayList<>();//Time Stamp
            ArrayList<Double> psthTypeB2 = new ArrayList<>();//Time Stamp

            for (int i = 0; i < sampleCount[0][0]; i++) {
                if (aPerm[i] < typeAPool.size()) {
                    psthTypeA1.addAll(typeAPool.get(aPerm[i]).getSpikesList());
                }
            }
            for (int i = sampleCount[0][0]; i < sampleCount[0][0] + sampleCount[0][1]; i++) {
                if (aPerm[i] < typeAPool.size()) {
                    psthTypeA2.addAll(typeAPool.get(aPerm[i]).getSpikesList());
                }
            }

            for (int i = 0; i < sampleCount[1][0]; i++) {
                if (bPerm[i] < typeBPool.size()) {
                    psthTypeB1.addAll(typeBPool.get(bPerm[i]).getSpikesList());
                }
            }

            for (int i = sampleCount[1][0]; i < sampleCount[1][0] + sampleCount[1][1]; i++) {
                if (bPerm[i] < typeBPool.size()) {
                    psthTypeB2.addAll(typeBPool.get(bPerm[i]).getSpikesList());
                }
            }
            int binCount = Math.round((binEnd - binStart) / binSize);
            int[] binedPSTHA1 = new int[binCount];//time stamp
            int[] binedPSTHA2 = new int[binCount];//time stamp
            int[] binedPSTHB1 = new int[binCount];//time stamp
            int[] binedPSTHB2 = new int[binCount];//time stamp

            for (Double d : psthTypeA1) {
                if ((int) ((d - binStart) / binSize) < binCount) {
                    binedPSTHA1[(int) ((d - binStart) / binSize)]++;
                }
            }

            for (Double d : psthTypeA2) {
                if ((int) ((d - binStart) / binSize) < binCount) {
                    binedPSTHA2[(int) ((d - binStart) / binSize)]++;
                }
            }

            for (Double d : psthTypeB1) {
                if ((int) ((d - binStart) / binSize) < binCount) {
                    binedPSTHB1[(int) ((d - binStart) / binSize)]++;
                }
            }
            for (Double d : psthTypeB2) {
                if ((int) ((d - binStart) / binSize) < binCount) {
                    binedPSTHB2[(int) ((d - binStart) / binSize)]++;
                }
            }

            double[] normalized = new double[binCount * 4];

            for (int i = 0; i < binedPSTHA1.length; i++) {
                normalized[i] = (((double) binedPSTHA1[i] / sampleCount[0][0] / binSize) - meanBaseFR) / stdBaseFR;
            }

            for (int i = 0; i < binedPSTHA2.length; i++) {
                normalized[i + binCount] = (((double) binedPSTHA2[i] / sampleCount[0][1] / binSize) - meanBaseFR) / stdBaseFR;
            }

            for (int i = 0; i < binedPSTHB1.length; i++) {
                normalized[i + binCount * 2] = (((double) binedPSTHB1[i] / sampleCount[1][0] * (1 / binSize)) - meanBaseFR) / stdBaseFR;
            }

            for (int i = 0; i < binedPSTHB2.length; i++) {
                normalized[i + binCount * 3] = (((double) binedPSTHB2[i] / sampleCount[1][1] * (1 / binSize)) - meanBaseFR) / stdBaseFR;
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

    private double[] getBaselineStats(ClassifyType cType, ArrayList<Trial> trialPool, int totalTrialCount) {
//        System.out.println(totalTrialCount);
        double[] baselineTSCount = new double[totalTrialCount];
        int trialIdx = 0;
        boolean allZero = true;
        switch (cType) {

            case BY_ODOR_Z:
                for (Trial trial : trialPool) {
//                    System.out.println(trialPool.size());
                    if (trial.isCorrect()) {
                        for (Double d : trial.getSpikesList()) {
                            if (d < 0) {
                                baselineTSCount[trialIdx]++;
                                allZero = false;
                            } else {
                                break;
                            }
                        }
                        trialIdx++;
                    }
                }
                break;
            case BY_OP_SUPPRESS:
                for (Trial trial : trialPool) {
                    for (Double d : trial.getSpikesList()) {
                        if (d < 1) {
                            baselineTSCount[trialIdx]++;
                            allZero = false;
                        } else {
                            break;
                        }
                    }
                    trialIdx++;
                }
                break;

            case BY_CORRECT_OdorA_Z:
                for (Trial trial : trialPool) {
                    if (trial.firstOdorIs(EventType.OdorA)) {
                        for (Double d : trial.getSpikesList()) {
                            if (d < 0) {
                                baselineTSCount[trialIdx]++;
                                allZero = false;
                            } else {
                                break;
                            }
                        }
                        trialIdx++;
                    }
                }
                break;

            case BY_CORRECT_OdorB_Z:
                for (Trial trial : trialPool) {
                    if (trial.firstOdorIs(EventType.OdorB)) {
                        for (Double d : trial.getSpikesList()) {
                            if (d >= 1 & d < 2) {
                                baselineTSCount[trialIdx]++;
                                allZero = false;
                            } else if (d >= 2) {
                                break;
                            }
                        }
                        trialIdx++;
                    }
                }
                break;

            case BY_ODOR_WITHIN_MEAN_TRIAL_Z:
                int[] tsbins = new int[10];
                for (Trial trial : trialPool) {
//                    System.out.println(trialPool.size());
                    if (trial.isCorrect()) {
                        for (Double d : trial.getSpikesList()) {
                            if (d < 0 && d > -1) {
                                tsbins[(int) ((d + 1) * 10)]++;
                                allZero = false;
                            } else {
                                break;
                            }
                        }
                    }
                }
                double[] binFR = new double[10];
                for (int i = 0; i < 10; i++) {
                    binFR[i] = (double) tsbins[i] / totalTrialCount / 0.1;
                }
                return new double[]{StatUtils.mean(binFR), Math.sqrt(StatUtils.variance(binFR))};
//                break;
        }
        if (allZero) {
            baselineTSCount[0] = 1;
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

            if (trial.firstOdorIs(EventType.OdorA) && trial.isCorrect()) {
                double[] temp = new double[trial.getSpikesList().size()];
                int idx = 0;
                for (Double d : trial.getSpikesList()) {
                    temp[idx] = d;
                    idx++;
                }
                firingTSOdorA[trialAIdx] = temp;
                trialAIdx++;

            } else if (trial.firstOdorIs(EventType.OdorB) && trial.isCorrect()) {
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
