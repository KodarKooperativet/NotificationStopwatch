package com.kodarkooperativet.notificationstopwatch;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;

import com.kodarkooperativet.notificationstopwatch.TimeService.TimeContainer;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * 
 *    Copyright 2013 KodarKooperativet
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 * The one and only Activity class. Uses {@link TimeContainer} for communication.
 * 
 * @author KodarKooperativet
 *
 */
public class StopwatchActivity extends Activity implements PropertyChangeListener {
	
	private TextView tvTime;
	private Timer t; 
	private Button btnStart;
	private Handler h;
	
	private final Runnable updateTextRunnable = new Runnable() {
		@Override
		public void run() {
			updateTimeText();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.activity_stopwatch);
		tvTime = (TextView) findViewById(R.id.tvTime);
		btnStart = (Button) findViewById(R.id.btnStart);
		h = new Handler();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
	
	@Override
	protected void onResume() {
		checkServiceRunning();
		TimeContainer t = TimeContainer.getInstance();
		if(t.getCurrentState() == TimeContainer.STATE_RUNNING) {
			btnStart.setText(R.string.stop);
			startUpdateTimer();
		} else {
			btnStart.setText(R.string.start);
			updateTimeText();
		}
		TimeContainer.getInstance().addObserver(this);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		if(t != null) {
			t.cancel();
			t = null;
		}
		TimeContainer.getInstance().removeObserver(this);
		super.onPause();
	}
	
	private void checkServiceRunning() {
		if(!TimeContainer.getInstance().isServiceRunning.get()) {
			startService(new Intent(this, TimeService.class));
		}
	}
	
	public void changeState(View v) {
		TimeContainer tc = TimeContainer.getInstance();
		if (tc.getCurrentState() == TimeContainer.STATE_RUNNING ) { 
			TimeContainer.getInstance().pause();
			btnStart.setText("Start");
		} else {
			TimeContainer.getInstance().start();
			startUpdateTimer();
			btnStart.setText("Stop");
		}
	}
	
	public void reset(View v) {
		TimeContainer.getInstance().reset();
		updateTimeText();
	}
	
	private void updateTimeText() {
		tvTime.setText(getTimeString(TimeContainer.getInstance().getElapsedTime()));
	}
	
	public void startUpdateTimer() {
		if(t != null) {
			t.cancel();
			t = null;
		}
		t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				h.post(updateTextRunnable);
			}
		}, 0, 16);
	}
	
	private String getTimeString(long ms) {
		if(ms == 0) {
			return "00:00:00";
		} else {
			long millis = (ms % 1000) / 10;
			long seconds = (ms / 1000) % 60;
			long minutes = (ms / 1000) / 60;
			long hours = minutes / 60;
			
			StringBuilder sb = new StringBuilder();
			if(hours > 0) {
				sb.append(hours);
				sb.append(':');
			} 
			if(minutes > 0) {
				minutes = minutes % 60;
				if(minutes >= 10) {
					sb.append(minutes);
				} else {
					sb.append(0);
					sb.append(minutes);
				}
			} else {
				sb.append('0');
				sb.append('0');
			}
			sb.append(':');
			if(seconds > 0) {
				if(seconds >= 10) {
					sb.append(seconds);
				} else {
					sb.append(0);
					sb.append(seconds);
				}
			} else {
				sb.append('0');
				sb.append('0');
			}
			sb.append(':');
			if(millis > 0) {
				if(millis >= 10) {
					sb.append(millis);
				} else {
					sb.append(0);
					sb.append(millis);
				}
			} else {
				sb.append('0');
				sb.append('0');
			}
			return sb.toString();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if(event.getPropertyName() == TimeContainer.STATE_CHANGED) {
			TimeContainer t = TimeContainer.getInstance();
			if(t.getCurrentState() == TimeContainer.STATE_RUNNING) {
				btnStart.setText(R.string.stop);
				startUpdateTimer();
			} else {
				btnStart.setText(R.string.start);
				updateTimeText();
			}
			checkServiceRunning();
		}
	}

}
