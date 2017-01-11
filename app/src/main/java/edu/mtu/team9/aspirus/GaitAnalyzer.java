package edu.mtu.team9.aspirus;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by nssch on 1/10/2017.
 */
public class GaitAnalyzer {

    public int total_time, split_left, split_right;
    public Anklet leftAnklet, rightAnklet;
    private Timer timer;
    private OrientationEngine orientationEngine;
    public static final String TAG = "Gait-Analyzer";
    private Thread oeThread;
    private double[] orientation = new double[3];

    final double conversionC = 180/Math.PI;

    // Constructor
    GaitAnalyzer(OrientationEngine in, Anklet leftAnklet, Anklet rightAnklet)
    {
        this.leftAnklet = leftAnklet;
        this.rightAnklet = rightAnklet;
        orientationEngine = in;
        oeThread = new Thread();
        oeThread = new Thread(orientationEngine);
       //oeThread.start();
        timer = new Timer();
        //timer.schedule(mainLoop, 100);

    }

    public void processStep(Anklet anklet, byte[] data){

        // Extract the step time
        int time =  ((0xFF & data[5]) << 24) | ((0xFF & data[4]) << 16) |
                    ((0xFF & data[3]) << 8) | (0xFF & data[2]);

        // Extract  the step length
        // int length = (0xFF & data[6]) | 0;

        Log.d(TAG, "Data Message [" + (char)anklet.anklet_id + "D |" + time + " | " + data[6] + " | " + data[7] + " ]");

        // Update the anklet stats
        anklet.TOTAL_STEPS += 1;
        anklet.TOTAL_TIME += time;

        // Update internal metrics

    }

    // Main loop that runs to watch all data fields. Executes audio corrections.
    private TimerTask mainLoop = new TimerTask() {
        @Override
        public void run() {

            // Get the array or orientation values
            orientation[0] = orientationEngine.fusedOrientation[0]*conversionC; // Azimuth Z
            orientation[0] = orientationEngine.fusedOrientation[0]*conversionC; // Pitch X
            orientation[0] = orientationEngine.fusedOrientation[0]*conversionC; // Roll Y

            // Evaluate based on history and known problem thresholds

        }
    };

    public void shutdown()
    {

        orientationEngine.Shutdown();
        timer.cancel();
        timer.purge();
    }
}
