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
public class GyroSensor implements Runnable, SensorEventListener {

    /**********************************************************************************************/
    public static final String TAG = "Gyro-Thread:";
    private Context context;
    private Timer fuseTimer = new Timer();
    private SensorManager mSensorManager;
    private Sensor sensor;
    private GyroEventListener listener;
    private Vibrator hapticEngine;
    private ToneGenerator toneGen1;
    /**********************************************************************************************/
    private static final int SAMPLE_RATE = 20; // in ms => 50Hz
    private static final float alpha = .0625f, alphaINV = .9375f;
    private float lastX, lastY, lastZ;
    private float[] gyroVector = new float[3]; // accelerometer vector x,y,z
    /********************************************************************************************/
    private boolean running;
    private long issueTime = 0;

    public GyroSensor(Context in) {
        Log.d(TAG, "Building new gyro sensor");

        context = in;
        // get sensorManager and initialise sensor listeners
        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, sensor , SensorManager.SENSOR_DELAY_GAME);

        toneGen1 = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        hapticEngine = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public boolean isRunning() {
        return running;
    }

    public interface GyroEventListener {
        void onTrendelenburgSpike();
    }
    public void setListener(GyroEventListener in){
        listener = in;
    }
    @Override
    public void run() {
        Log.d(TAG, "Starting Gyro Filter");
        running = true;
        fuseTimer.scheduleAtFixedRate(new filterTask(),1000, SAMPLE_RATE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_GYROSCOPE:
                // copy new accelerometer data into accel array
                System.arraycopy(event.values, 0, gyroVector, 0, 3);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void Shutdown() {
        fuseTimer.cancel();
        fuseTimer.purge();
        mSensorManager.unregisterListener(this);
    }
    public void pause(){

    }
    private class filterTask extends TimerTask {
        public void run() {

            // Run a smoothing filter on the data.
            lastX = alpha* gyroVector[0] + alphaINV*lastX;
            gyroVector[0] = lastX;
            lastY = alpha* gyroVector[1] + alphaINV*lastY;
            gyroVector[1] = lastY;
            lastZ = alpha* gyroVector[2] + alphaINV*lastZ;
            gyroVector[2] = lastZ;

            // use for acceleration
            processZ(gyroVector[2]);
        }
    };

    private void processZ(float zRad){

        // Step 1 is to square x to highlight the larger values of accleleration
        zRad = zRad*zRad;

        // Step 2 is to filter for a trendelenburg spike
        if(zRad > .08){
            if((System.currentTimeMillis()-issueTime) > 1000){
                toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT,100);
                hapticEngine.vibrate(100);
                listener.onTrendelenburgSpike();
                issueTime = System.currentTimeMillis();
            }
        }
    }
}