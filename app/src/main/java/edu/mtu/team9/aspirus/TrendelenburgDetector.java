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
 *  Created by Nicholas Schweikart, CPE, for Biomedical Senior Design Team 9
 *  Contact: nsschwei@mtu.edu
 *
 *  Description: This class implements a Trendelenburg Gait detector based off gyro acceleration on
 *  the Z axis.Essentially, it runs the data throw a low pass filter to remove noise, and then
 *  watches for a Z impulse that is higher than a set threshold. The Z gyro data is squared to
 *  exaggerate values closer too and greater than 1. This was found to get a good method with
 *  external analysis.
 */
public class TrendelenburgDetector extends Thread implements Runnable, SensorEventListener {
    public static final String TAG = "trendelenburg-detector";

    // System Components
    private Timer fuseTimer = new Timer();
    private SensorManager mSensorManager;
    private TrendelenburgEventListener listener;
    private Vibrator hapticEngine;
    private ToneGenerator toneGenerator;

    // Constants
    private static final int SAMPLE_RATE = 20;                      // period in ms => 50Hz
    private static final float alpha = .0625f, alphaINV = .9375f;   // Filter coefficients
    private static final int PHYSICAL_FEEDBACK_DURATION = 100;      // 100ms long sounds and vibrations
    private static final double DETECTION_THRESHOLD = .08;          // When to detect Trendelenburg Event
    private static final int PHYSICAL_FEEDBACK_MIN_PERIOD = 1000;   // Min time between feedback events in ms

    // Data variables
    private float lastZ = 0.0f, currentZ = 0.0f;
    private long lastCommandIssueTime = 0;

    public TrendelenburgDetector(Context context, TrendelenburgEventListener listener) {
        Log.d(TAG, "Starting Trendelenburg Detector");

        this.listener = listener;

        // Get sensorManager and initialise sensor listeners
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

        // Start filter in 1 second, with our predefined period.
        fuseTimer.scheduleAtFixedRate(new filterTask(),1000, SAMPLE_RATE);
    }

    /***********************************************************************************************
     * Class Control Methods
     **********************************************************************************************/

    /**
     * Cancels the timer task thread and unregisters all listeners.
     */
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

                // Update Current Gyro Data
                currentZ = event.values[2];
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // IGNORE EVENT
    }

    /**
     *  Performs data filtering on gyro data to remove noise. Also detects and issues response for
     *  any Trendelenburg like activity.
     */
    private class filterTask extends TimerTask {
        public void run() {

            // Run a smoothing filter on the data.
            lastZ = alpha* currentZ + alphaINV * lastZ;

            //Process new values for Trendelenburg Designators
            if((System.currentTimeMillis()- lastCommandIssueTime) > PHYSICAL_FEEDBACK_MIN_PERIOD){

                // Step 1 is to square the z rads/s
                float zImpulse = lastZ * lastZ;

                // If it beats our threshold then alert the user through physical feedback.
                if(zImpulse > DETECTION_THRESHOLD){

                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, PHYSICAL_FEEDBACK_DURATION);
                    hapticEngine.vibrate(PHYSICAL_FEEDBACK_DURATION);

                    // Update the UI thread and the gait session data.
                    listener.onTrendelenburgEvent();

                    // Reset the last command issue time.
                    lastCommandIssueTime = System.currentTimeMillis();
                }
            }
        }
    }
}