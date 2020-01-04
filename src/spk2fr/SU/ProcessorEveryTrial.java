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
public class ProcessorEveryTrial extends Processor {

    boolean isZ;
    boolean incError;

    public ProcessorEveryTrial(boolean isZ, boolean incIncorr) {
        this.isZ = isZ;
        this.incError = incIncorr;

    }

    @Override
    void fillPoolsByType(ArrayList<Trial> trialPool) {
        if (this.incError) {
            for (Trial trial : trialPool) {
                typeAPool.add(trial);
            }
        } else {
            for (Trial trial : trialPool) {
                if (trial.isCorrect()) {
                    typeAPool.add(trial);
                }
            }
        }
    }

    @Override
    double[] getBaselineStats(ArrayList<Trial> trialPool) {
        ProcessorSample ps = new ProcessorSample(isZ, false, incError);
        return ps.getBaselineStats(trialPool);
    }
}
