package com.excel.datadownloader;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
	
	/*
	 * Note : In order to Read/Write to other app's SP, you need to include same
	 * android:sharedUserId="same for both the apps" in both the app's Manifest File 
	 * 
	 */
	
	final static String APPLICATION_SETTINGS						= "settings";
	final static String APPLICATION_DATA							= "data";

	/*
	 * Creating SharedPreference with Private Mode
	 * @params context Application Context
	 * @params name Name of the SharedPreferences XML file
	 * 
	 */
	public static SharedPreferences createSharedPreference( Context context, String name ){
		SharedPreferences spfs = context.getSharedPreferences( name, Context.MODE_PRIVATE );
		return spfs;
	}
	// ----- /Creating SharedPreference with Private Mode

	// ----- Creating SharedPreference for any Mode
	public static SharedPreferences createSharedPreference( Context context, String name, int MODE ){
		SharedPreferences spfs = context.getSharedPreferences( name, MODE );
		return spfs;
	}
	// ----- /Creating SharedPreference for any Mode

	// ----- Creating SharedPreference of another Application
	public static SharedPreferences openOtherAppsSharedPreference( Context context, String package_name, String SP_NAME, int MODE ){
		SharedPreferences spfs = null;
		Context other_apps_context;
		try{
			other_apps_context = context.createPackageContext
	        		( package_name, Context.MODE_WORLD_WRITEABLE );
			spfs = other_apps_context.getSharedPreferences
	        		( SP_NAME, MODE );
		}
		catch( Exception e ){
			e.printStackTrace();
		}
		return spfs;
	}
	// ----- /Creating SharedPreference of another Application
	
	// ----- Editing SharedPreference, Adding value to SP
	public static void editSharedPreference( SharedPreferences spfs, String key, String value ){
		SharedPreferences.Editor spe = spfs.edit();
		spe.putString( key, value );
		spe.commit();
	}
	// ----- /Editing SharedPreference , Adding value to SP

	// ----- Retrieving From SharedPreference Starts Here
	public static Object getSharedPreference( SharedPreferences spfs, String name, String default_value ){
		Object value = spfs.getString( name, default_value );
		return value;
	}
	// ----- Accessing SharedPreference Ends Here
	
/*	// ----- Retrieving From Other App's SharedPreference Starts Here
	public static Object getFromOtherAppsSharedPreference( Context context, String package_name, String SP_NAME, int MODE ){
		
		Context other_apps_context = context.createPackageContext
        		( package_name, Context.MODE_WORLD_WRITEABLE );
        
		SharedPreferences spfs = other_apps_context.getSharedPreferences
        		( SP_NAME, MODE );
		
		Object value = spfs.getString( name, default_value );
		return value;
	}
	// ----- Retrieving From Other App's SharedPreferences Ends Here
*/}
