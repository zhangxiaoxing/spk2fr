/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.SU;

import java.util.ArrayList;

/**
 *
 * @author zx
 */
public class ProcessorWOClassify {

    private ArrayList<Trial> trialPool = new ArrayList<>();
    final private boolean isZ;
    final private boolean isError;
    final private boolean incError;

    public ProcessorWOClassify(boolean isZ, boolean isError, boolean incError) {
        this.isZ = isZ;
        this.isError = isError;
        this.incError = incError;
    }

    public ArrayList<Trial> getTrialPool() {
        return trialPool;
    }

    double[] convert2Stats(double[] baselineTSCount) {
//        double[] stats = new double[]{StatUtils.mean(baselineTSCount), Math.sqrt(StatUtils.variance(baselineTSCount))};
//        if (stats[0] == 0 && stats[1] == 0) {
//            System.out.println("Zero baseline.");
//            baselineTSCount[0] = 1;
//            stats = new double[]{StatUtils.mean(baselineTSCount), Math.sqrt(StatUtils.variance(baselineTSCount))};
//        }
//        return stats;
        return Processor.convert2Stats(baselineTSCount);
    }

    double[] getBaselineStats(final ArrayList<Trial> trialPool) {
        ProcessorTestOdor pr = new ProcessorTestOdor(isZ, isError, incError);
        return pr.getBaselineStats(trialPool);
    }

    void fillPoolsByType(final ArrayList<Trial> trialPool) {
        if (this.incError) {
            this.trialPool = trialPool;
        } else if (this.isError) {
            for (Trial trial : trialPool) {
                if (!trial.isCorrect()) {
                    this.trialPool.add(trial);
                }
            }
        } else {
            for (Trial trial : trialPool) {
                if (trial.isCorrect()) {
                    this.trialPool.add(trial);
                }
            }
        }
    }

}
