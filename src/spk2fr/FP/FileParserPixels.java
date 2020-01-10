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
import spk2fr.SU.Trial;

/**
 *
 * @author zx
 */
public class FileParserPixels extends FileParser {

    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
     */
    /**
     *
     * @author Libra
     * @return
     */
    @Override
    public MiceDay processFile(double[][] evts, double[][] spk) {

        for (double[] oneSpk : spk) {
            unitSet.add((int) oneSpk[1]);
        }
        ArrayList<ArrayList<EventType[]>> behaviorSessions = new ArrayList<>();
        MiceDay miceDay = new MiceDay();
        if (spk.length < 1000 || evts.length < 10) {
            return miceDay;
        }
        Arrays.sort(spk, new FileParser.SpkSorterByTime(true));
        miceDay.setRecordingLength(spk[spk.length - 1][2] - spk[0][2]);

        spkIdx = 0;
        ArrayList<EventType[]> behaviorSession = new ArrayList<>();

        int sessionIdx = 0;
        ///If need to remove later trials with no lick ////
//        evts=removeMissingTrials(evts);
        //////////////////////////////////////////////////
        for (double[] evtDouble : evts) {

            EventType sample = evtDouble[2] < 6 ? EventType.OdorA : EventType.OdorB;
            EventType secondOdor = evtDouble[3] < 6 ? EventType.OdorA : EventType.OdorB;
            EventType response;
            if (sample == secondOdor) {
                response = evtDouble[4] > 0 ? EventType.FalseAlarm : EventType.CorrectRejection;
            } else {
                response = evtDouble[4] > 0 ? EventType.Hit : EventType.Miss;
            }
//            double sampleOnset = evtDouble[0];
//            double testOnset = evtDouble[1] + 1;

            sortSpikes(spk, miceDay, evtDouble[0], evtDouble[1], sample, secondOdor, response, sessionIdx, behaviorSession.size());
            EventType[] behaviorTrial = {sample, secondOdor, response};
            behaviorSession.add(behaviorTrial);

        }
        behaviorSessions.add(behaviorSession);
        poolTrials(miceDay);
        miceDay.setBehaviorSessions(behaviorSessions);
        return miceDay;
    }

    @Override
    protected void sortSpikes(double[][] spk, MiceDay miceDay, double sampleOnset, double testOnset, EventType sample, EventType test, EventType response, int sessionIdx, int trialIdx) {
//        System.out.println("unitset size, "+unitSet.size());
        for (Integer unit : unitSet) {
//            System.out.println("unit, "+unit);
            int tet = 1;
            int su = unit;
            Trial currentTrial = new Trial();
//            currentTrial.setTrialParameter(sample, test, response, testOffset - baseOnset + FileParser.baseBias + FileParser.rewardBias, baseOnset);

            currentTrial.setTrialParameter(sample, test, response, 17.0, sampleOnset);// set trial length 17s for both 3s and 6s trials
            miceDay.getTetrode(tet)
                    .getSingleUnit(su)
                    .setTrial(sessionIdx, trialIdx, currentTrial);

//                    .getTrial(sessionIdx, trialIdx, tet, su)
//                    .setTrialParameter(sample, test, response, testOffset - baseOnset + FileParser.baseBias + FileParser.rewardBias);
        }

        while (spkIdx < spk.length && spk[spkIdx][2] <= sampleOnset + 14.0) {
            miceDay.getTetrode(1)
                    .getSingleUnit((int) spk[spkIdx][1]).addspk();//for average over whole trial;
            if (spk[spkIdx][2] > sampleOnset - 3.0) {// count  spks 3s before sample and 5s after test
                miceDay.getTetrode(1)
                        .getSingleUnit((int) spk[spkIdx][1])
                        .getTrial(sessionIdx, trialIdx, 1, (int) spk[spkIdx][1])
                        .addSpk(spk[spkIdx][2] - sampleOnset);//Odor1 Start at 0;
            }
            spkIdx++;
        }
    }

}
