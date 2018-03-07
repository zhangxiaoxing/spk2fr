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
import spk2fr.SU.DualTrial;
import spk2fr.SU.Trial;

/**
 *
 * @author zx
 */
public class FileParserDual extends FileParser {

    @Override
    public MiceDay processFile(double[][] evts, double[][] spk) {
        for (double[] oneSpk : spk) {
            if (oneSpk[1] > 0.5) {
                unitSet.add((((int) (oneSpk[3] + 0.5)) << 8) + (int) (oneSpk[1] + 0.5));
            }
        }
        ArrayList<ArrayList<EventType[]>> behaviorSessions = new ArrayList<>();
        MiceDay miceDay = new MiceDay();
        Arrays.sort(spk, new SpkSorterByTime(true));
        miceDay.setRecordingLength(spk[spk.length - 1][2] - spk[0][2]);

        spkIdx = 0;
        ArrayList<EventType[]> behaviorSession = new ArrayList<>();

        int sessionIdx = 0;
//        double[][] rEvts = removeMissingTrials(evts);
        for (double[] evtDouble : evts) {
            EventType sampleOdor = evtDouble[0] < 1.5 ? EventType.OdorA : EventType.OdorB;
            EventType testOdor = evtDouble[4] < 1.5 ? EventType.OdorA : EventType.OdorB;
            EventType distrOdor = getDistrOdor(evtDouble[2]);

            EventType response;
            if (evtDouble[0] < 1.5 == evtDouble[4] < 1.5) {
                response = evtDouble[6] > 0.5 ? EventType.FalseAlarm : EventType.CorrectRejection;
            } else {
                response = evtDouble[6] > 0.5 ? EventType.Hit : EventType.Miss;
            }
            double baselineStart = evtDouble[1] - 1;
            double secondOdorEnd = evtDouble[5] + 1;

            sortSpikes(spk, miceDay, baselineStart, secondOdorEnd, sampleOdor, testOdor, response, sessionIdx, behaviorSession.size(), distrOdor);
            EventType[] behaviorTrial = {sampleOdor, testOdor, response, distrOdor};
            behaviorSession.add(behaviorTrial);

        }

        behaviorSessions.add(behaviorSession);
        poolTrials(miceDay);
        miceDay.setBehaviorSessions(behaviorSessions);
        return miceDay;
    }

    private EventType getDistrOdor(double d) {
        switch (Math.round((float) d)) {
            case 0:
                return EventType.NONE;
            case 1:
                return EventType.OdorA;
            case 2:
                return EventType.OdorB;
            default:
                throw new IllegalArgumentException("Unknown distractor type!");
        }
    }


    private void sortSpikes(double[][] spk, MiceDay miceDay, double baseOnset, double testOffset, EventType sample, EventType test, EventType response, int sessionIdx, int trialIdx, EventType distrOdor) {
        for (Integer unit : unitSet) {
            int tet = unit >> 8;
            int su = unit & 0xff;
//            System.out.println("Set, tet "+tet+", su "+su);
            Trial currentTrial = new DualTrial();
            currentTrial.setTrialParameter(sample, test, response, testOffset - baseOnset + FileParser.baseBias + FileParser.rewardBias,baseOnset);
            ((DualTrial) currentTrial).setDistrOdor(distrOdor);
            miceDay.getTetrode(tet)
                    .getSingleUnit(su)
                    .setTrial(sessionIdx, trialIdx, currentTrial);
        }

        while (spkIdx < spk.length && spk[spkIdx][2] < testOffset + FileParser.rewardBias) {
            if (spk[spkIdx][1] > 0.5) {
                miceDay.getTetrode((int) Math.round(spk[spkIdx][3]))
                        .getSingleUnit((int) Math.round(spk[spkIdx][1])).addspk();
                if (spk[spkIdx][2] > baseOnset - FileParser.baseBias) {
                    miceDay.getTetrode((int) (spk[spkIdx][3] + 0.5))
                            .getSingleUnit((int) (spk[spkIdx][1] + 0.5))
                            .getTrial(sessionIdx, trialIdx,(int) Math.round(spk[spkIdx][3]),(int) Math.round(spk[spkIdx][1]))
                            .addSpk(spk[spkIdx][2] - baseOnset - 1);//Odor1 Start at 0;
                }
            }
            spkIdx++;
        }
    }
}
