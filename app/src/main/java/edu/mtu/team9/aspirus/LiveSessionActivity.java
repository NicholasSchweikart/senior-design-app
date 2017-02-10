package edu.mtu.team9.aspirus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ProgressBar;


public class LiveSessionActivity extends AppCompatActivity implements TrendelenburgDetector.TrendelenburgEventListener,BluetoothAnklet.AnkletListener {


    public static final String TAG = "Live-Session";
    private static final String LEFT_ANKLET_ADDRESS = "98:D3:34:90:DC:D0";
    private static final String RIGHT_ANKLET_ADDRESS = "98:D3:36:00:B3:22";

    // UI Components
    private FloatingActionButton startButton, pauseButton;
    private Chronometer chronometer;
    private View layoutWaitScreen, layoutReadyScreen;

    // System Components
    private TrendelenburgDetector trendelenburgDetector;
    private BluetoothAnklet leftAnklet, rightAnklet;

    // Control Variables
    private boolean SYSTEM_RUNNING = false;


    /***********************************************************************************************
     * Activity Functions
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_live_session);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // Create new Trendelenburg detector for the session
        trendelenburgDetector = new TrendelenburgDetector(getApplicationContext(), this);

        // Build and start the anklets for the session
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        leftAnklet = new BluetoothAnklet(LEFT_ANKLET_ADDRESS, 'L', mBluetoothAdapter, this);

        // Access UI Components
        startButton = (FloatingActionButton) findViewById(R.id.start_button);
        pauseButton = (FloatingActionButton) findViewById(R.id.pause_button);
        chronometer =   (Chronometer) findViewById(R.id.chronometer);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(50);


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(SYSTEM_RUNNING){
                    Log.d(TAG, "done button click");

                    chronometer.stop();
                    trendelenburgDetector.Shutdown();
                    leftAnklet.sendStop();
                    leftAnklet.shutdown();
                    startSessionReview();
                }else{
                    Log.d(TAG, "start button click");

                    SYSTEM_RUNNING = true;
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    leftAnklet.sendStart();
                    trendelenburgDetector.start();
                    final String text = "DONE";

                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "pause button click");

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        trendelenburgDetector.Shutdown();

        finish();
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

    public void startSessionReview()
    {
        Intent reviewIntent = new Intent(this, SessionReviewActivity.class);
//        reviewIntent.putExtra("LEFT_SUMMARY", gaitService.getTimeArrayLeft());
//        reviewIntent.putExtra("RIGHT_SUMMARY", gaitService.getTimeArrayRight());

        startActivity(reviewIntent);
    }

    @Override
    public void onAnkletReady(char ankletId) {

    }

    @Override
    public void onAnkletFailure(char ankletID) {

    }
    /***********************************************************************************************
     * Gait Logic
     **********************************************************************************************/

    @Override
    public void onTrendelenburgEvent() {
        Log.d(TAG, "Trend Spike Detected!");

    }

}
