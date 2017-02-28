package edu.mtu.team9.aspirus;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by nssch on 2/28/2017.
 */

public class SessionListViewAdapter extends ArrayAdapter<SessionFromJSONString> {

    public SessionListViewAdapter(Context context, ArrayList<SessionFromJSONString> allSessions) {
        super(context, 0, allSessions);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        SessionFromJSONString session = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.performance_review_list_item, parent, false);
        }

        TextView sessionDate = (TextView)convertView.findViewById(R.id.sessionDateTextView);
        sessionDate.setText(session.getDate());
        return convertView;
    }
}
