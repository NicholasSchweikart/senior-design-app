package edu.mtu.team9.aspirus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.os.Handler;
import java.util.ArrayList;
import java.util.Locale;


public class LiveSessionActivity extends AppCompatActivity implements TrendelenburgDetector.TrendelenburgEventListener,BluetoothAnklet.AnkletListener {


    public static final String TAG = "Live-Session";
    private static final String LEFT_ANKLET_ADDRESS = "98:D3:34:90:DC:D0";
    private static final String RIGHT_ANKLET_ADDRESS = "98:D3:36:00:B3:22";

    // UI Components
    private FloatingActionButton startButton, pauseButton;
    private TextView countDownText;
    private View layoutWaitScreen, layoutReadyScreen;
    ProgressBar progressBar;

    // System Components
    private TrendelenburgDetector trendelenburgDetector;
    private BluetoothAnklet leftAnklet, rightAnklet;
    private GaitSession gaitSession;
    private CountDownTimer countDownTimer;
    private Handler handler = new Handler();
    PowerManager.WakeLock wakeLock;

    // Control Variables
    private int SYSTEM_STATE = 0;
    private static final int LIMP_UPDATE_INTERVAL = 15000;          // 15 seconds
    private static final int
            SYSTEM_INIT = 0,
            SYSTEM_READY = 1,
            SYSTEM_RUNNING = 2,
            SYSTEM_PAUSED = 3;

    /***********************************************************************************************
     * Activity Functions
     **********************************************************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_live_session);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // Create a new gait session
        gaitSession = new GaitSession();

        // Create new Trendelenburg detector for the session
        trendelenburgDetector = new TrendelenburgDetector(getApplicationContext(), this);

        // Build and start the anklets for the session
        initAnklets();

        // Access UI Components
        startButton = (FloatingActionButton) findViewById(R.id.start_button);
        pauseButton = (FloatingActionButton) findViewById(R.id.pause_button);
        countDownText =   (TextView) findViewById(R.id.countDownText);


        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(0);

        // Create the Count Down Timer for the session.
        initCountDownTimer();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(SYSTEM_STATE == SYSTEM_READY){
                    Log.d(TAG, "done button click");
                    stopAll();
                    startSessionReview();

                }else{
                    Log.d(TAG, "start button click");
                    startAll();
                    startButton.setImageResource(R.drawable.ic_stop_white_24px);
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "pause button click");
                SYSTEM_STATE = SYSTEM_PAUSED;
            }
        });

        // Prevent the device from going to sleep for the duration of the session, but allow screen to turn off :)
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
        wakeLock.acquire();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        // Make sure the wake lock is off
        if(wakeLock != null)
            wakeLock.release();
        wakeLock = null;
        handler.removeCallbacks(monitorGait);
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
        Log.d(TAG,"Starting Session Review");

        // Release our wake lock ASAP
        if(wakeLock != null)
            wakeLock.release();
        wakeLock = null;

        Intent reviewIntent = new Intent(this, SessionReviewActivity.class);

        reviewIntent.putExtra("SCORES_ARRAY", gaitSession.getScores());
        reviewIntent.putExtra("LIMP_ARRAY", gaitSession.getLimpBreakdown());
        reviewIntent.putExtra("TRENDELENBURG_SCORE", gaitSession.getTrendelenburgScore());

        startActivity(reviewIntent);
        finish();
    }

    public void startAll(){
        Log.d(TAG, "startAll()");

        if(SYSTEM_STATE == SYSTEM_RUNNING)
            return;

        SYSTEM_STATE = SYSTEM_RUNNING;
        countDownTimer.start();
        trendelenburgDetector.start();
        leftAnklet.activate();
        rightAnklet.activate();
        handler.postDelayed(monitorGait,LIMP_UPDATE_INTERVAL);
    }

    public void stopAll(){
        Log.d(TAG, "stopAll()");

        if(SYSTEM_STATE != SYSTEM_RUNNING)
            return;
        SYSTEM_STATE = SYSTEM_RUNNING;
        countDownTimer.cancel();
        trendelenburgDetector.Shutdown();
        leftAnklet.shutdown();
        rightAnklet.shutdown();
    }

    public void initCountDownTimer(){

        countDownTimer =  new CountDownTimer(300000, 1000) {

            public void onTick(long millisUntilFinished) {

                if(SYSTEM_STATE != SYSTEM_RUNNING){
                    this.onFinish();
                }

                String v = String.format(Locale.US,"%02d", millisUntilFinished/60000);
                int va = (int)( (millisUntilFinished%60000)/1000);
                countDownText.setText(v+":"+String.format(Locale.US,"%02d",va));
                double percentDone = 1.0-((double)millisUntilFinished / 300000);
                int progress = (int) (percentDone*100);
                progressBar.setProgress(progress);
            }

            public void onFinish() {
                countDownText.setText("Done!");
            }
        };
    }

    public void initAnklets(){
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        leftAnklet = new BluetoothAnklet(LEFT_ANKLET_ADDRESS, 'L', mBluetoothAdapter, this);
        rightAnklet = new BluetoothAnklet(RIGHT_ANKLET_ADDRESS,'R', mBluetoothAdapter,this);
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
        gaitSession.incrementTrendel();
    }

    Runnable monitorGait = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG,"Running Gait Session Snapshot");
            gaitSession.updateLimpStatus(leftAnklet.getAvgAcceleration(),rightAnklet.getAvgAcceleration());
            gaitSession.takeScoreSnapshot();
            handler.postDelayed(this,LIMP_UPDATE_INTERVAL);
        }
    };

}
