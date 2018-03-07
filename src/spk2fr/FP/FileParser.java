/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.FP;

import spk2fr.SU.Trial;
import java.util.Comparator;
import java.util.HashSet;
import spk2fr.EventType;
import spk2fr.MiceDay;
import spk2fr.SU.SingleUnit;
import spk2fr.Tetrode;

/**
 *
 * @author Libra
 */
public abstract class FileParser {

    public static final int baseBias = 0;
    public static final int rewardBias = 5;
    protected HashSet<Integer> unitSet = new HashSet<>();

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

    protected void sortSpikes(double[][] spk, MiceDay miceDay, double baseOnset, double testOffset, EventType sample, EventType test, EventType response, int sessionIdx, int trialIdx) {
//        System.out.println("unitset size, "+unitSet.size());
        for (Integer unit : unitSet) {
//            System.out.println("unit, "+unit);
            int tet = unit >> 8;
            int su = unit & 0xff;
            Trial currentTrial = new Trial();
            currentTrial.setTrialParameter(sample, test, response, testOffset - baseOnset + FileParser.baseBias + FileParser.rewardBias,baseOnset);
            miceDay.getTetrode(tet)
                    .getSingleUnit(su)
                    .setTrial(sessionIdx, trialIdx, currentTrial);

//                    .getTrial(sessionIdx, trialIdx, tet, su)
//                    .setTrialParameter(sample, test, response, testOffset - baseOnset + FileParser.baseBias + FileParser.rewardBias);
        }

        while (spkIdx < spk.length && spk[spkIdx][2] < testOffset + FileParser.rewardBias) {
            if (spk[spkIdx][1] > 0.5) {
                miceDay.getTetrode((int) Math.round(spk[spkIdx][0]))
                        .getSingleUnit((int) Math.round(spk[spkIdx][1])).addspk();//for average over whole trial;
                if (spk[spkIdx][2] > baseOnset - FileParser.baseBias) {
                    miceDay.getTetrode((int) (spk[spkIdx][0] + 0.5))
                            .getSingleUnit((int) (spk[spkIdx][1] + 0.5))
                            .getTrial(sessionIdx, trialIdx, (int) Math.round(spk[spkIdx][0]), (int) Math.round(spk[spkIdx][1]))
                            .addSpk(spk[spkIdx][2] - baseOnset - 1);//Odor1 Start at 0;
                }
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
