/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.FP;

import spk2fr.SU.Trial;
import java.util.Comparator;
import spk2fr.EventType;
import spk2fr.MiceDay;
import spk2fr.SU.SingleUnit;
import spk2fr.Tetrode;

/**
 *
 * @author Libra
 */
public abstract class FileParser {

    public abstract MiceDay processFile(double[][] evts, double[][] spk);

    protected int spkIdx = 0;

    protected class SpkSorterByTime implements Comparator<double[]> {

        boolean byTime;// false=by id;

        public SpkSorterByTime(boolean byTime) {
            this.byTime = byTime;
        }

        @Override
        public int compare(double[] o1, double[] o2) {
            if (byTime) {
                return o1[2] < o2[2] ? -1 : 1;
            } else if (o1[0] == o2[0]) {
                return o1[1] < o2[1] ? -1 : 1;
            } else {
                return o1[0] < o2[0] ? -1 : 1;
            }
        }
    }

    protected void sortSpikes(double[][] spk, MiceDay miceDay, double baselineStart, double secondOdorEnd, EventType firstOdor, EventType secondOdor, EventType response, int sessionIdx, int trialIdx) {
        while (spkIdx < spk.length && spk[spkIdx][2] < secondOdorEnd) {
            if (spk[spkIdx][2] > baselineStart && spk[spkIdx][1] > 0.5) {
                Trial currentTrial = miceDay.getTetrode((int) (spk[spkIdx][0] + 0.5))
                        .getSingleUnit((int) (spk[spkIdx][1] + 0.5))
                        .getTrial(sessionIdx, trialIdx);

                if (!currentTrial.isSet()) {
                    currentTrial.setTrialParameter(firstOdor, secondOdor, response, secondOdorEnd - baselineStart);
                }
                currentTrial.addSpk(spk[spkIdx][2] - baselineStart - 1);//Odor1 Start at 0;
//                System.out.println("spks "+currentTrial.getSpikesList().size());
            }
            spkIdx++;
        }
    }

    protected void poolTrials(MiceDay miceDay) {
        for (Tetrode tet : miceDay.getTetrodes()) {
            for (SingleUnit unit : tet.getUnits()) {
                unit.poolTrials();
            }
        }
    }

}
