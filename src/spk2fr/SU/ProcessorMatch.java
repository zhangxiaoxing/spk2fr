/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.SU;

import java.util.ArrayList;
import spk2fr.EventType;
import spk2fr.FP.FileParser;
import spk2fr.MiceDay;

/**
 *
 * @author zx
 */
public class ProcessorMatch extends Processor {

    final boolean incIncorrect;
    final boolean z;
    final boolean error;

    public ProcessorMatch(boolean incIncorrect, boolean z, boolean error) {
        this.incIncorrect = incIncorrect;
        this.z = z;
        this.error = error;
    }

    @Override
    void fillPoolsByType(final ArrayList<Trial> trialPool) {
        if (this.incIncorrect) {
            for (Trial trial : trialPool) {
                if (!trial.isMatch()) {
                    typeAPool.add(trial);
                } else if (trial.isMatch()) {
                    typeBPool.add(trial);
                }
            }
        } else if (this.error) {
            for (Trial trial : trialPool) {
                if ((!trial.isMatch()) && !trial.isCorrect()) {
                    typeAPool.add(trial);
                } else if (trial.isMatch() && !trial.isCorrect()) {
                    typeBPool.add(trial);
                }
            }
        } else {
            for (Trial trial : trialPool) {
                if ((!trial.isMatch()) && trial.isCorrect()) {
                    typeAPool.add(trial);
                } else if (trial.isMatch() && trial.isCorrect()) {
                    typeBPool.add(trial);
                }
            }
        }
    }

    @Override
    int getTypeATrialNum(MiceDay md) {
        return md.countCorrectTrialByMatch(EventType.MATCH, !this.incIncorrect);
    }

    @Override
    int getTypeBTrialNum(MiceDay md) {
        return md.countCorrectTrialByMatch(EventType.NONMATCH, !this.incIncorrect);
    }

    @Override
    double[] getBaselineStats(ArrayList<Trial> trialPool, int totalTrialCount) {
        if (this.z) {
            double[] baselineTSCount = new double[totalTrialCount];
            int trialIdx = 0;
            boolean allZero = true;

            for (Trial trial : trialPool) {
                double testOnset = trial.getLength() - FileParser.rewardBias - FileParser.baseBias - 2;
                if ((trial.isCorrect() && !this.error) || (this.error && !trial.isCorrect())) {
                    for (Double d : trial.getSpikesList()) {
                        if (d < testOnset) {
                            if (d >= testOnset - 1) {
                                baselineTSCount[trialIdx]++;
                                allZero = false;
                            }
                        } else {
                            break;
                        }
                    }
                    trialIdx++;
                }
            }

            if (allZero) {
                baselineTSCount[0] = 1;
            }
            return convert2Stats(baselineTSCount);
        } else {
            return new double[]{0, 1};
        }
    }
}
