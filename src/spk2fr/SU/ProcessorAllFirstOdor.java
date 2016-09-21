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
public abstract class ProcessorAllFirstOdor extends Processor {

    @Override
    void fillPoolsByType(final ArrayList<Trial> trialPool) {
        for (Trial trial : trialPool) {
            if (trial.sampleOdorIs(EventType.OdorA) && trial.isCorrect()) {
                typeAPool.add(trial);
            } else if (trial.sampleOdorIs(EventType.OdorB) && trial.isCorrect()) {
                typeBPool.add(trial);
            }
        }
    }

   

    @Override
    int getTypeATrialNum(MiceDay md) {
        return md.countCorrectTrialByOdor(0, EventType.OdorA);
    }

    @Override
    int getTypeBTrialNum(MiceDay md) {
        return md.countCorrectTrialByOdor(0, EventType.OdorB);
    }

}
