package edu.mtu.team9.aspirus;

import android.util.Log;
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

    public String updateLimpStatus(Double leftAcceleration, Double rightAcceleration){
        String outputLimp = null;

        //Push into list for later
        leftData.add(leftAcceleration);
        rightData.add(rightAcceleration);

        // Find the difference in limp
        if(leftAcceleration < rightAcceleration){
            currentLimpValue = rightAcceleration - leftAcceleration;
            outputLimp = "left";
        }
        else{
            currentLimpValue = leftAcceleration - rightAcceleration;
            outputLimp = "right";
        }

        Log.d(TAG,"Right Avg = " + rightAcceleration + " Left Avg = " + leftAcceleration);
        Log.d(TAG, "Update Limp Status: " + currentLimpValue);
        if(currentLimpValue > .25){
            return outputLimp;
        }
        return null;
    }

    public Integer takeScoreSnapshot(){

        totalSamples += 1;

        Integer trendScore = 50;
        if(totalTrendelenburgEvents > TR_BAD_THRESH)
        {
            trendelPositiveSamples += 1;
            trendScore = 50 * (TR_BAD_THRESH/totalTrendelenburgEvents);
        }

        Integer limpScore = (int)(50 - 50 * currentLimpValue);
        if(limpScore < 0){
            limpScore = 0;
        }

        Integer score = limpScore + trendScore;
        scores.add(score);

        Log.d(TAG, "Score Snapshot: " + score);

        // Reset for the next interval
        totalTrendelenburgEvents = 0;

        return score;
    }

    public void incrementTrendel(){
        totalTrendelenburgEvents+=1;
    }

    public ArrayList<Integer> getScores(){
        return this.scores;
    }

    public int getTrendelenburgScore(){
        int scoreOut = (int)((double)trendelPositiveSamples/totalSamples*100.0);
        scoreOut = 100 - scoreOut;
        Log.d(TAG,"Final Trendelenburg Score: " + scoreOut);
        return scoreOut;
    }

    /**
     * Calculates the average acceleration values on each leg for the hole session.
     * @return
     */
    public int[] getLimpBreakdown(){
        int[] out = new int[2];
        double left = 0.0, right = 0.0;
        int totalValues = leftData.size();
        for (int i = 0; i < totalValues; i++){
            left += leftData.get(i);
            right += rightData.get(i);
        }
        left /= totalValues;
        right /= totalValues;
        out[0] =  (int)((left)/(right+left)*100);
        out[1] = 100 - out[0];

        Log.d(TAG,"Limp Breakdown: LEFT = " + out[0] + " RIGHT = " + out[1]);
        return out;
    }
}
