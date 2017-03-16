package edu.mtu.team9.aspirus.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
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
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.mtu.team9.aspirus.R;
import edu.mtu.team9.aspirus.SessionFileUtility;
import edu.mtu.team9.aspirus.SessionFromJSONString;

/**
 *  Created by Nicholas Schweikart, CPE, for Biomedical Senior Design Team 9
 *
 *  Description: This activity shows the user the aggregate results from a session they just
 *  completed.
 */
public class SessionReviewActivity extends AppCompatActivity {
    public static final String TAG = "session-review:";

    private static final String COMPLETION_PHRASE = "Your session is complete.";
    private SessionFromJSONString session;
    private LineChart lineChart;
    private PieChart pieChart;
    private TextView finalScoreText;
    private DonutProgress donutProgress;
    private SessionFileUtility sessionFileUtility;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_session_review);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        sessionFileUtility = new SessionFileUtility(this);

        // Find the Charts in the XML, then fill with data.
        lineChart = (LineChart) findViewById(R.id.chart);
        pieChart = (PieChart) findViewById(R.id.pieChart);
        donutProgress = (DonutProgress) findViewById(R.id.donut_progress);
        finalScoreText = (TextView)findViewById(R.id.finalScoreText);

        // Parse the intent extras to get the session stats
        Intent intent = getIntent();
        String sessionJsonString = intent.getStringExtra("JSON_SESSION_STRING");
        new SaveSessionToFile().execute(sessionJsonString);

        FloatingActionButton doneButton = (FloatingActionButton) findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                    textToSpeech.speak(COMPLETION_PHRASE, TextToSpeech.QUEUE_FLUSH,null,null);
                }
            }
        });
    }

    private class SaveSessionToFile extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String...param) {

            session = new SessionFromJSONString(param[0]);
            return sessionFileUtility.saveSession(session.toJSON());
        }

        @Override
        protected void onPostExecute(Boolean saveSuccessful){

            if(!saveSuccessful){
               Log.e(TAG, "Failed to save session");
                Toast.makeText(getApplicationContext(),"Session Failed to Save", Toast.LENGTH_LONG).show();
            }

            // Format the charts initially
            formatLineChart(lineChart);
            FormatPieChart(pieChart);


            finalScoreText.setText(String.format(String.valueOf(session.getFinalScore()), Locale.US));

            LineDataSet sessionScoresLineDataSet = session.getScoresLineDataSet();
            formatLineDataSet(sessionScoresLineDataSet);

            // Update the chart with the new data
            LineData lineData = new LineData(sessionScoresLineDataSet);
            lineChart.setData(lineData);
            lineChart.invalidate(); // refresh chart

            // create pie data set
            List<PieEntry> entries2 = new ArrayList<>();
            entries2.add(new PieEntry(session.getLegBreakdownLeft(),"Left"));
            entries2.add(new PieEntry(session.getLegBreakdownRight(),"Right"));


            PieDataSet pieDataSet = new PieDataSet(entries2, null);
            formatPieDataSet(pieDataSet);

            PieData pieData = new PieData(pieDataSet);
            pieData.setValueTextColor(Color.BLACK);
            pieChart.setData(pieData);
            pieChart.invalidate();

            donutProgress.setProgress(session.getTrendelenburgPercentage());
            Toast.makeText(getApplicationContext(),"Session Saved!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        if(textToSpeech !=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
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
