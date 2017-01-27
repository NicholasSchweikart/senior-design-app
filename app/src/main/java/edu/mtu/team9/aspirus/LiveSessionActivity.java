package edu.mtu.team9.aspirus;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import java.util.TimerTask;

public class LiveSessionActivity extends AppCompatActivity implements ReconnectFragment.NoticeDialogListener {

    public static final String TAG = "Live-Session";

    private GaitService gaitService;

    private Handler handler;

    // UI components
    private Button startButton, pauseButton, endButton;
    private Chronometer chronometer;
    private View layoutWaitScreen, layoutReadyScreen;
    private TextView stepsLeftT, stepsRightT;
    private Integer stepsLeft=0, stepsRight =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_live_session);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



        // Start a timer to restart everything if we dont connect within 5 seconds
        handler = new Handler();
        //handler.postDelayed(connectionCheck,20000);

        layoutWaitScreen =  findViewById(R.id.layoutConnectingScreen);
        layoutReadyScreen = findViewById(R.id.layoutLiveScreen);

        // Get access to all the buttons
        startButton = (Button) findViewById(R.id.start_button);
        pauseButton = (Button) findViewById(R.id.pause_button);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(gaitService.isSERVICE_RUNNING()){
                    Log.d(TAG, "done button click");

                    gaitService.stopSystem();
                    chronometer.stop();
                    startSessionReview();
                }else{
                    Log.d(TAG, "start button click");

                    gaitService.startSystem();
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();

                    final String text = "DONE";
                    startButton.setText(text);
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "pause button click");
                gaitService.pauseSystem();
            }
        });

        chronometer = (Chronometer) findViewById(R.id.chronometer);

        stepsLeftT = (TextView) findViewById(R.id.steps_left );
        stepsRightT = (TextView) findViewById(R.id.steps_right );
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");

        Intent bindIntent = new Intent(this, GaitService.class);
        bindService(bindIntent, gaitServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(gaitReciever, makeGaitIntentFilter());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(gaitReciever);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(gaitServiceConnection);
        gaitService.stopSelf();
        gaitService = null;
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
        reviewIntent.putExtra("LEFT_SUMMARY", gaitService.getTimeArrayLeft());
        reviewIntent.putExtra("RIGHT_SUMMARY", gaitService.getTimeArrayRight());

        startActivity(reviewIntent);
    }

    private ServiceConnection gaitServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            gaitService = ((GaitService.MyLocalBinder)iBinder).getService();
            Log.d(TAG, "onServiceConnected GaitService= " + gaitService);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            gaitService = null;
        }
    };



    private IntentFilter makeGaitIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GaitService.ACTION_ANKLETS_READY);
        intentFilter.addAction(GaitService.ACTION_STEP_MESSAGEL);
        intentFilter.addAction(GaitService.ACTION_STEP_MESSAGER);
        return intentFilter;
    }

    private final BroadcastReceiver gaitReciever = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            //*********************//
            if (action.equals(GaitService.ACTION_ANKLETS_READY)) {

                Log.d(TAG, "Gait Service says its ready to go!");

                // Change the UI
                layoutWaitScreen.setVisibility(View.GONE);
                layoutReadyScreen.setVisibility(View.VISIBLE);
            }
            //*********************//
            if (action.equals(GaitService.ACTION_STEP_MESSAGEL)){
                Log.d(TAG, "Left anklet step detection");
                stepsLeft += 1;
                stepsLeftT.setText(stepsLeft.toString());
            }
            //*********************//
            if (action.equals(GaitService.ACTION_STEP_MESSAGER)) {

                Log.d(TAG, "Right anklet step detection");
                stepsRight += 1;
                stepsRightT.setText(stepsRight.toString());
            }
        }
    };

    private TimerTask connectionCheck = new TimerTask() {
        @Override
        public void run() {
            if(!gaitService.SERVICE_READY)
            {
                // Destroy the GaitService and retry everything
                Log.d(TAG, "Failed to connect anklets");
                showDialog();
            }
        }
    };

    void showDialog() {
        DialogFragment newFragment = new ReconnectFragment();
        newFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // Restart the gait service

        handler.postDelayed(connectionCheck, 20000);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // Go back to the main Activity
        finish();
    }

}
