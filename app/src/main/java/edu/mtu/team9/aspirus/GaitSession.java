package edu.mtu.team9.aspirus;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created for Aspirus2
 * By: nicholas on 2/10/17.
 * Description:
 */

public class GaitSession {
    private final String TAG = "Gait-Session";

    private static final int TR_BAD_THRESH = 4;

    // Gait Metric Variables
    private ArrayList<Integer> scores;
    private ArrayList<Double> leftData, rightData;
    private int totalTrendelenburgEvents, totalSamples, trendelPositiveSamples;
    private double currentLimpValue = 0.0;

    GaitSession(){

        // Create list to hold score values
        scores = new ArrayList<Integer>();
        leftData = new ArrayList<Double>();
        rightData = new ArrayList<Double>();
        currentLimpValue = 0.0;
        totalTrendelenburgEvents = 0;
        totalSamples = 0;

    }

    public void updateLimpStatus(Double leftAcceleration, Double rightAcceleration){

        //Push into list for later
        leftData.add(leftAcceleration);
        rightData.add(rightAcceleration);

        // Find the difference in limp
        currentLimpValue = Math.abs(leftAcceleration - rightAcceleration);
        Log.d(TAG, "Update Limp Status: " + currentLimpValue);
    }

    public void takeScoreSnapshot(){

        totalSamples += 1;

        Integer trendScore = 50;
        if(totalTrendelenburgEvents > TR_BAD_THRESH)
        {
            trendelPositiveSamples += 1;
            trendScore = 50 * (TR_BAD_THRESH/totalTrendelenburgEvents);
        }

        Integer limpScore = (int)(50 - (50*currentLimpValue));
        if(limpScore < 0){
            limpScore = 10;
        }

        Integer score = limpScore + trendScore;
        scores.add(score);

        Log.d(TAG, "Score Snapshot: " + score);

        // Reset for the next interval
        totalTrendelenburgEvents = 0;
    }

    public void incrementTrendel(){
        totalTrendelenburgEvents+=1;
    }

    public ArrayList<Integer> getScores(){
        return this.scores;
    }

    public int getTrendelenburgScore(){
        int scoreOut = (int)((double)trendelPositiveSamples/totalSamples*100.0);
        Log.d(TAG,"Final Trendelenburg Score: " + scoreOut);
        return scoreOut;
    }

    /**
     * Calculates the average acceleration values on each leg for the hole session.
     * @return
     */
    public double[] getLimpBreakdown(){
        double[] out = new double[2];

        int totalValues = leftData.size();
        for (int i = 0; i < totalValues; i++){
            out[0] += leftData.get(i);
            out[1] += rightData.get(i);
        }
        out[0] = out[0]/totalValues;
        out[1] = out[1]/totalValues;

        Log.d(TAG,"Limp Breakdown: LEFT = " + out[0] + "RIGHT = " + out[1]);
        return out;
    }
}
