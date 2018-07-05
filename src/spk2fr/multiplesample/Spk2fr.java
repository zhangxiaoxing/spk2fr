/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.multiplesample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import spk2fr.ComboReturnType;
import spk2fr.EventType;
import spk2fr.FP.FileParser;
import spk2fr.MiceDay;
import spk2fr.SU.SingleUnit;
import spk2fr.Tetrode;

/**
 *
 * @author Libra
 */
public class Spk2fr extends FileParser {

    private ArrayList<int[]> keyIdx;

    public double[][] buildTrials(double[] onset, double[] offset, double[] id, double[] lick, double delayLen) {
        int lickIdx = 0;
        delayLen = delayLen + 1;
        LinkedList<double[]> trials = new LinkedList<>();
        if (onset.length != offset.length || offset.length != id.length) {
            System.out.println("Lenth mismatch");
            return null;
        }
        for (int i = 0; i < onset.length - 1; i++) {
            if ((onset[i + 1] - onset[i]) < delayLen + 0.5 && (onset[i + 1] - onset[i]) > delayLen - 0.5) {
                double[] oneTrial = new double[]{onset[i], onset[i + 1], id[i], id[i + 1], 0, 0};
                while (lickIdx < lick.length && lick[lickIdx] < offset[i + 1] + 1) {
                    if (lick[lickIdx] > offset[i + 1]) {
                        oneTrial[4] = 1;
                        oneTrial[5] = lick[lickIdx];
                        break;
                    }
                    lickIdx++;
                }
                trials.add(oneTrial);
            }
        }
        return trials.toArray(new double[trials.size()][]);
    }

    @Override
    public MiceDay processFile(double[][] evts, double[][] spk) {
        unitSet.clear();
        for (double[] oneSpk : spk) {
            if (oneSpk[1] > 0.5) {
                unitSet.add((((int) (oneSpk[0] + 0.5)) << 8) + (int) (oneSpk[1] + 0.5));
            }
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
            int sample = Math.round((float) evtDouble[2]);
            EventType firstOdor = (sample == 3 || sample == 4 || sample == 6) ? EventType.OdorA : EventType.OdorB;
            EventType secondOdor = evtDouble[3] > 2 ? EventType.OdorA : EventType.OdorB;
            EventType response;
            if (firstOdor == secondOdor) {
                response = evtDouble[6] > 0.5 ? EventType.CorrectRejection : EventType.FalseAlarm;
            } else {
                response = evtDouble[6] > 0.5 ? EventType.Hit : EventType.Miss;
            }
            double baselineStart = evtDouble[0] - 1;
            double secondOdorEnd = evtDouble[1] + 1;

            sortSpikes(spk, miceDay, baselineStart, secondOdorEnd, firstOdor, secondOdor, response, sessionIdx, behaviorSession.size());
//            System.out.println(""+miceDay.getTetrodes().size()+" C");
            EventType[] behaviorTrial = {firstOdor, secondOdor, response};
            behaviorSession.add(behaviorTrial);

        }
        behaviorSessions.add(behaviorSession);
        poolTrials(miceDay);
        miceDay.setBehaviorSessions(behaviorSessions);
        return miceDay;
    }

    public ComboReturnType getSampleFiringRate(MiceDay miceDay, float[] binsDesc, int[] sampleSize, int repeats, String criteria) {
        if (miceDay.getTetrodes().size() < 1) {
//            System.out.println("Zero unit");
            return null;
        }
        ArrayList<double[][]> frs = new ArrayList<>();
        ArrayList<int[]> keyIdx = new ArrayList<>();
        for (Integer tetKey : miceDay.getTetrodeKeys()) {
            Tetrode tetrode = miceDay.getTetrode(tetKey);
            for (Integer unitKey : tetrode.getUnitKeys()) {
                SingleUnit unit = tetrode.getSingleUnit(unitKey);
                double[][] rtn = unit.getOneSampleFR(miceDay, binsDesc, sampleSize, repeats, criteria);
                if (null != rtn && rtn.length > 0) {
                    keyIdx.add(new int[]{tetKey, unitKey});
                    frs.add(rtn);
                }
            }
        }
        ArrayList<double[]> evts = new ArrayList<>();
        miceDay.getBehaviorSessions().forEach((lst) -> {
            lst.forEach((evt) -> {
                evts.add(new double[]{evt[0].ordinal(), evt[1].ordinal(), evt[2].ordinal()});
            });
        });

        return new ComboReturnType(frs.toArray(new double[frs.size()][][]),
                keyIdx.toArray(new int[keyIdx.size()][]),
                evts.toArray(new double[evts.size()][]));
    }

    public ComboReturnType getAllFiringRate(MiceDay miceDay, float[] bin) {
        if (miceDay.getTetrodes().size() < 1) {
            return null;
        }
        ArrayList<double[][]> frs = new ArrayList<>();
        keyIdx = new ArrayList<>();
        for (Integer tetKey : miceDay.getTetrodeKeys()) {
            Tetrode tetrode = miceDay.getTetrode(tetKey);
            for (Integer unitKey : tetrode.getUnitKeys()) {
                SingleUnit unit = tetrode.getSingleUnit(unitKey);
                double[][] rtn = unit.getAllFR(miceDay, "everytrial", bin, true);
                if (null != rtn && rtn.length > 0) {
                    keyIdx.add(new int[]{tetKey, unitKey});
                    frs.add(rtn);
                }
            }
        }
        ArrayList<double[]> evts = new ArrayList<>();
        miceDay.getBehaviorSessions().forEach((lst) -> {
            lst.forEach((evt) -> {
                evts.add(new double[]{evt[0].ordinal(), evt[1].ordinal(), evt[2].ordinal()});
            });
        });
        return new ComboReturnType(frs.toArray(new double[frs.size()][][]),
                keyIdx.toArray(new int[keyIdx.size()][]),
                evts.toArray(new double[evts.size()][]));
    }

}
