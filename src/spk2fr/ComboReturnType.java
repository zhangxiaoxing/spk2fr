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

    final double[][][] FRDataA;
    final double[][][] FRDataB;
    final int[][] keyIdx;
    final double[][] evts;

    public ComboReturnType(double[][][] FRData, int[][] keyIdx, double[][] evts) {
        this.FRDataA = FRData;
        this.FRDataB = FRData;
        this.keyIdx = keyIdx;
        this.evts = evts;
    }

    public ComboReturnType(double[][][] FRDataA, double[][][] FRDataB, int[][] keyIdx, double[][] evts) {
        this.FRDataA = FRDataA;
        this.FRDataB = FRDataB;
        this.keyIdx = keyIdx;
        this.evts = evts;
    }

    public double[][][] getFRData() {
        return FRDataA;
    }

    public double[][][] getFRDataA() {
        return FRDataA;
    }

    public double[][][] getFRDataB() {
        return FRDataB;
    }

    public int[][] getKeyIdx() {
        return keyIdx;
    }

    public double[][] getEvts() {
        return evts;
    }

}
