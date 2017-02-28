package edu.mtu.team9.aspirus;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class PerformanceReviewActivity extends AppCompatActivity {
    public static final String TAG = "performance-review:";
    private SessionFileUtility sessionFileUtility;
    private LineChart lineChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance_review);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Find the Charts in the XML, then fill with data.
        lineChart = (LineChart) findViewById(R.id.mainChart);

        // Format the charts initially
        formatLineChart(lineChart);

        // load in the sessions data file; report sent to callback.
        sessionFileUtility = new SessionFileUtility(this);
        new populateDashboard().execute();
    }

    private class populateDashboard extends AsyncTask<Void, Void, FinalSession>{

        @Override
        protected FinalSession doInBackground(Void...voids) {

            // Load in the file data.
            if(!sessionFileUtility.getSessionsData()){
                Log.e(TAG,"Load failure");
                return null;
            }

            ArrayList<SessionFromJSONString> allSessions = sessionFileUtility.getSessionsArrayLists();
            List<Entry> entriesScores = new ArrayList<Entry>();
            List<Entry> entriesLeg = new ArrayList<Entry>();
            List<Entry> entriesTrendel = new ArrayList<Entry>();
            int i = 0;
            for(SessionFromJSONString session : allSessions){
                entriesScores.add(new Entry(i, session.getFinalScore()));
                entriesTrendel.add(new Entry(i, session.getTrendelenburgScore()));
                entriesLeg.add(new Entry(i, session.getLegScore()));
                i+=1;
            }

            LineData lineData1 = new LineData();

            LineDataSet scores = new LineDataSet(entriesScores,"Session Score");
            formatLineDataSetScores(scores);
            lineData1.addDataSet(scores);

            LineDataSet trendel = new LineDataSet(entriesTrendel,"Trendelenburg Score");
            formatLineDataSetTrendel(trendel);
            lineData1.addDataSet(trendel);

            LineDataSet leg = new LineDataSet(entriesLeg,"Limp Score");
            formatLineDataSetLimp(leg);
            lineData1.addDataSet(leg);

            return new FinalSession(lineData1);
        }

        @Override
        protected void onPostExecute(FinalSession rslt){
            lineChart.setData(rslt.dataSet);
            lineChart.invalidate();
            lineChart.setVisibleXRangeMaximum(14);  // Show up to 14 sessions at a time
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_performance_review, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return false;
        }

    }

    public void formatLineDataSetScores(LineDataSet lineDataSet){
        lineDataSet.setColor(Color.WHITE);
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setLineWidth(3.0f);
        lineDataSet.setCircleRadius(4.0f);
        lineDataSet.setCircleColor(Color.WHITE);
        lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSet.setHighlightEnabled(false);
        lineDataSet.setDrawCircleHole(false);
    }

    public void formatLineDataSetTrendel(LineDataSet lineDataSet){
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setDrawValues(true);
        lineDataSet.setLineWidth(1.0f);
        lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSet.setHighlightEnabled(false);
        lineDataSet.enableDashedLine(8,4,0);
        lineDataSet.setCircleRadius(4.0f);
        lineDataSet.setCircleColor(Color.BLUE);
        lineDataSet.setDrawCircleHole(false);
    }
    public void formatLineDataSetLimp(LineDataSet lineDataSet){
        lineDataSet.setColor(Color.BLACK);
        lineDataSet.setDrawValues(true);
        lineDataSet.setHighlightEnabled(false);
        lineDataSet.setLineWidth(1.0f);
        lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSet.enableDashedLine(16,8,0);
        lineDataSet.setCircleRadius(4.0f);
        lineDataSet.setCircleColor(Color.BLACK);
        lineDataSet.setDrawCircleHole(false);
    }
    private void formatLineChart(LineChart lineChart){
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(false);
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(100.0f);
        leftAxis.setAxisMinimum(0.0f);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.setDescription(null);
        lineChart.setDrawBorders(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setPinchZoom(false);
        lineChart.setScaleYEnabled(false);
        lineChart.setDragEnabled(true);
        lineChart.getLegend().setEnabled(true);
    }

    private class FinalSession{
        LineData dataSet;
        FinalSession(LineData dataSet){
            this.dataSet = dataSet;
        }
    }
}
