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
public class FileParserOpGeneSuppression extends spk2fr.FP.FileParser {

    @Override
    public MiceDay processFile(double[][] evts, double[][] spk) {
        for (double[] oneSpk : spk) {
            if (oneSpk[1] > 0.5) {
                unitSet.add((((int) (oneSpk[0] + 0.5)) << 8) + (int) (oneSpk[1] + 0.5));
            }
        }
        MiceDay miceDay = new MiceDay();
        ArrayList<ArrayList<EventType[]>> behaviorSessions = new ArrayList<>();
        Arrays.sort(spk, new SpkSorterByTime(true));
        miceDay.setRecordingLength(spk[spk.length - 1][2] - spk[0][2]);

        spkIdx = 0;
        ArrayList<EventType[]> behaviorSession = new ArrayList<>();
        for (double[] ts : evts) {
            sortSpikes(spk, miceDay, ts[0] - 2, ts[0] + 9, EventType.OdorA, EventType.OdorA, EventType.Hit, 0, behaviorSession.size());
            behaviorSession.add(new EventType[]{EventType.OdorA, EventType.OdorA, EventType.Hit});
        }
        behaviorSessions.add(behaviorSession);
        poolTrials(miceDay);
        miceDay.setBehaviorSessions(behaviorSessions);
        return miceDay;
    }
}
