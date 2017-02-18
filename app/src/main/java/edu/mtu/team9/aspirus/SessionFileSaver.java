package edu.mtu.team9.aspirus;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created for Aspirus2
 * By: nicholas on 2/14/17.
 * Description:
 */

public class SessionFileSaver{
    public static final String TAG = "session-file-utility:";

    public static final int SAVE_SUCCESS = 1,
            SAVE_FAILURE = 2,
            OPEN_FAILURE = 3,
            OPEN_SUCCESS = 4;

    private Context context;
    private JSONObject sessionDataObject = null;
    private Handler handler;

    File JSONfile;

    SessionFileSaver(Context context, Handler handler){
        this.context = context;
        this.handler = handler;
        Init.start();
    }

    public class BuildAndSaveSession extends Thread{
        ArrayList<Integer> scores;
        int[] legBreakdown;
        int trendelenburgScore;
        Integer averageScore;
        BuildAndSaveSession(ArrayList<Integer> scores, int[] legBreakdown, int trendelenburgScore, Integer averageScore){
            this.scores = scores;
            this.legBreakdown = legBreakdown;
            this.trendelenburgScore = trendelenburgScore;
            this.averageScore = averageScore;
        }

        @Override
        public void run(){
            Message message = new Message();
            if(sessionDataObject == null)
            {
                Log.e(TAG, "Error sessionDataOBject not ready");
                return;
            }

            JSONObject newSession = new JSONObject();
            JSONArray scoresArray = new JSONArray();

            for (Integer score : scores) {
                scoresArray.put(score);
            }

            try {
                newSession.put("date", Calendar.getInstance().getTime());
                newSession.put("final_score",averageScore);
                newSession.put("trendelenburg_score",trendelenburgScore);
                newSession.put("left_leg_percent",legBreakdown[0]);
                newSession.put("right_leg_percent",legBreakdown[0]);
                newSession.put("scores_array", scoresArray);
                sessionDataObject.getJSONArray("sessions_array").put(newSession);
                message.what = SAVE_SUCCESS;
            } catch (JSONException e) {
                Log.e(TAG, "Error building new session object");
                message.what = SAVE_FAILURE;
            }

            try {
                FileOutputStream os = new FileOutputStream(JSONfile,false);
                os.write(sessionDataObject.toString(2).getBytes());
                os.flush();
                os.close();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Error couldnt open output file");
                message.what = SAVE_FAILURE;
            } catch (IOException e) {
                Log.e(TAG,"Error writing data to the output stream");
                message.what = SAVE_FAILURE;
            } catch (JSONException e) {
                Log.e(TAG,"Error couldnt toString with tab of 2");
            }

            // Alert the UI thread to the changes
            handler.sendMessage(message);
        }
    }

    private Thread Init = new Thread() {
        @Override
        public void run() {
            Message message = handler.obtainMessage();
            try {
                StringBuilder jsonString = new StringBuilder();

                JSONfile = new File(context.getExternalFilesDir(null).getPath(),"session-data.json");
                FileInputStream is = new FileInputStream(JSONfile);

                byte[] buffer = new byte[1024];
                int bytesRead = is.read(buffer,0,1024);
                while(bytesRead != -1){
                    jsonString.append(new String(buffer,"UTF-8"));
                    bytesRead = is.read(buffer,0,1024);
                }
                is.close();
                sessionDataObject = new JSONObject(jsonString.toString());
                message.what = OPEN_SUCCESS;

            }catch (IOException e) {
                Log.e(TAG,"ERROR: couldnt open session data file");
                message.what = OPEN_FAILURE;
            } catch (JSONException e) {
                Log.e(TAG,"ERROR: parse file as JSON data");
                message.what = OPEN_FAILURE;
            }
            handler.sendMessage(message);
        }
    };

}
