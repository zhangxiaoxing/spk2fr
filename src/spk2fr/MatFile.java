/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import java.util.ArrayList;

/**
 *
 * @author TonylabSv
 */
public class MatFile {

    public static double[][] getFile(String fileName, String dataName) {

        //read array form file
        try {
//            System.out.println("before 0");
            MatFileReader mfr = new MatFileReader(fileName);
            MLArray mlArrayRetrived = mfr.getMLArray(dataName);
//            System.out.println("before 1");
            return ((MLDouble) mlArrayRetrived).getArray();
        } catch (Exception ioe) {
            System.out.println(fileName + ", " + dataName + ", " + ioe.toString());
        }
        throw new IllegalArgumentException("Error in mat file " + fileName);
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
