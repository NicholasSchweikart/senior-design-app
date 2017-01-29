package edu.mtu.team9.aspirus;

import android.app.Service;
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

    public boolean SERVICE_READY = false;
    private boolean SERVICE_RUNNING = false;
    private BluetoothManager mBluetoothManager;
    private Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
            }
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");


        this.stopSelf();
    }

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

    public BleAnklet.AnkletListener ankletListener = new BleAnklet.AnkletListener() {
        @Override
        public void onStrideMessage(char id) {
            Log.d(TAG, "onStrideMessage: " + id);
            if(id == 'L')
                broadcastUpdate(ACTION_STEP_MESSAGEL);
            if(id == 'R')
                broadcastUpdate(ACTION_STEP_MESSAGER);
        }

        @Override
        public void onAnkletReady(char anklet_id) {
            Log.d(TAG, "onAnkletReady: " + anklet_id);

            if(true){
                if(!SERVICE_READY){
                    Log.d(TAG, "System is ready to go, altering UI thread...");
                    broadcastUpdate(ACTION_ANKLETS_READY);
                    SERVICE_READY = true;
                }
            }else{
                SERVICE_READY = false;
            }
        }


    };

    public void startSystem(){

        SERVICE_RUNNING = true;

    }

    public void pauseSystem() {
        SERVICE_RUNNING = false;
    }

    public void stopSystem(){

    }

    public boolean isSERVICE_RUNNING(){
        return this.SERVICE_RUNNING;
    }

//    public int[] getTimeArrayLeft()
//    {
//        return leftAnklet.getTimeArray();
//    }
//    public int[] getTimeArrayRight()
//    {
//        return rightAnklet.getTimeArray();
//    }
}


