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
public class ProcessorLick extends Processor {

    final boolean incError;
    final boolean isError;
    final boolean isZ;

    public ProcessorLick(boolean incIncorr, boolean isError, boolean isZ) {
        this.incError = incIncorr;
        this.isError = isError;
        this.isZ = isZ;
    }

    @Override
    void fillPoolsByType(ArrayList<Trial> trialPool) {
        for (Trial trial : trialPool) {
            if (this.incError || (trial.isCorrect() && !this.isError)) {
                if (trial.isMatch()) {
                    typeBPool.add(trial);
                } else {
                    typeAPool.add(trial);
                }
            } else if (this.isError && !trial.isCorrect()) {
                if (trial.isMatch()) {
                    typeAPool.add(trial);
                } else {
                    typeBPool.add(trial);
                }
            }
        }
    }

    @Override
    double[] getBaselineStats(ArrayList<Trial> trialPool) {
        ProcessorTestOdor pr = new ProcessorTestOdor(isZ, isError,incError);
        return pr.getBaselineStats(trialPool);
    }
}
