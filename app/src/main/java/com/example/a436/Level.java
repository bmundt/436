package com.example.a436;

import com.example.a436.orientation.Orientation;
import com.example.a436.orientation.OrientationListener;
import com.example.a436.orientation.OrientationProvider;
import com.example.a436.painter.LevelPainter;
import com.example.a436.view.LevelView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

/*
 *  This file is part of Level (an Android Bubble Level).
 *  <https://github.com/avianey/Level>
 *  
 *  Copyright (C) 2014 Antoine Vianey
 *  
 *  Level is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Level is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Level. If not, see <http://www.gnu.org/licenses/>
 */
public class Level extends Activity implements OrientationListener {
	
	private static Level CONTEXT;
	
	private static final int DIALOG_CALIBRATE_ID = 1;
	private static final int TOAST_DURATION = 10000;
	
	private OrientationProvider provider;
	
    private LevelView view;
	private LevelPainter painter;
    
	/** Gestion du son */
	private SoundPool soundPool;
	private boolean soundEnabled;
	private int bipSoundID;
	private int bipRate;
	private long lastBip;

	private boolean timerStarted;
	private int taps;
	private int totalTaps;
	private CountDownTimer timer;
	public TextView text;
	String hand;

	ArrayList Xlist = new ArrayList<Double>(2000);
	ArrayList Ylist = new ArrayList<Double>(2000);
	int middleX, middleY;
	private static final String TAG = "MyActivity";



	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		hand = getIntent().getExtras().getString("hand");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		text = (TextView) this.findViewById(R.id.timeLeft);
        CONTEXT = this;
        view = (LevelView) findViewById(R.id.level);

        // sound
    	soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
    	bipSoundID = soundPool.load(this, R.raw.bip, 1);
    	bipRate = getResources().getInteger(R.integer.bip_rate);

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				text.setText("READY");
			}
		}, 1000);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				text.setText("SET");
			}
		}, 3000);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				text.setText("BALANCE");
			}
		}, 5000);

		running();
		//final Button tap = (Button) findViewById(R.id.startButton);


    }

	public void running() {
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Xlist = view.getXList();
				Ylist = view.getYList();
				middleX = view.getMiddleX();
				middleY = view.getMiddleY();
				Intent goToNextActivity = new Intent(getApplicationContext(), LevelResults.class);
				goToNextActivity.putExtra("XList", Xlist);
				goToNextActivity.putExtra("YList", Ylist);
				goToNextActivity.putExtra("middleX", middleX);
				goToNextActivity.putExtra("middleY", middleY);
				goToNextActivity.putExtra("hand", hand);
				startActivity(goToNextActivity);
			}
		}, 19000);
	}

	public void startButton(View v) {
		if (!timerStarted) { // only start timer if not already started
			timer.start();
			taps++;
			timerStarted = true;
		} else {
			taps++;
		}
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return true;
	}
    
    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case R.id.calibrate:
	            showDialog(DIALOG_CALIBRATE_ID);
	            return true;
	        case R.id.preferences:
	            startActivity(new Intent(this, LevelPreferences.class));
	            return true;
        }
        return false;
    }
    
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
	        case DIALOG_CALIBRATE_ID:
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setTitle(R.string.calibrate_title)
	        			.setIcon(null)
	        			.setCancelable(true)
	        			.setPositiveButton(R.string.calibrate, new DialogInterface.OnClickListener() {
	        	           	public void onClick(DialogInterface dialog, int id) {
	        	        	   	provider.saveCalibration();
	        	           	}
	        			})
	        	       	.setNegativeButton(R.string.cancel, null)
	        	       	.setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
	        	           	public void onClick(DialogInterface dialog, int id) {
	        	           		provider.resetCalibration();
	        	           	}
	        	       	})
	        	       	.setMessage(R.string.calibrate_message);
	        	dialog = builder.create();
	            break;
	        default:
	            dialog = null;
        }
        return dialog;
    }
    
    protected void onResume() {
    	super.onResume();
    	Log.d("Level", "Level resumed");
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	provider = OrientationProvider.getInstance();
    	// chargement des effets sonores
        soundEnabled = prefs.getBoolean(LevelPreferences.KEY_SOUND, false);
        // orientation manager
        if (provider.isSupported()) {
    		provider.startListening(this);
    	} else {
    		Toast.makeText(this, getText(R.string.not_supported), TOAST_DURATION).show();
    	}
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (provider.isListening()) {
        	provider.stopListening();
    	}
    }
    
    @Override
    public void onDestroy() {
		if (soundPool != null) {
			soundPool.release();
		}
		super.onDestroy();
    }

	@Override
	public void onOrientationChanged(Orientation orientation, float pitch, float roll, float balance) {
	    if (soundEnabled 
				&& orientation.isLevel(pitch, roll, balance, provider.getSensibility())
				&& System.currentTimeMillis() - lastBip > bipRate) {
			AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_RING);
			float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_RING);
			float volume = streamVolumeCurrent / streamVolumeMax;
			lastBip = System.currentTimeMillis();
			soundPool.play(bipSoundID, volume, volume, 1, 0, 1);
		}
		view.onOrientationChanged(orientation, pitch, roll, balance);
	}

	@Override
	public void onCalibrationReset(boolean success) {
		Toast.makeText(this, success ? 
				R.string.calibrate_restored : R.string.calibrate_failed, 
				Level.TOAST_DURATION).show();
	}


	@Override
	public void onCalibrationSaved(boolean success) {
		Toast.makeText(this, success ? 
				R.string.calibrate_saved : R.string.calibrate_failed,
				Level.TOAST_DURATION).show();
	}

    public static Level getContext() {
		return CONTEXT;
	}
    
    public static OrientationProvider getProvider() {
    	return getContext().provider;
    }
    
}
