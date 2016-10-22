/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.SU;

import java.util.ArrayList;
import java.util.Arrays;
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
//    final private ArrayList<Double> spkPool = new ArrayList<>();
    private int unitSPKCount = 0;

//    HashMap<Integer, Trial> trials;
    public SingleUnit() {
        sessions = new HashMap<>();
    }

//    public void addspk(Double d) {
//        spkPool.add(d);
//    }
    public SingleUnit addspk() {
        unitSPKCount++;
        return this;
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

    public void setTrial(int sessionIdx, int trialIdx, Trial t) {
        sessions.get(sessionIdx).put(trialIdx, t);
    }

    public void removeSession(int sessionIdx) {
        sessions.remove(sessionIdx);
    }

    public boolean isSparseFiring(ClassifyType type, int trialCount, double refracRatio, double recordingLength) {
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
                return (unitSPKCount / recordingLength) < 2;
//                return spkPool.size() / (spkPool.get(spkPool.size() - 1) - spkPool.get(0)) < 2;
//            case BY_PEAK2Hz_WHOLETRIAL:
//                if (spkPool.size() > 1) {
//                    for (int i = 1; i < spkPool.size(); i++) {
//                        if (spkPool.get(i) - spkPool.get(i - 1) < 0.5) {
//                            return false;
//                        }
//                    }
//                }
//                return true;
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
        sampleCount = reduceSampleIfNecessary(sampleCount, typeATrialCount, typeBTrialCount, repeatCount);
        if (null == sampleCount || typeATrialCount < 1 || typeBTrialCount < 1) {
            return null;
        }
        double[][] samples = new double[repeatCount][];

        double[] stats = pr.getBaselineStats(this.trialPool, totalTrial(miceday));
        double meanBaseFR = stats[0];
        double stdBaseFR = stats[1];
        RandomDataGenerator rng = new RandomDataGenerator();
        for (int repeat = 0; repeat < repeatCount; repeat++) {
//            System.out.println("RPT" + repeat);
            int[] aPerm = rng.nextPermutation(typeATrialCount, sumSampleCount(sampleCount, 0));
            int[] bPerm = rng.nextPermutation(typeBTrialCount, sumSampleCount(sampleCount, 1));
//            System.out.println("SPCT" + Arrays.deepToString(sampleCount));
//            System.out.println("APERM" + Arrays.toString(aPerm));
            ArrayList<ArrayList<Double>> psthA = genPSTH(sampleCount, 0, aPerm, typeAPool);
            ArrayList<ArrayList<Double>> psthB = genPSTH(sampleCount, 1, bPerm, typeBPool);

            float binStart = bin[0];
            float binSize = bin[1];
            float binEnd = bin[2];

            int binCount = Math.round((binEnd - binStart) / binSize);
            ArrayList<int[]> binnedA = genBinned(psthA, binCount, binStart, binSize);
            ArrayList<int[]> binnedB = genBinned(psthB, binCount, binStart, binSize);

            double[] normalized = new double[binCount * (binnedA.size() + binnedB.size())];
//            System.out.println("NORINIT");
            for (int i = 0; i < binnedA.size(); i++) {
                for (int j = 0; j < binnedA.get(i).length; j++) {
                    normalized[j + i * binCount] = (((double) binnedA.get(i)[j] / sampleCount[0][i] / binSize) - meanBaseFR) / stdBaseFR;
                }
            }
            for (int i = 0; i < binnedB.size(); i++) {
                for (int j = 0; j < binnedB.get(i).length; j++) {
                    normalized[j + (i + binnedA.size()) * binCount] = (((double) binnedB.get(i)[j] / sampleCount[1][i] / binSize) - meanBaseFR) / stdBaseFR;
                }
            }
//            System.out.println("NORM" + Arrays.toString(normalized));
            samples[repeat] = normalized;
        }
        return samples;
    }

    ArrayList<ArrayList<Double>> genPSTH(final int[][] sampleCount, final int grp, final int[] perm, final ArrayList<Trial> trialPool) {
        ArrayList<ArrayList<Double>> psth = new ArrayList<>();
//        System.out.println("PERM" + Arrays.toString(perm));
        int currSampCount = 0;
        for (int i = 0; i < sampleCount[grp].length; i++) {
            ArrayList<Double> oneSample = new ArrayList<>();
            for (int j = currSampCount; j < currSampCount + sampleCount[grp][i]; j++) {
//                System.out.println("J" + j);
//                System.out.println("PERMJ" + perm[j]);
                if (perm[j] < trialPool.size()) {
                    oneSample.addAll(trialPool.get(perm[j]).getSpikesList());
                }
            }
            currSampCount += sampleCount[grp][i];
            psth.add(oneSample);
        }
//        System.out.println("PSTHE" + psth.size());
        return psth;
    }

    ArrayList<int[]> genBinned(ArrayList<ArrayList<Double>> psth, int binCount, float binStart, float binSize) {
        ArrayList<int[]> binned = new ArrayList<>();
        for (int i = 0; i < psth.size(); i++) {
            int[] binnedSample = new int[binCount];
            for (Double d : psth.get(i)) {
//                System.out.println("d"+d);
//                System.out.println("BS"+binStart);

                if ((int) ((d - binStart) / binSize) < binCount) {
                    binnedSample[(int) ((d - binStart) / binSize)]++;
                }
            }
            binned.add(binnedSample);
        }
        return binned;
    }

    public void poolTrials() {

        for (HashMap<Integer, Trial> sess : this.sessions.values()) {
            for (Trial t : sess.values()) {
                trialPool.add(t);
            }
        }
    }

    private int totalTrial(spk2fr.MiceDay md) {
        int counter = 0;
        for (ArrayList<EventType[]> session : md.getBehaviorSessions()) {
            counter += session.size();
        }
        return counter;
    }

    int sumSampleCount(int[][] samples, int grp) {
        int count = 0;
        for (int oneGrp : samples[grp]) {
            count += oneGrp;
        }
        return count;
    }

    int[][] reduceSampleIfNecessary(final int[][] sampleCount, int typeATrialCount, int typeBTrialCount, int repeat) {
//        double[][] samples = new double[repeat][];
        for (int j = 0; j < 2; j++) {
            int requiredCount = sumSampleCount(sampleCount, j);
            int typeTrialCount = j == 0 ? typeATrialCount : typeBTrialCount;
            if (requiredCount > typeTrialCount) {
                if (sampleCount[j].length < 3 && sampleCount[j][1] < 2) {//Decoding
                    sampleCount[j][0] = typeTrialCount - sampleCount[j][1];
                } else {
                    sampleCount[j][0] = typeTrialCount;
                    for (int i = sampleCount[j].length - 1; i > 0; i--) {
                        sampleCount[j][i] = sampleCount[j][i] * typeTrialCount / requiredCount;
                        sampleCount[j][0] -= sampleCount[j][i];
                    }
                }
                if (sampleCount[j][0] < 1) {
                    System.out.println("Not Enough Trial!");
                    return null;
                }
            }
        }

        return sampleCount;
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

            if (trial.sampleOdorIs(EventType.OdorA) && trial.isCorrect()) {
                double[] temp = new double[trial.getSpikesList().size()];
                int idx = 0;
                for (Double d : trial.getSpikesList()) {
                    temp[idx] = d;
                    idx++;
                }
                firingTSOdorA[trialAIdx] = temp;
                trialAIdx++;

            } else if (trial.sampleOdorIs(EventType.OdorB) && trial.isCorrect()) {
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
                case "distrgoz":
                case "distrgo":
                    processor = new ProcessorSamplenDistrZ(EventType.OdorA, type.toLowerCase().endsWith("z"));
                    break;
                case "distrnogoz":
                case "distrnogo":
                    processor = new ProcessorSamplenDistrZ(EventType.OdorB, type.toLowerCase().endsWith("z"));
                    break;
                case "distrnonez":
                case "distrnone":
                    processor = new ProcessorSamplenDistrZ(EventType.NONE, type.toLowerCase().endsWith("z"));
                    break;
                case "match":
                case "matchincincorr":
                    processor=new ProcessorMatch(type.toLowerCase().endsWith("incorr"));
                    break;
                default:
                    System.out.println(type);
                    throw new IllegalArgumentException("Unknown Processor Type");
            }
        }
    }

}
