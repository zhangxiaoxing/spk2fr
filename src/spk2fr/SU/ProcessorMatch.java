/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.SU;

import java.util.ArrayList;
import spk2fr.EventType;
import spk2fr.MiceDay;

/**
 *
 * @author zx
 */
public class ProcessorMatch extends Processor {

    final boolean incIncorrect;

    public ProcessorMatch(boolean incIncorrect) {
        this.incIncorrect = incIncorrect;
    }

    @Override
    void fillPoolsByType(final ArrayList<Trial> trialPool) {
        if (this.incIncorrect) {
            for (Trial trial : trialPool) {
                if (trial.isMatch()) {
                    typeAPool.add(trial);
                } else if ((!trial.isMatch())) {
                    typeBPool.add(trial);
                }
            }
        } else {
            for (Trial trial : trialPool) {
                if (trial.isMatch() && trial.isCorrect()) {
                    typeAPool.add(trial);
                } else if ((!trial.isMatch()) && trial.isCorrect()) {
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
        return new double[]{0, 1};
    }
}
