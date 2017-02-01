package edu.mtu.team9.aspirus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

public class LiveSessionActivity extends AppCompatActivity implements GyroSensor.GyroEventListener,BluetoothAnklet.AnkletListener {


    public static final String TAG = "Live-Session";
    private static final String LEFT_ANKLET_ADDRESS = "98:D3:34:90:DC:D0";
    private static final String RIGHT_ANKLET_ADDRESS = "98:D3:36:00:B3:22";
    /**********************************************************************************************/
    // UI Components
    private Button startButton, pauseButton, endButton;
    private Chronometer chronometer;
    private View layoutWaitScreen, layoutReadyScreen;
    private TextView stepsLeftT, stepsRightT;
    /*********************************************************************************************/
    // System Components
    private Integer stepsLeft=0, stepsRight =0;
    private boolean SYSTEM_RUNNING = false;
    Thread accelerationThread;
    private GyroSensor gyroSensor;
    private BluetoothAnklet leftAnklet, rightAnklet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_live_session);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        gyroSensor = new GyroSensor(getApplicationContext());
        gyroSensor.setListener(this);
        accelerationThread = new Thread(gyroSensor);

        // Get access to all the buttons
        startButton = (Button) findViewById(R.id.start_button);
        pauseButton = (Button) findViewById(R.id.pause_button);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        // Instantiate new anklets here TODO

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(SYSTEM_RUNNING){
                    Log.d(TAG, "done button click");

                    chronometer.stop();
                    gyroSensor.Shutdown();
                    startSessionReview();
                }else{
                    Log.d(TAG, "start button click");

                    SYSTEM_RUNNING = true;
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    accelerationThread.start();
                    final String text = "DONE";
                    startButton.setText(text);
                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "pause button click");

                gyroSensor.pause();
            }
        });

        chronometer =   (Chronometer) findViewById(R.id.chronometer);
        stepsLeftT =    (TextView) findViewById(R.id.steps_left );
        stepsRightT =   (TextView) findViewById(R.id.steps_right );

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
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
        gyroSensor.Shutdown();

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
    public void onTrendelenburgSpike() {
        Log.d(TAG, "Trend Spike Detected!");

    }

    @Override
    public void onAnkletReady(char ankletId) {

    }

    @Override
    public void onAnkletFailure(char ankletID) {

    }
}
