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
public class FileParserWJ extends FileParser {

    @Override
    public MiceDay processFile(double[][] evts, double[][] spk) {
        ArrayList<ArrayList<EventType[]>> behaviorSessions = new ArrayList<>();
        MiceDay miceDay = new MiceDay();
        if (spk.length < 1000 || evts.length < 10) {
            return miceDay;
        }
        Arrays.sort(spk, new SpkSorterByTime(true));
        miceDay.setRecordingLength(spk[spk.length - 1][2] - spk[0][2]);

        spkIdx = 0;
        ArrayList<EventType[]> behaviorSession = new ArrayList<>();

        int sessionIdx = 0;
        for (double[] evtDouble : evts) {

            EventType firstOdor = evtDouble[2] < 1.5 ? EventType.OdorA : EventType.OdorB;
            EventType secondOdor = evtDouble[3] < 1.5 ? EventType.OdorA : EventType.OdorB;
            EventType response;
            if (evtDouble[2] < 1.5 == evtDouble[3] < 1.5) {
                response = evtDouble[4] > 0.5 ? EventType.FalseAlarm : EventType.CorrectRejection;
            } else {
                response = evtDouble[4] > 0.5 ? EventType.Hit : EventType.Miss;
            }
            double baselineStart = evtDouble[0] - 1;
            double secondOdorEnd = evtDouble[1] + 1;

            sortSpikes(spk, miceDay, baselineStart, secondOdorEnd, firstOdor, secondOdor, response, sessionIdx, behaviorSession.size());
            EventType[] behaviorTrial = {firstOdor, secondOdor, response};
            behaviorSession.add(behaviorTrial);

        }
        behaviorSessions.add(behaviorSession);
        poolTrials(miceDay);
        miceDay.setBehaviorSessions(behaviorSessions);
        return miceDay;
    }

}
