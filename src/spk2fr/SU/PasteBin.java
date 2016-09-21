/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.SU;

import java.util.ArrayList;
import org.apache.commons.math3.stat.StatUtils;
import spk2fr.ClassifyType;
import spk2fr.EventType;

/**
 *
 * @author Libra
 */
public class PasteBin {
    ArrayList<Trial> trialPool;

    private double[] getBaselineStats(final ClassifyType cType, final ArrayList<Trial> trialPool, int totalTrialCount) {
        if (cType == ClassifyType.BY_ODOR_WITHIN_MEAN_TRIAL_Z
                || cType == ClassifyType.BY_CORRECT_OdorA_Z || cType == ClassifyType.BY_CORRECT_OdorB_Z) {

//        System.out.println(totalTrialCount);
            double[] baselineTSCount = new double[totalTrialCount];
            int trialIdx = 0;
            boolean allZero = true;
            switch (cType) {

                case BY_CORRECT_OdorA_Z:
                    for (Trial trial : trialPool) {
                        if (trial.sampleOdorIs(EventType.OdorA)) {
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
                        if (trial.sampleOdorIs(EventType.OdorB)) {
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

        } else {
            return new double[]{0, 1};
        }
    }

    void fillPoolsByType(final ClassifyType cType, final ArrayList<Trial> typeAPool, final ArrayList<Trial> typeBPool) {

        switch (cType) {
            case BY_ODOR:
            case BY_ODOR_WITHIN_MEAN_TRIAL:
            case BY_ODOR_WITHIN_MEAN_TRIAL_Z:
                for (Trial trial : this.trialPool) {
                    if (trial.sampleOdorIs(EventType.OdorA) && trial.isCorrect()) {
                        typeAPool.add(trial);
                    } else if (trial.sampleOdorIs(EventType.OdorB) && trial.isCorrect()) {
                        typeBPool.add(trial);
                    }
                }
                break;

            case BY_SECOND_ODOR:
                for (Trial trial : this.trialPool) {
                    if (trial.testOdorIs(EventType.OdorA) && trial.isCorrect()) {
                        typeAPool.add(trial);
                    } else if (trial.testOdorIs(EventType.OdorB) && trial.isCorrect()) {
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
                    if (trial.sampleOdorIs(EventType.OdorA) && trial.isCorrect()) {
                        typeAPool.add(trial);
                    } else if (trial.sampleOdorIs(EventType.OdorA) && !trial.isCorrect()) {
                        typeBPool.add(trial);
                    }
                }
                break;
            case BY_CORRECT_OdorB:
            case BY_CORRECT_OdorB_Z:
                for (Trial trial : this.trialPool) {
                    if (trial.sampleOdorIs(EventType.OdorB) && trial.isCorrect()) {
                        typeAPool.add(trial);
                    } else if (trial.sampleOdorIs(EventType.OdorB) && !trial.isCorrect()) {
                        typeBPool.add(trial);
                    }
                }
                break;
            case ALL_ODORA:
                for (Trial trial : this.trialPool) {
                    if (trial.sampleOdorIs(EventType.OdorA)) {
                        typeAPool.add(trial);
                        typeBPool.add(trial);
                    }
                }
                break;
            case ALL_ODORB:
                for (Trial trial : this.trialPool) {
                    if (trial.sampleOdorIs(EventType.OdorB)) {
                        typeAPool.add(trial);
                        typeBPool.add(trial);
                    }
                }
                break;
        }
    }
//    
//    
//     void GetType(final spk2fr.MiceDay miceDay, String type) {
////            int[] counts;
//            switch (type.toLowerCase()) {
//                case "odor":
//                    processor = new Processor4Odor();
//                    typeATrials = miceDay.countCorrectTrialByOdor(0, EventType.OdorA);
//                    typeBTrials = miceDay.countCorrectTrialByOdor(0, EventType.OdorB);
//                    break;
////                case "secondodor":
////                    processor = ClassifyType.BY_SECOND_ODOR;
////                    typeATrials = miceDay.countCorrectTrialByOdor(1, EventType.OdorA);
////                    typeBTrials = miceDay.countCorrectTrialByOdor(1, EventType.OdorB);
////                    break;
////                case "odorwithinmeantrial":
////                    processor = ClassifyType.BY_ODOR_WITHIN_MEAN_TRIAL;
////                    typeATrials = miceDay.countCorrectTrialByOdor(0, EventType.OdorA);
////                    typeBTrials = miceDay.countCorrectTrialByOdor(0, EventType.OdorB);
////                    break;
//                case "odorz":
//                    processor = new Processor4OdorZ();
//                    typeATrials = miceDay.countCorrectTrialByOdor(0, EventType.OdorA);
//                    typeBTrials = miceDay.countCorrectTrialByOdor(0, EventType.OdorB);
//                    break;
////                case "odorwithinmeantrialz":
////                    processor = ClassifyType.BY_ODOR_WITHIN_MEAN_TRIAL_Z;
////                    typeATrials = miceDay.countCorrectTrialByOdor(0, EventType.OdorA);
////                    typeBTrials = miceDay.countCorrectTrialByOdor(0, EventType.OdorB);
////                    break;
////                case "correcta":
////                    counts = miceDay.countByCorrect(EventType.OdorA);
////                    typeATrials = counts[0];
////                    typeBTrials = counts[1];
////                    System.out.println("A ," + typeATrials + "\t" + typeBTrials);
////                    processor = ClassifyType.BY_CORRECT_OdorA;
////                    break;
////                case "alla":
////                    counts = miceDay.countByCorrect(EventType.OdorA);
////                    typeATrials = counts[0] + counts[1];
////                    typeBTrials = counts[0] + counts[1];
////                    processor = ClassifyType.ALL_ODORA;
////                    break;
////                case "correctza":
////                    counts = miceDay.countByCorrect(EventType.OdorA);
////                    typeATrials = counts[0];
////                    typeBTrials = counts[1];
////                    processor = ClassifyType.BY_CORRECT_OdorA_Z;
////                    break;
////                case "correctb":
////                    counts = miceDay.countByCorrect(EventType.OdorB);
////                    typeATrials = counts[0];
////                    typeBTrials = counts[1];
////                    System.out.println("B ," + typeATrials + "\t" + typeBTrials);
////                    processor = ClassifyType.BY_CORRECT_OdorB;
////                    break;
////                case "allb":
////                    counts = miceDay.countByCorrect(EventType.OdorB);
////                    typeATrials = counts[0] + counts[1];
////                    typeBTrials = counts[0] + counts[1];
////                    processor = ClassifyType.ALL_ODORB;
////                    break;
////                case "correctzb":
////                    counts = miceDay.countByCorrect(EventType.OdorB);
////                    typeATrials = counts[0];
////                    typeBTrials = counts[1];
////                    processor = ClassifyType.BY_CORRECT_OdorB_Z;
////                    break;
////                case "match":
////                    processor = ClassifyType.BY_MATCH;
////                    typeATrials = miceDay.countCorrectTrialByMatch(EventType.MATCH, true);
////                    typeBTrials = miceDay.countCorrectTrialByMatch(EventType.NONMATCH, true);
////                    break;
////                case "matchincincorr":
////                    processor = ClassifyType.BY_MATCH;
////                    typeATrials = miceDay.countCorrectTrialByMatch(EventType.MATCH, false);
////                    typeBTrials = miceDay.countCorrectTrialByMatch(EventType.NONMATCH, false);
////                    break;
//                case "opsuppress":
//                    processor = new Processor4OpSuppress();
//                    typeATrials = miceDay.getBehaviorSessions().get(0).size();
//                    typeBTrials = miceDay.getBehaviorSessions().get(0).size();
//                    break;
//                default:
//                    throw new IllegalArgumentException("Unknown Processor Type");
//            }
//        }
}
