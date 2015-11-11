/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author Libra
 */
public class MiceDay {

    final private HashMap<Integer, Tetrode> tetrodes;
    private ArrayList<ArrayList<EventType[]>> behaviorSessions; //EventType[] behaviorTrial = {firstOdor, secondOdor, response};

    public MiceDay() {
        tetrodes = new HashMap<>();
    }

    public boolean isWellTrained() {
        ArrayList<Boolean> results = new ArrayList<>();
        for (ArrayList<EventType[]> session : behaviorSessions) {
            for (EventType[] evt : session) {
                if (evt[2] == EventType.Hit || evt[2] == EventType.CorrectRejection) {
                    results.add(Boolean.TRUE);
                } else {
                    results.add(Boolean.FALSE);
                }
                int sumTrial = results.size();
                if (sumTrial >= 40) {
                    int count = 0;
                    for (int i = sumTrial - 1; i > sumTrial - 40; i--) {
                        if (results.get(i)) {
                            count++;
                            if (count > 31) {
                                return true;
                            }
                        }
                    }
                }
            }

        }
        return false;
    }

    public Tetrode getTetrode(int idx) {
        if (!tetrodes.containsKey(idx)) {
            tetrodes.put(idx, new Tetrode());
        }
        return tetrodes.get(idx);
    }

    public Collection<Tetrode> getTetrodes() {
        return tetrodes.values();
    }

    public void removeSession(int sessionIdx) {
        for (Tetrode tetrode : tetrodes.values()) {
            tetrode.removeSession(sessionIdx);
        }

    }

    int countCorrectTrialByFirstOdor(int odor) {
        int count = 0;
        EventType targetOdor = odor == 0 ? EventType.OdorA : EventType.OdorB;
        for (ArrayList<EventType[]> session : behaviorSessions) {
            for (EventType[] trial : session) {
                if (trial[0] == targetOdor && (trial[2] == EventType.Hit || trial[2] == EventType.CorrectRejection)) {
                    count++;
                }
            }
        }
        return count;
    }

    public void setBehaviorSessions(ArrayList<ArrayList<EventType[]>> behaviorSessions) {
        this.behaviorSessions = behaviorSessions;
    }
}