package com.excel.datadownloader;

import static com.excel.datadownloader.Constants.FIRMWARE_FILE_SIZE;
import static com.excel.datadownloader.Constants.TAG;
import static com.excel.datadownloader.Constants.URL_WEB_SERVICE;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/*
 * OTA TYPES 
 * 0 -> Regular OTA
 * 1 -> Background OTA
 * 2 -> Download OTA
 * 3 -> Quiet OTA
 * 
 **/


public class OTAUpdateService1 extends Service {

	Context context = this;
	DownloadManager dwnld_mgr = null;
	final long lastDownload = -1L;
	Timer time_updater;
	
	SharedPreferences spfs;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand( Intent intent, int flags, int startId ) {
		Log.i( TAG, "OTAUpdateService1 started" );
		spfs = SharedPreferencesHelper.createSharedPreference( context, SharedPreferencesHelper.APPLICATION_DATA );
		
		// 1. Request for md5 of the firmware present on the server
		InternetRequest ir = new InternetRequest(){

			@Override
			protected void onPostExecute( String result ) {
				super.onPostExecute( result );
				
				if( result == null ){
					Log.i( TAG, "Result is Null, hence exiting" );
					return;
				}
				
				Log.i( TAG, "Result : "+result );
				JSONObject jsonObject = null;
				String require_update = null;
				String file_size = null;
				String version_name = null;
				String file_path = null;
				String ota_type = null;
				
				try{
					jsonObject = new JSONObject( result );
					require_update		= jsonObject.getString( "require_update" );
					file_size			= jsonObject.getString( "file_size" );
					version_name 		= jsonObject.getString( "version_name" );
					file_path			= jsonObject.getString( "file_path" );
					ota_type			= jsonObject.getString( "ota_type" );
					
					// Store file size into spfs 
					SharedPreferencesHelper.editSharedPreference( spfs, FIRMWARE_FILE_SIZE, file_size );
					Log.i( "OTAUpdateService : ", "Update : "+require_update+", File Size : "+file_size+" MB , ro_build_display_id : "+version_name+" , Ota Type : "+ota_type );
				}
				catch( Exception e ){
					e.printStackTrace();
				}
				
				startOta( require_update, file_size, version_name, file_path, ota_type );
			}
		};

		String IP = Functions.getCMSIpFromTextFile();
		String WEB_SERVICE = "http://" + IP + File.separator + URL_WEB_SERVICE;
		String url_params = "what_do_you_want=get_ota_info&mac_address="+Functions.getMacAddress( context );
		Log.i( "OTAUpdateService", WEB_SERVICE + "?" +url_params );
		String params[] = { WEB_SERVICE, "POST", url_params };
		ir.execute( params );
		
		return START_NOT_STICKY;
	}
	
	public void startOta( String require_update, String file_size, String version_name, String file_path, String ota_type ){
		Log.i( TAG, "Environment.DIRECTORY_DOWNLOADS : "+Environment.DIRECTORY_DOWNLOADS );
		
		String ro_build_display_id_of_box = Functions.executeShellCommandWithOp( "getprop ro.build.display.id" ).trim(); 
		Log.i( "OTAUpdateService", "Internal ro_build_display_id : "+ro_build_display_id_of_box );
		
		// Delete the Download Directory
		Functions.executeShellCommandWithOp( "rm -rf /mnt/sdcard/Download" );
		
		if( ( require_update.equals( "1" ) ) && 
						( ! ro_build_display_id_of_box.equals( version_name ) ) ){
			
			Functions.executeShellCommandWithOp( "setprop ota_type "+ota_type );
			Log.i( TAG, "Ota Type set using setprop ota_type "+ota_type );
			
			Log.i( TAG, "Time to update the firmware !!" );
			
			if( ota_type.trim().equals( "0" ) ){ // Normal OTA
				Intent in = new Intent( context, OTADownloadingActivity.class );
				in.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
				context.startActivity( in );
			}
			
			// 4. Download the firmware to /sdcard/Download/update.zip
			dwnld_mgr = (DownloadManager) context.getSystemService( Context.DOWNLOAD_SERVICE );
			
			String URL = "http://" + Functions.getCMSIpFromTextFile() + "/appstv/" + file_path;
			Log.i( TAG, URL );
			Uri uri = Uri.parse( URL );
			   
			Environment
		      .getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS )
		      .mkdirs();
			
			// 5. Delete the firmware update.zip file if already exist
			File firmware_file = Functions.getFile( "Download", "update.zip" );
			if( firmware_file.exists() )
				firmware_file.delete();
			
			dwnld_mgr.enqueue( new DownloadManager.Request( uri )
              .setTitle( "Demo" )
              .setDescription( "Something useful. No, really." )
              .setDestinationInExternalPublicDir( Environment.DIRECTORY_DOWNLOADS,
                                                 "update.zip" ) );
			
			// tickTime();
			
		}
		else{
			Log.i( "OTAUpdateService", "Firmware is up to date. No need to OTA !" );
		}
	}

	public void queryStatus() {
		Cursor c = dwnld_mgr.query( new DownloadManager.Query().setFilterById( lastDownload ) );

		if ( c == null ) {
			Toast.makeText( this, "Download not found!", Toast.LENGTH_LONG ).show();
		}
		else {
			try{
			c.moveToFirst();

			/*Log.d( getClass().getName(), "COLUMN_ID: "+
					c.getLong( c.getColumnIndex( DownloadManager.COLUMN_ID )));*/
			Log.d( getClass().getName(), "COLUMN_BYTES_DOWNLOADED_SO_FAR: "+
					c.getLong( c.getColumnIndex( DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR )));
			Log.d( getClass().getName(), "COLUMN_LAST_MODIFIED_TIMESTAMP: "+
					c.getLong( c.getColumnIndex( DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP )));
			Log.d( getClass().getName(), "COLUMN_LOCAL_URI: "+
					c.getString( c.getColumnIndex( DownloadManager.COLUMN_LOCAL_URI )));
			Log.d( getClass().getName(), "COLUMN_STATUS: "+
					c.getInt( c.getColumnIndex( DownloadManager.COLUMN_STATUS )));
			Log.d( getClass().getName(), "COLUMN_REASON: "+
					c.getInt( c.getColumnIndex( DownloadManager.COLUMN_REASON )));
			
			Log.v( TAG, "Downloaded : "+c.getLong( c.getColumnIndex( DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR ))+ "bytes, Status : "+c.getInt( c.getColumnIndex( DownloadManager.COLUMN_STATUS )) );
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private void tickTime(){
		time_updater = new Timer();
		time_updater.scheduleAtFixedRate( new TimerTask() {
			
			@Override
			public void run(){
				// Update download status
				queryStatus();
			}
		}, 0, 5000 );
	}
}
