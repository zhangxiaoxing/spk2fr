/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk2fr.SU;

import java.util.ArrayList;
import spk2fr.EventType;

/**
 *
 * @author Libra
 */
public class Trial {

    private EventType sampleOdor; // 0 for Q, 1 for R
    private EventType testOdor; //^^
    private EventType response;// 0=hit,1=miss;2=cr,3=false;
    private boolean isSet = false;
    private ArrayList<Double> spks = new ArrayList<>();
    private double trialLength;
    private double baseOnset;

    double[] fr100msBin;

    public void addSpk(double spkTimeStamp) {
        spks.add(spkTimeStamp);
    }

    public boolean isSet() {
        return isSet;
    }

    public void setTrialParameter(EventType sampleOdor, EventType testOdor, EventType response, double trialLength, double baseOnset) {
        this.trialLength = trialLength;
        if ((sampleOdor != EventType.OdorA && sampleOdor != EventType.OdorB)
                || (testOdor != EventType.OdorA && testOdor != EventType.OdorB)) {
            System.out.println("Error Trial Parameter");
        }
        this.sampleOdor = sampleOdor;
        this.testOdor = testOdor;
        this.response = response;
        this.baseOnset = baseOnset;

        isSet = true;
    }

    public boolean sampleOdorIs(EventType odor) {
        return this.sampleOdor == odor;
    }

    public boolean testOdorIs(EventType odor) {
        return this.testOdor == odor;
    }

//    4 removal  
//    public Double[] getSpikes(){
//        return spks.toArray(new Double[spks.size()]);
//    }
    public ArrayList<Double> getSpikesList() {
        return spks;
    }

    public double getLength() {
        return trialLength;
    }

    public boolean isCorrect() {
        return response == EventType.CorrectRejection || response == EventType.Hit;
    }

    public boolean isLick() {
        return response == EventType.FalseAlarm || response == EventType.Hit;
    }

    public boolean isMatch() {
        return sampleOdor == testOdor;
    }

    public double getBaseOnset() {
        return baseOnset;
    }
    
    
}
