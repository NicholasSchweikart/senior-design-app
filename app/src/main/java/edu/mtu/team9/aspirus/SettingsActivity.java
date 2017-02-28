package edu.mtu.team9.aspirus;

import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "settings-activity";
    SessionFileUtility sessionFileUtility;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sessionFileUtility = new SessionFileUtility(this);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new SettingsFragment())
                .commit();

        Button clearButton = (Button)findViewById(R.id.clearSavedDataButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ClearSessionsFile().execute();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return false;
        }
    }

    //TODO Implement on PrefChange listener and actions

    //TODO Complete doctor access page for any further features
    private class ClearSessionsFile extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void...voids) {

            return sessionFileUtility.clearSessionsFile();
        }

        @Override
        protected void onPostExecute(Boolean deleteSuccessful){

            if(deleteSuccessful){
                Log.e(TAG, "Session File Cleared");
                Toast.makeText(getApplicationContext(),"Session File Cleared", Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(getApplicationContext(),"Error Clearing File", Toast.LENGTH_LONG).show();
        }
    }
}
