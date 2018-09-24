/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import java.io.File;
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
        int samplePos = in[0].length == 8 ? 0 : 2;
        int testPos = in[0].length == 8 ? 4 : 3;

        boolean[] correct = new boolean[in.length];
        boolean[] wellTrained = new boolean[in.length];
        for (int i = 0; i < in.length; i++) {
            if ((in[i][samplePos] != in[i][testPos] && in[i][lickPos] > 0.5)
                    || (in[i][samplePos] == in[i][testPos] && in[i][lickPos] < 0.5)) {
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
        System.out.println("Well-trained only, pre-filter trials:" + in.length + ", "
                + "post-filter trials:" + rtn.size());
        return rtn.toArray(new double[rtn.size()][]);
    }

    public static double[][] wellTrainedTrials(double[][] in, boolean wellTrainOnly) {
        return wellTrainOnly ? wellTrainedTrials(in) : in;
    }

    public static LinkedList<String> buildFileList(String rootPath, String pattern) {
        LinkedList<String> fileList = new LinkedList<>();
        if (rootPath == null) {
            return null;
        }
        File root = new File(rootPath);
        if (!root.exists()) {
            return null;
        }
        File[] list = root.listFiles();

        if (list != null) {
            for (File f : list) {
                if (f.isDirectory()) {
                    fileList.addAll(buildFileList(f.getAbsolutePath(), pattern));
                } else {
                    String fPath = f.getAbsolutePath();
                    if (fPath.contains(pattern)) {
                        fileList.add(fPath);
                    }
                }
            }
        }

        return fileList;
    }

}
