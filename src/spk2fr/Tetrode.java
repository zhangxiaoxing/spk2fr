/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr;

import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author Libra
 */
public class Tetrode {

    private HashMap<Integer, SingleUnit> singleUnits;

    public Tetrode() {
        singleUnits = new HashMap<>();
    }

    public SingleUnit getSingleUnit(int idx) {
        if (!singleUnits.containsKey(idx)) {
            singleUnits.put(idx, new SingleUnit());
        }
        return singleUnits.get(idx);
    }

    public void removeSession(int sessionIdx) {
        for (SingleUnit unit : singleUnits.values()) {
            unit.removeSession(sessionIdx);
        }
    }

    public void removeSparseFiringUnits(int trialCount) {
//        for (SingleUnit unit : singleUnits.values()) {
//            unit.removeSession(sessionIdx);
//        }
        HashMap<Integer, SingleUnit> selectedUnits=new HashMap<>();
        for (Integer key:singleUnits.keySet()){
            if(!singleUnits.get(key).isStrictSparseFiring(trialCount)){
                selectedUnits.put(key, singleUnits.get(key));
            }
        }
        this.singleUnits=selectedUnits;
    }

    public Collection<SingleUnit> getUnits() {
        return singleUnits.values();
    }
}
