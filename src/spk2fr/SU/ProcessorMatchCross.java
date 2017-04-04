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
 * @author zx
 */
public class ProcessorMatchCross extends Processor {

    final boolean isZ;// or fr
    final boolean isError;// or correct
    final boolean isMatch;// or nonmatch
    final boolean isSample;//or test

    public ProcessorMatchCross(boolean z, boolean error, boolean match, boolean isSample) {
        this.isZ = z;
        this.isError = error;
        this.isMatch = match;
        this.isSample = isSample;
    }

    @Override
    void fillPoolsByType(ArrayList<Trial> trialPool) {
        if (this.isSample) {
            for (Trial t : subpool(trialPool, this.isError, this.isMatch)) {
                if (t.sampleOdorIs(EventType.OdorA)) {
                    typeAPool.add(t);
                } else if (t.sampleOdorIs(EventType.OdorB)) {
                    typeBPool.add(t);
                }
            }
        } else {
            for (Trial t : subpool(trialPool, this.isError, this.isMatch)) {
                if (t.testOdorIs(EventType.OdorA)) {
                    typeAPool.add(t);
                } else if (t.testOdorIs(EventType.OdorB)) {
                    typeBPool.add(t);
                }
            }
        }
    }

    ArrayList<Trial> subpool(ArrayList<Trial> fullpool, boolean error, boolean match) {
        ArrayList<Trial> subpool = new ArrayList<>();
        for (Trial t : fullpool) {
            if ((t.isCorrect() && !error)
                    || (error && !t.isCorrect())) {
                if ((t.isMatch() && match)
                        || ((!match) && !t.isMatch())) {
                    subpool.add(t);
                }
            }
        }
        return subpool;
    }

    @Override
    double[] getBaselineStats(ArrayList<Trial> trialPool) {
        if (this.isSample) {
            ProcessorSample ps = new ProcessorSample(isZ, isError,false);
            return ps.getBaselineStats(trialPool);
        } else {
            ProcessorTestOdor pr = new ProcessorTestOdor(isZ, isError,false);
            return pr.getBaselineStats(trialPool);
        }
    }

}
