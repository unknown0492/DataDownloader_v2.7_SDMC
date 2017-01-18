package com.excel.datadownloader;

public class Constants {
	
	public static final String TAG = "DataDownloader";
	static final String URL_WEB_SERVICE = "appstv/webservice.php";
	public static final String URL_FIRMWARE						= "appstv/update/update.zip";
		
	
	// FILE
	public final static String FIRMWARE_MD5_FILE_NAME			= "firmware_md5";
	public final static String FIRMWARE_MD5_FILE_PATH			= "/sdcard/OTS/" + FIRMWARE_MD5_FILE_NAME;
	public final static String FIRMWARE_FILE_SIZE				= "firmware_file_size";
	
	// Script
	public static final String FIRMWARE_UPDATE_SCRIPT			= 	"echo 'boot-recovery ' > /cache/recovery/command\n" +
																		"echo '--update_package=/cache/update.zip' >> /cache/recovery/command\n"+
																		"reboot recovery";
}
