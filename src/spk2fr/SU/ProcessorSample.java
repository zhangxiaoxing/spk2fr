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
public class ProcessorSample extends Processor {

    final boolean isZ;
    final boolean isError;

    public ProcessorSample(boolean isZ, boolean isError) {
        this.isZ = isZ;
        this.isError = isError;
    }

    @Override
    void fillPoolsByType(final ArrayList<Trial> trialPool) {
        if (this.isError) {
            for (Trial trial : trialPool) {
                if (trial.sampleOdorIs(EventType.OdorA) && !trial.isCorrect()) {
                    typeAPool.add(trial);
                } else if (trial.sampleOdorIs(EventType.OdorB) && !trial.isCorrect()) {
                    typeBPool.add(trial);
                }
            }
        } else {
            for (Trial trial : trialPool) {
                if (trial.sampleOdorIs(EventType.OdorA) && trial.isCorrect()) {
                    typeAPool.add(trial);
                } else if (trial.sampleOdorIs(EventType.OdorB) && trial.isCorrect()) {
                    typeBPool.add(trial);
                }
            }
        }
    }

    @Override
    public double[] getBaselineStats(final ArrayList<Trial> trialPool) {
        if (this.isZ) {

            double[] baselineTSCount = new double[trialPool.size()];
            int trialIdx = 0;

            for (Trial trial : trialPool) {
                for (Double d : trial.getSpikesList()) {
                    if (d < 0d) {
                        if (d >= -1) {
                            baselineTSCount[trialIdx]++;
                        }
                    } else {
                        break;
                    }
                }
                trialIdx++;
            }

            return convert2Stats(baselineTSCount);
        } else {
            return new double[]{0, 1};
        }
    }

}
