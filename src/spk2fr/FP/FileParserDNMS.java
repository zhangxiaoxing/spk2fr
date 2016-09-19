/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.FP;

import java.util.ArrayList;
import java.util.Arrays;
import spk2fr.EventType;
import spk2fr.MiceDay;

/**
 *
 * @author Libra
 */
public class FileParserDNMS extends FileParser {

    @Override
    public MiceDay processFile(double[][] evts, double[][] spk) {
        ArrayList<ArrayList<EventType[]>> behaviorSessions = new ArrayList<>();
        MiceDay miceDay = new MiceDay();
        Arrays.sort(spk, new FileParser.SpkSorterByTime(true));
        for (double[] oneSpk : spk) {
            if (oneSpk[1] > 0.5) {
                miceDay.getTetrode((int) Math.round(oneSpk[0]))
                        .getSingleUnit((int) Math.round(oneSpk[1])).addspk(oneSpk[2]);
            }
        }

        spkIdx = 0;
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
//                        double trialDelay = secondOdorEnd - baselineStart - 3;
//                        if (trialDelay > 4.8 && trialDelay < 5.2) {
//                            System.out.println("length " + (secondOdorEnd - baselineStart));
                        sortSpikes(spk, miceDay, baselineStart, secondOdorEnd, firstOdor, secondOdor, response, sessionIdx, behaviorSession.size());
                        EventType[] behaviorTrial = {firstOdor, secondOdor, response};
                        behaviorSession.add(behaviorTrial);
//                        }
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
        behaviorSessions.add(behaviorSession);
        poolTrials(miceDay);
        miceDay.setBehaviorSessions(behaviorSessions);
        return miceDay;
    }
}
