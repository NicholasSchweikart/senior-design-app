package edu.mtu.team9.aspirus;

import android.content.Context;
import android.util.Log;

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
 *  Created by Nicholas Schweikart, CPE, for Biomedical Senior Design Team 9
 *  Contact: nsschwei@mtu.edu
 *
 *  Description: This class is a simple utility used to interface with the saves session data on
 *  the local file system.
 */
public class SessionFileUtility {
    public static final String TAG = "session-file-utility:";

    private Context context;
    private JSONObject sessionsDataObject = null;
    private ArrayList<SessionFromJSONString> sessionsArrayList;
    private File JSONfile;

    SessionFileUtility(Context context){
        this.context = context;
        sessionsArrayList = new ArrayList<SessionFromJSONString>();
    }

    /**
     *  Opens the local file where the session data is kept.
     * @return  true if the file was opened, false if not.
     */
    private boolean openSessionDataFile(){

        Log.d(TAG, "Opening Sessions Data File");
        try {

            StringBuilder jsonString = new StringBuilder();

            JSONfile = new File(context.getExternalFilesDir(null).getPath(),"session-data.json");
            if(!JSONfile.exists()){
                return createNewSessionsDataFile();              // Create new file if none exist
            }

            FileInputStream is = new FileInputStream(JSONfile); // Read in the whole file to string

            byte[] buffer = new byte[1024];
            int bytesRead = is.read(buffer,0,1024);
            while(bytesRead != -1){
                jsonString.append(new String(buffer,"UTF-8"));
                bytesRead = is.read(buffer,0,1024);
            }
            is.close();

            // Convert string to session JSON object for all saved sessions.
            sessionsDataObject = new JSONObject(jsonString.toString());

        }catch (IOException e) {
            Log.e(TAG,"ERROR: couldnt open session data file");
            return false;
        } catch (JSONException e) {
            Log.e(TAG,"ERROR: parse file as JSON data");
            return false;
        }
        return true;
    }

    /**
     *  Creates a new sessions data file to store all use data. NOTE: should only ever run after a
     *  fresh install of the app.
     * @return true if file was created, false if not.
     */
    private boolean createNewSessionsDataFile(){

        Log.d(TAG, "Creating data file for the first time");
        try {

            // Build new empty sessions file with an empty array.
            JSONfile.createNewFile();
            sessionsDataObject = new JSONObject("{}");
            sessionsDataObject.put("sessions_array", new JSONArray());
        } catch (IOException e) {
            Log.e(TAG, "Error file IO create new data file");
            return false;
        } catch (JSONException e) {
            Log.e(TAG, "Error json create new data file");
            return false;
        }
        return  saveSessionDataFile();
    }

    /**
     *  Saves the sessions data file to the local file system.
     * @return true if save successful, false if not.
     */
    private boolean saveSessionDataFile(){
        Log.d(TAG, "Saving session data file");
        try {
            FileOutputStream os = new FileOutputStream(JSONfile,false);
            os.write(sessionsDataObject.toString(2).getBytes());         // Adds tabs to output text
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error couldnt open output file");
            return false;
        } catch (IOException e) {
            Log.e(TAG,"Error writing data to the output stream");
            return false;
        } catch (JSONException e) {
            Log.e(TAG,"Error couldn't toString() with tab of 2");
            return false;
        }
        return true;
    }

    /**
     *  Saves a session JSON object to the sessions data file.
     * @param newSession JSON object of the session to save.
     * @return true if saved, false if not
     */
    public boolean saveSession(JSONObject newSession){

        // Attempt to open the sessions data file.
        if(!openSessionDataFile()){
            Log.e(TAG, "Error couldnt open sessions data file");
            return false;
        }

        // Write the newSession to the sessions array
        try {
            sessionsDataObject.getJSONArray("sessions_array").put(newSession);
        } catch (JSONException e) {
            Log.e(TAG, "Error building new session object");
            return false;
        }

        return saveSessionDataFile();
    };

    /**
     *  Loads in the entire sessions data file to the global sessionsDataObject.
     * @return true if loaded, false if error.
     */
    public boolean getSessionsData(){

        // Attempt to open the file.
        if(!openSessionDataFile()){
            Log.e(TAG, "Error sessionsDataOBject not ready");
            return  false;
        }

        // Create list of all session objects from the internal array.
        try {
            JSONArray sessionsArray = sessionsDataObject.getJSONArray("sessions_array");
            int len = sessionsArray.length();
            for(int i = 0; i < len; i++){
                sessionsArrayList.add(new SessionFromJSONString(sessionsArray.getJSONObject(i).toString()));
            }
        } catch (JSONException e) {
            Log.e(TAG,"Error couldnt create sessionsArrayList");
            return false;
        }
        return true;
    }

    /**
     *  Clears all the saved sessions in the sessions data file. NOTE: does not remove the file
     *  from the local file system.
     * @return true if cleared, false if not.
     */
    public Boolean clearSessionsFile() {

        // Attempt to open the file
        if(!openSessionDataFile()){
            Log.e(TAG, "Error couldnt open sessions data file");
            return false;
        }

        // Insert a new empty JSON array.
        try {
            JSONArray emptyArray = new JSONArray();
            sessionsDataObject.put("sessions_array", emptyArray);
        } catch (JSONException e) {
            Log.e(TAG, "Error clearing sessions");
            return false;
        }
        return saveSessionDataFile();
    }

    public ArrayList<SessionFromJSONString> getSessionsArrayLists() {
        return sessionsArrayList;
    }

}
