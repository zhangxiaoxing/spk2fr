/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author Libra
 */
public class FileParser {

    private int spkIdx = 0;
    

    class spkSorter implements Comparator<double[]> {

        @Override
        public int compare(double[] o1, double[] o2) {

            return o1[2] < o2[2] ? -1 : 1;
        }
    }



    MiceDay processFile(double[][] evts, double[][] spk) {
        ArrayList<ArrayList<EventType[]>> behaviorSessions = new ArrayList<>();
        MiceDay miceDay = new MiceDay();
        Arrays.sort(spk, new spkSorter());
        spkIdx = 0;
        System.gc();
        double baselineStart = 0;
        double secondOdorEnd = 0;
        EventType[] responses = {EventType.FalseAlarm, EventType.CorrectRejection, EventType.Miss, EventType.Hit};
        EventType[] odors = {EventType.OdorA, EventType.OdorB};
        ArrayList<EventType[]> behaviorSession = new ArrayList<>();
        EventType firstOdor = EventType.unknown;
        EventType secondOdor = EventType.unknown;
        EventType response;

        int sessionIdx = 0;
        for (double[] evtDouble : evts) {
            int[] evt = new int[4];
            evt[2] = (int) Math.round(evtDouble[2]);
            evt[3] = (int) Math.round(evtDouble[3]);
            switch (evt[2]) {
                case 1:
                    if (evt[3] == 1) {
                        break;
                    }
                    evt[2] = 61;
                    evt[3] = 0;
                case 61:
                    switch (evt[3]) {
                        case 0:
//                            if (behaviorSession.size() < 20) {
//                                miceDay.removeSession(sessionIdx);
//                            } else {
                                behaviorSessions.add(behaviorSession);
                                behaviorSession = new ArrayList<>();
//                            }
                            sessionIdx++;
                            break;
                    }
                    break;
                case 4:
                case 5:
                case 6:
                case 7:
                    response = responses[evt[2] - 4];
                    if (firstOdor != EventType.unknown && secondOdor != EventType.unknown) {
                        sortSpikes(spk, miceDay, baselineStart, secondOdorEnd, firstOdor, secondOdor, response, sessionIdx, behaviorSession.size());
                        EventType[] behaviorTrial = {firstOdor, secondOdor, response};
                        behaviorSession.add(behaviorTrial);
                    }
                    firstOdor = EventType.unknown;
                    secondOdor = EventType.unknown;
                    
                    break;
                case 9:
                case 10:
                    if (evt[3] != 0) {
                        if (firstOdor == EventType.unknown) {
                            firstOdor = odors[evt[2] - 9];
                            baselineStart = evtDouble[0] - 1;
                        } else {
                            secondOdor = odors[evt[2] - 9];
                            secondOdorEnd = evtDouble[0] + 1;//
                        }
                    }
                    break;
            }
        }
        poolTrials(miceDay);
        miceDay.setBehaviorSessions(behaviorSessions);
        return miceDay;
    }

    private void sortSpikes(double[][] spk, MiceDay miceDay, double baselineStart, double secondOdorEnd, EventType firstOdor, EventType secondOdor, EventType response, int sessionIdx, int trialIdx) {
        while (spkIdx < spk.length && spk[spkIdx][2] < secondOdorEnd) {
            if (spk[spkIdx][2] > baselineStart) {
                Trial currentTrial = miceDay.getTetrode((int) Math.round(spk[spkIdx][0]))
                        .getSingleUnit((int) Math.round(spk[spkIdx][1]))
                        .getTrial(sessionIdx, trialIdx);

                if (!currentTrial.isSet()) {
                    currentTrial.setTrialParameter(firstOdor, secondOdor, response,secondOdorEnd-baselineStart);
                }
                currentTrial.addSpk(spk[spkIdx][2] - baselineStart - 1);//Odor1 Start at 0;
            }
            spkIdx++;
        }
    }

    private void poolTrials(MiceDay miceDay) {
        for (Tetrode tet : miceDay.getTetrodes()) {
            for (SingleUnit unit : tet.getUnits()) {
                unit.poolTrials();
            }
        }
    }

    
    
}
