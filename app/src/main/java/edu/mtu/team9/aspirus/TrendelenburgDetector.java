package edu.mtu.team9.aspirus;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Vibrator;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by nssch on 9/30/2016.
 */
public class TrendelenburgDetector extends Thread implements Runnable, SensorEventListener {
    public static final String TAG = "Gyro-Thread:";

    // System Components
    private Timer fuseTimer = new Timer();
    private SensorManager mSensorManager;
    private TrendelenburgEventListener listener;
    private Vibrator hapticEngine;
    private ToneGenerator toneGenerator;

    // Constants
    private static final int SAMPLE_RATE = 20;                      // in ms => 50Hz
    private static final float alpha = .0625f, alphaINV = .9375f;   // Filter stuff
    private static final int FEEDBACK_TIME_LENGTH = 100;            // 100ms long sounds and vibrations
    private static final double DETECTION_THRESHOLD = .08;          // When to detect Trendelenburg Event
    private static final int FEEDBACK_MIN_PERIOD = 1000;            // Min time between feedback events

    // Data variables
    private float lastX, lastY, lastZ;
    private float[] gyroVector = new float[3]; // accelerometer vector x,y,z

    // Control variables
    private boolean running;
    private long lastCommandIssueTime = 0;

    public TrendelenburgDetector(Context context, TrendelenburgEventListener listener) {
        Log.d(TAG, "Starting new Trendelenburg Detector");

        this.listener = listener;

        // get sensorManager and initialise sensor listeners
        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, sensor , SensorManager.SENSOR_DELAY_GAME);

        // Create the output sound and vibration devices
        toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        hapticEngine = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public interface TrendelenburgEventListener {
        void onTrendelenburgEvent();
    }

    @Override
    public void run() {
        Log.d(TAG, "Starting Gyro Filter");

        running = true;

        // Start filter in 1 second, with our predefined period.
        fuseTimer.scheduleAtFixedRate(new filterTask(),1000, SAMPLE_RATE);
    }

    /***********************************************************************************************
     * Class Control Methods
     **********************************************************************************************/
    public void Shutdown() {
        fuseTimer.cancel();
        fuseTimer.purge();
        mSensorManager.unregisterListener(this);
    }

    /***********************************************************************************************
     * Operational Logic
     **********************************************************************************************/
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_GYROSCOPE:

                // Copy new accelerometer data into gyro array
                System.arraycopy(event.values, 0, gyroVector, 0, 3);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     *  Performs data filtering on gyroVector to remove noise. Also detects and issues response for
     *  any Trendelenburg like activity.
     */
    private class filterTask extends TimerTask {
        public void run() {

            // Run a smoothing filter on the data.
            lastX = alpha* gyroVector[0] + alphaINV*lastX;
            gyroVector[0] = lastX;
            lastY = alpha* gyroVector[1] + alphaINV*lastY;
            gyroVector[1] = lastY;
            lastZ = alpha* gyroVector[2] + alphaINV*lastZ;
            gyroVector[2] = lastZ;

            /*** Process new values for Trendelenburg Designators ***/
            if((System.currentTimeMillis()- lastCommandIssueTime) > FEEDBACK_MIN_PERIOD){

                // Step 1 is to square the z rads/s
                float zImpulse = lastZ * lastZ;

                // If it beats our threshold then react
                if(zImpulse > DETECTION_THRESHOLD){

                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, FEEDBACK_TIME_LENGTH);
                    hapticEngine.vibrate(FEEDBACK_TIME_LENGTH);
                    listener.onTrendelenburgEvent();
                    lastCommandIssueTime = System.currentTimeMillis();

                }

            }

        }
    };

}