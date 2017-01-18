package com.excel.datadownloader;

import static com.excel.datadownloader.Constants.TAG;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {

	@Override
	public void onReceive( Context context, Intent intent ) {
		String s = intent.getAction();
		Functions.logAndToast( context, TAG, ( new StringBuilder("[Receiver] Action : ")).append(s).toString());
		
		if ( s.equals( "android.intent.action.BOOT_COMPLETED" ) || s.equals( "ACTION_BOOT_COMPLETED" ) ){
			Log.i( TAG, "Boot Completed Received 2" );
			// Wake up the app on every boot
			Intent in = new Intent( context, MainActivity.class );
			in.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			context.startActivity( in );
			
			/*Intent in = new Intent( context, DownloadService.class );
			context.startService( in );
			
			// Start RepairDTVService
			context.sendBroadcast( new Intent( "turn_on_repairdtvservice" ) );
			*/
			// Toast.makeText( context, "Boot Completed Received 1", Toast.LENGTH_LONG ).show();
		}
		if ( s.equals( "download_launcher_config" ) ){
			Log.i( TAG, "download_launcher_config has been broadcasted" );
			Intent in = new Intent( context, DownloadService.class );
			in.putExtra( "what", "download_launcher_config" );
			context.startService( in );
		}
		if ( s.equals( "update_cms_ip" ) ){
			Log.i( TAG, "update_cms_ip has been broadcasted" );
			Intent in = new Intent( context, DownloadService.class );
			in.putExtra( "what", "update_cms_ip" );
			context.startService( in );
		}
		if ( s.equals( "update_room_no" ) ){
			Log.i( TAG, "update_room_no has been broadcasted" );
			Intent in = new Intent( context, DownloadService.class );
			in.putExtra( "what", "update_room_no" );
			context.startService( in );
		}
		if ( s.equals( "start_ota" ) ){
			Log.i( TAG, "start_ota has been broadcasted" );
			//Intent in = new Intent( context, OTAUpdateService.class );
			Intent in = new Intent( context, OTAUpdateService1.class );
			context.startService( in );
		}
		if( s.equals( "android.intent.action.DOWNLOAD_COMPLETE" ) ){
			Log.i( TAG, "android.intent.action.DOWNLOAD_COMPLETE" );
			
			// Show Download Complete Screen, for Ota types 0 and 1
			String ota_type = Functions.executeShellCommandWithOp( "getprop ota_type" ).trim();
			if( ota_type.equals( "0" ) || ota_type.equals( "1" ) ){
				context.sendBroadcast( new Intent( "show_ota_download_complete" ) );
			}
		}
		if( s.equals( "start_downloading_wallpapers" ) ){
			Log.i( TAG, "start_downloading_wallpapers has been broadcasted" );
			Intent in = new Intent( context, DownloadWallpaperService.class );
			context.startService( in );
		}
		if( s.equals( "start_restore_service" ) ){
			Log.i( TAG, "start_restore_service has been broadcasted" );
			Intent in = new Intent( context, TVRestoreService.class );
			context.startService( in );
		}
		if( s.equals( "start_tv_download_service" ) ){
			Log.i( TAG, "start_tv_download_service has been broadcasted" );
			Intent in = new Intent( context, TVChannelDownloadService.class );
			context.startService( in );
		}
		if( s.equals( "unzip_launcher_config" ) ){
			Log.i( TAG, "unzip_launcher_config has been broadcasted" );
			Functions.executeShellCommandWithOp( "rm -r /mnt/sdcard/appstv_data/launcher_config", "unzip -o /mnt/sdcard/appstv_data/launcher_config.zip -d /mnt/sdcard/appstv_data" );
			Functions.logAndToast( context, TAG, "launcher_config.zip extracted successfully" );
		}
		if( s.equals( "unzip_tv_channels" ) ){
			Log.i( TAG, "unzip_tv_channels has been broadcasted" );
			TVRestoreService tvrs = new TVRestoreService();
			tvrs.restoreOldZip();
		}
		if ( s.equals( "android.net.conn.CONNECTIVITY_CHANGE" ) || s.equals( "CONNECTIVITY_CHANGE" ) ){
			// Log.i( TAG, "Connectivity change intent sent broadcast : download_launcher_config" );
		}
	}
}