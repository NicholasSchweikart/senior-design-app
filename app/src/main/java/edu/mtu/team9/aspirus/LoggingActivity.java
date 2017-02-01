package edu.mtu.team9.aspirus;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class LoggingActivity extends AppCompatActivity implements SensorEventListener {

    public static final String TAG = "logging-main:";

    private TextView outputTxt;
    private TextView xtxt, ytxt, ztxt;
    private ListView outputListView;
    private ArrayAdapter outputAdapter;

    private Timer fuseTimer = new Timer();
    private SensorManager mSensorManager;
    private Sensor accelerationSensor, gyroSensor;
    private static final int SAMPLE_RATE = 20;          // in ms => 50Hz
    private FileOutputStream fileOutputStream;
    private boolean STATE_LOGGING = false;
    public float[] accelerationVector = new float[3];   // accelerometer vector x,y,z m/s^2
    public float[] gyroVector = new float[3];           // gyro vector x,y,z rad/s

    String myDir;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging);

        mSensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        accelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME);

        final EditText filename_etxt = (EditText) findViewById(R.id.edittxt_filename);
        xtxt = (TextView) findViewById(R.id.x_text);
        ytxt = (TextView) findViewById(R.id.y_text);
        ztxt = (TextView) findViewById(R.id.z_text);

        final Button startBtn = (Button) findViewById(R.id.start_btn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!STATE_LOGGING){
                    String filename =  filename_etxt.getText().toString()+".csv";
                    File dir = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"CSVs");
                    if(!dir.exists()){
                        Log.d(TAG, "Directory not created");
                        dir.mkdirs();
                    }
                    File outputCSV = new File(dir,filename);
                    try {
                        fileOutputStream = new FileOutputStream(outputCSV);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    fuseTimer.scheduleAtFixedRate(writeToFile,1000,SAMPLE_RATE);
                    startBtn.setText("Done");
                    STATE_LOGGING = true;
                }else{
                    shutdown();
                    STATE_LOGGING = false;
                    startBtn.setText("Start");
                }
            }
        });
    }
    private void shutdown(){
        fuseTimer.cancel();
        fuseTimer.purge();
        mSensorManager.unregisterListener(this);
        try {
            fileOutputStream.flush();
            fileOutputStream.close();
            Toast.makeText(this, "File Saved!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected  void onStop(){
        super.onStop();
        mSensorManager.unregisterListener(this);
       finish();
    }



    private TimerTask writeToFile = new TimerTask() {
        @Override
        public void run() {
            final String xAccel = String.valueOf(accelerationVector[0]);
            final String yAccel = String.valueOf(accelerationVector[1]);
            final String zAccel = String.valueOf(accelerationVector[2]);
            final String xGyro = String.valueOf(gyroVector[0]);
            final String yGyro = String.valueOf(gyroVector[1]);
            final String zGyro = String.valueOf(gyroVector[2]);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    xtxt.setText(xAccel);
                    ytxt.setText(yAccel);
                    ztxt.setText(zAccel);

                }
            });

            String out = String.valueOf(System.currentTimeMillis()) +','+ xAccel +','+ yAccel +','
                    + zAccel + ',' + xGyro + ',' + yGyro + ',' + zGyro +'\n';
            try{
                fileOutputStream.write(out.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:

                // copy new accelerometer data into accel array
                System.arraycopy(sensorEvent.values, 0, accelerationVector, 0, 3);
                break;
            case Sensor.TYPE_GYROSCOPE:

                System.arraycopy(sensorEvent.values, 0, gyroVector, 0, 3);
            break;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
