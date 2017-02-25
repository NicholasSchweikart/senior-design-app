package edu.mtu.team9.aspirus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.os.Handler;

import java.util.Locale;


public class LiveSessionActivity extends AppCompatActivity implements TrendelenburgDetector.TrendelenburgEventListener,BluetoothAnklet.AnkletListener {


    public static final String TAG = "Live-Session";
    private static final String RIGHT_ANKLET_ADDRESS= "98:D3:34:90:DC:D0";
    private static final String LEFT_ANKLET_ADDRESS = "98:D3:36:00:B3:22";

    // UI Components
    private FloatingActionButton startButton;
    private TextView countDownText, timeLeftText;
    private View connectingOverlay, countDownOverlay;
    private ProgressBar progressBar;

    // System Components
    private TrendelenburgDetector trendelenburgDetector;
    private BluetoothAnklet leftAnklet, rightAnklet;
    private GaitSessionAnalyzer gaitSessionAnalyzer;
    private CountDownTimer sessionTimer;
    private Handler handler = new Handler();
    PowerManager.WakeLock wakeLock;
    TextToSpeech textToSpeech;

    // Control Variables
    private static final int
            SYSTEM_INIT = 0,
            SYSTEM_RUNNING = 1;
    private int SYSTEM_STATE;

    // Constants
    private static final int LIMP_UPDATE_INTERVAL = 15000;          // 30 seconds
    private static final int COUNT_DOWN_TIME = 10000;               // 10 seconds
    private static final int SESSION_TIME = 300000;                 // 5 minutes
    private static final String LIMP_UPDATE_PHRASE = "Detecting a limp on your ";
    private static final String SCORE_UPDATE_PHRASE = "Your current score is ";

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
        gaitSessionAnalyzer = new GaitSessionAnalyzer();

        // Create new Trendelenburg detector for the session
        trendelenburgDetector = new TrendelenburgDetector(getApplicationContext(), this);

        // Build and start the anklets for the session
        initAnklets();

        // Access UI Components
        startButton = (FloatingActionButton) findViewById(R.id.start_button);
        countDownText =   (TextView) findViewById(R.id.countDownText);
        connectingOverlay = findViewById(R.id.layoutConnectingScreen);
        countDownOverlay = findViewById(R.id.layoutTimeLeftScreen);
        timeLeftText = (TextView)findViewById(R.id.timeLeftText);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(0);

        // Create the Count Down Timer for the session.
        initSessionTimer();
        SYSTEM_STATE = SYSTEM_INIT;

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(SYSTEM_STATE == SYSTEM_RUNNING){
                    Log.d(TAG, "done button click");

                    startSessionReview();
                }else if(SYSTEM_STATE == SYSTEM_INIT){
                    Log.d(TAG, "start button click");
                    startButton.setImageResource(R.drawable.ic_stop_white_24px);
                    showCountDownTillStart();
                    handler.postDelayed(startSystem, COUNT_DOWN_TIME);
                }
            }
        });

        // Prevent the device from going to sleep for the duration of the session, but allow screen to turn off :)
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");
        wakeLock.acquire();

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        shutdownSystem();
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

    public void startSessionReview() {
        Log.d(TAG,"Starting Session Review");

        Intent reviewIntent = new Intent(this, SessionReviewActivity.class);

        reviewIntent.putExtra("JSON_SESSION_STRING", gaitSessionAnalyzer.toJSON().toString());

        startActivity(reviewIntent);
        finish();
    }

    public void startSystem(){
        Log.d(TAG, "startSystem()");

        sessionTimer.start();
        trendelenburgDetector.start();
        leftAnklet.sendStart();
        rightAnklet.sendStart();
        leftAnklet.activate();
        rightAnklet.activate();
        SYSTEM_STATE = SYSTEM_RUNNING;
        handler.postDelayed(monitorGait,LIMP_UPDATE_INTERVAL);
    }

    public void shutdownSystem(){
        Log.d(TAG, "shutdownSystem()");

        sessionTimer.cancel();
        trendelenburgDetector.Shutdown();

        // Terminate the session successfully
        leftAnklet.shutdown();
        rightAnklet.shutdown();

        handler.removeCallbacksAndMessages(null);

        if(textToSpeech !=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        // Release our wake lock ASAP
        if(wakeLock != null)
            wakeLock.release();
        wakeLock = null;
    }

    public void initSessionTimer(){

        sessionTimer =  new CountDownTimer(SESSION_TIME, 1000) {

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
                shutdownSystem();
                startSessionReview();
            }
        };
    }

    public void initAnklets(){

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        leftAnklet = new BluetoothAnklet(LEFT_ANKLET_ADDRESS, 'L', mBluetoothAdapter, this);
        rightAnklet = new BluetoothAnklet(RIGHT_ANKLET_ADDRESS,'R', mBluetoothAdapter,this);
    }

    public void showCountDownTillStart(){

        countDownOverlay.setVisibility(View.VISIBLE);
        new CountDownTimer(COUNT_DOWN_TIME, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                String timeLeft = String.valueOf(millisUntilFinished/1000);
                timeLeftText.setText(timeLeft);
                textToSpeech.speak(timeLeft, TextToSpeech.QUEUE_FLUSH,null,null);
            }

            @Override
            public void onFinish() {
                countDownOverlay.setVisibility(View.GONE);
                textToSpeech.speak("Session Starting Now", TextToSpeech.QUEUE_FLUSH,null,null);
            }
        }.start();
    }

    @Override
    public void onAnkletReady(char ankletId) {
        if(leftAnklet.isConnected() && rightAnklet.isConnected()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectingOverlay.setVisibility(View.GONE);
                }
            });
        }
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
        gaitSessionAnalyzer.incrementTrendel();
    }

    Runnable monitorGait = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG,"----------Taking GaitSessionAnalyzer Snapshot---------");
            String limpPhrase = gaitSessionAnalyzer.updateLimpStatus(leftAnklet.getAvgAcceleration(),rightAnklet.getAvgAcceleration());
            Integer score = gaitSessionAnalyzer.takeScoreSnapshot();
            if(limpPhrase != null){
                textToSpeech.speak(LIMP_UPDATE_PHRASE + limpPhrase + "leg. " + SCORE_UPDATE_PHRASE + score, TextToSpeech.QUEUE_FLUSH,null,null);
            }else{
                textToSpeech.speak(SCORE_UPDATE_PHRASE + score, TextToSpeech.QUEUE_FLUSH,null,null);
            }
            handler.postDelayed(this,LIMP_UPDATE_INTERVAL);
        }
    };

    Runnable startSystem = new Runnable() {

        @Override
        public void run() {
            startSystem();
        }
    };
}
