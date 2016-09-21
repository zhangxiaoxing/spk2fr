/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.SU;

import spk2fr.EventType;

/**
 *
 * @author zx
 */
public class DualTrial extends Trial{
    private EventType distrOdor;

    public void setDistrOdor(EventType distrOdor) {
        this.distrOdor = distrOdor;
    }

    public EventType getDistrOdor() {
        return distrOdor;
    }
    
}
