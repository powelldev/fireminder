package com.fireminder.fireminder;

//import org.holoeverywhere.widget.TextView;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class AlarmView extends Activity {
	String title; 
	String body;
	String dest;
	long arrivalTime;
	long deptTime;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_view);
        LoadPreferences();
        Log.e("test", title);
        Log.e("Body", body);
    }
    
    public void LoadPreferences(){
    	SharedPreferences sp = getPreferences(MODE_PRIVATE);
		title = sp.getString("title", "");
		body = sp.getString("body", "");
		dest = sp.getString("destination", "");
		arrivalTime = sp.getLong("arrival_time", 0);
		deptTime = sp.getLong("dept_time", 0);
    }



    
}
