package edu.mtu.team9.aspirus;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.intrusoft.scatter.ChartData;
import com.intrusoft.scatter.PieChart;

import java.util.ArrayList;
import java.util.List;

import mobi.gspd.segmentedbarview.Segment;
import mobi.gspd.segmentedbarview.SegmentedBarView;

/**
 * Created by nssch on 1/8/2017.
 */

public class SessionReviewActivity extends AppCompatActivity {


    public static final String TAG = "session-review:";
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_review);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        SegmentedBarView barView = (SegmentedBarView) findViewById(R.id.bar_view);
        List<Segment> segments = new ArrayList<>();
        Segment segment = new Segment(0, 30f, "Bad", Color.parseColor("#ff4444"));
        segments.add(segment);
        Segment segment2 = new Segment(30f, 70f, "Ok", Color.parseColor("#ffbb33"));
        segments.add(segment2);
        Segment segment3 = new Segment(70f, 100f, "Great", Color.parseColor("#00C851"));
        segments.add(segment3);
        barView.setValueWithUnit(88f, ""); /* You can use Html tags here in unit to support superscript and subscript */
        barView.setSegments(segments);

        PieChart pieChart = (PieChart) findViewById(R.id.pie_chart);
        List<ChartData> data = new ArrayList<>();
        data.add(new ChartData("Left 53%", 53, Color.WHITE, Color.parseColor("#ffbb33")));
        data.add(new ChartData("Right 47%", 47, Color.WHITE, Color.parseColor("#00C851")));
        pieChart.setChartData(data);
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
