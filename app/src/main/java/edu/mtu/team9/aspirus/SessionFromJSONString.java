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
 * Created for Aspirus2
 * By: nicholas on 2/19/17.
 * Description:
 */

public class SessionFromJSONString {
    private final static String TAG = "session-from-json";
    private ArrayList<Integer> scores;
    private int left_leg_percent, right_leg_percent, trendelenburg_score, final_score;
    private JSONObject jsonSession;
    private LineDataSet lineDataSet = null;

    SessionFromJSONString(String jsonSessionString){

        List<Entry> entries = new ArrayList<Entry>();
        scores = new ArrayList<Integer>();
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
            trendelenburg_score = jsonSession.getInt("trendelenburg_score");
            final_score = jsonSession.getInt("final_score");
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
    public int getTrendelenburgScore(){
        return trendelenburg_score;
    }
    public LineDataSet getScoresLineDataSet(){return lineDataSet;}
}
