package edu.mtu.team9.aspirus;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by nssch on 9/30/2016.
 */
public class OrientationEngine implements Runnable, SensorEventListener {

    /*****************************************************/
    public float[] accel = new float[3]; // accelerometer vector
    public static final float EPSILON = 0.000000001f;
    public static final int SAMPLE_PERIOD = 20; // 50Hz
    public static final float FILTER_COEFFICIENT = 0.98f;
    private Timer fuseTimer = new Timer();
    private SensorManager mSensorManager;
    private Sensor sensor;
    public Context context;

    /********************************************************************/

    public OrientationEngine(Context in) {
        context = in;

        // get sensorManager and initialise sensor listeners
        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor , SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void run() {
        Log.d("HERE", "HERE");

        fuseTimer.scheduleAtFixedRate(new filterTask(),1000, SAMPLE_PERIOD);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                // copy new accelerometer data into accel array
                System.arraycopy(event.values, 0, accel, 0, 3);
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

    private class filterTask extends TimerTask {
        public void run() {

        }
    };
}