/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.SU;

import java.util.ArrayList;
import spk2fr.MiceDay;

/**
 *
 * @author Libra
 */
public class Processor4OpSuppress extends Processor {

    final boolean z;

    public Processor4OpSuppress(boolean z) {
        this.z = z;
    }

    @Override
    double[] getBaselineStats(ArrayList<Trial> trialPool) {
        if (!z) {
            return new double[]{0, 1};
        }

        double[] baselineTSCount = new double[trialPool.size()];
        int trialIdx = 0;
        for (Trial trial : trialPool) {
            for (Double d : trial.getSpikesList()) {
                if (d < 0 && d>=-1) {
                    baselineTSCount[trialIdx]++;
                } else {
                    break;
                }
            }
            trialIdx++;
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

//    @Override
//    int getTypeATrialNum(MiceDay md) {
//        return md.getBehaviorSessions().get(0).size();
//    }
//
//    @Override
//    int getTypeBTrialNum(MiceDay md) {
//        return md.getBehaviorSessions().get(0).size();
//    }

}
