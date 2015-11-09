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
    private ArrayList<ArrayList<EventType[]>> behaviorSessions;

    public MiceDay() {
        tetrodes = new HashMap<>();
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
    
    

// 4 removal    
//    public Double[][][] getFiringTSByOdor(int odor) { //time stamp
//        ArrayList<Double[][]> allUnits = new ArrayList<>();
//        for (Tetrode tetrode : tetrodes.values()) {
//            for (SingleUnit unit : tetrode.getUnits()) {
//                allUnits.add(unit.getFiringTimesByOdor(odor));
//            }
//        }
//        return allUnits.toArray(new Double[allUnits.size()][][]);
//    }

    public void setBehaviorSessions(ArrayList<ArrayList<EventType[]>> behaviorSessions) {
        this.behaviorSessions = behaviorSessions;
    }
}
