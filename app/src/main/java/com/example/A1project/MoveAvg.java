package com.example.A1project;

import java.util.LinkedList;
import java.util.Queue;

public class MoveAvg {
    Queue<Float> window = new LinkedList<>();
    private final int period;
    private float sum;
    private float prevAvg;
    private float currAvg;
    private int peakCount = 0;
    private boolean increasing = false;

    public MoveAvg(int period) {
        assert period > 0 : "Period must be a positive integer!";
        this.period = period;
    }

    public void addData(float num) {
        sum += num;
        prevAvg = currAvg;
        window.add(num);
        if (window.size() > period) {
            sum -= window.remove();
        }
        currAvg = getAvg();
        if(currAvg>prevAvg){
            if(!increasing){
                peakCount++;
            }
            increasing = true;
        }
        else if(currAvg<prevAvg){
            if(increasing){
                peakCount++;
            }
            increasing = false;
        }
    }
    public int getPeakCount() {
        return peakCount;
    }
    public float getAvg() {
        if (window.isEmpty()) return 0;
        return sum / window.size();
    }
    public int getWindowSize() {
        return window.size();
    }
}
