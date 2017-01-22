/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.SU;

import java.util.ArrayList;
import org.apache.commons.math3.stat.StatUtils;
import spk2fr.MiceDay;

/**
 *
 * @author Libra
 */
public abstract class Processor {

    ArrayList<Trial> typeAPool = new ArrayList<>();
    ArrayList<Trial> typeBPool = new ArrayList<>();

    public ArrayList<Trial> getTypeAPool() {
        return typeAPool;
    }

    public ArrayList<Trial> getTypeBPool() {
        return typeBPool;
    }

    double[] convert2Stats(double[] baselineTSCount) {
        double[] stats = new double[]{StatUtils.mean(baselineTSCount), Math.sqrt(StatUtils.variance(baselineTSCount))};
        if (stats[0] == 0 && stats[1] == 0) {
            System.out.println("Zero baseline.");
            baselineTSCount[0]=1;
            stats = new double[]{StatUtils.mean(baselineTSCount), Math.sqrt(StatUtils.variance(baselineTSCount))};
        }
        return stats;
    }

    abstract double[] getBaselineStats(final ArrayList<Trial> trialPool);

    abstract void fillPoolsByType(final ArrayList<Trial> trialPool);

//    abstract int getTypeATrialNum(MiceDay md);
//
//    abstract int getTypeBTrialNum(MiceDay md);

}
