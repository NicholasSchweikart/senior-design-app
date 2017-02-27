package edu.mtu.team9.aspirus;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created for Aspirus2
 * By: nicholas on 2/10/17.
 * Description:
 */

public class GaitSessionAnalyzer {
    private final String TAG = "gait-session-analyzer";

    private static final int TR_BAD_THRESH = 4;

    // Gait Metric Variables
    private ArrayList<Integer> scores;
    private ArrayList<Double> leftData, rightData;
    private int totalTrendelenburgEvents, totalSamples, trendelPositiveSamples;
    private int currentLimpPercent;

    GaitSessionAnalyzer(){

        // Create list to hold score values
        scores = new ArrayList<Integer>();
        leftData = new ArrayList<Double>();
        rightData = new ArrayList<Double>();
        currentLimpPercent = 0;
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
            currentLimpPercent = (int)((leftAcceleration)/(rightAcceleration+leftAcceleration)*100);
            outputLimp = "left";
        }
        else{
            currentLimpPercent = (int)((rightAcceleration)/(rightAcceleration+leftAcceleration)*100);
            outputLimp = "right";
        }

        Log.d(TAG,"Right Avg = " + rightAcceleration + " Left Avg = " + leftAcceleration + "Update Limp Status: " + currentLimpPercent);

        if(currentLimpPercent < 47){
            return outputLimp;
        }
        return null;
    }

    public Integer takeScoreSnapshot(){

        totalSamples += 1;

        Integer trendScore = 50;                        // Max of 50 points
        if(totalTrendelenburgEvents >= TR_BAD_THRESH)   // Trigger point reduction at threshold
        {
            trendelPositiveSamples += 1;                // Flag this window as trendel positive
            trendScore -= totalTrendelenburgEvents * 6; // score = 50 - totalEvents * 6% each
            if(trendScore < 0)
                trendScore = 0;                         // Minimum of zero
        }
        int limpScore = 50-(50 - currentLimpPercent) * 5;
        if(limpScore<0)
            limpScore = 0;
        Integer score =  limpScore + trendScore;
        scores.add(score);
        Log.d(TAG, "Score Snapshot: " + score);

        // Reset for the next interval
        totalTrendelenburgEvents = 0;
        return score;
    }

    public void incrementTrendel(){
        totalTrendelenburgEvents+=1;
    }


    public JSONObject toJSON(){
        try {
            JSONObject sessionData = new JSONObject("{}");
            JSONArray scoresArray = new JSONArray();
            sessionData.put("date", Calendar.getInstance().getTime());

            int trendelPercentage = getTrendelenburgPercentage();
            sessionData.put("trendelenburg_percentage", trendelPercentage);
            int finalTrendelScore = (int)(50*(trendelPercentage/100.0));
            sessionData.put("trendelenburg_score", finalTrendelScore);

            int[] lb = getLimpBreakdown();
            sessionData.put("left_leg_percent", lb[0]);
            sessionData.put("right_leg_percent", lb[1]);
            sessionData.put("leg_score", lb[2]);

            sessionData.put("final_score", lb[2] + finalTrendelScore);

            for (Integer score : scores) {
                scoresArray.put(score);
            }

            sessionData.put("scores_array", scoresArray);
            return sessionData;
        } catch (JSONException e) {
            Log.e(TAG,"Error parsing to json");
            return null;
        }
    }

    public ArrayList<Integer> getScores(){
        return this.scores;
    }

    private int getTrendelenburgPercentage(){
        int scoreOut = (int)((double)trendelPositiveSamples/totalSamples*100.0);
        scoreOut = 100 - scoreOut;
        Log.d(TAG,"Final Trendelenburg Percentage: " + scoreOut);
        return scoreOut;
    }

    /**
     * Calculates the average acceleration values on each leg for the hole session.
     * @return  the percent of effort on each leg averaged over the whole session.
     */
    private int[] getLimpBreakdown(){
        int[] out = new int[3];
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
        if(out[1] == 100 || out[0] == 100){
            out[0] = 50;
            out[1] = 50;
        }
        out[2] = 50-(Math.abs(out[0] - out[1])*5);
        if(out[2]<0)
            out[2] = 0;
        Log.d(TAG,"Limp Breakdown: LEFT = " + out[0] + " RIGHT = " + out[1] + "Overall = " + out[2]);
        return out;
    }

}