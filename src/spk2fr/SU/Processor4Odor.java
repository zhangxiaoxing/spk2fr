/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.SU;

import java.util.ArrayList;

/**
 *
 * @author Libra
 */
public class Processor4Odor extends ProcessorAllFirstOdor {

    @Override
    double[] getBaselineStats(ArrayList<Trial> trialPool, int totalTrialCount) {
        return new double[]{0, 1};
    }

}
