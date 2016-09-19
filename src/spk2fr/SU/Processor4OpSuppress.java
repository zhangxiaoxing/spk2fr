/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.SU;

import java.util.ArrayList;

/**
 *
 * @author Libra
 */
public class Processor4OpSuppress extends Processor {

    @Override
    double[] getBaselineStats(ArrayList<Trial> trialPool, int totalTrialCount) {
        double[] baselineTSCount = new double[totalTrialCount];
        int trialIdx = 0;
        boolean allZero = true;
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
        if (allZero) {
            baselineTSCount[0] = 1;
        }
        return convert2Stats(baselineTSCount);
    }

    @Override
    void fillPoolsByType(ArrayList<Trial> trialPool) {
        for (Trial trial : trialPool) {
            typeAPool.add(trial);
            typeBPool.add(trial);
        }
    }

}
