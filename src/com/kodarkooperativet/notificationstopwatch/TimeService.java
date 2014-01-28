package com.kodarkooperativet.notificationstopwatch;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;






import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.widget.RemoteViews;

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
 *    
 * Service for handling remove calls through the notification.
 * 
 * @author KodarKooperativet
 *
 */
public class TimeService extends Service implements PropertyChangeListener {

	private static final String ACTION_CHANGESTATE	= "com.kodarkooperativet.notificationstopwatch.action_changestate";
	private static final String ACTION_RESET 		= "com.kodarkooperativet.notificationstopwatch.action_reset";
	private static final String ACTION_EXIT 		= "com.kodarkooperativet.notificationstopwatch.action_exit";

    private static final int NOTIFICATION_ID = 590123562;

    private NotificationManager mNotificationManager;
    private boolean isNotificationShowing;
	private BroadcastReceiver recieverStateChange;
	private BroadcastReceiver recieverReset;
	private BroadcastReceiver recieverExit;
	private Timer t;
//	private Bitmap btnPause;
//	private Bitmap btnStart;
	private Builder mBuilder;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		isNotificationShowing = false;
        this.mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	IntentFilter filterNext = new IntentFilter(ACTION_CHANGESTATE);
    	recieverStateChange = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(TimeContainer.getInstance().getCurrentState() == TimeContainer.STATE_RUNNING) {
					TimeContainer.getInstance().pause();
				} else {
					TimeContainer.getInstance().start();
				}
				updateNotification();
			}
		};
    	registerReceiver(recieverStateChange, filterNext);
    	recieverReset = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				TimeContainer.getInstance().reset();
				updateNotification();
			}
		};
    	IntentFilter filterPause = new IntentFilter(ACTION_RESET);
    	registerReceiver(recieverReset, filterPause);
    	IntentFilter filterPrev = new IntentFilter(ACTION_EXIT);
    	recieverExit = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				TimeContainer.getInstance().reset();
				stopForeground(true);
				isNotificationShowing = false;
				stopSelf();
			}
		};
    	registerReceiver(recieverExit, filterPrev);
		startUpdateTimer();
		TimeContainer.getInstance().isServiceRunning.set(true);
		return START_STICKY;
	}
	
	@Override
	public void onCreate() {
//		btnPause = BitmapFactory.decodeResource(getResources(), R.drawable.btn_pause);
//		btnStart = BitmapFactory.decodeResource(getResources(), R.drawable.btn_start);
    	mBuilder = new Notification.Builder(this)
    	        .setSmallIcon(R.drawable.ic_launcher)
    	        .setOnlyAlertOnce(true);
		super.onCreate();
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
				updateNotification();
			}
		}, 0, 1000);
	}
	
	private synchronized void updateNotification() {
    	RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.stopwatch_notification);
    	if(TimeContainer.getInstance().getCurrentState() == TimeContainer.STATE_RUNNING) {
			contentView.setImageViewResource(R.id.btn_notification_changestate, R.drawable.btn_pause);
    	} else {
			contentView.setImageViewResource(R.id.btn_notification_changestate, R.drawable.btn_start);
    	}

    	contentView.setTextViewText(R.id.tv_notification_time, msToMinSec(TimeContainer.getInstance().getElapsedTime()) );

    	Intent changeStateIntent = new Intent(ACTION_CHANGESTATE, null);
    	PendingIntent changeStatePendingIntent = PendingIntent.getBroadcast(this, 0, changeStateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    	
    	Intent resetIntent = new Intent(ACTION_RESET, null);
    	PendingIntent resetPendingIntent = PendingIntent.getBroadcast(this, 0, resetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    	
    	Intent exitIntent = new Intent(ACTION_EXIT, null);
    	PendingIntent exitPendingIntent = PendingIntent.getBroadcast(this, 0, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    	contentView.setOnClickPendingIntent(R.id.btn_notfication_reset, resetPendingIntent);
    	contentView.setOnClickPendingIntent(R.id.btn_notification_changestate, changeStatePendingIntent);
    	contentView.setOnClickPendingIntent(R.id.btn_notification_exit, exitPendingIntent);

        mBuilder.setContent(contentView);
    	Intent resultIntent = new Intent(this, StopwatchActivity.class);


        Intent notificationIntent = new Intent(this, StopwatchActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, PendingIntent.FLAG_UPDATE_CURRENT, notificationIntent, 0);
    	mBuilder.setContentIntent(intent);
    	if(isNotificationShowing) {
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.getNotification());
    	} else {
    		isNotificationShowing = true;
    		startForeground(NOTIFICATION_ID, mBuilder.getNotification());
    	}
	}
	
	@Override
	public void onDestroy() {
		if(t != null) {
			t.cancel();
			t = null;
		}
		unregisterReceiver(recieverExit);
		unregisterReceiver(recieverReset);
		unregisterReceiver(recieverStateChange);
		TimeContainer.getInstance().isServiceRunning.set(false);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public static class TimeContainer {
		
		private static TimeContainer instance;

		public AtomicBoolean isServiceRunning;
		
		private PropertyChangeSupport observers;
		
		public static final int STATE_STOPPED = 0;
		public static final int STATE_PAUSED  = 1;
		public static final int STATE_RUNNING = 2;
		
		public static final String STATE_CHANGED = "state_changed";
		
		private int currentState;
		private long startTime;
		private long elapsedTime;
		
		private Object mSynchronizedObject = new Object();
		
		private TimeContainer() { 
			isServiceRunning = new AtomicBoolean(false);
			observers = new PropertyChangeSupport(this);
		}
		
		public void addObserver(PropertyChangeListener listener) {
			observers.addPropertyChangeListener(listener);
		}
		
		public void removeObserver(PropertyChangeListener listener) {
			observers.removePropertyChangeListener(listener);
		}
		
		public static TimeContainer getInstance() {
			if(instance == null) {
				instance = new TimeContainer();
			}
			return instance;
		}
		
		public void notifyStateChanged() {
			observers.firePropertyChange(STATE_CHANGED, null, currentState);
		}
		
		public int getCurrentState() {
			return currentState;
		}
		
		public long getStartTime() {
			return startTime;
		}
		
		public long getElapsedTime() {
			if(startTime == 0) {
				return elapsedTime;
			} else {
				return elapsedTime + (System.currentTimeMillis() - startTime);
			}
		}
		
		public void start() {
			synchronized (mSynchronizedObject) {
				startTime = System.currentTimeMillis();
				currentState = STATE_RUNNING;
				notifyStateChanged();
			}
		}
		
		public void reset() {
			synchronized (mSynchronizedObject) {
				if(currentState == STATE_RUNNING) {
					startTime = System.currentTimeMillis();
					elapsedTime = 0;
					currentState = STATE_RUNNING;
					notifyStateChanged();
				} else {
					startTime = 0;
					elapsedTime = 0;
					currentState = STATE_STOPPED;
					notifyStateChanged();
				}
			}
		}

		public void stopAndReset() {
			synchronized (mSynchronizedObject) {
				startTime = 0;
				elapsedTime = 0;
				currentState = STATE_STOPPED;
				notifyStateChanged();
			}
		}
		
		public void pause() {
			synchronized (mSynchronizedObject) {
				elapsedTime = elapsedTime + (System.currentTimeMillis() - startTime);
				startTime = 0;
				currentState = STATE_PAUSED;
				notifyStateChanged();
			}
		}
		
	}
	
	private String msToMinSec(long l){
		if(l == 0) {
			return "00:00";
		} else {
			long seconds = (l / 1000) % 60;
			long minutes = (l / 1000) / 60;
			StringBuilder sb = new StringBuilder();
			if(minutes < 10) {
				sb.append(0);
			}
			sb.append(minutes);
			sb.append(':');
			if(seconds < 10) {
				sb.append(0);
			}
			sb.append(seconds);
			return sb.toString();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if(event.getPropertyName() == TimeContainer.STATE_CHANGED) {
			startUpdateTimer();
		}
	}

}
