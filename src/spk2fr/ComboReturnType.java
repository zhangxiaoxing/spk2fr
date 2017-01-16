/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr;

/**
 *
 * @author zx
 */
public class ComboReturnType {
    double[][][] FRData;
    int[][] keyIdx;

    public ComboReturnType(double[][][] FRData, int[][] keyIdx) {
        this.FRData = FRData;
        this.keyIdx = keyIdx;
    }

    public double[][][] getFRData() {
        return FRData;
    }

    public int[][] getKeyIdx() {
        return keyIdx;
    }
    
    
}
