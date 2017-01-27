package edu.mtu.team9.aspirus;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class LoggingActivity extends AppCompatActivity implements SensorEventListener {

    public static final String TAG = "logging-main:";
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serialPort;
    private TextView outputTxt;
    private TextView xtxt, ytxt, ztxt;
    private Timer fuseTimer = new Timer();
    private SensorManager mSensorManager;
    private Sensor sensor;
    private static final int SAMPLE_RATE = 20; // in ms => 50Hz
    private FileOutputStream fileOutputStream;
    private boolean STATE_LOGGING = false;
    public float[] accelerationVector = new float[3]; // accelerometer vector x,y,z

    String myDir;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging);

//        outputTxt = (TextView) findViewById(R.id.output_text);
//        usbManager = (UsbManager) getSystemService(getApplicationContext().USB_SERVICE);
//        findSerialPortDevice();
        mSensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, sensor , SensorManager.SENSOR_DELAY_GAME);

        final EditText filename_etxt = (EditText) findViewById(R.id.edittxt_filename);
        xtxt = (TextView) findViewById(R.id.x_text);
        ytxt = (TextView) findViewById(R.id.y_text);
        ztxt = (TextView) findViewById(R.id.z_text);

        final Button startBtn = (Button) findViewById(R.id.start_btn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!STATE_LOGGING){
                    String filename =  filename_etxt.getText().toString()+".csv";
                    File dir = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"CSVs");
                    if(!dir.exists()){
                        Log.d(TAG, "Directory not created");
                        dir.mkdirs();
                    }
                    File outputCSV = new File(dir,filename);
                    try {
                        fileOutputStream = new FileOutputStream(outputCSV);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    fuseTimer.scheduleAtFixedRate(writeToFile,1000,SAMPLE_RATE);
                    startBtn.setText("Done");
                    STATE_LOGGING = true;
                }else{
                    shutdown();
                    STATE_LOGGING = false;
                    startBtn.setText("Start");
                }
            }
        });
    }
    private void shutdown(){
        fuseTimer.cancel();
        fuseTimer.purge();
        try {
            fileOutputStream.flush();
            fileOutputStream.close();
            Toast.makeText(this, "File Saved!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected  void onStop(){
        super.onStop();
        mSensorManager.unregisterListener(this);
       finish();
    }

//    /*
//  *  Data received from serial port will be received here. Just populate onReceivedData with your code
//  *  In this particular example. byte stream is converted to String and send to UI thread to
//  *  be treated there.
//  */
//    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
//        @Override
//        public void onReceivedData(byte[] arg0) {
//            try {
//                String data = new String(arg0, "UTF-8");
//                outputTxt.setText(data);
//
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//        }
//    };
//
//    private void findSerialPortDevice() {
//
//        // This snippet will try to open the first encountered usb device connected, excluding usb root hubs
//        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
//        if (!usbDevices.isEmpty()) {
//            boolean keep = true;
//            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
//                device = entry.getValue();
//                int deviceVID = device.getVendorId();
//                int devicePID = device.getProductId();
//
//                if (deviceVID != 0x1d6b && (devicePID != 0x0001 && devicePID != 0x0002 && devicePID != 0x0003)) {
//                    // There is a device connected to our Android device. Try to open it as a Serial Port.
//                    connection = usbManager.openDevice(device);
//                    new ConnectionThread().start();
//                    keep = false;
//                } else {
//                    connection = null;
//                    device = null;
//                }
//
//                if (!keep)
//                    break;
//            }
//            if (!keep) {
//                // There is no USB devices connected (but usb host were listed). Send an intent to MainActivity.
//                Log.d(TAG, "Only Hosts");
//            }
//        } else {
//            // There is no USB devices connected. Send an intent to MainActivity
//            Log.d(TAG, "No USB devices connected");
//        }
//    }
//
//    private class ConnectionThread extends Thread {
//        @Override
//        public void run() {
//            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
//            if (serialPort != null) {
//                if (serialPort.open()) {
//                    serialPort.setBaudRate(115200);
//                    serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
//                    serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
//                    serialPort.setParity(UsbSerialInterface.PARITY_NONE);
//                    /**
//                     * Current flow control Options:
//                     * UsbSerialInterface.FLOW_CONTROL_OFF
//                     * UsbSerialInterface.FLOW_CONTROL_RTS_CTS only for CP2102 and FT232
//                     * UsbSerialInterface.FLOW_CONTROL_DSR_DTR only for CP2102 and FT232
//                     */
//                    serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
//                    serialPort.read(mCallback);
//
//                    // Everything went as expected. Send an intent to MainActivity
//                    Log.d(TAG, "USB Ready!");
//                } else {
//                    // Serial port could not be opened, maybe an I/O error or if CDC driver was chosen, it does not really fit
//                    // Send an Intent to Main Activity
//                    if (serialPort instanceof CDCSerialDevice) {
//                        Log.d(TAG, "CDC not working");
//                    } else {
//                        Log.d(TAG, "USB not working");
//                    }
//                }
//            } else {
//                // No driver for given device, even generic CDC driver could not be loaded
//                Log.d(TAG, "No driver");
//            }
//        }
//    }
    private TimerTask writeToFile = new TimerTask() {
        @Override
        public void run() {
            final String xout = String.valueOf(accelerationVector[0]);
            final String yout = String.valueOf(accelerationVector[1]);
            final String zout = String.valueOf(accelerationVector[2]);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    xtxt.setText(xout);
                    ytxt.setText(yout);
                    ztxt.setText(zout);

                }
            });

            String out = String.valueOf(System.currentTimeMillis()) +','+ xout +','+ yout +','+ zout + '\n';
            try{
                fileOutputStream.write(out.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:

                // copy new accelerometer data into accel array
                System.arraycopy(sensorEvent.values, 0, accelerationVector, 0, 3);
                break;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
