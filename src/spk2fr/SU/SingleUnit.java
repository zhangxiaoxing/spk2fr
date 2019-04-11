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
import spk2fr.MiceDay;

/**
 *
 * @author Libra
 */
public class SingleUnit {

    final boolean discardLackingTrials = false;
    final private HashMap<Integer, HashMap<Integer, Trial>> sessions;
    final private ArrayList<Trial> trialPool = new ArrayList<>();
//    final private ArrayList<Double> spkPool = new ArrayList<>();
    private int unitSPKCount = 0;
    private double ISIratio;
    private double avgFR;

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

    public Trial getTrial(int sessionIdx, int trialIdx, int tet, int su) {
//        System.out.println("Get Ses:"+sessionIdx+", trial: "+trialIdx+", For tet:"+tet+", su: "+su);
        return sessions.get(sessionIdx).get(trialIdx);
    }

    public void setTrial(int sessionIdx, int trialIdx, Trial t) {
        if (!sessions.containsKey(sessionIdx)) {
            sessions.put(sessionIdx, new HashMap<>());
        }
        if (!sessions.get(sessionIdx).containsKey(trialIdx)) {
            sessions.get(sessionIdx).put(trialIdx, t);
        }
//        System.out.println("Put Ses:"+sessionIdx+", trial: "+trialIdx);
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

//        System.out.println("DBG ISI "+
        this.ISIratio = (double) lowRefracSpike / totalSpike;
        if (totalSpike < 1 || this.ISIratio > refracRatio) {
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
            case BY_AVERAGE2Hz_COMPAT:
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
                if (type == ClassifyType.BY_AVERAGE2Hz_COMPAT) {
                    this.avgFR = spkCount / trialCount / avgLength;
                } else {
                    this.avgFR = (double) spkCount / trialCount / avgLength;
                }
                return !(this.avgFR > fr); //2Hz
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
    public double[][] getSampleFR(spk2fr.MiceDay miceday, String groupBy, float[] bin, int[][] sampleCountIn, int repeatCount) {  //firing rate, baseline assumed to be 1s

        Processor pr = new GetType(groupBy).getProcessor();

        pr.fillPoolsByType(this.trialPool);
        ArrayList<Trial> typeAPool = pr.getTypeAPool();
        ArrayList<Trial> typeBPool = pr.getTypeBPool();
        int typeATrialCount = typeAPool.size();
        int typeBTrialCount = typeBPool.size();
        int[][] sampleCount = new int[][]{
            reduceSampleIfNecessary(sampleCountIn[0], typeATrialCount), reduceSampleIfNecessary(sampleCountIn[1], typeBTrialCount)};
        if (null == sampleCount || typeATrialCount < 1 || typeBTrialCount < 1) {
            if (discardLackingTrials) {
                return null;
            } else {
                double[][] samples = new double[repeatCount][Math.round((bin[2] - bin[0]) / bin[1]) * 4];
                for (double[] row : samples) {
                    Arrays.fill(row, 65535d);
                }
                return samples;
            }
        }
        double[][] samples = new double[repeatCount][];

        double[] stats = pr.getBaselineStats(this.trialPool);
        double meanBaseFR = stats[0];
        double stdBaseFR = stats[1];
        RandomDataGenerator rng = new RandomDataGenerator();

        float binStart = bin[0];
        float binSize = bin[1];
        float binEnd = bin[2];
        float unitFR = 1f / binSize;
//if(typeATrialCount<40)
//        System.out.println(typeATrialCount+","+Arrays.toString(sampleCount[0])+","+typeBTrialCount+","+Arrays.toString(sampleCount[1])); 
        for (int repeat = 0; repeat < repeatCount; repeat++) {
            int[] aPerm = rng.nextPermutation(typeATrialCount, Arrays.stream(sampleCount[0]).sum());
            int[] bPerm = rng.nextPermutation(typeBTrialCount, Arrays.stream(sampleCount[1]).sum());
            ArrayList<ArrayList<Double>> psthA = genPSTH(sampleCount[0], aPerm, typeAPool);
            ArrayList<ArrayList<Double>> psthB = genPSTH(sampleCount[1], bPerm, typeBPool);

            int binCount = Math.round((binEnd - binStart) / binSize);
            ArrayList<double[]> binnedA = genBinned(psthA, binCount, binStart, binSize, unitFR);
            ArrayList<double[]> binnedB = genBinned(psthB, binCount, binStart, binSize, unitFR);

            double[] normalized = new double[binCount * (binnedA.size() + binnedB.size())];
            for (int i = 0; i < binnedA.size(); i++) {
                for (int j = 0; j < binnedA.get(i).length; j++) {
                    normalized[j + i * binCount] = ((binnedA.get(i)[j] / sampleCount[0][i]) - meanBaseFR) / stdBaseFR;
                }
            }
            for (int i = 0; i < binnedB.size(); i++) {
                for (int j = 0; j < binnedB.get(i).length; j++) {
                    normalized[j + (i + binnedA.size()) * binCount] = ((binnedB.get(i)[j] / sampleCount[1][i]) - meanBaseFR) / stdBaseFR;
                }
            }
            normalized[normalized.length - 1] = getPerf(sampleCount, aPerm, bPerm, typeAPool, typeBPool);
            samples[repeat] = normalized;
        }
//        System.out.println("normal "+samples[0].length);
        return samples;

    }

    public double[][] getOneSampleFR(MiceDay miceday, float[] binning, int[] sampleCountIn, int repeatCount, String criteria) {  //firing rate, baseline assumed to be 1s

        ProcessorWOClassify pr = new ProcessorWOClassify(
                criteria.toLowerCase().contains("z"),
                criteria.toLowerCase().contains("error"),
                criteria.toLowerCase().contains("all"));

        pr.fillPoolsByType(this.trialPool);
        ArrayList<Trial> trialPool = pr.getTrialPool();
        int[] sampleCount = reduceSampleIfNecessary(sampleCountIn, trialPool.size());
        if (null == sampleCount || trialPool.size() < 1) {
            if (discardLackingTrials) {
                return null;
            } else {
                double[][] samples = new double[repeatCount][Math.round((binning[2] - binning[0]) / binning[1]) * 2];
                for (double[] row : samples) {
                    Arrays.fill(row, 65535d);
                }
                return samples;
            }
        }
        double[][] samples = new double[repeatCount][];

        double[] stats = pr.getBaselineStats(this.trialPool);
        double meanBaseFR = stats[0];
        double stdBaseFR = stats[1];
        RandomDataGenerator rng = new RandomDataGenerator();

        float binStart = binning[0];
        float binSize = binning[1];
        float binEnd = binning[2];
        float unitFR = 1f / binSize;
//if(typeATrialCount<40)
//        System.out.println(typeATrialCount+","+Arrays.toString(sampleCount[0])+","+typeBTrialCount+","+Arrays.toString(sampleCount[1])); 
        for (int repeat = 0; repeat < repeatCount; repeat++) {
            int[] perm = rng.nextPermutation(trialPool.size(), Arrays.stream(sampleCount).sum());
            ArrayList<ArrayList<Double>> psth = genPSTH(sampleCount, perm, trialPool);

            int binCount = Math.round((binEnd - binStart) / binSize);
            ArrayList<double[]> binned = genBinned(psth, binCount, binStart, binSize, unitFR);

            double[] normalized = new double[binCount * binned.size()];
            for (int i = 0; i < binned.size(); i++) {
                for (int j = 0; j < binned.get(i).length; j++) {
                    normalized[j + i * binCount] = ((binned.get(i)[j] / sampleCount[i]) - meanBaseFR) / stdBaseFR;
                }
            }

//            normalized[normalized.length - 1] = getPerf(sampleCount, perm, bPerm, typeAPool, typeBPool);
            samples[repeat] = normalized;
        }
//        System.out.println("normal "+samples[0].length);
        return samples;

    }

    public double[][] getAllFR(spk2fr.MiceDay miceday, String groupBy, float[] bin, boolean isS1) {  //firing rate, baseline assumed to be 1s

        Processor pr = new GetType(groupBy).getProcessor();

        pr.fillPoolsByType(this.trialPool);
        ArrayList<Trial> pool = isS1 ? pr.getTypeAPool() : pr.getTypeBPool();
        ArrayList<ArrayList<Double>> rasters = new ArrayList<>();
        for (Trial t : pool) {
            rasters.add(t.getSpikesList());
        }

        float binStart = bin[0];
        float binSize = bin[1];
        float binEnd = bin[2];
        float unitFR = 1 / binSize;

        int binCount = Math.round((binEnd - binStart) / binSize);
        ArrayList<double[]> binned = genBinned(rasters, binCount, binStart, binSize, unitFR);
        return binned.toArray(new double[binned.size()][]);
    }

    ArrayList<ArrayList<Double>> genPSTH(final int[] sampleCount, final int[] perm, final ArrayList<Trial> trialPool) {
        ArrayList<ArrayList<Double>> psth = new ArrayList<>();
        int currSampCount = 0;
        for (int i = 0; i < sampleCount.length; i++) {
            ArrayList<Double> oneSample = new ArrayList<>();
            for (int j = currSampCount; j < currSampCount + sampleCount[i]; j++) {
                if (perm[j] < trialPool.size()) {
                    oneSample.addAll(trialPool.get(perm[j]).getSpikesList());
                }
            }
            currSampCount += sampleCount[i];
            psth.add(oneSample);
        }
        return psth;
    }

    float getPerf(final int[][] sampleCount, final int[] aPerm, final int[] bPerm, final ArrayList<Trial> aPool, final ArrayList<Trial> bPool) {
        if (sampleCount[0][1] < 1) {
            return 0;
        }
        float correctCount = 0;
        float totalCount = 0;
        for (int j = sampleCount[0][0]; j < sampleCount[0][0] + sampleCount[0][1]; j++) {
            if (aPerm[j] < aPool.size()) {
                totalCount++;
                correctCount += aPool.get(aPerm[j]).isCorrect() ? 1 : 0;
            }
        }

        for (int j = sampleCount[1][0]; j < sampleCount[1][0] + sampleCount[1][1]; j++) {
            if (bPerm[j] < bPool.size()) {
                totalCount++;
                correctCount += bPool.get(bPerm[j]).isCorrect() ? 1 : 0;
            }
        }
        return correctCount / totalCount;
    }

    ArrayList<double[]> genBinned(ArrayList<ArrayList<Double>> psth, int binCount, float binStart, float binSize, float unitFR) {
        ArrayList<double[]> binned = new ArrayList<>();
        for (ArrayList<Double> oneSample : psth) {
            double[] binnedSample = new double[binCount];
            for (Double d : oneSample) {
                int binIdx = (int) ((d - binStart) / binSize);
                if (binIdx >= 0 && binIdx < binCount) {
                    binnedSample[binIdx] += unitFR;
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

//    int sumSampleCount(int[][] samples, int grp) {
//        int count = 0;
//        for (int oneGrp : samples[grp]) {
//            count += oneGrp;
//        }
//        return count;
//    }
    int[] reduceSampleIfNecessary(final int[] sampleCount, int typeTrialCount) {
        int[] local = new int[sampleCount.length];
        int requiredCount = Arrays.stream(sampleCount).sum();
        if (requiredCount > typeTrialCount) {
            if (sampleCount.length < 3 && sampleCount[1] < 2) {//Decoding
                local[0] = typeTrialCount - sampleCount[1];
                local[1] = sampleCount[1];
            } else {
                local[0] = typeTrialCount;
                for (int i = sampleCount.length - 1; i > 0; i--) {
                    local[i] = sampleCount[i] * typeTrialCount / requiredCount;
                    local[0] -= local[i];
                }
            }
            if (local[0] < 1) {
                System.out.println("Not Enough Trial!");
                return null;
            }
            return local;
        } else {
            return sampleCount;
        }
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
    public double[][][] getTrialTS(int a_TrialCount, int b_TrialCount, boolean byLick, boolean isCorrect) {
        double[][] firingTSA = new double[a_TrialCount][];//Time Stamp
        double[][] firingTSB = new double[b_TrialCount][];//Time Stamp
        double[][] trialStartA = new double[a_TrialCount][];
        double[][] trialStartB = new double[b_TrialCount][];

        int trialAIdx = 0;
        int trialBIdx = 0;
//
//        for (Trial trial : trialPool) {
//            if (byLick && trial.isLick()) {
//                trialAIdx++;
//            }
//            if (byLick && !trial.isLick()) {
//                trialBIdx++;
//            }
//        }
//        System.out.println("REC " + trialAIdx + ", " + trialBIdx);
//
//        trialAIdx = 0;
//        trialBIdx = 0;

        for (Trial trial : trialPool) {

            if ((byLick && trial.isLick())
                    || (trial.isCorrect() == isCorrect && trial.sampleOdorIs(EventType.OdorA) && !byLick)) {
                double[] temp = new double[trial.getSpikesList().size()];

                if (trial.getSpikesList().size() > 0) {
                    int idx = 0;
                    for (Double d : trial.getSpikesList()) {
                        temp[idx] = d;
                        idx++;
                    }
                    firingTSA[trialAIdx] = temp;
                } else {
                    temp = new double[]{65535};
                    firingTSA[trialAIdx] = temp;
                }
                trialStartA[trialAIdx] = new double[]{trial.getBaseOnset()};
                trialAIdx++;
            } else if ((byLick && !trial.isLick())
                    || (trial.isCorrect() == isCorrect && trial.sampleOdorIs(EventType.OdorB) && !byLick)) {
                double[] temp = new double[trial.getSpikesList().size()];
                if (trial.getSpikesList().size() > 0) {
                    int idx = 0;
                    for (Double d : trial.getSpikesList()) {
                        temp[idx] = d;
                        idx++;
                    }
                    firingTSB[trialBIdx] = temp;
                } else {
                    temp = new double[]{65535};
                    firingTSB[trialAIdx] = temp;
                }
                trialStartB[trialBIdx] = new double[]{trial.getBaseOnset()};
                trialBIdx++;
            }
        }
//        Collections.sort(firingTS);

        return new double[][][]{firingTSA, firingTSB, trialStartA, trialStartB};
    }

    class GetType {

        final private Processor processor;

        public Processor getProcessor() {
            return processor;
        }

        GetType(String type) {
//            System.out.println("DBG TYPE"+type.toLowerCase());
            switch (type.toLowerCase()) {
                case "matchsample":
                case "matchsamplez":
                case "nonmatchsample":
                case "nonmatchsamplez":
                case "matchsampleerror":
                case "matchsampleerrorz":
                case "nonmatchsampleerror":
                case "nonmatchsampleerrorz":
                case "matchtest":
                case "matchtestz":
                case "nonmatchtest":
                case "nonmatchtestz":
                case "matchtesterror":
                case "matchtesterrorz":
                case "nonmatchtesterror":
                case "nonmatchtesterrorz":
                    processor = new ProcessorMatchCross(type.toLowerCase().endsWith("z"),
                            type.toLowerCase().contains("error"),
                            type.toLowerCase().startsWith("match"),
                            type.toLowerCase().contains("sample"));
                    break;
//                case "odor":
//                case "odorz":
                case "sample":
                case "samplez":
                case "sampleerror":
                case "sampleerrorz":
                case "sampleall":
                case "sampleallz":
                case "sampleincerror":
                    processor = new ProcessorSample(
                            type.toLowerCase().endsWith("z"),
                            type.toLowerCase().contains("error"),
                            type.toLowerCase().contains("all") || type.toLowerCase().contains("incerror"));
                    break;
                case "test":
                case "testz":
                case "testerror":
                case "testerrorz":
                case "testall":
                    processor = new ProcessorTestOdor(type.toLowerCase().endsWith("z"), type.toLowerCase().contains("error"), type.toLowerCase().contains("all"));
                    break;
                case "opsuppress":
                case "opsuppressz":
                case "opsuppressallfr":
                    processor = new Processor4OpSuppress(type.toLowerCase().endsWith("z"));
                    break;
                case "distrgoz":
                case "distrgo":
                case "distrgoincincorr":
                case "distrgoincerror":
                    processor = new ProcessorSamplenDistrZ(EventType.OdorA,
                            type.toLowerCase().endsWith("z"),
                            type.toLowerCase().endsWith("incorr") || type.toLowerCase().endsWith("incerror"));
                    break;
                case "distrnogoz":
                case "distrnogo":
                case "distrnogoincincorr":
                case "distrnogoincerror":
                    processor = new ProcessorSamplenDistrZ(EventType.OdorB,
                            type.toLowerCase().endsWith("z"),
                            type.toLowerCase().endsWith("incorr") || type.toLowerCase().endsWith("incerror"));
                    break;
                case "distrnonez":
                case "distrnone":
                case "distrnoneincincorr":
                case "distrnoneincerror":
                    processor = new ProcessorSamplenDistrZ(EventType.NONE,
                            type.toLowerCase().endsWith("z"),
                            type.toLowerCase().endsWith("incorr") || type.toLowerCase().endsWith("incerror"));
                    break;
                case "match":
                case "matchincincorr":
                case "matchincincorrz":
                case "matchz":
                case "matcherrorz":
                case "matcherror":
                    processor = new ProcessorMatch(type.toLowerCase().contains("incorr"), type.toLowerCase().endsWith("z"), type.toLowerCase().contains("error"));
                    break;
                case "shuffle":
                case "shuffleall":
                    processor = new ProcessorShuffle(type.toLowerCase().endsWith("z"), !type.toLowerCase().contains("test"), type.toLowerCase().contains("all"));
                    break;
                case "lick":
                case "lickall":
                case "lickerror":
                case "lickz":
                case "lickallz":
                    processor = new ProcessorLick(type.toLowerCase().contains("all"), type.toLowerCase().contains("error"), type.toLowerCase().endsWith("z"));
                    break;
                case "everytrial":
                    processor = new ProcessorEveryTrial(type.toLowerCase().endsWith("z"), !type.toLowerCase().contains("test"), type.toLowerCase().contains("all"));
                    break;
                default:
                    System.out.println(type + ": Unknown Processor Type");
                    throw new IllegalArgumentException("Unknown Processor Type");
            }
        }
    }

    public double[] getCriteria() {
        return new double[]{avgFR, this.ISIratio};
    }

}
