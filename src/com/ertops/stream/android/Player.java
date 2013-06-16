package com.ertops.stream.android;


import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class Player extends Service {
	private static final String TAG = "Player";
	MediaPlayer player;
	
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		player.reset();
		
		Toast.makeText(this, "Connecting ...", Toast.LENGTH_LONG).show();
		
		try {
			player.setDataSource(intent.getStringExtra("url"));
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			player.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		player.start();
		
		Toast.makeText(this, "Listening to " + intent.getStringExtra("name"), Toast.LENGTH_LONG).show();
		
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		player = new MediaPlayer();
	}

	@Override
	public void onDestroy() {
		player.stop();
		player.reset();
		player.release();
		Toast.makeText(this, "Stream Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		Log.d(TAG, "onStart");
		onBind(intent);
	}
}