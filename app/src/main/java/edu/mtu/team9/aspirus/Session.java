package edu.mtu.team9.aspirus;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

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
    private Integer averageScore;
    private LineDataSet lineDataSet = null;
    Session(){

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
    public Integer getAverageScore(){

        if(averageScore != null)
            return averageScore;

        int len = scores.size();
        for(int i=0; i<len; i++){
            Integer s = scores.get(i);
            averageScore += s;
        }
        averageScore /= len;

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

        List<Entry> entries = new ArrayList<Entry>();

        int len = scores.size();
        for(int i=0; i<len; i++){
            Integer s = scores.get(i);
            entries.add(new Entry(i,s));
        }
        lineDataSet = new LineDataSet(entries, null);
        return lineDataSet;
    }
}
