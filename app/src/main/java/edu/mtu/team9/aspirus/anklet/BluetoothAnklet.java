package edu.mtu.team9.aspirus.anklet;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Created by Nicholas Schweikart, CPE, for Biomedical Senior Design Team 9
 *
 * Description: This class builds an API around the wireless 'Anklets' for the gait project.
 * It handles all Bluetooth Communication as well as the recording of various gait metrics.
 */
public class BluetoothAnklet implements BluetoothService.BluetoothLinkListener {
    private String TAG; //TAG will be specific to anklet ID.

    // System Components
    private final char ankletID;
    private BluetoothService bluetoothService;
    private AnkletListener listener;
    private FileOutputStream loggingOutputStream;
    private ArrayList<Double> accelerations;

    // Control Variables
    private boolean CSV_IS_ENABLED = false;
    private int ankletState = AnkletConst.STATE_CONNECTING;
    private boolean ACTIVE = false;

    /**
     * Creates a new Bluetooth Anklet.
     * @param deviceAddress The UUID of the device to connect with.
     * @param ankletID The ID for the anklet, 'L' for left, 'R' for right.
     * @param adapter A BluetoothAdapter accessed from a class with Context permission.
     * @param listener An AnkletListener to report events through.
     */
    public BluetoothAnklet(String deviceAddress, char ankletID, BluetoothAdapter adapter, AnkletListener listener) {

        this.listener = listener;
        TAG = "BluetoothAnklet-" + ankletID;
        this.ankletID = ankletID;
        BluetoothDevice device = adapter.getRemoteDevice(deviceAddress);
        bluetoothService = new BluetoothService(device, this);
        bluetoothService.connect();
        accelerations = new ArrayList<Double>();
    }

    /*
        Listen for changes in state from the BluetoothService.
     */
    @Override
    public void onStateChange(int state) {
        Log.d(TAG, "onStateChange()");

        switch (state) {
            case AnkletConst.CONNECTED:
                Log.d(TAG, "CONNECTED");
                ankletState = AnkletConst.STATE_CONNECTED;
                listener.onAnkletReady(ankletID);
                break;
            case AnkletConst.CONNECTION_FAILED:
                Log.d(TAG, "CONNECTION FAILED");
                ankletState = AnkletConst.STATE_CONNECTING;
                listener.onAnkletFailure(ankletID);
                break;
            case AnkletConst.CONNECTION_LOST:
                Log.d(TAG, "CONNECTION LOST");
                ankletState = AnkletConst.STATE_CONNECTING;
                //TODO implement auto reconnect feature, or at least some failure procedure.
                break;
            default:
                break;
        }
    }

    /**
     * Our own interface to report events to our creator.
     */
    public interface AnkletListener {

        /**
         * Called once an anklet is ready.
         * @param ankletID the ID of the ready anklet: 'L' or 'R'.
         */
        void onAnkletReady(char ankletID);

        /**
         * Called if an anklet fails in any way.
         * @param ankletID the ID if the effected anklet: 'L' or 'R'.
         */
        void onAnkletFailure(char ankletID);
    }

    /***********************************************************************************************
     * Class Control Methods
     **********************************************************************************************/

    /**
     * Cancels the associated BluetoothSerice component for this anklet.
     */
    public void shutdown() {
        bluetoothService.stop();
    }

    /**
     * True if the Anklet is connected and ready to go.
     * @return status of anklet connection.
     */
    public boolean isConnected(){

        return (ankletState == AnkletConst.STATE_CONNECTED);
    }

    /**
     * Deterimines if Anklet is stopped.
     * @return true if stopped, false otherwise.
     */
    public boolean isStoped(){
        return (ankletState == AnkletConst.STATE_READY);
    }

    /**
     * Enables file output logging for the anklet data stream. This will capture any data that comes
     * from the Anklet over the Bluetooth link. You must provide your own output File.
     * @param loggingOutputFile the File object to write the log into.
     */
    public void enableFileLogging(File loggingOutputFile) {
        Log.d(TAG, "Enabling file logging in bluetooth service");

        try {
            this.loggingOutputStream = new FileOutputStream(loggingOutputFile);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Give the value to the bluetoothService for use.
        bluetoothService.setLoggingEnabled(loggingOutputStream);
    }

    /**
     * Gets the average acceleration value by averaging all values in the acceleartions ArrayList.
     * This will reset and clear the ArrayList once called.
     * @return the average acceleration
     */
    public Double getAvgAcceleration(){
        // Calculate Harmonic Mean
        Double avgOut = 0.0;
        for (Double value:accelerations) {
            avgOut += value;
        }
        avgOut = avgOut/accelerations.size();
        accelerations.clear();
        Log.d(TAG, "mean: " + avgOut);
        return avgOut;
    }

    /***********************************************************************************************
     * Anklet Command Methods
     **********************************************************************************************/

    /**
     * Sends a start message to the Anklet.
     */
    public void sendStart() {

        if(ankletState <= AnkletConst.STATE_CONNECTED){
            Log.d(TAG, "Sending start message >>>");
            bluetoothService.write(AnkletConst.START_MESSAGE);
        }else{
            Log.e(TAG, "Cannot Send Start");
        }
    }

    /**
     * Puts the Anklet into CSV output mode by sending a message. This will cause it to print only
     * raw acceleration values in x,y,z format.
     */
    public void enableCSVoutput() {

        if(ankletState <= AnkletConst.STATE_CONNECTED){
            Log.d(TAG, "Sending enable CSV message >>>");
            bluetoothService.write(AnkletConst.ENABLE_CSV_MESSAGE);
        }else{
            Log.e(TAG, "NO CONNECTION CANT SEND CSV ENABLE");
        }
    }

    /**
     * Sends a stop message to the Anklet.
     */
    public void sendStop() {
        if(ankletState <= AnkletConst.STATE_RUNNING){
            Log.d(TAG, "Sending stop message >>>");
            bluetoothService.write(AnkletConst.STOP_MESSAGE);
        }else{
            Log.e(TAG, "NOT RUNNING CANT STOP");
        }
    }

    /**
     * Allows this anklet to start actively recording acceleration updates.
     */
    public void activate(){
        ACTIVE = true;
    }

    /**
     * Stops this anklet from recording acceleration updates.
     */
    public void deActivate(){
        ACTIVE = false;
    }

    /***********************************************************************************************
     * Message Processing !!! \n triggers every onDataRecieved event
     **********************************************************************************************/

    /**
     * Called if the underlying BluetoothService reports new data on the Bluetooth link.
     * @param s the string sent from the anklet.
     */
    @Override
    public void onDataRecieved(String s) {

        // If we are not active, we dont care what the Anklet has to say.
        if(!ACTIVE )
            return;

        //TODO Actually act of the response messages from the anklet for START, STROP, ETC.
        // Right now we simply igonre the response and assume all is well..... spooooky hahaha.

        // If we are active, then parse the new string as a Double value, and save it.
        try{
            Double in = Double.parseDouble(s);
            accelerations.add(in);
            Log.d(TAG, "value: " + in);
        }catch (Exception e){
            Log.d(TAG,"New AVG: bad Value");
        }
    }
}