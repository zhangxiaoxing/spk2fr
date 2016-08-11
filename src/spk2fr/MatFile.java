/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

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
        return null;
    }
}
