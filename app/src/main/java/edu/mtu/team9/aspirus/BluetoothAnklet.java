package edu.mtu.team9.aspirus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created for Aspirus2
 * By: nssch on 10/30/2016.
 * Description: This class builds an API and object layer around the wireless 'Anklets' for the
 * gait project. The following class handles all Bluetooth Comm. as well as the recording of
 * various gait metrics.
 */

public class BluetoothAnklet implements BluetoothService.BluetoothLinkListener{

    private final char ankletID;
    private final String deviceAddress;
    private String TAG;
    private BluetoothDevice device;
    private BluetoothService bluetoothService;
    private AnkletListener listener;
    private boolean STATUS;
    public String accel = "0,0,0";
    public BluetoothAnklet(String deviceAddress, char ankletID, BluetoothAdapter adapter, AnkletListener listener) {

        this.listener = listener;
        this.deviceAddress = deviceAddress;
        TAG = "BluetoothAnklet-" + ankletID;
        this.ankletID = ankletID;
        device = adapter.getRemoteDevice(deviceAddress);
        bluetoothService = new BluetoothService(device, this);
        bluetoothService.connect();
    }

    @Override
    public void onStateChange(int state) {
        Log.d(TAG, "onStateChange()");
        switch (state){
            case AnkletConnection.CONNECTED:
                listener.onAnkletReady(ankletID);
                STATUS = true;
                break;
            case AnkletConnection.CONNECTION_FAILED:
                listener.onAnkletFailure(ankletID);
                break;
            case AnkletConnection.CONNECTION_LOST:

                break;
            default:
                break;
        }
    }

    @Override
    public void onDataRecieved(String data) {
        accel = data.substring(0,data.length()-2);
    }

    public interface AnkletListener {

        void onAnkletReady(char ankletID);
        void onAnkletFailure(char ankletID);
    }

    public void setAnkletListener(AnkletListener listener) {
        this.listener = listener;
    }

    public boolean isReady(){
        return STATUS;
    }

    public void shutdown()
    {
        bluetoothService.stop();
    }


    /***********************************************************************************************
     * Message Processing Section
     **********************************************************************************************/


}