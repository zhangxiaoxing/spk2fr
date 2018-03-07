/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import java.util.LinkedList;

/**
 *
 * @author TonylabSv
 */
public class MatFile {

    public static double[][] getFile(String fileName, String dataName) {

        //read array form file
        try {
            MatFileReader mfr = new MatFileReader(fileName);
            MLArray mlArrayRetrived = mfr.getMLArray(dataName);
            return ((MLDouble) mlArrayRetrived).getArray();
        } catch (Exception ioe) {
            System.out.println(fileName + ", " + dataName + ", " + ioe.toString());
        }
        throw new IllegalArgumentException("Error in mat file " + fileName);
    }

    public static double[][] wellTrainedTrials(double[][] in) {
        if (in.length < 80) {
            return new double[0][0];
        }

        int lickPos = in[0].length - 2;
//        int missThreshold = 10;

        boolean[] correct = new boolean[in.length];
//        boolean[] noLick = new boolean[in.length];
        boolean[] wellTrained = new boolean[in.length];
        for (int i = 0; i < in.length; i++) {
//            if (in[i][lickPos] < 0.5) {
//                noLick[i] = true;
//            }
            if ((in[i][2] != in[i][3] && in[i][lickPos] > 0.5)
                    || (in[i][2] == in[i][3] && in[i][lickPos] < 0.5)) {
                correct[i] = true;
            }

        }
//        boolean wellTrained = false;
        for (int i = 0; i <= in.length - 40; i++) {
            int sum = 0;
            for (int j = i; j < i + 40; j++) {
                sum += correct[j] ? 1 : 0;
            }
            if (sum > 31) {
//                wellTrained = true;
//                break;

                for (int j = i; j < i + 40; j++) {
                    wellTrained[j] = true;
                }
            }
        }
//        if (!wellTrained) {
//            System.out.println("not well-trained");
//            return new double[0][0];
//        }
//        int sum = 0;
//        for (int i = 0; i < wellTrained.length; i++) {
//            sum += (wellTrained[i] && correct[i]) ? 1 : 0;
//        }
//
//        if (sum < 32) {
//            System.out.println("not enough left");
//            return new double[0][0];
//        }
//        int sum;
//        int endWith;
//
//        for (endWith = in.length, sum = missThreshold; sum >= missThreshold; endWith--) {
//            sum = 0;
//            for (int i = endWith - missThreshold; i < endWith; i++) {
////                try {
//                sum += noLick[i] ? 1 : 0;
////                } catch (ArrayIndexOutOfBoundsException e) {
////                    System.out.println(Arrays.deepToString(in));
////                }
//            }
////            System.out.print("miss" + sum);
//        }
//        System.out.println("Well-trained only, pre-filter trials:" + in.length + ", "
//                + "post-filter trials:" + ++endWith);
//        double[][] rtn = Arrays.copyOf(in, endWith);

        LinkedList<double[]> rtn = new LinkedList<>();
        for (int i = 0; i < in.length; i++) {
            if (wellTrained[i]) {
                rtn.add(in[i]);
            }
        }
        System.out.println("Well-trained only, pre-filter trials:" + in.length + ", "
                + "post-filter trials:" + rtn.size());
        return rtn.toArray(new double[rtn.size()][]);
//        return rtn;
    }

    public static double[][] wellTrainedTrials(double[][] in, boolean wellTrainOnly) {
        return wellTrainOnly ? wellTrainedTrials(in) : in;
    }

}
