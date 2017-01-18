package com.excel.datadownloader;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

public class InternetRequest extends AsyncTask<String, Integer, String> {

	Context ct;
	static int URL = 0;
	static int METHOD = 1;
	static int URL_PARAMETERS = 2;
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(String... params) {
		String result = Functions.makeRequestForData( params[URL], params[METHOD], params[URL_PARAMETERS] );
		return result;
	} 
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		
	}

}
