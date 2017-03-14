package edu.mtu.team9.aspirus;

import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nicholas Schweikart, CPE, for Biomedical Senior Design Team 9
 * Description: This class is a simple utility used to convert a JSON string into a Session Object.
 */

public class SessionFromJSONString {
    private final static String TAG = "session-from-json";

    private ArrayList<Integer> scores;
    private int left_leg_percent, right_leg_percent, leg_score, trendelenburg_percentage, trendelenburg_score, final_score;
    private JSONObject jsonSession;
    private LineDataSet lineDataSet = null;
    private String date;

    public SessionFromJSONString(String jsonSessionString){

        List<Entry> entries = new ArrayList<>();
        scores = new ArrayList<>();
        try {
            jsonSession = new JSONObject(jsonSessionString);
            JSONArray scoresJSONArray = jsonSession.getJSONArray("scores_array");
            int len = scoresJSONArray.length();
            for (int i = 0; i < len; i++) {
                entries.add(new Entry(i, scoresJSONArray.getInt(i)));
                scores.add(scoresJSONArray.getInt(i));
            }
            lineDataSet = new LineDataSet(entries,null);
            left_leg_percent = jsonSession.getInt("left_leg_percent");
            right_leg_percent = jsonSession.getInt("right_leg_percent");
            leg_score = jsonSession.getInt("leg_score");
            trendelenburg_score = jsonSession.getInt("trendelenburg_score");
            trendelenburg_percentage = jsonSession.getInt("trendelenburg_percentage");
            final_score = jsonSession.getInt("final_score");
            date = jsonSession.getString("date");
        }catch (JSONException e){
            Log.e(TAG,"Error parsing JSON");
        }
    }
    public JSONObject toJSON(){
        return this.jsonSession;
    }
    public int getFinalScore(){
        return final_score;
    }
    public ArrayList<Integer> getScores(){
        return scores;
    }
    public int getLegBreakdownLeft(){
        return left_leg_percent;
    }
    public int getLegBreakdownRight(){
        return right_leg_percent;
    }
    public int getLegScore(){return this.leg_score;}
    public int getTrendelenburgScore(){
        return trendelenburg_score;
    }
    public int getTrendelenburgPercentage(){
        return trendelenburg_percentage;
    }
    public LineDataSet getScoresLineDataSet(){return lineDataSet;}
    public String getDate(){return this.date;}
}
