/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.SU;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.math3.random.RandomDataGenerator;
import spk2fr.ClassifyType;
import spk2fr.EventType;

/**
 *
 * @author Libra
 */
public class SingleUnit {

    final private HashMap<Integer, HashMap<Integer, Trial>> sessions;
    final private ArrayList<Trial> trialPool = new ArrayList<>();
    final private ArrayList<Double> spkPool = new ArrayList<>();
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
    public double[][] getSampleFR(spk2fr.MiceDay miceday, String type, float[] bin, int[][] sampleCount, int repeatCount) {  //firing rate, baseline assumed to be 1s

        Processor pr = new GetType(type).getProcessor();
        int typeATrialCount = pr.getTypeATrialNum(miceday);
        int typeBTrialCount = pr.getTypeBTrialNum(miceday);

        pr.fillPoolsByType(this.trialPool);
        ArrayList<Trial> typeAPool = pr.getTypeAPool();
        ArrayList<Trial> typeBPool = pr.getTypeBPool();

        double[][] samples = reduceSampleIfNecessary(sampleCount, typeATrialCount, typeBTrialCount, repeatCount);

        double[] stats = pr.getBaselineStats(this.trialPool, typeATrialCount + typeBTrialCount);
        double meanBaseFR = stats[0];
        double stdBaseFR = stats[1];

        RandomDataGenerator rng = new RandomDataGenerator();
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

            float binStart = bin[0];
            float binSize = bin[1];
            float binEnd = bin[2];

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

        for (HashMap<Integer, Trial> sess : this.sessions.values()) {
            for (Trial t : sess.values()) {
                trialPool.add(t);
            }
        }
    }

    double[][] reduceSampleIfNecessary(final int[][] sampleCount, int typeATrialCount, int typeBTrialCount, int repeat) {
        double[][] samples = new double[repeat][];
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
        return samples;
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

    class GetType {

        final private Processor processor;

        public Processor getProcessor() {
            return processor;
        }

        GetType(String type) {
//            int[] counts;
            switch (type.toLowerCase()) {
                case "odor":
                    processor = new Processor4Odor();
                    break;
                case "odorz":
                    processor = new Processor4OdorZ();
                    break;
                case "opsuppress":
                    processor = new Processor4OpSuppress();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Processor Type");
            }
        }
    }

}
