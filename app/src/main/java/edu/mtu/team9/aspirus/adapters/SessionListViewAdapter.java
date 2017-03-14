package edu.mtu.team9.aspirus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import edu.mtu.team9.aspirus.R;
import edu.mtu.team9.aspirus.SessionFromJSONString;

/**
 *  Created by Nicholas Schweikart, CPE, for Biomedical Senior Design Team 9.
 *
 *  Description: This is a simple data adapter for the Session objects that are stored while the
 *  system is being used. It allows us to create a list of all the Sessions in an activity.
 */
public class SessionListViewAdapter extends ArrayAdapter<SessionFromJSONString> {

    /**
     * Create a new List View Adapter for Sessions.
     * @param context the application context.
     * @param allSessions the ArrayList of all the Sessions your want to display.
     */
    public SessionListViewAdapter(Context context, ArrayList<SessionFromJSONString> allSessions) {
        super(context, 0, allSessions);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        // Get the session at this position.
        SessionFromJSONString session = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.performance_review_list_item, parent, false);
        }

        // Populate the two fields in our simple list item layout (See res/layouts)
        TextView sessionDate = (TextView)convertView.findViewById(R.id.sessionDateTextView);
        sessionDate.setText(session.getDate());

        return convertView;
    }
}
