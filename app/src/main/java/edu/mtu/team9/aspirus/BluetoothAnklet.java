package edu.mtu.team9.aspirus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.UUID;

/**
 * Created for Aspirus2
 * By: nicholas on 1/29/17.
 * Description:
 */

public class BluetoothAnklet implements ConnectedThread.ConnectionListener{
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String TAG = "Bluetooth Anklet";
    private final BluetoothSocket socket;
    BluetoothAdapter mBluetoothAdapter;
    private ConnectedThread connectedThread;
    private boolean READY_FLAG = false;
    private int totalTime, totalSteps;

    public BluetoothAnklet(Context context, String address) {

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        BluetoothSocket tmp = null;
        try {
            tmp = device.createRfcommSocketToServiceRecord(myUUID);
        } catch (IOException e) { }
        socket =tmp;
        mBluetoothAdapter.cancelDiscovery();
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
        READY_FLAG = true;
    }

    public boolean isReady()
    {
        return READY_FLAG;
    }
    public int[] getTimeArray(){
        int[] out = new int[5];
        out[0] = totalTime;
        out[1] = totalSteps;
        return out;
    }
    @Override
    public void onData(byte[] data, int numbytes) {
        try {
            String out = new String(data, "US-ASCII");
            Log.d(TAG, "Recieved: "+out);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
