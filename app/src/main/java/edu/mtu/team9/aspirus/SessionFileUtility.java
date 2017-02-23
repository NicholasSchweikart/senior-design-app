package edu.mtu.team9.aspirus;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created for Aspirus2
 * By: nicholas on 2/14/17.
 * Description:
 */

public class SessionFileUtility {
    public static final String TAG = "session-file-utility:";

    public static final int SAVE_SUCCESS = 1,
            SAVE_FAILURE = 2,
            OPEN_FAILURE = 3,
            OPEN_SUCCESS = 4;

    private Context context;
    private JSONObject sessionDataObject = null;
    private Handler handler;
    private static ArrayList<SessionFromJSON> sessionsArrayList;
    private File JSONfile;

    SessionFileUtility(Context context, Handler handler){
        this.context = context;
        this.handler = handler;
        sessionsArrayList = new ArrayList<SessionFromJSON>();
    }

    private class BuildAndSaveSession extends Thread{
        JSONObject newSession;

        BuildAndSaveSession(JSONObject newSession){
            this.newSession = newSession;
        }

        @Override
        public void run(){

            Message message = new Message();

            if(!openSessionDataFile()){
                Log.e(TAG, "Error couldnt open sessions data file");
                message.what = OPEN_FAILURE;
            }else {
                try {
                    sessionDataObject.getJSONArray("sessions_array").put(newSession);
                    message.what = SAVE_SUCCESS;
                } catch (JSONException e) {
                    Log.e(TAG, "Error building new session object");
                    message.what = SAVE_FAILURE;
                }

                if (!saveSessionDataFile()) {
                    message.what = SAVE_FAILURE;
                }
            }

            // Alert the UI thread to the changes
            handler.sendMessage(message);
        }
    };

    private class GetSessionsData extends Thread{
        @Override
        public void run(){

            Message message = new Message();

            if(!openSessionDataFile()){
                Log.e(TAG, "Error sessionDataOBject not ready");
                message.what = OPEN_FAILURE;
            }else {
                message.what = OPEN_SUCCESS;
            }

            try {
                JSONArray sessionsArray = sessionDataObject.getJSONArray("sessions_array");
                int len = sessionsArray.length();
                for(int i = 0; i < len; i++){
                    sessionsArrayList.add(new SessionFromJSON(sessionsArray.getJSONObject(i).toString()));
                }
            } catch (JSONException e) {
                Log.e(TAG,"Error couldnt create sessionsArrayList");
            }


            // Alert the UI thread to the changes
            handler.sendMessage(message);
        }
    }

    private boolean openSessionDataFile(){

        Log.d(TAG, "Opening Sessions Data File");
        try {

            StringBuilder jsonString = new StringBuilder();

            JSONfile = new File(context.getExternalFilesDir(null).getPath(),"session-data.json");
            if(!JSONfile.exists()){
                createNewSessionsDataFile();
                return true;
            }

            FileInputStream is = new FileInputStream(JSONfile);

            byte[] buffer = new byte[1024];
            int bytesRead = is.read(buffer,0,1024);
            while(bytesRead != -1){
                jsonString.append(new String(buffer,"UTF-8"));
                bytesRead = is.read(buffer,0,1024);
            }
            is.close();
            sessionDataObject = new JSONObject(jsonString.toString());


        }catch (IOException e) {
            Log.e(TAG,"ERROR: couldnt open session data file");
            return false;
        } catch (JSONException e) {
            Log.e(TAG,"ERROR: parse file as JSON data");
            return false;
        }
        return true;
    }

    private boolean createNewSessionsDataFile(){

        Log.d(TAG, "Creating data file for the first time");
        try {
            JSONfile.createNewFile();
            sessionDataObject = new JSONObject("{}");
            sessionDataObject.put("sessions_array", new JSONArray());
        } catch (IOException e) {
            Log.e(TAG, "Error file IO create new data file");
            return false;
        } catch (JSONException e) {
            Log.e(TAG, "Error json create new data file");
            return false;
        }

        return  saveSessionDataFile();
    }

    private boolean saveSessionDataFile(){
        Log.d(TAG, "Saving session data file");
        try {
            FileOutputStream os = new FileOutputStream(JSONfile,false);
            os.write(sessionDataObject.toString(2).getBytes());
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error couldnt open output file");
            return false;
        } catch (IOException e) {
            Log.e(TAG,"Error writing data to the output stream");
            return false;
        } catch (JSONException e) {
            Log.e(TAG,"Error couldnt toString with tab of 2");
            return false;
        }
        return true;
    }

    public void loadSessionsData(){
        new GetSessionsData().start();
    }

    public void saveSession(JSONObject newSession){
        new BuildAndSaveSession(newSession).start();
    }

    public static ArrayList<SessionFromJSON> getAllSessions() {
        return sessionsArrayList;
    }
}
