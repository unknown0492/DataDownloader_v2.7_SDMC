package com.excel.datadownloader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.excel.util.MD5;

import static com.excel.datadownloader.Constants.*;

public class DownloadWallpaperService extends Service
{

	String IP;
	String URL;
	final Context context = this;
	SharedPreferences spfs;

	private void downloadSingleWallpaper( final String wp_name ){
		( new InternetRequest() {

			protected void onPostExecute( String s )
			{
				super.onPostExecute( s );
				if ( s == null ){
					Functions.logAndToast( context, TAG, "Result null" );
					return;
				}
				try{
					Object obj = ( new JSONArray( s ) ).getJSONObject( 0 );
					s = ((JSONObject) (obj)).getString( "for" );
					obj = ((JSONObject) (obj)).getString( "info" );
					Log.i( TAG, "Downloaded Wallpaper : " + s );
					Functions.saveFile( "Launcher", wp_name, "jpg", Base64.decode(((String) (obj)), 0));
				}
				catch ( Exception e ){
					e.printStackTrace();
				}
			}

		}).execute( new String[] {
				"http://" + IP + File.separator + "appstv/webservice.php", "POST", 
				"what_do_you_want=get_bg&file_url=" + wp_name + "&mac_address=" + Functions.getMacAddress( context )
		});
	}

	public void downloadLogoCollarAndAirplay( final Context context, final SharedPreferences spfs, final String TAG ){
		String s = "what_do_you_want=launcher_stuffs&mac_address=" + Functions.getMacAddress( context );
		InternetRequest ir = new InternetRequest() {

			protected void onPostExecute( String s1 ){
				super.onPostExecute( s1 );
				if ( s1 == null ){
					Functions.logAndToast( context, TAG, "No logo found or not connected to the Internet" );
					return;
				}
				String s2 = null;
				String s3 = null;
				String s4 = null;
				String base64;
				String as[];
				int l = 10;
				JSONObject jsonObject, jsonObject1 = null;
				JSONArray jsonArray;
				
				try{
					jsonObject = new JSONObject( s1 );
					base64 = jsonObject.getString( "launcher_logo" );
					s2 = jsonObject.getString( "airplay_nickname" );
					s3 = jsonObject.getString( "airplay_password" );
					s4 = jsonObject.getString( "collar_text" );
					Log.d( null, "collar : " + s4 );
					l = jsonObject.getInt( "slideshow_interval" );
					Functions.saveFile( "Launcher", "launcher_logo", "png", Base64.decode( base64, 0 ) );
					jsonArray = jsonObject.getJSONArray( "backgrounds" );
					
					jsonObject1 = jsonArray.getJSONObject( 0 );  // store first wallpaper into as[ 0 ]
					as = new String[ jsonObject1.length() ];
					
					File ff = new File( "/mnt/sdcard/Launcher" + File.separator + "launcher_bg" + "." + "jpg" );
					boolean md5_same = false;
					
					// Compare the md5, if same, dont download
					try{
						if( jsonObject1.getString( "launcher_bg_md5" ).equals( MD5.getMD5Checksum( ff ) ) ){
							md5_same = true;
						}
					}
					catch( JSONException e ){
						Log.e( TAG, e.getMessage() );
					}
					catch( Exception e ){
						e.printStackTrace();
					}
					
					if( ! md5_same ){
						if ( ff.exists() ){
							ff.delete();
						}
						ff.createNewFile();
						
						downloadSingleWallpaper( "launcher_bg" );
						Log.i( TAG, "downloading launcher_bg" );
					}
					else{
						Log.i( TAG, "launcher_bg md5 matched, no need to download it again !" );
					}
				}
				catch( JSONException e ){
					Log.e( TAG, e.getMessage() );
				}
				catch( Exception e ){
					e.printStackTrace();
				}
				
				int i = 1;
				File ff;
				Log.e( null, "obj.length() : " + jsonObject1.length() );

				while ( i <= 14 ){

					try{
						jsonObject1.getString( "launcher_bg" + i );
						ff = new File( "/mnt/sdcard/Launcher" + File.separator + "launcher_bg" + i + "." + "jpg" );
						
						// Compare the md5, if same, dont download
						try{
							if( jsonObject1.getString( "launcher_bg" + i + "_md5").equals( MD5.getMD5Checksum( ff ) ) ){
								Log.i( TAG, "launcher_bg" + i +" md5 matched, no need to download it again !" );
								i++;
								continue;
							}
						}
						catch( JSONException e ){
							Log.e( TAG, e.getMessage() );
						}
						catch( Exception e ){
							e.printStackTrace();
						}
						
						if ( ff.exists() ){
							ff.delete();
						}
						ff.createNewFile();

						downloadSingleWallpaper( "launcher_bg"+i );
						Log.i( TAG, "downloading launcher_bg"+i );
					}
					catch( JSONException e ){
						Log.e( TAG, e.getMessage() );
						ff = new File( "/mnt/sdcard/Launcher" + File.separator + "launcher_bg" + i + "." + "jpg" );
						if ( ff.exists() ){
							ff.delete();
						}
					}
					catch( Exception e ){
						ff = new File( "/mnt/sdcard/Launcher" + File.separator + "launcher_bg" + i + "." + "jpg" );
						if ( ff.exists() ){
							ff.delete();
						}
						e.printStackTrace();
					}

					i++;
				}

				Functions.editSharedPreference( spfs, "airplay_nickname", s2 );
				Functions.editSharedPreference( spfs, "airplay_apssword", s3 );
				Functions.editSharedPreference( spfs, "collar_text", s4 );
				Functions.editSharedPreference( spfs, "slideshow_interval", String.valueOf( l ) );
				s1 = String.format( "nickname:%s,password:%s", new Object[] {
						s2, s3
				});
				Functions.saveDataToFile( Functions.getFile( "OTS", "screencast.txt" ), s1 );
				Log.d( null, "nick n pass : " + s2 + ", " + s3 );
			}	
		};
		Log.i(TAG, (new StringBuilder("URL ISS : ")).append(URL).toString());
		ir.execute( new String[] {
				URL, "POST", s
		});
		Log.i(TAG, (new StringBuilder(String.valueOf(URL))).append("?").append(s).toString());
	}

	public IBinder onBind(Intent intent)
	{
		return null;
	}

	public int onStartCommand( Intent intent, int i, int j ){
		Object obj;
		Functions.logAndToast( context, TAG, "DataDownloadService started" );
		spfs = Functions.createSharedPreference( context, "settings" );
		obj = "";
		String ip = Functions.readData( "OTS", "ip.txt" );
		Functions.logAndToast( context, TAG, "ip ipi pipip : " + ip );
		IP = ip;
		URL = "http://" + IP + File.separator + "appstv/webservice.php";
		downloadLogoCollarAndAirplay( context, spfs, TAG );
		
		return START_STICKY;
	}

}
