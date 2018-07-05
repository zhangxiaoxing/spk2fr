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
    final double[][][] FRData;
    final int[][] keyIdx;
    final double[][] evts;

    public ComboReturnType(double[][][] FRData, int[][] keyIdx, double[][] evts) {
        this.FRData = FRData;
        this.keyIdx = keyIdx;
        this.evts=evts;
    }

    public double[][][] getFRData() {
        return FRData;
    }

    public int[][] getKeyIdx() {
        return keyIdx;
    }

    public double[][] getEvts() {
        return evts;
    }
    
}
