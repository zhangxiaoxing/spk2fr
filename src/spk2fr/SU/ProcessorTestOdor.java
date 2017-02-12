/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.SU;

import java.util.ArrayList;
import java.util.LinkedList;
import spk2fr.EventType;
import spk2fr.FP.FileParser;

/**
 *
 * @author Libra
 */
public class ProcessorTestOdor extends ProcessorSample {

    public ProcessorTestOdor(boolean isZ, boolean isError) {
        super(isZ, isError, false);
    }

    @Override
    public double[] getBaselineStats(ArrayList<Trial> trialPool) {
        if (this.isZ) {
            LinkedList<Integer> baselineTSCount = new LinkedList<>();
            int counter;
            for (Trial trial : trialPool) {
                double testOnset = trial.getLength() - FileParser.rewardBias - FileParser.baseBias - 2;
                counter = 0;
                for (Double d : trial.getSpikesList()) {
                    if (d < testOnset) {
                        if (d >= testOnset - 1) {
                            counter++;
                        }
                    } else {
                        break;
                    }
                }
                baselineTSCount.addLast(counter);
            }

            double[] base = new double[baselineTSCount.size()];
            int cIdx = 0;
            for (Integer t : baselineTSCount) {
                base[cIdx++] = t;
            }
            return convert2Stats(base);
        } else {
            return new double[]{0, 1};
        }
    }

    @Override
    void fillPoolsByType(final ArrayList<Trial> trialPool) {
        if (this.isError) {
            for (Trial trial : trialPool) {
                if (trial.testOdorIs(EventType.OdorA) && !trial.isCorrect()) {
                    typeAPool.add(trial);
                } else if (trial.testOdorIs(EventType.OdorB) && !trial.isCorrect()) {
                    typeBPool.add(trial);
                }
            }
        } else {
            for (Trial trial : trialPool) {
                if (trial.testOdorIs(EventType.OdorA) && trial.isCorrect()) {
                    typeAPool.add(trial);
                } else if (trial.testOdorIs(EventType.OdorB) && trial.isCorrect()) {
                    typeBPool.add(trial);
                }
            }
        }
    }
}
