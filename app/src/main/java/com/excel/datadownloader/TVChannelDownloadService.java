package com.excel.datadownloader;

import static com.excel.datadownloader.Constants.TAG;

import java.io.File;
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

public class TVChannelDownloadService extends Service {

	JSONArray jsonArray;
	JSONObject jsonObject;
	String[] jsonData;
	SharedPreferences spfs;
	String md5;
	String temp_md5;
	int download_deadlock = 1;
	
	Context context = this;
	
	final static String TV_ZIP_PATH 		= "/mnt/sdcard/appstv_data/tv_channels/tv_channels.zip";
	final static String TV_ZIP_RESTORE_DIR 	= "/mnt/sdcard/appstv_data/tv_channels";
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand( Intent intent, int flags, int startId ) {
		Log.i( TAG, "TVChannel Downloading Service started " );
		
		// Initialize SP
		// spfs = Functions.createSharedPreference( context, Constants.SP_SETTINGS );
		
		// 2. Connect to the internet and perform downloading process
		downloadIt();
		
		return START_STICKY;
	}
	
	public void downloadIt(){
    	// 1. Download zip file from internet
    	InternetRequest download_zip = new InternetRequest(){

    		@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}
    		
			@Override
			public String doInBackground( String... params ) {
				String result = Functions.makeRequestForData( params[ URL ], params[ METHOD ], params[ URL_PARAMETERS ] );
				return result;
			}

    		String base64zip = null;
    		boolean ok = false;
			@Override
			public void onPostExecute(String result) {
				super.onPostExecute(result);

				/*Functions.executeShellCommandWithOp( "am force-stop com.amlogic.tvservice",
			   			 "am force-stop com.amlogic.DTVPlayer" );
					Log.i( TAG, "Kill : am force-stop com.amlogic.tvservice    &    am force-stop com.amlogic.DTVPlayer" );
				*/
				try{
					if( result != null ){  
						jsonArray = new JSONArray( String.valueOf( result ) );
						jsonData  = new String[ jsonArray.length() ];
						jsonObject = jsonArray.getJSONObject( 0 );
						md5 = (String) jsonObject.get( "md5" );
						base64zip = (String) jsonObject.get( "base64zip" );   
						String old_md5 = "none";
						
						if( ( new File( TV_ZIP_PATH ) ).exists() ){
							// calculate the md5 of the previously downloaded file
							old_md5 = MD5.getMD5Checksum( new File( TV_ZIP_PATH ) );
						}
						Log.i( TAG, "md5 : "+md5 );
						Log.i( TAG, "old md5 : "+old_md5 );
						
						
						if( old_md5.equals( "none" ) || ( !old_md5.equals( md5 ) ) ){
							// store and unzip the file
							unzipNewZip( base64zip );
							Functions.logAndToast( context, TAG, "Unzip new zip" );
							ok = true;
						}
						else{
							// unzip the perviously downloaded file again
					    	unzipOldZip();
					    	Functions.logAndToast( context, TAG, "Unzip old zip" );
					    	ok = true;
						}
					}
					else{
						// Toast.makeText( RestoreService.this, "abc.zip File not available on the server", Toast.LENGTH_LONG).show();
						Log.i( "TVChannelsRestore", "tv_channels.zip File not available on the server" );
					}
				}
				catch( Exception e ){
					e.printStackTrace();
				}
				
				if( !ok ){
					// unzip the perviously downloaded file again
			    	unzipOldZip();
			    	Functions.logAndToast( context, TAG, "Unzip old zip" );
				}
				
				// Compare if the downloaded tv_channels.zip file is correctly downloaded or not.
				try{
					temp_md5 = MD5.getMD5Checksum( new File( TV_ZIP_PATH ) );
					// if md5 is different, then download again
					if( ! temp_md5.equals( md5 ) ){
						if( download_deadlock == 3 ){
							Log.e( TAG, "TvChannels zip file Download Deadlock occurred. So suspending !" );
							return;
						}
						downloadIt();
						download_deadlock++;
					}
				}
				catch( Exception e ){
					e.printStackTrace();
				}
			}
    	};
    	String ip = Functions.getCMSIpFromTextFile();
    	String URL = "http://" + ip + File.separator + Constants.URL_WEB_SERVICE;
    	String URL_PARAMETERS = "what_do_you_want=tv_channels_file&mac_address=" + Functions.getMacAddress( context );
    	Functions.logAndToast( context, TAG, URL + "?" + URL_PARAMETERS );
    	download_zip.execute( URL, "POST", URL_PARAMETERS );
    }
	
	public void unzipNewZip( String base64zip ){
		Functions.logAndToast( context, TAG, "Restore dir path : "+TV_ZIP_RESTORE_DIR );
		File f = new File( TV_ZIP_RESTORE_DIR );
		
		if( ! f.exists() )
			f.mkdirs();
		
		byte binary_zip_file[] = Base64.decode( String.valueOf( base64zip ), Base64.DEFAULT );
		
		FileOutputStream fos;
		try{
			fos = new FileOutputStream( new File( TV_ZIP_PATH ) );
			fos.write( binary_zip_file );
			
			// 2. Extract the zip file to where it was downloaded
			Functions.executeShellCommandWithOp( "rm -r /mnt/sdcard/appstv_data/tv_channels/backup" );
	    	Compress.unZipIt( TV_ZIP_PATH, TV_ZIP_RESTORE_DIR );
	    	Functions.logAndToast( context, TAG, "New tv_channels.zip extracted successfully" );
		}
		catch( Exception e ){
			e.printStackTrace();
			Toast.makeText( TVChannelDownloadService.this, "Exception : " + e.toString(), Toast.LENGTH_LONG ).show();
		}
	}
	
	void deleteDir( File file ){
	    File[] contents = file.listFiles();
	    if ( contents != null ) {
	        for ( File f : contents ) {
	            deleteDir( f );
	        }
	    }
	    file.delete();
	}
	
	public void unzipOldZip(){
		Functions.logAndToast( context, TAG, "Restore dir path : "+TV_ZIP_RESTORE_DIR );
		File f = new File( TV_ZIP_RESTORE_DIR );
		
		try{
			Functions.executeShellCommandWithOp( "rm -r /mnt/sdcard/appstv_data/tv_channels/backup" );
			Compress.unZipIt( TV_ZIP_PATH, TV_ZIP_RESTORE_DIR );
	    	Functions.logAndToast( context, TAG, "Old tv_channels.zip extracted successfully" );
		}
		catch( Exception e ){
			e.printStackTrace();
			Toast.makeText( TVChannelDownloadService.this, "Exception : " + e.toString(), Toast.LENGTH_LONG ).show();
		}
	}
}
