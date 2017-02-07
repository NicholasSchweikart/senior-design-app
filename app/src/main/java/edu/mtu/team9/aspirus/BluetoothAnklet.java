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

    private final char ankletID;
    private final String deviceAddress;
    private String TAG;
    public BluetoothService bluetoothService;
    private AnkletListener listener;
    private boolean STATUS;
    private FileOutputStream loggingOutputStream;

    public BluetoothAnklet(String deviceAddress, char ankletID, BluetoothAdapter adapter, AnkletListener listener) {

        this.listener = listener;
        this.deviceAddress = deviceAddress;
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
            case AnkletConnection.CONNECTED:
                listener.onAnkletReady(ankletID);
                STATUS = true;
                break;
            case AnkletConnection.CONNECTION_FAILED:
                listener.onAnkletFailure(ankletID);
                break;
            case AnkletConnection.CONNECTION_LOST:
                //TODO implement autoreconnect feature
                break;
            default:
                break;
        }
    }

    @Override
    public void onDataRecieved(byte[] data) {

    }

    public interface AnkletListener {

        void onAnkletReady(char ankletID);

        void onAnkletFailure(char ankletID);
    }

    /***********************************************************************************************
     * Class Control Methods
     **********************************************************************************************/
    public boolean isReady() {
        return STATUS;
    }

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

    /***********************************************************************************************
     * Anklet Command Methods
     **********************************************************************************************/
    public void sendStart() {
        Log.d(TAG, "Sending start message >>>");

        byte[] startMessage = {'s'};
        bluetoothService.write(startMessage);
    }

    public void enableCSVoutput() {
        Log.d(TAG, "Sending enable CSV message >>>");

        byte[] csvEnableMessage = {'e'};
        bluetoothService.write(csvEnableMessage);
    }

    public void sendStop() {
        Log.d(TAG, "Sending stop message >>>");

        byte[] stopMessage = {'x'};
        bluetoothService.write(stopMessage);
    }

}