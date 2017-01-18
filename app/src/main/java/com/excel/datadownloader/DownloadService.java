package com.excel.datadownloader;

import static com.excel.datadownloader.Constants.TAG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.excel.util.MD5;

public class DownloadService extends Service {
	
	Context context = null;
	JSONArray jsonArray;
	JSONObject jsonObject;
	String[] jsonData;
	SharedPreferences spfs;
	String md5;
	String base64zip;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i( null, "Download Service Started !" );
		context = this;
		
		String what = intent.getStringExtra( "what" );
		
		if( what.equals( "download_launcher_config" ) ){
			downloadLauncherConfig();
		}
		if( what.equals( "update_cms_ip" ) ){
			updateCmsIp();
		}
		if( what.equals( "update_room_no" ) ){
			updateRoomNo();
		}
		
		return START_STICKY;
	}
	
	public void downloadLauncherConfig(){
		InternetRequest download_zip = new InternetRequest(){

    		@Override
			protected String doInBackground( String... params ) {
				String result = Functions.makeRequestForData( params[ URL ], params[ METHOD ], params[ URL_PARAMETERS ] );
				return result;
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				
				boolean ok = false;
				
				try{
					if( result != null ){  
						jsonArray = new JSONArray( String.valueOf( result ) );
						jsonData  = new String[ jsonArray.length() ];
						jsonObject = jsonArray.getJSONObject( 0 );
						md5 = (String) jsonObject.get( "md5" );
						base64zip = (String) jsonObject.get( "base64zip" );
						String old_md5 = "none";
						
						if( ( new File( "/mnt/sdcard/appstv_data/launcher_config.zip" ) ).exists() ){
							// calculate the md5 of the previously downloaded file
							old_md5 = MD5.getMD5Checksum( new File( "/mnt/sdcard/appstv_data/launcher_config.zip" ) );
						}
						Log.i( TAG, "md5 : "+md5 );
						Log.i( TAG, "old md5 : "+old_md5 );
						
						if( old_md5.equals( "none" ) || ( !old_md5.equals( md5 ) ) ){
							// store and unzip the file
							saveLauncherConfigZIP();
							ok = true;
						}
						else{
							// unzip the perviously downloaded file again
					    	Functions.executeShellCommand( "unzip -o /mnt/sdcard/appstv_data/launcher_config.zip -d /mnt/sdcard/appstv_data" );
							Functions.logAndToast( context, TAG, "launcher_config.zip extracted successfully" );
							ok = true;
						}
						
						Functions.logAndToast( context, TAG, "Returned launcher_config.zip" );
					}
					else{
						Log.i( TAG, "launcher_config.zip File not available on the server" );
					}
				}
				catch( Exception e ){
					e.printStackTrace();
				}
				
				if( !ok ){
					// unzip the perviously downloaded file again
					Functions.executeShellCommand( "unzip -o /mnt/sdcard/appstv_data/launcher_config.zip -d /mnt/sdcard/appstv_data" );
					Functions.logAndToast( context, TAG, "launcher_config.zip extracted successfully" );
				}
			}
    	};
    	String ip = Functions.getCMSIpFromTextFile();
    	String URL = "http://" + ip/*(String) Functions.getSharedPreference( spfs, Constants.SP_SETTINGS_SERVER_IP_KEY, Constants.DV_IP_ADDRESS )*/ + File.separator + Constants.URL_WEB_SERVICE;
    	String URL_PARAMETERS = "what_do_you_want=get_appstv_data&data_type=launcher_config&mac_address=" + Functions.getMacAddress( context );
    	Functions.logAndToast( context, TAG, URL + "?" + URL_PARAMETERS );
    	download_zip.execute( URL, "POST", URL_PARAMETERS );
	}
	
	public void saveLauncherConfigZIP(){
		String appstv_data_dir_path = "/mnt/sdcard/appstv_data/";
		Functions.logAndToast( context, TAG, "appstv_data_dir_path : "+appstv_data_dir_path );
		File f = new File( appstv_data_dir_path );
		
		if( !f.exists() )
			f.mkdirs();
		
		byte binary_zip_file[] = Base64.decode( String.valueOf( base64zip ), Base64.DEFAULT );
		
		FileOutputStream fos;
		try{
			String cmd;

			fos = new FileOutputStream( new File( "/mnt/sdcard/appstv_data/launcher_config.zip" ) );
			fos.write( binary_zip_file );
			
			// 2. Extract the zip file to where it was downloaded
			Functions.executeShellCommand( "unzip -o /mnt/sdcard/appstv_data/launcher_config.zip -d /mnt/sdcard/appstv_data" );
	    	Functions.logAndToast( context, TAG, "launcher_config.zip extracted successfully" );
			
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}

	public void updateCmsIp(){
		InternetRequest download_cms_ip = new InternetRequest(){

    		@Override
			protected String doInBackground( String... params ) {
				String result = Functions.makeRequestForData( params[ URL ], params[ METHOD ], params[ URL_PARAMETERS ] );
				return result;
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);

				try{
					if( result != null ){  
						jsonArray = new JSONArray( String.valueOf( result ) );
						jsonObject = jsonArray.getJSONObject( 0 );
						if( jsonObject.get( "type" ).equals( "error" ) ){
							Log.i( TAG, "Error : "+jsonObject.get( "info" ) );
						}
						else if( jsonObject.get( "type" ).equals( "success" ) ){
							Log.i( TAG, "Success : "+jsonObject.get( "info" ) );
							// Retrieve the CMS IP from the AppsTv Box
							String old_cms_ip = Functions.getCMSIpFromTextFile();
							String new_cms_ip = (String) jsonObject.get( "info" );
							if( ! old_cms_ip.equals( new_cms_ip ) ){
								// Overwrite the new cms ip onto the old cms ip
								Functions.saveDataToFile( Functions.getFile( "OTS", "ip.txt" ), new_cms_ip );
								Log.i( TAG, "CMS IP updated successfully !" );
							}
							// String new_room_no = (String) jsonObject.get( "room_no" );
						}
						else{
							Log.i( TAG, "Unknown Error : " + result );
						}
						
					}
					else{
						Log.i( TAG, "" );
					}
				}
				catch( Exception e ){
					e.printStackTrace();
				}
			}
    	};
    	String ip = Functions.getCMSIpFromTextFile();
    	String URL = "http://" + ip/*(String) Functions.getSharedPreference( spfs, Constants.SP_SETTINGS_SERVER_IP_KEY, Constants.DV_IP_ADDRESS )*/ + File.separator + Constants.URL_WEB_SERVICE;
    	String URL_PARAMETERS = "what_do_you_want=get_appstv_data&data_type=cms_ip&mac_address=" + Functions.getMacAddress( context );
    	Functions.logAndToast( context, TAG, URL + "?" + URL_PARAMETERS );
    	download_cms_ip.execute( URL, "POST", URL_PARAMETERS );
	}
	
	public void updateRoomNo(){
		InternetRequest download_room_no = new InternetRequest(){

    		@Override
			protected String doInBackground( String... params ) {
				String result = Functions.makeRequestForData( params[ URL ], params[ METHOD ], params[ URL_PARAMETERS ] );
				return result;
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);

				try{
					if( result != null ){  
						jsonArray = new JSONArray( String.valueOf( result ) );
						jsonObject = jsonArray.getJSONObject( 0 );
						if( jsonObject.get( "type" ).equals( "error" ) ){
							Log.i( TAG, "Error : "+jsonObject.get( "info" ) );
						}
						else if( jsonObject.get( "type" ).equals( "success" ) ){
							Log.i( TAG, "Success : "+jsonObject.get( "info" ) );
							// Retrieve the Room No from the AppsTv Box
							String old_room_no = Functions.getRoomNoFromTextFile();
							String new_room_no = (String) jsonObject.get( "info" );
							if( ! old_room_no.equals( new_room_no ) ){
								// Overwrite the new cms ip onto the old cms ip
								Functions.saveDataToFile( Functions.getFile( "OTS", "room_no.txt" ), new_room_no );
								Log.i( TAG, "Room Number updated successfully !" );
							}
						}
						else{
							Log.i( TAG, "Unknown Error : " + result );
						}
						
					}
					else{
						Log.i( TAG, "" );
					}
				}
				catch( Exception e ){
					e.printStackTrace();
				}
			}
    	};
    	String ip = Functions.getCMSIpFromTextFile();
    	String URL = "http://" + ip/*(String) Functions.getSharedPreference( spfs, Constants.SP_SETTINGS_SERVER_IP_KEY, Constants.DV_IP_ADDRESS )*/ + File.separator + Constants.URL_WEB_SERVICE;
    	String URL_PARAMETERS = "what_do_you_want=get_appstv_data&data_type=room_no&mac_address="+Functions.getMacAddress( context );
    	Functions.logAndToast( context, TAG, URL + "?" + URL_PARAMETERS );
    	download_room_no.execute( URL, "POST", URL_PARAMETERS );
	}
}
