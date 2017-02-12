package edu.mtu.team9.aspirus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by nssch on 1/8/2017.
 */

public class SessionReviewActivity extends AppCompatActivity {


    public static final String TAG = "session-review:";
    private static final String DATA_DIRECTORY = "SessionData";

    private LineChart lineChart;
    private PieChart pieChart;
    private DonutProgress donutProgress;
    private FileOutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_session_review);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // Parse the intent extras to get the session stats
        Intent intent = getIntent();
        ArrayList<Integer> scores = intent.getIntegerArrayListExtra("SCORES_ARRAY");
        double[] legBreakdown = intent.getDoubleArrayExtra("LIMP_ARRAY");
        int trendelenburgScore = intent.getIntExtra("TRENDELENBURG_SCORE",0);

        // Find the Charts in the XML, then fill with data.
        lineChart = (LineChart) findViewById(R.id.chart);
        pieChart = (PieChart) findViewById(R.id.pieChart);
        donutProgress = (DonutProgress)findViewById(R.id.donut_progress);
        TextView finalScoreText = (TextView)findViewById(R.id.finalScoreText);

        // Format the charts initially
        formatLineChart(lineChart);
        FormatPieChart(pieChart);

        // Convert score data into entry objects, also calculate the average score
        List<Entry> entries = new ArrayList<Entry>();
        Integer averageScore = 0;
        int len = scores.size();
        for(int i=0; i<len; i++){
            Integer s = scores.get(i);
            entries.add(new Entry(i,s));
            averageScore += s;
        }
        averageScore /= len;
        finalScoreText.setText(averageScore.toString());

        // Get new LineDataSet from the score entries.
        LineDataSet lineDataSet = new LineDataSet(entries, null);
        formatLineDataSet(lineDataSet);

        // Update the chart with the new data
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        //chart.invalidate(); // refresh

        // create pie data set
        List<PieEntry> entries2 = new ArrayList<PieEntry>();
        entries2.add(new PieEntry(60,"Left"));
        entries2.add(new PieEntry(40,"Right"));

        PieDataSet pieDataSet = new PieDataSet(entries2, null);
        formatPieDataSet(pieDataSet);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextColor(Color.BLACK);
        pieChart.setData(pieData);

        donutProgress.setProgress(trendelenburgScore);

        //initFileSystem();
        //TODO save all session data to the file system!

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

    public boolean initFileSystem(){

        // Open the session files directory.
        File sessionFilesDirectory = getApplicationContext().getDir(DATA_DIRECTORY, Context.MODE_PRIVATE);

        // Create new file name for this session.
        Date today = Calendar.getInstance().getTime();
        String filename = today.toString();

        File fileWithinMyDir = new File(sessionFilesDirectory, filename);   //Getting a file within the dir.
        try {
            outputStream = new FileOutputStream(fileWithinMyDir);           //Use the stream as usual to write into the file.
        } catch (FileNotFoundException e) {
            Log.e(TAG,"ERROR: couldnt open file directory");
            return false;
        }
        return true;
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
