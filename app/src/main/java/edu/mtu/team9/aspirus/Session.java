package edu.mtu.team9.aspirus;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created for Aspirus2
 * By: nicholas on 2/19/17.
 * Description:
 */

public class Session {
    private ArrayList<Integer> scores;
    private int[] legBreakdown;
    private int trendelenburgScore;
    private int averageScore;
    private LineDataSet lineDataSet = null;

    Session(){

    }
    Session(JSONObject jsonSession){

    }
    public void setScores(ArrayList<Integer> scores){
        this.scores = scores;

    }
    public void setLegBreakdown(int[] legBreakdown){
        this.legBreakdown = legBreakdown;

    }
    public void setTrendelenburgScore(int trendelenburgScore){
        this.trendelenburgScore = trendelenburgScore;

    }
    public void setAverageScore(int score){
        this.averageScore = score;
    }
    public int getAverageScore(){
        return averageScore;
    }
    public ArrayList<Integer> getScores(){
        return scores;
    }
    public int getLegBreakdownLeft(){
        return legBreakdown[0];
    }
    public int getLegBreakdownRight(){
        return legBreakdown[1];
    }
    public int getTrendelenburgScore(){
        return trendelenburgScore;
    }
    public LineDataSet getScoresLineDataSet(){

        if(lineDataSet != null)
            return lineDataSet;


        return lineDataSet;
    }
}
