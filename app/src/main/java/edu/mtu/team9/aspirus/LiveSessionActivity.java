package edu.mtu.team9.aspirus;

import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by nssch on 1/8/2017.
 */

public class LiveSessionActivity extends AppCompatActivity implements ReconnectFragment.NoticeDialogListener {


    public static final String TAG = "Live-Session";
    private static final int REQUEST_ENABLE_BT = 2;
    private Toolbar toolbar;
    private GaitService gaitService;
    private BluetoothAdapter bluetoothAdapter = null;
    private Handler handler;
    private boolean system_ready = false;

    // UI components
    private Button startButton, pauseButton, endButton;
    private Chronometer chronometer;
    private View layoutWaitScreen, layoutReadyScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_session);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Enabling bluetooth");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }else{
            enableGaitService();
        }

        // Start a timer to restart everything if we dont connect within 5 seconds
        handler = new Handler();
        handler.postDelayed(connectionCheck,5000);

        layoutWaitScreen =  findViewById(R.id.layoutWaitScreen);
        layoutReadyScreen = findViewById(R.id.layoutReadyScreen);

        // Get access to all the buttons
        startButton = (Button) findViewById(R.id.start_button);
        endButton = (Button) findViewById(R.id.end_button);
        pauseButton = (Button) findViewById(R.id.pause_button);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "start button click");
                gaitService.startSystem();
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                final String text = "RUNNING";
                startButton.setText(text);
                startButton.setEnabled(false);
            }
        });

        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "end button click");
                gaitService.stopSystem();
                chronometer.stop();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "pause button click");
                gaitService.stopSystem();
                chronometer.stop();
            }
        });

        chronometer = (Chronometer) findViewById(R.id.chronometer);

    }

    public void startSessionReview()
    {
        startActivity(new Intent(this, SessionReviewActivity.class));
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        if(gaitService != null)
            shutdownGS();

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

    private ServiceConnection gaitServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            gaitService = ((GaitService.MyLocalBinder)iBinder).getService();
            Log.d(TAG, "onServiceConnected GaitService= " + gaitService);

            if (!gaitService.initialize()) {
                Log.e(TAG, "Unable to initialize Gait Service");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            gaitService = null;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
                    enableGaitService();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    //finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    private void enableGaitService(){
        Intent bindIntent = new Intent(this, GaitService.class);
        bindService(bindIntent, gaitServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(gaitReciever, makeGaitIntentFilter());
    }

    private void shutdownGS()
    {

        try {
            //LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(gaitServiceConnection);
        gaitService.stopSelf();
        gaitService = null;
    }
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

            }
            //*********************//
            if (action.equals(GaitService.ACTION_STEP_MESSAGER)) {

                Log.d(TAG, "Right anklet step detection");

            }
        }
    };

    private TimerTask connectionCheck = new TimerTask() {
        @Override
        public void run() {
            if(!system_ready)
            {
                // Destroy the GaitService and retry everything
                Log.d(TAG, "Failed to connect anklets");
                shutdownGS();
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
        enableGaitService();
        handler.postDelayed(connectionCheck, 5000);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // Go back to the main Activity
        finish();
    }

}
