/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.SU;

import java.util.ArrayList;
import spk2fr.EventType;

/**
 *
 * @author Libra
 */
public class Processor4OdorZ extends ProcessorAllFirstOdor {

    @Override
    public double[] getBaselineStats(final ArrayList<Trial> trialPool, int totalTrialCount) {
        double[] baselineTSCount = new double[totalTrialCount];
        int trialIdx = 0;
        boolean allZero = true;

        for (Trial trial : trialPool) {
//            if (trial.isCorrect()) {
            for (Double d : trial.getSpikesList()) {
                if (d < 0d) {
                    if (d >= -1) {
                        baselineTSCount[trialIdx]++;
                        allZero = false;
                    }
                } else {
                    break;
                }
            }
            trialIdx++;
//            }
        }

        if (allZero) {
            baselineTSCount[0] = 1;
        }
        return convert2Stats(baselineTSCount);
    }

}
