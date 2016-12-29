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
    private double recordingLength;

    public MiceDay() {
        tetrodes = new HashMap<>();
    }

    public void setRecordingLength(double recordingLength) {
        this.recordingLength = recordingLength;
    }

    public boolean isWellTrained() {
        ArrayList<Boolean> results = new ArrayList<>();
        for (ArrayList<EventType[]> session : behaviorSessions) {
            for (EventType[] evt : session) {
                results.add(evt[2] == EventType.Hit || evt[2] == EventType.CorrectRejection);
                int sumTrial = results.size();
                if (sumTrial >= 40) {
                    int count = 0;
                    for (int i = sumTrial - 1; i > sumTrial - 40; i--) {
                        count += results.get(i) ? 1 : 0;
                        if (count > 31) {
                            return true;
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
    
    public Collection<Integer> getTetrodeKeys() {
        return tetrodes.keySet();
    }

//    public void removeSession(int sessionIdx) {
//        for (Tetrode tetrode : tetrodes.values()) {
//            tetrode.removeSession(sessionIdx);
//        }
//
//    }
    public int countCorrectTrialByOdor(int odorPosition, EventType odor) {
        int count = 0;
        for (ArrayList<EventType[]> session : behaviorSessions) {
            for (EventType[] trial : session) {

                if (trial[odorPosition] == odor && (trial[2] == EventType.Hit || trial[2] == EventType.CorrectRejection)) {
                    count++;
                }
            }
        }
        return count;
    }

    public int countCorrectTrialByMatch(EventType isMatch, boolean correct) {
        int count = 0;
        for (ArrayList<EventType[]> session : behaviorSessions) {
            for (EventType[] trial : session) {
                boolean needCorrect = correct ? (trial[2] == EventType.Hit || trial[2] == EventType.CorrectRejection) : true;
                if ((trial[0] == trial[1]) == (isMatch == EventType.MATCH)
                        && needCorrect) {
                    count++;
                }
            }
        }
        return count;
    }

    public int[] countByCorrect(EventType odor) {    //{correct, incorrect}
        int countCorrect = 0;
        int countIncorrect = 0;
        for (ArrayList<EventType[]> session : behaviorSessions) {
            for (EventType[] trial : session) {
                if (trial[0] == odor) {
                    if (trial[2] == EventType.Hit || trial[2] == EventType.CorrectRejection) {
                        countCorrect++;
                    } else {
                        countIncorrect++;
                    }
                }
            }
        }
        return new int[]{countCorrect, countIncorrect};
    }

    public void setBehaviorSessions(ArrayList<ArrayList<EventType[]>> behaviorSessions) {
        this.behaviorSessions = behaviorSessions;
    }

    public ArrayList<ArrayList<EventType[]>> getBehaviorSessions() {
        return behaviorSessions;
    }

    public MiceDay removeSparseFiringUnits(ClassifyType leastFR, double refracRatio) {
        for (Tetrode t : this.getTetrodes()) {
            t.removeSparseFiringUnits(leastFR,
                    this.countByCorrect(EventType.OdorA)[0] + this.countByCorrect(EventType.OdorA)[1]
                    + this.countByCorrect(EventType.OdorB)[0] + this.countByCorrect(EventType.OdorB)[1], refracRatio, recordingLength);
        }
        return this;
    }

}
