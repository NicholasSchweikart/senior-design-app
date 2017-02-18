package edu.mtu.team9.aspirus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class LoggingActivity extends AppCompatActivity implements SensorEventListener,BluetoothAnklet.AnkletListener {

    public static final String TAG = "logging-main:";

    // System Components
    private Timer fuseTimer = new Timer();
    private SensorManager mSensorManager;
    private FileOutputStream onboardOutputStream;
    private BluetoothAnklet leftAnklet, rightAnklet;
    public float[] accelerationVector = new float[3];   // accelerometer vector x,y,z m/s^2
    public float[] gyroVector = new float[3];           // gyro vector x,y,z rad/s

    // Constants
    private static final int SAMPLE_RATE = 20;          // in ms => 50Hz
    private static final String RIGHT_ANKLET_ADDRESS= "98:D3:34:90:DC:D0";
    private static final String LEFT_ANKLET_ADDRESS = "98:D3:36:00:B3:22";

    // Control Variables
    private boolean STATE_LOGGING = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging);

        mSensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        Sensor accelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor gyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        leftAnklet = new BluetoothAnklet(LEFT_ANKLET_ADDRESS, 'L', mBluetoothAdapter, this);
        rightAnklet = new BluetoothAnklet(RIGHT_ANKLET_ADDRESS,'R',mBluetoothAdapter, this);

        final EditText filename_etxt = (EditText) findViewById(R.id.edittxt_filename);

        final Button startBtn = (Button) findViewById(R.id.start_btn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!STATE_LOGGING){

                    String filePrefix = filename_etxt.getText().toString();
                    String onboardFilename =  filePrefix + ".csv";
                    String filenameAnkletLeft = filePrefix + "-left-anklet.csv";
                    String filenameAnkletRight = filePrefix + "-right-anklet.csv";

                    // Create the new CSV directory if it doesnt exist already.
                    File dir = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"CSVs");
                    if(!dir.exists()){
                        Log.d(TAG, "Making new directory...");
                        dir.mkdirs();
                    }

                    // Open up three new output files for the logging.
                    File onboardOutputFile = new File(dir,onboardFilename);
                    File leftAnkletOutputFile = new File(dir,filenameAnkletLeft);
                    File rightAnkletOutputFile = new File(dir, filenameAnkletRight);

                    try {
                        onboardOutputStream = new FileOutputStream(onboardOutputFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    leftAnklet.enableFileLogging(leftAnkletOutputFile);
                    rightAnklet.enableFileLogging(rightAnkletOutputFile);
                    rightAnklet.sendStart();
                    leftAnklet.sendStart();
                    rightAnklet.enableCSVoutput();
                    leftAnklet.enableCSVoutput();

                    fuseTimer.scheduleAtFixedRate(writeToFile,1000,SAMPLE_RATE);
                    startBtn.setText("Done");
                    STATE_LOGGING = true;
                }else{
                    reset();
                    STATE_LOGGING = false;
                    startBtn.setText("Start");
                }
            }
        });
    }
    private void reset() {
        fuseTimer.cancel();
        fuseTimer.purge();
        rightAnklet.sendStop();
        leftAnklet.sendStop();
        try {
            onboardOutputStream.flush();
            onboardOutputStream.close();
            Toast.makeText(this, "Files Saved!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void shutdown(){
        fuseTimer.cancel();
        fuseTimer.purge();
        mSensorManager.unregisterListener(this);
        leftAnklet.shutdown();
        rightAnklet.shutdown();
        leftAnklet = null;
        rightAnklet = null;

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
    protected  void onStop(){
        super.onStop();
        mSensorManager.unregisterListener(this);
        shutdown();
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

    /**
     *  Writes the collected data to the file.
     */
    private TimerTask writeToFile = new TimerTask() {
        @Override
        public void run() {
            final String xAccel = String.valueOf(accelerationVector[0]);
            final String yAccel = String.valueOf(accelerationVector[1]);
            final String zAccel = String.valueOf(accelerationVector[2]);
            final String xGyro = String.valueOf(gyroVector[0]);
            final String yGyro = String.valueOf(gyroVector[1]);
            final String zGyro = String.valueOf(gyroVector[2]);

            String out = String.valueOf(System.currentTimeMillis()) +','+ xAccel +','+ yAccel +','
                    + zAccel + ',' + xGyro + ',' + yGyro + ',' + zGyro + '\n';
            try{
                onboardOutputStream.write(out.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:

                // Copy new accelerometer data into accel array
                System.arraycopy(sensorEvent.values, 0, accelerationVector, 0, 3);
                break;
            case Sensor.TYPE_GYROSCOPE:

                // Copy new gyro data into the gyro array
                System.arraycopy(sensorEvent.values, 0, gyroVector, 0, 3);
                break;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onAnkletReady(char ankletID) {

    }

    @Override
    public void onAnkletFailure(char ankletID) {

    }

}
