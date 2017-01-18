package com.excel.datadownloader;

import static com.excel.datadownloader.Constants.FIRMWARE_FILE_SIZE;
import static com.excel.datadownloader.Constants.TAG;
import static com.excel.datadownloader.Constants.URL_FIRMWARE;
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

public class OTAUpdateService extends Service {

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
		Log.i( TAG, "OTAUpdateService started" );
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
				String current_md5 = null;
				String new_md5 = null;
				String require_update = null;
				String file_size = null;
				String ro_build_display_id = null;
				String excel_version_id = null;
				String file_path = null;
				
				try{
					jsonObject = new JSONObject( result );
					current_md5 		= jsonObject.getString( "current_md5" );
					new_md5				= jsonObject.getString( "new_md5" );
					require_update		= jsonObject.getString( "require_update" );
					file_size			= jsonObject.getString( "file_size" );
					ro_build_display_id = jsonObject.getString( "ro_build_display_id" );
					excel_version_id	= jsonObject.getString( "excel_version_id" );
					file_path			= jsonObject.getString( "file_path" );
					
					// Store file size into spfs 
					SharedPreferencesHelper.editSharedPreference( spfs, FIRMWARE_FILE_SIZE, file_size );
					Log.i( "OTAUpdateService : ", "Current MD5 : "+current_md5+", New MD5 : "+new_md5+", Update : "+require_update+", File Size : "+file_size+" MB , ro_build_display_id : "+ro_build_display_id+", Excel version ID : "+excel_version_id );
				}
				catch( Exception e ){
					e.printStackTrace();
				}
				
				//String md5 = result;
				
				// 2. Compare the md5 received with the md5 stored on /sdcard/firmware_md5.txt
				String prev_md5 = Functions.getMD5OfPreviousFirmware( context );
				Log.i( TAG, "Previous md5 : "+prev_md5 );
				
				Log.i( TAG, "Environment.DIRECTORY_DOWNLOADS : "+Environment.DIRECTORY_DOWNLOADS );
				
				// 3. if firmware_md5.txt is empty OR firmware_md5 != md5 received, then update the firmware
				prev_md5 = prev_md5.trim();
				String ro_build_display_id_of_box = Functions.executeShellCommandWithOp( "getprop ro.build.display.id" ).trim(); 
				//String excel_version_id_of_box = Functions.getContentFromFile( Functions.getFile( "OTS", "excel.version.id" ) ); 
				Log.i( "OTAUpdateService", "Internal ro_build_display_id : "+ro_build_display_id_of_box );
				
				// Delete the Download Directory
				Functions.executeShellCommandWithOp( "rm -rf /mnt/sdcard/Download" );
				
				if( prev_md5.equals( "" ) ){
					// This part is required because, when fresh install of OTS, there is not md5 stored on the box
					Functions.saveDataToFile( Functions.getFile( "OTS", Constants.FIRMWARE_MD5_FILE_NAME ), current_md5 );
				}
				/*else */if( ( require_update.equals( "1" ) ) && 
							( ! current_md5.equals( new_md5 ) ) &&  
								( ! ro_build_display_id_of_box.equals( ro_build_display_id ) ) ){
					
					Log.i( "OTAUpdateService", String.valueOf( require_update.equals( "1" ) ) + ", " +String.valueOf( !current_md5.equals( new_md5 ) ) + ", " + String.valueOf( !ro_build_display_id_of_box.equals( ro_build_display_id ) ) );
					
					Functions.saveDataToFile( Functions.getFile( "OTS", Constants.FIRMWARE_MD5_FILE_NAME ), new_md5 );
					Log.i( TAG, "Old Md5 : "+current_md5+", New md5 : "+new_md5 );
					Log.i( TAG, "Time to update the firmware !!" );
					
					// sendBroadcast( new Intent( "show_ota_download_complete" ) );
					
					Intent in = new Intent( context, OTADownloadingActivity.class );
					in.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
					context.startActivity( in );
					
					// Store md5 into spfs
					SharedPreferencesHelper.editSharedPreference( spfs, "md5", new_md5 );
					Functions.saveDataToFile( Functions.getFile( "OTS", Constants.FIRMWARE_MD5_FILE_NAME ), current_md5 );
					
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
				
			}

		};

		String IP = Functions.getCMSIpFromTextFile();
		String WEB_SERVICE = "http://" + IP + File.separator + URL_WEB_SERVICE;
		String url_params = "what_do_you_want=get_firmware_md5&mac_address=" + Functions.getMacAddress( context );;
		String params[] = { WEB_SERVICE, "POST", url_params };
		ir.execute( params );
		
		return START_NOT_STICKY;
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
