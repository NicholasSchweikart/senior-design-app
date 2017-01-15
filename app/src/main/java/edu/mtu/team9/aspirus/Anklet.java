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
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by nssch on 10/30/2016.
 */

public class Anklet {

    public static final UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    private String TAG;
    public ANKLET_STATE ankletState;
    private String device_address;
    public char anklet_id;
    public int total_time = 0;
    public int total_steps = 0;
    public boolean in_stride = false;
    private AnkletListener listener;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private Timer timer = new Timer();
    private int connection_attempts = 0;
    public final byte
            RUNNING =       (byte) 'U',
            READY =         (byte) 'R',
            STOP =          (byte) 'X',
            MESSAGE =       (byte) 'D',
            COMMAND =       (byte) 'C',
            START =         (byte) 'S',
            STATUS =        (byte) 'S',
            HANDSHAKE =     (byte) 'H',
            LIFT_OFF  =     (byte) '^',
            HEEL_DOWN =     (byte) '_',
            EVENT =         (byte) 'E';

    public byte[] message = new byte[3];

    private Context context;

    public interface AnkletListener {
        public void onStrideMessage(char anklet_id);

        public void onHeelDown(char anklet_id);

        public void onAnkletReady(char anklet_id);

        public void onLiftOff(char anklet_id);
    }

    public Anklet(String device_address, char anklet_id, Context context) {

        this.listener = null;
        this.context = context;
        this.device_address = device_address;
        TAG = "Anklet-Class-" + anklet_id;
        this.anklet_id = anklet_id;

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
        }

    }

    public void setAnkletListener(AnkletListener listener) {
        this.listener = listener;
    }

    public void startAnklet() {
        message[0] = COMMAND;
        message[1] = START;
        txAnklet(message);
    }

    public void pauseAnklet() {
        if (ankletState != ANKLET_STATE.RUNNING)
            return;
        message[0] = COMMAND;
        message[1] = STOP;//TODO implement complete pause functionality
        txAnklet(message);
    }

    public void shutdownAnklet() {
        message[0] = COMMAND;
        message[1] = STOP;
        txAnklet(message);

        if (mBluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.disconnect();
        Log.w(TAG, "bluetoothGatt closed");
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                Log.d(TAG, "Connected to right GATT server.");

                // Attempts to discover services after successful connection.
                Log.d(TAG, "Attempting to start service discovery" + bluetoothGatt.discoverServices());
                ankletState = ANKLET_STATE.CONNECTED;

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                Log.i(TAG, "Lost connection");
                ankletState = ANKLET_STATE.DISCONNECTED;
                bluetoothGatt.connect();//TODO flag system to trigger smart reconnect
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {

                Log.i(TAG, "Service Discovered");

                if (enableTXNotification()) {

                    Log.d(TAG, "TX ready on anklet");
                    ankletState = ANKLET_STATE.TXENABLED;

                    // Send handshake message in 2 seconds
                    timer.schedule(sendHandshake, 2000, 4000);

                } else {
                    Log.d(TAG, "TX not enabled");
                }

            } else {
                Log.d(TAG, "onServicesDiscovered GATT failure");
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharRead Left Anklet");

                byte[] data = characteristic.getValue();
                processMessage(data);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged Left Anklet");

            byte[] data = characteristic.getValue();
            processMessage(data);
        }
    };

    private TimerTask sendHandshake = new TimerTask() {

        @Override
        public void run() {

            message[0] = HANDSHAKE;
            message[1] = ((byte) anklet_id);
            connection_attempts++;
            txAnklet(message);
            if (connection_attempts == 2) {
                timer.purge();
                timer.cancel();
                //TODO throw connection error to service
            }
        }
    };

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connectAnklet() {

        if (mBluetoothAdapter == null) {
            Log.d(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(device_address);

        if (device == null) {
            Log.d(TAG, "Left device not found.  Unable to connect.");
            return false;
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        bluetoothGatt = device.connectGatt(context, false, mGattCallback);

        return true;
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

    /**
     * Transmits a message over BLE to an anklet
     * @param value of data to send over BLE
     */
    private void txAnklet(byte[] value) {

        BluetoothGattService RxService = bluetoothGatt.getService(RX_SERVICE_UUID);
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);

        if (RxService == null) {
            showMessage("Rx services not found!");
            return;
        }

        if (RxChar == null) {
            showMessage("Rx characteristics not found!");
            return;
        }

        RxChar.setValue(value);
        boolean status = bluetoothGatt.writeCharacteristic(RxChar);
        String txval = "";
        try {
            txval = new String(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "TX to anklet [" + txval + "] status: " + status);
    }

    private void showMessage(String msg) {
        Log.e(TAG, msg);
    }

    private void processMessage(byte[] data) {

        String got = "XXX";
        try {
            got = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Processing [" + got + "]");

        // Filter via the offset 0 flag field
        switch (data[0]) {

            case MESSAGE:

                listener.onStrideMessage(anklet_id);
                processStep(data);
                break;

            case EVENT:

                if (data[1] == HEEL_DOWN)
                    listener.onHeelDown(anklet_id);

                if (data[1]== LIFT_OFF)
                    listener.onLiftOff(anklet_id);
                break;

            case STATUS:

                if (data[2] == READY) {
                    Log.d(TAG, "ready message");

                    ankletState = ANKLET_STATE.READY;
                    timer.purge();
                    timer.cancel();
                    listener.onAnkletReady(anklet_id);
                }

                if (data[2] == RUNNING){
                    Log.d(TAG, "running message");
                    ankletState = ANKLET_STATE.RUNNING;
                }

                break;

            default:
                break;
        }

    }

    public void processStep(byte[] data) {

        // Extract the step_duration & swing_duration in microseconds
        int step_duration = ((0xFF & data[4]) << 24) | ((0xFF & data[3]) << 16) |
                ((0xFF & data[2]) << 8) | (0xFF & data[1]);

        int swing_duration = ((0xFF & data[8]) << 24) | ((0xFF & data[7]) << 16) |
                ((0xFF & data[6]) << 8) | (0xFF & data[5]);

        //TODO Extract  the step length this method may be wrong
        char step_length = ((char) data[9]);

        Log.d(TAG, "Stride Message [ M|" + step_duration + "|"+ swing_duration + "|" + ((int) step_length) + "|" + data[5] + " ]");

        // Update the anklet stats
        total_steps += 1;

    }
}
