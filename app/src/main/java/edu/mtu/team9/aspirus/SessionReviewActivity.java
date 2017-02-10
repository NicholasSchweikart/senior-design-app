package edu.mtu.team9.aspirus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nssch on 1/8/2017.
 */

public class SessionReviewActivity extends AppCompatActivity {


    public static final String TAG = "session-review:";
    private int[] leftSummary, rightSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_session_review);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // Parse the intent extras to get the session stats
        Intent intent = getIntent();
        leftSummary = intent.getIntArrayExtra("LEFT_SUMMARY");
        rightSummary = intent.getIntArrayExtra("RIGHT_SUMMARY");

        // in this example, a LineChart is initialized from xml
        LineChart chart = (LineChart) findViewById(R.id.chart);
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(false);

        xAxis.setDrawLabels(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(100.0f);
        leftAxis.setAxisMinimum(0.0f);

        chart.getAxisRight().setEnabled(false);
        chart.setDescription(null);
        chart.setDrawBorders(false);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(false);
        chart.setScaleYEnabled(false);
        chart.setDragEnabled(true);
        chart.setVisibleXRangeMaximum(5.0f);
        chart.getLegend().setEnabled(false);
        List<Entry> entries = new ArrayList<Entry>();

        // turn your data into Entry objects
        entries.add(new Entry(1.0f, 55.0f));
        entries.add(new Entry(2.0f,80.0f));
        entries.add(new Entry(3.0f,70.0f));
        entries.add(new Entry(4.0f,70.0f));
        entries.add(new Entry(5.0f,50.0f));
        entries.add(new Entry(6.0f,60.0f));
        LineDataSet dataSet = new LineDataSet(entries, null); // add entries to dataset
        dataSet.setColor(Color.WHITE);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setLineWidth(3.0f);
        dataSet.setCircleRadius(4.0f);
        dataSet.setCircleColor(Color.WHITE);
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setHighlightEnabled(false);

        LineData lineData = new LineData(dataSet);

        chart.setData(lineData);
        //chart.invalidate(); // refresh

        PieChart pieChart = (PieChart) findViewById(R.id.pieChart);
        pieChart.setDescription(null);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.getLegend().setEnabled(false);
        // create pie data set
        List<PieEntry> entries2 = new ArrayList<PieEntry>();
        entries2.add(new PieEntry(60,"Left"));
        entries2.add(new PieEntry(40,"Right"));

        PieDataSet pieDataSet = new PieDataSet(entries2, null);
        int[] colors = {Color.WHITE,Color.GREEN};
        pieDataSet.setColors(colors);
        pieDataSet.setValueTextSize(12.0f);
        pieDataSet.setValueTextColor(Color.BLACK);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextColor(Color.BLACK);
        pieChart.setData(pieData);

        FloatingActionButton doneButton = (FloatingActionButton) findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

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

}
