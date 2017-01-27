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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
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

public class Anklet extends BluetoothGattCallback{

    public static final UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    public static final String UUID_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_RX = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_TX = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";

    private String TAG;
    public ANKLET_STATE ankletState;
    private String device_address;
    public char anklet_id;
    private Timer timer = new Timer();
    // Gait Metrics
    public int totalTime,
            totalSteps,
            totalSwingTime,
            totalStanceTime,
            averageStepTime;

    public boolean in_stride = false;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    public final byte
            RUNNING =       (byte) 'U',
            READY =         (byte) 'R',
            STOP =          (byte) 'X',
            PAUSE=          (byte) 'P',
            MESSAGE =       (byte) 'M',
            COMMAND =       (byte) 'C',
            START =         (byte) 'S',
            STATUS =        (byte) 'S',
            HANDSHAKE =     (byte) 'H',
            LIFT_OFF  =     (byte) '^',
            HEEL_DOWN =     (byte) '_',
            EVENT =         (byte) 'E';

    private Context context;
    private AnkletListener listener;

    public Anklet(String device_address, char anklet_id, Context context) {

        this.listener = null;
        this.context = context;
        this.device_address = device_address;
        TAG = "Anklet-" + anklet_id;
        this.anklet_id = anklet_id;

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
        }
    }

    public interface AnkletListener {
        void onStrideMessage(char anklet_id);

        void onHeelDown(char anklet_id);

        void onAnkletReady(char anklet_id);

        void onLiftOff(char anklet_id);
    }

    public void setAnkletListener(AnkletListener listener) {
        this.listener = listener;
    }

    public int[] getTimeArray(){
        int[] out = new int[5];
        out[0] = totalTime;
        out[1] = totalSteps;
        out[2] = totalSwingTime;
        out[3] = totalStanceTime;
        out[4] = averageStepTime;
        return out;
    }

    public boolean isReady(){
        if(ankletState == ANKLET_STATE.READY)
            return true;
        return false;
    }

    public void connect(){

        if (mBluetoothAdapter == null) {
            Log.d(TAG, "BluetoothAdapter not initialized or unspecified address.");
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(device_address);

        if (device == null) {
            Log.d(TAG, "Left device not found.  Unable to connect.");
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        bluetoothGatt = device.connectGatt(context, false, this);

    }


    /***********************************************************************************************
        Bluetooth Interface Section
     **********************************************************************************************/

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {

            Log.d(TAG, "Connected to GATT server.");

            // Attempts to discover services after successful connection.
            Log.d(TAG, "Attempting to start service discovery" + bluetoothGatt.discoverServices());
            ankletState = ANKLET_STATE.READY;
            listener.onAnkletReady(anklet_id);
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

            Log.i(TAG, "Lost connection");
            ankletState = ANKLET_STATE.DISCONNECTED;
            bluetoothGatt.connect();
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {

        if (status == BluetoothGatt.GATT_SUCCESS) {

            Log.i(TAG, "onServicesDiscovered");

            if (enableTXNotification()) {
                Log.d(TAG, "onServicesDiscovered");

            } else {
                Log.d(TAG, "TX not enabled");
            }

        } else {
            Log.d(TAG, "onServicesDiscovered GATT failure");
        }

    }

    /**
     * Enable Notification on TX characteristic
     *
     * @return true if attempt successful
     */
    public boolean enableTXNotification() {

        BluetoothGattService RxService = bluetoothGatt.getService(RX_SERVICE_UUID);
        if (RxService == null) {
            showMessage("Rx services not found!");
            return false;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            showMessage("Tx characteristics not found!");
            return false;
        }
        bluetoothGatt.setCharacteristicNotification(TxChar, true);

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
        return true;
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onCharacteristicChanged");

        byte[] data = characteristic.getValue();
        processMessage(data);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt,BluetoothGattDescriptor descriptor, int status){
        Log.d(TAG, "onDescriptorWrite");
        if(status == BluetoothGatt.GATT_SUCCESS){
            //sendHandshake();
        }
    }

    public void shutDown(){
        bluetoothGatt.disconnect();
        bluetoothGatt.close();
    }
    /**********************************************************************************************/

    /***********************************************************************************************
     * Message Processing Section
     **********************************************************************************************/

    /**
     * Filter the incomming message for appropriate handling. Also fires events over the interface.
     * @param data the incomming data byte array
     */
    private void processMessage(byte[] data) {

        Log.d(TAG, "Processing Message: length = " + data.length);

        // Filter via the offset 0 flag field
        switch (data[0]) {

            case MESSAGE:

                listener.onStrideMessage(anklet_id);
                processStep(data);
                break;

            default:
                break;
        }

    }

    public void processStep(byte[] data) {

        // Extract the step_duration & swing_duration in microseconds
        int stepDuration = ((0xFF & data[4]) << 24) | ((0xFF & data[3]) << 16) |
                ((0xFF & data[2]) << 8) | (0xFF & data[1]);

        int swingDuration = ((0xFF & data[8]) << 24) | ((0xFF & data[7]) << 16) |
                ((0xFF & data[6]) << 8) | (0xFF & data[5]);

        Log.d(TAG, "Stride Message [ M|" + stepDuration + "|"+ swingDuration + "|" + data[9] + " ]");

        // Update the anklet stats
        totalSteps++;
        totalTime += swingDuration + stepDuration;
        totalSwingTime += swingDuration;
        totalStanceTime += stepDuration - swingDuration;
    }

    private void showMessage(String msg) {
        Log.e(TAG, msg);
    }
}
