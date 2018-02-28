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
        System.out.print("Well-trained only, pre-filter trials:" + in.length + ", ");
        boolean[] correct = new boolean[in.length];
        boolean[] wellTrained = new boolean[in.length];
        for (int i = 0; i < in.length; i++) {
            if ((in[i][2] != in[i][3] && in[i][4] > 0.5)
                    || (in[i][2] == in[i][3] && in[i][4] < 0.5)) {
                correct[i] = true;
            }
        }

        for (int i = 0; i <= in.length - 40; i++) {
            int sum = 0;
            for (int j = i; j < i + 40; j++) {
                sum += correct[j] ? 1 : 0;
            }
            if (sum > 31) {
                for (int j = i; j < i + 40; j++) {
                    wellTrained[j] = true;
                }
            }
        }
        LinkedList<double[]> rtn = new LinkedList<>();
        for (int i = 0; i < in.length; i++) {
            if (wellTrained[i]) {
                rtn.add(in[i]);
            }
        }
        int sum=0;
        for(int i=0;i<wellTrained.length;i++){
            sum+=(wellTrained[i] && correct[i])?1:0;
        }
        
        if (sum < 64) {
            return new double[0][0];
        }

        System.out.println("post-filter trials:" + rtn.size());
        return rtn.toArray(new double[rtn.size()][]);
    }

    public static double[][] wellTrainedTrials(double[][] in, boolean wellTrainOnly) {
        return wellTrainOnly ? wellTrainedTrials(in) : in;
    }

//    public static double[][] getFile(String fileName, String dataName, double[][] evt, int grp) {
//        double[][] spkT0 = getFile(fileName, dataName);
//        int[][] tetrode = {{0, 33}, {32, 65}, {64, 97}};
//        ArrayList<double[]> spk = new ArrayList<>();
//        for (double[] oneSPK : spkT0) {
//            if (oneSPK[3] > tetrode[grp-1][0] && oneSPK[3] < tetrode[grp-1][1] && oneSPK[2] > evt[0][1] - 5 && oneSPK[2] < evt[evt.length - 1][1] + 30) {
//                spk.add(oneSPK);
//            }
//        }
//        return spk.toArray(new double[spk.size()][]);
//    }
}
