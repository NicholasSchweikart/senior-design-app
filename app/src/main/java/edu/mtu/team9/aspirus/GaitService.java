package edu.mtu.team9.aspirus;

import android.app.Service;
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
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class GaitService extends Service {

    public static final String TAG = "Gait-Service";

    public final static String ACTION_ANKLETS_READY = "ANKLETS_READY",
                                ACTION_STEP_MESSAGEL = "STEP_MESSAGE_L",
                                ACTION_STEP_MESSAGER = "STEP_MESSAGE_R";

    private final IBinder myBinder = new MyLocalBinder();

    private static final String LEFT_ANKLET_ADDRESS = "C3:02:46:89:C4:DC";
    private static final String RIGHT_ANKLET_ADDRESS = "E2:6D:EE:37:74:1E";

    public Anklet leftAnklet = new Anklet(LEFT_ANKLET_ADDRESS, 'L');
    public Anklet rightAnklet = new Anklet(RIGHT_ANKLET_ADDRESS, 'R');
    public int TOTAL_TIME = 0;

    private boolean SYSTEM_READY = false;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt bluetoothGattL, bluetoothGattR;

    public static final UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    public final byte RUNNING = (byte) 'U',
            READY = (byte) 'R',
            STOP = (byte) 'X',
            DATA = (byte) 'D',
            COMMAND = (byte) 'C',
            START = (byte) 'S',
            STATUS = (byte) 'S',
            HANDSHAKE = (byte) 'H';

    private Handler handler = new Handler();
    private GaitAnalyzer gaitAnalyzer;
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

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        if(!connectSystem()){
            return false;
        }

        gaitAnalyzer = new GaitAnalyzer(new OrientationEngine(getApplicationContext()),leftAnklet, rightAnklet);

        Log.d(TAG, "gait service ready!");
        return true;
    }

    public boolean connectSystem(){

        if(connectLeft()){
            Log.d(TAG,"Left anklet connection made!");
        }else{
            return false;
        }

        if(connectRight()){
            Log.d(TAG,"Left anklet connection made!");
        }else {
            return false;
        }
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connectLeft() {

        if (mBluetoothAdapter == null) {
            Log.d(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(leftAnklet.DEVICE_ADDRESS);

        if (device == null ) {
            Log.d(TAG, "Left device not found.  Unable to connect.");
            return false;
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        bluetoothGattL = device.connectGatt(this, false, mGattCallbackL);

        return true;
    }

    public boolean connectRight() {

        if (mBluetoothAdapter == null) {
            Log.d(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        final BluetoothDevice device2 = mBluetoothAdapter.getRemoteDevice(rightAnklet.DEVICE_ADDRESS);

        if (device2 == null ) {
            Log.d(TAG, "Right device not found.  Unable to connect.");
            return false;
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        bluetoothGattR = device2.connectGatt(this, false, mGattCallbackR);

        return true;
    }



    /**
     * Enable Notification on TX characteristic
     *
     * @return
     */
    public boolean enableTXNotification(BluetoothGatt gatt)
    {

        BluetoothGattService RxService = gatt.getService(RX_SERVICE_UUID);
        if (RxService == null ) {
            showMessage("Rx services not found!");
            return false;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            showMessage("Tx characteristics not found!");
            return false;
        }
        gatt.setCharacteristicNotification(TxChar,true);

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
        return true;
    }


    public void txAnklet(byte[] value, BluetoothGatt gatt)
    {
        BluetoothGattService RxService = gatt.getService(RX_SERVICE_UUID);
        BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);

        if (RxService == null) {
            showMessage("Rx services not found!");
            return;
        }

        if (RxChar == null ) {
            showMessage("Rx characteristics not found!");
            return;
        }

        RxChar.setValue(value);
        boolean status = gatt.writeCharacteristic(RxChar);
        String txval = "";
        try {
            txval = new String(value,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "TX to anklet ["+ txval + "] status: " + status);
    }

    private void processInput( byte[] data) {

        Anklet anklet = null;
        byte anklet_id = data[0];
        byte message_type = data[1];

        if (anklet_id == leftAnklet.anklet_id)
            anklet = leftAnklet;

        if (anklet_id == rightAnklet.anklet_id)
            anklet = rightAnklet;

        if(anklet == null)
            return;

        switch (message_type) {
            case DATA:
                gaitAnalyzer.processStep(anklet, data);
                break;

            case STATUS:
                if (data[2] == READY){
                    Log.d(TAG, "got ready message");
                    anklet.ankletState = ANKLET_STATE.READY;
                    updateUI();
                }


                if (data[2] == RUNNING)
                    anklet.ankletState = ANKLET_STATE.RUNNING;
                break;
            default:
                break;
        }

        String got="XXX";
        try {
            got = new String(data,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Processing ["+ got +"]");
    }

    private final BluetoothGattCallback mGattCallbackL = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                Log.i(TAG, "Connected to left GATT server.");

                Log.i(TAG, "Attempting to start service discovery L:" + bluetoothGattL.discoverServices());
                leftAnklet.ankletState = ANKLET_STATE.CONNECTED;

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Lost connection with L:");
                leftAnklet.ankletState = ANKLET_STATE.DISCONNECTED;
                bluetoothGattL.connect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            // Enable TX stuff here prbs.
            if (status == BluetoothGatt.GATT_SUCCESS) {

                Log.i(TAG, "Service Discovered L:");
                if( enableTXNotification(gatt) ){
                    Log.d(TAG, "TX ready on left anklet");
                    leftAnklet.ankletState = ANKLET_STATE.TXENABLED;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            txAnklet(leftAnklet.handshakeMessage,bluetoothGattL);
                        }
                    }, 2000);

                }else{
                    Log.d(TAG, "TX not enabled on left anklet");
                }

            } else {
                Log.d(TAG, "onServicesDiscovered left GATT failure");
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharRead Left Anklet");
                byte[] data = characteristic.getValue();
                processInput(data);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharChanged Left Anklet");
            byte[] data = characteristic.getValue();
            processInput(data);
        }
    };

    private final BluetoothGattCallback mGattCallbackR = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                Log.d(TAG, "Connected to right GATT server.");

                // Attempts to discover services after successful connection.
                Log.d(TAG, "Attempting to start service discovery R:" + bluetoothGattR.discoverServices());
                rightAnklet.ankletState = ANKLET_STATE.CONNECTED;

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                Log.i(TAG, "Lost connection with R:");
                rightAnklet.ankletState = ANKLET_STATE.DISCONNECTED;
                bluetoothGattR.connect();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            // Enable TX stuff here prbs.
            if (status == BluetoothGatt.GATT_SUCCESS) {

                Log.d(TAG, "Service Discovered R:");
                if( enableTXNotification(gatt) ){
                    Log.d(TAG, "TX ready on right anklet");
                    rightAnklet.ankletState = ANKLET_STATE.TXENABLED;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            txAnklet(rightAnklet.handshakeMessage, bluetoothGattR);
                        }
                    }, 2000);
                }else{
                    Log.d(TAG, "TX not enabled on right anklet");
                }
            } else {
                Log.d(TAG, "onServicesDiscovered right received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharRead Right Anklet");
                byte[] data = characteristic.getValue();
                processInput(data);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharChanged Right Anklet");
            byte[] data = characteristic.getValue();
            processInput(data);
        }
    };

    private void updateUI(){
        if(rightAnklet.ankletState == ANKLET_STATE.READY && leftAnklet.ankletState == ANKLET_STATE.READY){
            if(!SYSTEM_READY){
                Log.d(TAG, "System is ready to go, altering UI thread...");
                broadcastUpdate(ACTION_ANKLETS_READY);
                SYSTEM_READY = true;
            }
        }else{
            SYSTEM_READY = false;
        }
    }

    public boolean startSystem(){
        if(leftAnklet.ankletState != ANKLET_STATE.READY)
            return false;
        if(rightAnklet.ankletState != ANKLET_STATE.READY)
            return false;

        // Start both the anklets
        txAnklet(leftAnklet.startMessage, bluetoothGattL);
        txAnklet(rightAnklet.startMessage, bluetoothGattR);
        return true;
    }

    public boolean stopSystem()
    {
        if(leftAnklet.ankletState != ANKLET_STATE.RUNNING)
            return false;
        if(rightAnklet.ankletState != ANKLET_STATE.RUNNING)
            return false;

        // Stop both the anklets
        txAnklet(leftAnklet.stopMessage, bluetoothGattL);
        txAnklet(rightAnklet.stopMessage, bluetoothGattR);
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect(BluetoothGatt gatt) {
        if (mBluetoothAdapter == null || gatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        gatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (bluetoothGattL == null || bluetoothGattR == null) {
            return;
        }
        Log.w(TAG, "bluetoothGatts closed");

        bluetoothGattL.close();
        bluetoothGattL = null;
        bluetoothGattR.close();
        bluetoothGattR = null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {

    }

    private void showMessage(String msg) {
        Log.e(TAG, msg);
    }
}


