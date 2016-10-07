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
 * @author Libra
 */
public class ProcessorSamplenDistrZ extends Processor {

    final private EventType distr;
    final private boolean Z;

    public ProcessorSamplenDistrZ() {
        this.distr = EventType.NONE;
        this.Z = true;
    }

    public ProcessorSamplenDistrZ(EventType distr, boolean Z) {
        this.distr = distr;
        this.Z = Z;
    }

    @Override
    void fillPoolsByType(final ArrayList<Trial> trialPool) {
        for (Trial trial : trialPool) {
            if (trial.sampleOdorIs(EventType.OdorA) && trial.isCorrect() && ((DualTrial) trial).getDistrOdor() == distr) {
                typeAPool.add(trial);
            } else if (trial.sampleOdorIs(EventType.OdorB) && trial.isCorrect() && ((DualTrial) trial).getDistrOdor() == distr) {
                typeBPool.add(trial);
            }
        }
    }

    @Override
    int getTypeATrialNum(MiceDay md) {
        int counter = 0;
        for (ArrayList<EventType[]> session : md.getBehaviorSessions()) {
            for (EventType[] trial : session) {
                if (trial[0] == EventType.OdorA && (trial[2] == EventType.Hit || trial[2] == EventType.CorrectRejection) && trial[3] == distr) {
                    counter++;
                }
            }
        }
        return counter;
    }

    @Override
    int getTypeBTrialNum(MiceDay md) {
        int counter = 0;
        for (ArrayList<EventType[]> session : md.getBehaviorSessions()) {
            for (EventType[] trial : session) {
                if (trial[0] == EventType.OdorB && (trial[2] == EventType.Hit || trial[2] == EventType.CorrectRejection) && trial[3] == distr) {
                    counter++;
                }
            }
        }
        return counter;
    }

    @Override
    double[] getBaselineStats(ArrayList<Trial> trialPool, int totalTrialCount) {
        if (this.Z) {
            Processor4OdorZ pr = new Processor4OdorZ();
            return pr.getBaselineStats(trialPool, totalTrialCount);
        } else {
            return new double[]{0, 1};
        }
    }

}
