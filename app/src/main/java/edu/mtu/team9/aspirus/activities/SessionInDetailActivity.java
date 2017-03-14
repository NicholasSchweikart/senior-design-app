package edu.mtu.team9.aspirus.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

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
import edu.mtu.team9.aspirus.SessionFromJSONString;

/**
 *  Created by Nicholas Schweikart, CPE, for Biomedical Senior Design Team 9
 *
 *  Description: This activity shows the user the aggregate results from a single session in
 *  greater detail, only accessed from the Performance Review activity.
 */
public class SessionInDetailActivity extends AppCompatActivity {
    public static final String TAG = "session-in-detail:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_session_in_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Find the Charts in the XML, then fill with data.
        LineChart lineChart = (LineChart) findViewById(R.id.chart);
        PieChart pieChart = (PieChart) findViewById(R.id.pieChart);
        DonutProgress donutProgress = (DonutProgress) findViewById(R.id.donut_progress);
        TextView finalScoreText = (TextView) findViewById(R.id.finalScoreText);

        // Parse the intent extras to get the session stats
        Intent intent = getIntent();
        String sessionJsonString = intent.getStringExtra("JSON_SESSION_STRING");
        SessionFromJSONString session = new SessionFromJSONString(sessionJsonString);

        // Format the charts initially
        formatLineChart(lineChart);
        FormatPieChart(pieChart);

        // Set the final score text.
        finalScoreText.setText(String.format(String.valueOf(session.getFinalScore()), Locale.US));

        LineDataSet sessionScoresLineDataSet = session.getScoresLineDataSet();
        formatLineDataSet(sessionScoresLineDataSet);

        // Update the chart with the new data
        LineData lineData = new LineData(sessionScoresLineDataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // refresh chart

        // Create pie data set
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
    }

    /**
     * Formats the PieDataSet for this activity only.
     * @param pieDataSet the Pie DataSet to format.
     */
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

    /**
     * Formats the Pie Chart for this activity.
     * @param pieChart the PieChar to format.
     */
    private void FormatPieChart(PieChart pieChart){
        pieChart.setDescription(null);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.getLegend().setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_session_in_detail, menu);
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
}
