package edu.mtu.team9.aspirus;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

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

public class Anklet implements BleManager.BleManagerListener{

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
    private int totalTime,
            totalSteps,
            totalSwingTime,
            totalStanceTime,
            averageStepTime;

    public boolean in_stride = false;

    protected BleManager mBleManager;
    protected BluetoothGattService mUartService;
    private boolean isRxNotificationEnabled = false;

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
        mBleManager = new BleManager(context);
        mBleManager.setBleListener(this);
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

    public int getTotalTime(){return totalTime;}

    public int getTotalSteps(){return totalSteps;}

    public boolean isReady(){
        if(ankletState == ANKLET_STATE.READY)
            return true;
        return false;
    }

    public void connect(){
        mBleManager.connect(context,device_address);
    }

    public void startAnklet() {
        Log.d(TAG, "Sending Start Command");

        byte[] message = new byte[2];
        message[0] = COMMAND;
        message[1] = START;
        sendData(message);
    }

    public void pauseAnklet() {
        Log.d(TAG, "Sending Pause Command");

        byte[] message = new byte[2];
        message[0] = COMMAND;
        message[1] = PAUSE;
        sendData(message);
    }

    public void stopAnklet() {
        Log.d(TAG, "Sending Stop Command");

        byte[] message = new byte[2];
        message[0] = COMMAND;
        message[1] = STOP;
        sendData(message);
        shutDown();
    }

    private void sendHandshake() {
        Log.d(TAG, "Sending Handshake");

        byte[] message = new byte[2];
        message[0] = HANDSHAKE;
        message[1] = ((byte) anklet_id);
        sendData(message);
    }

    /***********************************************************************************************
        Bluetooth Interface Section
     **********************************************************************************************/

    private void sendData(byte[] data) {
        if (mUartService != null) {
            // Split the value into chunks (UART service has a maximum number of characters that can be written )
            for (int i = 0; i < data.length; i += 20) {
                final byte[] chunk = Arrays.copyOfRange(data, i, Math.min(i + 20, data.length));
                mBleManager.writeService(mUartService, UUID_TX, chunk);
            }
        } else {
            Log.w(TAG, "Uart Service not discovered. Unable to send data");
        }
    }

    @Override
    public void onConnecting() {
        Log.d(TAG, "onConnecting");
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "onConnected");
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "onDisconnected");
        ankletState = ANKLET_STATE.DISCONNECTED;
    }

    @Override
    public void onServicesDiscovered() {
        Log.d(TAG, "onServicesDiscovered");
        mUartService = mBleManager.getGattService(UUID_SERVICE);
        enableRxNotifications();
        sendHandshake();
    }

    protected void enableRxNotifications() {
        isRxNotificationEnabled = true;
        mBleManager.enableNotification(mUartService, UUID_RX, true);
    }

    @Override
    public synchronized void onDataAvailable(BluetoothGattCharacteristic characteristic) {

        if (characteristic.getService().getUuid().toString().equalsIgnoreCase(UUID_SERVICE)) {
            if (characteristic.getUuid().toString().equalsIgnoreCase(UUID_RX)) {

                Log.d(TAG, "RX data");

                final byte[] bytes = characteristic.getValue();
                processMessage(bytes);
            }
        }

    }

    @Override
    public void onDataAvailable(BluetoothGattDescriptor descriptor) {

    }

    @Override
    public void onReadRemoteRssi(int rssi) {

    }

    public void shutDown(){
        mBleManager.disconnect();
        mBleManager = null;
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

            case EVENT:

                if (data[1] == HEEL_DOWN)
                    listener.onHeelDown(anklet_id);

                if (data[1]== LIFT_OFF)
                    listener.onLiftOff(anklet_id);
                break;

            case STATUS:

                if (data[1] == READY) {
                    Log.d(TAG, "ready message");
                    ankletState = ANKLET_STATE.READY;
                    listener.onAnkletReady(anklet_id);
                }

                if (data[1] == RUNNING){
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
