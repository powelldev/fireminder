package com.fireminder.fireminder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
//
//import org.holoeverywhere.app.DatePickerDialog;
//import org.holoeverywhere.app.DatePickerDialog.OnDateSetListener;
//import org.holoeverywhere.app.Dialog;
//import org.holoeverywhere.app.ProgressDialog;
//import org.holoeverywhere.app.TimePickerDialog;
//import org.holoeverywhere.app.TimePickerDialog.OnTimeSetListener;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.TimePicker;
import android.widget.Toast;

public class Fireminder extends Activity{
	Button done_button;
	TimePicker arrival_time_tp;
	String arrival_time;
	EditText destination;
	LocationManager lm;
	LocationListener ll;
	Location current_location;
	TextView tv1;
	DistanceMatrix distance_matrix;
	ArrivalTimeGen atg;
	
	Dialog dialog;
	static final Integer notificationID = 139381;
	@Override
	public void onCreate(Bundle b){
		super.onCreate(b);
		setContentView(R.layout.set_alarm);
		LocationInit();
		SetupInit();
		
		
	}
	@Override
	public void onPause(){
		super.onPause();
		dialog.dismiss();
		lm.removeUpdates(ll);
	}

	@Override
	public void onResume(){
		super.onResume();
		LocationInit();
	}
	
 /*   public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem menu){
    	Intent intent = new Intent(this, AlarmView.class);
    	startActivity(intent);
		return true;
    	
    } */
	final OnDateSetListener odsl = new OnDateSetListener(){

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			atg.setArrivalYear(year);
			atg.setARRIVAL_MONTH(monthOfYear);
			atg.setArrivalDay(dayOfMonth);
			//current_year = year; current_month = monthOfYear; current_day = dayOfMonth;
			TextView tv2 = (TextView) findViewById(R.id.date_chosen);
			monthOfYear++;
			tv2.setText(""+monthOfYear+"/"+dayOfMonth+"/"+year);
		}
		
	};
	final OnTimeSetListener otsl = new OnTimeSetListener(){
		public void onTimeSet(TimePicker view,
				int hourOfDay, int minute) {
			atg.setARRIVAL_HOUR(hourOfDay); atg.setARRIVAL_MINUTE(minute);
			String helper="";
			if(minute == 0) {helper = "" + 0;}
			TextView tv = (TextView) findViewById(R.id.time_chosen);
			if(hourOfDay > 12) {hourOfDay = hourOfDay-12;}
			tv.setText(""+hourOfDay+":"+minute+helper);
			
			
		}
		
	};
	public void timepickerbutton(View v){
		Calendar cal = Calendar.getInstance();
		TimePickerDialog timePickDiag = new TimePickerDialog(this, otsl, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),false);
		timePickDiag.show();
	}
	public void datepickerbutton(View v){
		Calendar cal = Calendar.getInstance();
		DatePickerDialog datePickDiag = new DatePickerDialog(this, odsl, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		datePickDiag.show();
	}   
	 OnClickListener arrival_time_listener = new OnClickListener(){
		public void onClick(View v) {
			if(current_location != null){
				Calendar arrival_calendar = Calendar.getInstance();
				arrival_calendar.set(Calendar.DAY_OF_MONTH, atg.getArrivalDay());
				arrival_calendar.set(Calendar.MONTH, atg.getARRIVAL_MONTH());
				arrival_calendar.set(Calendar.YEAR, atg.getARRIVAL_YEAR());
				arrival_calendar.set(Calendar.HOUR_OF_DAY, atg.getARRIVAL_HOUR());
				arrival_calendar.set(Calendar.MINUTE, atg.getARRIVAL_MINUTE());
				
				
				distance_matrix = new DistanceMatrix(destination.getEditableText().toString(), current_location, arrival_calendar.getTimeInMillis());
				GetDistanceMatrix gdm = new GetDistanceMatrix();
				gdm.execute(distance_matrix);
				
				lm.removeUpdates(ll);
				finish(); 
			} else {
				Toast.makeText(getApplicationContext(), "No location known at the moment", Toast.LENGTH_SHORT).show();
			}
		} 
	}; 

	
	public void set_alarm(){
		Log.d("debug", distance_matrix.toString());
		if(distance_matrix.status.contains("OK")){
			
			Calendar calendar = Calendar.getInstance();
			long marginOfError = distance_matrix.duration / 4;
			distance_matrix.duration = distance_matrix.duration + marginOfError; 
			calendar.setTimeInMillis(distance_matrix.arrival_time - distance_matrix.duration);
			
			SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aaa");
			
			//Output the guesstimated time
			Toast.makeText(getApplicationContext(), "Reminder set:\n" + sdf.format(calendar.getTime()) + "\n" + distance_matrix.destination, Toast.LENGTH_LONG).show();	
			
			//Notifications
			String body = "Leave for " + distance_matrix.destination + " at " + sdf.format(calendar.getTime());
			String title = "" + sdf.format(calendar.getTime());

			// create margin of error
			Intent alarmIntent = new Intent(getApplicationContext(), LaunchNotification.class);
			alarmIntent.putExtra("destination", distance_matrix.destination);
			alarmIntent.putExtra("arrival_time", distance_matrix.arrival_time);
			alarmIntent.putExtra("dept_time", calendar.getTimeInMillis());
			alarmIntent.putExtra("title", title);
			alarmIntent.putExtra("body", body);

			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, alarmIntent, 0);
			am.cancel(pi);
			am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
		
		} else {
			Toast.makeText(getApplicationContext(), "Invalid address", Toast.LENGTH_SHORT).show();
		}

	}
	
	public class GetDistanceMatrix extends AsyncTask<DistanceMatrix, Void, DistanceMatrix>{ 

		@Override
		protected DistanceMatrix doInBackground(DistanceMatrix... params) {
			DistanceMatrix dm = params[0];
			dm.getDistanceMatrix();
			return dm;
		}
		
		protected void onPostExecute(DistanceMatrix result){
			distance_matrix = result;
			set_alarm();
		}
		
	} 
	
	public void SetupInit(){
		findViewsById();
		done_button.setOnClickListener(arrival_time_listener);
		//Initial time and date variables
		Calendar calendar = Calendar.getInstance();
		atg = new ArrivalTimeGen(calendar.get(Calendar.MINUTE),
		calendar.get(Calendar.HOUR_OF_DAY),
		calendar.get(Calendar.DAY_OF_MONTH),
		calendar.get(Calendar.MONTH),
		calendar.get(Calendar.YEAR));

	}
	public void findViewsById(){
		done_button = (Button) findViewById(R.id.done_button);
		destination = (EditText) findViewById(R.id.destination);
		
		
		destination.setOnEditorActionListener(new OnEditorActionListener(){
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				destination.clearFocus();
				done_button.performClick();
				return false;
			}
		});
	}	
	 public void LocationInit(){
	    	long UPDATE_TIME_MS = 60000;
	    	lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	    	ll = (LocationListener) new LocationListener() {

				public void onLocationChanged(Location arg0) {
					current_location = arg0;
					Log.d("tag", "" + current_location.toString());
				}

				public void onProviderDisabled(String arg0) {
					// TODO Auto-generated method stub
					
				}

				public void onProviderEnabled(String arg0) {
					// TODO Auto-generated method stub
					
				}

				public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
					// TODO Auto-generated method stub
					
				}
	    		
	    	};
	    	lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_TIME_MS, 0, ll);
	    	Runnable showWaitScreen = new Runnable() {

				public void run() {
					while(current_location == null) {
						current_location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					}
					dialog.dismiss();
				}
	    	};
	    	dialog = ProgressDialog.show(this, "Hold on a bit...", "We're getting your location ...", true);
	    	Thread t = new Thread(showWaitScreen);
	    	t.start();
	    }
}
