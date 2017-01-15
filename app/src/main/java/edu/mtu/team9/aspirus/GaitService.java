package edu.mtu.team9.aspirus;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class GaitService extends Service {

    public static final String TAG = "Gait-Service";
    public final static String ACTION_ANKLETS_READY = "ANKLETS_READY",
                                ACTION_STEP_MESSAGEL = "STEP_MESSAGE_L",
                                ACTION_STEP_MESSAGER = "STEP_MESSAGE_R";
    private final IBinder myBinder = new MyLocalBinder();
    private static final String LEFT_ANKLET_ADDRESS = "C3:02:46:89:C4:DC";
    private static final String RIGHT_ANKLET_ADDRESS = "E2:6D:EE:37:74:1E";
    public Anklet leftAnklet;
    public Anklet rightAnklet;
    public boolean SERVICE_READY = false;
    private BluetoothManager mBluetoothManager;
    private Handler handler = new Handler();

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return myBinder;
    }

    public class MyLocalBinder extends Binder {
        GaitService getService() {
            return GaitService.this;
        }
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public boolean initialize() {

        Log.d(TAG, "initializing gait service...");

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        leftAnklet = new Anklet(LEFT_ANKLET_ADDRESS, 'L', getApplicationContext());
        rightAnklet = new Anklet(RIGHT_ANKLET_ADDRESS, 'R', getApplicationContext());
        leftAnklet.setAnkletListener(ankletListener);
        rightAnklet.setAnkletListener(ankletListener);
        leftAnklet.connectAnklet();

        Log.d(TAG, "gait service ready!");
        return true;
    }

    public Anklet.AnkletListener ankletListener = new Anklet.AnkletListener() {
        @Override
        public void onStrideMessage(char id) {
            Log.d(TAG, "onStrideMessage: " + id);

        }

        @Override
        public void onHeelDown(char anklet_id) {
            Log.d(TAG, "onHeelDown: " + anklet_id);

        }

        @Override
        public void onAnkletReady(char anklet_id) {
            Log.d(TAG, "onAnkletReady: " + anklet_id);
            if(anklet_id == 'L'){
                rightAnklet.connectAnklet();
            }else if(anklet_id == 'R'){
                updateUI();
            }
        }

        @Override
        public void onLiftOff(char anklet_id) {
            Log.d(TAG, "onLiftOff: " + anklet_id);
        }
    };

    private void updateUI(){

        if(rightAnklet.ankletState == ANKLET_STATE.READY && leftAnklet.ankletState == ANKLET_STATE.READY){
            if(!SERVICE_READY){
                Log.d(TAG, "System is ready to go, altering UI thread...");
                broadcastUpdate(ACTION_ANKLETS_READY);
                SERVICE_READY = true;
            }
        }else{
            SERVICE_READY = false;
        }
    }

    public boolean startSystem(){

        // Start both the anklets
        leftAnklet.startAnklet();
        rightAnklet.startAnklet();
        return true;
    }

    public boolean stopSystem(){

        // Stop both the anklets
        leftAnklet.shutdownAnklet();
        rightAnklet.shutdownAnklet();
        return true;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
    }

}


