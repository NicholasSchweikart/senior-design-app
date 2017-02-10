package edu.mtu.team9.aspirus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created for Aspirus2
 * By: nssch on 10/30/2016.
 * Description: This class builds an API and object layer around the wireless 'Anklets' for the
 * gait project. The following class handles all Bluetooth Comm. as well as the recording of
 * various gait metrics.
 */

public class BluetoothAnklet implements BluetoothService.BluetoothLinkListener {
    private String TAG;

    // System Components
    private final char ankletID;
    public BluetoothService bluetoothService;
    private AnkletListener listener;
    private FileOutputStream loggingOutputStream;

    // Control Variables
    private boolean CSV_IS_ENABLED = false;
    private int ankletState = AnkletConst.STATE_CONNECTING;
    private boolean ACTIVE = false;

    // Constants
    private Double accelerationSum;
    private int accelerationsLogged = 0;

    public BluetoothAnklet(String deviceAddress, char ankletID, BluetoothAdapter adapter, AnkletListener listener) {

        this.listener = listener;
        accelerationSum = 0.0;
        TAG = "BluetoothAnklet-" + ankletID;
        this.ankletID = ankletID;
        BluetoothDevice device = adapter.getRemoteDevice(deviceAddress);
        bluetoothService = new BluetoothService(device, this);
        bluetoothService.connect();
    }

    @Override
    public void onStateChange(int state) {
        Log.d(TAG, "onStateChange()");

        switch (state) {
            case AnkletConst.CONNECTED:
                ankletState = AnkletConst.STATE_CONNECTED;

                listener.onAnkletReady(ankletID);
                break;
            case AnkletConst.CONNECTION_FAILED:
                ankletState = AnkletConst.STATE_CONNECTING;
                listener.onAnkletFailure(ankletID);
                break;
            case AnkletConst.CONNECTION_LOST:
                ankletState = AnkletConst.STATE_CONNECTING;
                //TODO implement autoreconnect feature
                break;
            default:
                break;
        }
    }

    public interface AnkletListener {

        void onAnkletReady(char ankletID);

        void onAnkletFailure(char ankletID);
    }

    /***********************************************************************************************
     * Class Control Methods
     **********************************************************************************************/

    public void shutdown() {
        bluetoothService.stop();
    }

    public void enableFileLogging(File loggingOutputFile) {
        Log.d(TAG, "Enabling file logging in bluetooth service");

        try {
            this.loggingOutputStream = new FileOutputStream(loggingOutputFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
        bluetoothService.setLoggingEnabled(loggingOutputStream);
    }

    public Double getAvgAcceleration(){
        Double out = accelerationSum/accelerationsLogged;
        accelerationSum = 0.0;
        accelerationsLogged = 0;
        return out;
    }
    /***********************************************************************************************
     * Anklet Command Methods
     **********************************************************************************************/
    public void sendStart() {

        if(ankletState <= AnkletConst.STATE_CONNECTED){
            Log.d(TAG, "Sending start message >>>");
            bluetoothService.write(AnkletConst.START_MESSAGE);
        }else{
            Log.e(TAG, "Cannot Send Start");
        }
    }

    public void enableCSVoutput() {

        if(ankletState <= AnkletConst.STATE_CONNECTED){
            Log.d(TAG, "Sending enable CSV message >>>");
            bluetoothService.write(AnkletConst.ENABLE_CSV_MESSAGE);
        }else{
            Log.e(TAG, "NO CONNECTION CANT SEND CSV ENABLE");
        }
    }

    public void sendStop() {
        if(ankletState <= AnkletConst.STATE_RUNNING){
            Log.d(TAG, "Sending stop message >>>");
            bluetoothService.write(AnkletConst.STOP_MESSAGE);
        }else{
            Log.e(TAG, "NOT RUNNING CANT STOP");
        }
    }

    public void activate(){
        ACTIVE = true;
    }

    public void deActivate(){
        ACTIVE = false;
    }
    /***********************************************************************************************
     * Message Processing !!! \n triggers every onDataRecieved event
     **********************************************************************************************/
    @Override
    public void onDataRecieved(byte[] data) {

        if(!ACTIVE)
            return;

        String s = new String(data);
        try{
            accelerationSum += Double.valueOf(s);
            accelerationsLogged += 1;
            Log.d(TAG,"New AVG: " + accelerationSum.toString());
        }catch (Exception e){
            Log.d(TAG,"New AVG: bad Value");
        }

    }

}