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

//    @Override
//    int getTypeATrialNum(MiceDay md) {
//        if (this.incIncorrect) {
//            return md.countTrialByMatch(EventType.MATCH, MiceDay.CorrectType.ALL);
//        } else if (this.error) {
//            return md.countTrialByMatch(EventType.MATCH, MiceDay.CorrectType.ERROR);
//        } else {
//            return md.countTrialByMatch(EventType.MATCH, MiceDay.CorrectType.CORRECT);
//        }
//    }
//
//    @Override
//    int getTypeBTrialNum(MiceDay md) {
//        if (this.incIncorrect) {
//            return md.countTrialByMatch(EventType.MATCH, MiceDay.CorrectType.ALL);
//        } else if (this.error) {
//            return md.countTrialByMatch(EventType.MATCH, MiceDay.CorrectType.ERROR);
//        } else {
//            return md.countTrialByMatch(EventType.MATCH, MiceDay.CorrectType.CORRECT);
//        }
//    }
    @Override
    double[] getBaselineStats(ArrayList<Trial> trialPool) {
        ProcessorTestOdor pr = new ProcessorTestOdor(z, error);
        return pr.getBaselineStats(trialPool);
    }
}
