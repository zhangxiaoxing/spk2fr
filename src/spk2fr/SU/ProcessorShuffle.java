/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.SU;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import spk2fr.EventType;

/**
 *
 * @author zx
 */
public class ProcessorShuffle extends Processor {

    boolean isZ;
    boolean incError;
    boolean isSample;

    public ProcessorShuffle(boolean isZ, boolean isSample, boolean incIncorr) {
        this.isZ = isZ;
        this.incError = incIncorr;
        this.isSample = isSample;
    }

    @Override
    void fillPoolsByType(ArrayList<Trial> trialPool) {
        if (this.incError) {
            for (Trial trial : trialPool) {
                if (ThreadLocalRandom.current().nextBoolean()) {
                    typeAPool.add(trial);
                } else {
                    typeBPool.add(trial);
                }
            }
        } else {
            for (Trial trial : trialPool) {
                if (ThreadLocalRandom.current().nextBoolean() && trial.isCorrect()) {
                    typeAPool.add(trial);
                } else if (trial.isCorrect()) {
                    typeBPool.add(trial);
                }
            }
        }
    }

    @Override
    double[] getBaselineStats(ArrayList<Trial> trialPool) {
        if (this.isSample) {
            ProcessorSample ps = new ProcessorSample(isZ, false, incError);
            return ps.getBaselineStats(trialPool);
        } else {
            ProcessorTestOdor pr = new ProcessorTestOdor(isZ, false, incError);
            return pr.getBaselineStats(trialPool);
        }
    }
}
