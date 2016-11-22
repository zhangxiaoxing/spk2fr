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
        ArrayList<ArrayList<EventType[]>> behaviorSessions = new ArrayList<>();
        MiceDay miceDay = new MiceDay();
        Arrays.sort(spk, new SpkSorterByTime(true));
        miceDay.setRecordingLength(spk[spk.length - 1][2] - spk[0][2]);

        spkIdx = 0;
        ArrayList<EventType[]> behaviorSession = new ArrayList<>();

        int sessionIdx = 0;
        double[][] rEvts = removeMissingTrials(evts);
        for (double[] evtDouble : rEvts) {
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

    private double[][] removeMissingTrials(double[][] evts) {
        int threshold = 10;
        for (int i = 0; i < evts.length - threshold; i++) {
            int counter = 0;
            for (int missingCount = i; missingCount < i + threshold; missingCount++) {
                counter += evts[missingCount][6] == 0 ? 1 : 0;
            }
            if (counter == threshold) {
//                System.out.println("Removing trials with much missing.");
                if (i > 15) {
                    double[][] removed = new double[i][];
                    for (int j = 0; j < i; j++) {
                        removed[j] = evts[j];
                    }
                    return removed;
                }else{
                    return new double[0][0];
                }
            }
        }
        return evts;
    }

    private void sortSpikes(double[][] spk, MiceDay miceDay, double baselineStart, double secondOdorEnd, EventType sampleOdor, EventType testOdor, EventType response, int sessionIdx, int trialIdx, EventType distrOdor) {
        while (spkIdx < spk.length && spk[spkIdx][2] < secondOdorEnd) {
            if (spk[spkIdx][1] > 0.5) {
                miceDay.getTetrode((int) Math.round(spk[spkIdx][3]))
                        .getSingleUnit((int) Math.round(spk[spkIdx][1])).addspk();
                if (spk[spkIdx][2] > baselineStart) {
                    Trial currentTrial = miceDay.getTetrode((int) (spk[spkIdx][3] + 0.5))
                            .getSingleUnit((int) (spk[spkIdx][1] + 0.5))
                            .getTrial(sessionIdx, trialIdx);
                    if (!(currentTrial instanceof DualTrial)) {
                        currentTrial = new DualTrial();
                        miceDay.getTetrode((int) (spk[spkIdx][3] + 0.5))
                                .getSingleUnit((int) (spk[spkIdx][1] + 0.5))
                                .setTrial(sessionIdx, trialIdx, currentTrial);
                    }

                    if (!currentTrial.isSet()) {
                        currentTrial.setTrialParameter(sampleOdor, testOdor, response, secondOdorEnd - baselineStart);
                        ((DualTrial) currentTrial).setDistrOdor(distrOdor);
                    }
                    currentTrial.addSpk(spk[spkIdx][2] - baselineStart - 1);//Odor1 Start at 0;
//                System.out.println("spks "+currentTrial.getSpikesList().size());
                }
            }
            spkIdx++;
        }
    }
}
