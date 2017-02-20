package edu.mtu.team9.aspirus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by nssch on 1/8/2017.
 */

public class SessionReviewActivity extends AppCompatActivity {


    public static final String TAG = "session-review:";

    private LineChart lineChart;
    private PieChart pieChart;
    private DonutProgress donutProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_session_review);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        SessionFileUtility sessionFileUtility = new SessionFileUtility(this,handler);

        // Parse the intent extras to get the session stats
        Intent intent = getIntent();
        String sessionJsonString = intent.getStringExtra("JSON_SESSION_STRING");
        JSONObject session = null;

        try {
            session = new JSONObject(sessionJsonString);
        } catch (JSONException e) {
            Log.e(TAG,"Error parsing intent for JSON string");
        }

        sessionFileUtility.saveSession(session);

        // Find the Charts in the XML, then fill with data.
        lineChart = (LineChart) findViewById(R.id.chart);
        pieChart = (PieChart) findViewById(R.id.pieChart);
        donutProgress = (DonutProgress)findViewById(R.id.donut_progress);
        TextView finalScoreText = (TextView)findViewById(R.id.finalScoreText);

        // Format the charts initially
        formatLineChart(lineChart);
        FormatPieChart(pieChart);

        try {
            finalScoreText.setText(String.format(String.valueOf(session.getInt("final_score")), Locale.US));
        } catch (JSONException e) {
            Log.e(TAG,"Error parsing final score");
        }

        List<Entry> entries = new ArrayList<Entry>();
        try {
            JSONArray scoresJSONArray = session.getJSONArray("scores_array");
            int len = scoresJSONArray.length();
            for (int i = 0; i < len; i++) {
                entries.add(new Entry(i, scoresJSONArray.getInt(i)));
            }
        }catch (JSONException e){
            Log.e(TAG,"Error parsing scores array");
        }

        LineDataSet sessionScoresLineDataSet = new LineDataSet(entries, null);
        formatLineDataSet(sessionScoresLineDataSet);

        // Update the chart with the new data
        LineData lineData = new LineData(sessionScoresLineDataSet);
        lineChart.setData(lineData);
        //chart.invalidate(); // refresh

        // create pie data set
        List<PieEntry> entries2 = new ArrayList<PieEntry>();
        try {
            entries2.add(new PieEntry(session.getInt("left_leg_percent"),"Left"));
            entries2.add(new PieEntry(session.getInt("right_leg_percent"),"Right"));
        } catch (JSONException e) {
            Log.e(TAG,"Error parsing leg breakdown data");
        }

        PieDataSet pieDataSet = new PieDataSet(entries2, null);
        formatPieDataSet(pieDataSet);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextColor(Color.BLACK);
        pieChart.setData(pieData);

        try {
            donutProgress.setProgress(session.getInt("trendelenburg_score"));
        } catch (JSONException e) {
            Log.e(TAG,"Error parsing trendelenburg score");
        }

        FloatingActionButton doneButton = (FloatingActionButton) findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case SessionFileUtility.SAVE_SUCCESS:
                    Log.d(TAG, "Data Saved Succesfully");
                    Toast.makeText(SessionReviewActivity.this,"Session Saved!",Toast.LENGTH_SHORT).show();
                    break;
                case SessionFileUtility.OPEN_FAILURE:
                    Log.e(TAG,"Data failed to save");
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

    }


    public void formatPieDataSet(PieDataSet pieDataSet){
        int[] colors = {Color.WHITE,Color.GREEN};
        pieDataSet.setColors(colors);
        pieDataSet.setValueTextSize(12.0f);
        pieDataSet.setValueTextColor(Color.BLACK);
    }

    public void formatLineDataSet(LineDataSet lineDataSet){
        lineDataSet.setColor(Color.WHITE);
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setLineWidth(3.0f);
        lineDataSet.setCircleRadius(4.0f);
        lineDataSet.setCircleColor(Color.WHITE);
        lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSet.setHighlightEnabled(false);
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
        lineChart.setVisibleXRangeMaximum(5.0f);
        lineChart.getLegend().setEnabled(false);
    }

    private void FormatPieChart(PieChart pieChart){
        pieChart.setDescription(null);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.getLegend().setEnabled(false);
    }

}
