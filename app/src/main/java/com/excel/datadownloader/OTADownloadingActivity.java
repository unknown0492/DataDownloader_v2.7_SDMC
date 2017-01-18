package com.excel.datadownloader;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

public class OTADownloadingActivity extends ActionBarActivity {

	TextView tv_progress,
		tv_file_size,
		tv_file_size_downloaded;
	
	Timer time_updater;
	String progress_arr[];
	int counter = 0;
	SharedPreferences spfs;
	Context context = this;
	
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_downloading );
        
        spfs = SharedPreferencesHelper.createSharedPreference( context, SharedPreferencesHelper.APPLICATION_DATA );
        
        tv_progress 			= (TextView) findViewById( R.id.tv_progress );
        tv_file_size			= (TextView) findViewById( R.id.tv_file_size );
        tv_file_size_downloaded	= (TextView) findViewById( R.id.tv_file_size_downloaded );
        progress_arr = new String[]{
        		"Please be patient",
        		"Please be patient.",
        		"Please be patient..",
        		"Please be patient..."
        	};
        String file_size = (String) SharedPreferencesHelper.getSharedPreference( spfs, Constants.FIRMWARE_FILE_SIZE, "0" );
        tv_file_size.setText( tv_file_size.getText().toString() + file_size + " MB" );
        
        tickTime();
    }
    
    private void tickTime(){
		time_updater = new Timer();
		time_updater.scheduleAtFixedRate( new TimerTask() {
			
			@Override
			public void run(){
				runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       if( counter % progress_arr.length == 0 ){
                    	   counter = 0;
                       }
                       tv_progress.setText( progress_arr[ counter ] );
                       // Get downloaded size of the file
                       String current_size = Functions.getSizeOfTheFile( "Download", "update.zip" );
                       Log.i( "MainActivity", "Downloaded size : "+current_size );
                       tv_file_size_downloaded.setText( current_size + " MB" );
                       counter++;
                    }
                });
			}
		}, 0, 500 );
	}
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	return true;
    }
}
