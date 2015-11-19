/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr;

import java.util.ArrayList;

/**
 *
 * @author Libra
 */
public class RebuildEventFile {

    static class Trial {

        int o1 = -1;
        int o2 = -1;
        int response = -1;
        double o1t = -1d;
        double o2t = -1d;
        double responseT = -1d;

        void appendNoLickResponse() {
            response = o1 == o2 ? 5 : 6;
            responseT = o2t + 2.5d;
        }

//        void appendLickResponse() {
//            response = o1 == o2 ? 4 : 7;
//            responseT = o2t + 2.5d;
//        }
        void appendProperResponse() {
            if (response < 0) {
                appendNoLickResponse();
            }
        }

        boolean isResonable(double delay) {
            return o1 > 0 && o2 > 0 && (o2t - o1t < delay + 1.2) && (o2t - o1t > delay + 0.2);
        }

        boolean delayMismatch(double delay) {
            return o1 > 0 && o2 > 0 && ((o2t - o1t > delay + 1.2) || (o2t - o1t < delay + 0.2));
        }

        void swapOdor(int newO2, double newO2t) {
            o1 = o2;
            o1t = o2t;
            o2 = newO2;
            o2t = newO2t;
        }

        boolean o1set() {
            return o1 > 0 && o2 < 0;
        }

        boolean isNew() {
            return o1 < 0 && o2 < 0;
        }

        boolean lickReady(double delay) {
            return this.isResonable(delay) && response < 0;
        }

        boolean tryLick(double lick) {
            if (lick < o2t + 1.5 && lick > o2 + 0.95) {
                response = o1 == o2 ? 4 : 7;
                responseT = lick;
                return true;
            }
            return false;
        }

    }

    public static double[][] rebulid(double[][] evts, double delay) {
        ArrayList<RebuildEventFile.Trial> trials = new ArrayList<>();

        RebuildEventFile.Trial currTrial = new Trial();

        for (double[] row : evts) {
//            System.out.println(row[0] + ", " + row[1]);

            switch ((int) Math.round(row[0])) {
                case 0:
//                    System.out.println(o2+", "+response+", "+(row[1]-o2Time));
                    if (currTrial.lickReady(delay)) {
                        if (currTrial.tryLick(row[1])) {
                            trials.add(currTrial);
                            currTrial = new Trial();
                        }

                    }
                    break;
                case 1:
                case 2:
                    if (currTrial.isResonable(delay)) {// last trial done
                        currTrial.appendProperResponse();
                        trials.add(currTrial);
                        currTrial = new Trial();

                        currTrial.o1 = (int) Math.round(row[0]);
                        currTrial.o1t = row[1];

                    } else if (currTrial.delayMismatch(delay)) { // odor 2
                        currTrial.swapOdor((int) Math.round(row[0]), row[1]);
                    } else if (currTrial.o1set()) { // odor 1
                        currTrial.o2 = (int) Math.round(row[0]);
                        currTrial.o2t = row[1];
                    } else if (currTrial.isNew()) {
                        currTrial.o1 = (int) Math.round(row[0]);
                        currTrial.o1t = row[1];
                    } else {
                        currTrial = new Trial();
                        currTrial.o1 = (int) Math.round(row[0]);
                        currTrial.o1t = row[1];
                    }
                    break;

            }
        }
        if (currTrial.isResonable(delay)) {
            trials.add(currTrial);
        }

        double[][] rtn = new double[trials.size() * 3][];
        for (int i = 0; i < trials.size(); i++) {
            rtn[i * 3] = new double[]{trials.get(i).o1t, (double) 0x55, trials.get(i).o1 == 1 ? 9d : 10d, 1d, (double) 0xaa};
            rtn[i * 3 + 1] = new double[]{trials.get(i).o2t, (double) 0x55, trials.get(i).o2 == 1 ? 9d : 10d, 1d, (double) 0xaa};
            rtn[i * 3 + 2] = new double[]{trials.get(i).responseT, (double) 0x55, trials.get(i).response, 1d, (double) 0xaa};
        }

        return rtn;
    }
}
