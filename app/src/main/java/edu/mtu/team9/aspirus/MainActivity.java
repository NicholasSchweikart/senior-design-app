package edu.mtu.team9.aspirus;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;


public class MainActivity extends AppCompatActivity implements SettingsAuthFragment.NoticeDialogListener {

    public static final String TAG = "main";
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        ImageButton newSessionBtn = (ImageButton) findViewById(R.id.new_session_button);
        ImageButton getHelpBtn = (ImageButton) findViewById(R.id.get_help_button);

        newSessionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "new session clicked");
                startNewSession();
            }
        });

        getHelpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "get help clicked");
                startHelp();
            }
        });
    }

    public void startNewSession() {
        startActivity(new Intent(this, LiveSessionActivity.class));
    }

    public void startHelp() {
        startActivity(new Intent(this, SessionReviewActivity.class));
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                showDialog();
                return true;

            case R.id.action_legal:
                // User chose the "Settings" item, show the app settings UI...
                startActivity(new Intent(this, LegalActivity.class));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    void showDialog() {
        DialogFragment newFragment = new SettingsAuthFragment();
        newFragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String pswrd, String usr) {
        //TODO use the actual SharedPref object to get the stored login info
        // User touched the dialog's positive button, compare password and username for match
        if (usr.equals("a") && pswrd.equals("a")) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

}
