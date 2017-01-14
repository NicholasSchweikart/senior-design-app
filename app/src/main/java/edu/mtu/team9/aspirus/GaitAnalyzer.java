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
