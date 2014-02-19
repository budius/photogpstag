package com.budius.photogpstag;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class ServiceLogger extends Service implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

	private static final int ID = 66642;

	public static final int STS_NONE = 0;
	public static final int STS_CONNECTING_TO_GOOGLE_PLAY = 1;
	public static final int STS_CONNECTED_TO_GOOGLE_PLAY = 2;
	public static final int STS_DISCONNECTED_FROM_GOOGLE_PLAY = 3;
	public static final int STS_FAIL_TO_CONNECT_TO_GOOGLE_PLAY = 4;

	private LocationRequest mLocationRequest;
	private LocationClient mLocationClient;
	private LoggerBinder mLoggerBinder;
	private ServiceLoggerListener listener;
	private Location lastLocation;
	private int status = STS_NONE;

	private FileHandler mFileHandler;

	/*-
	 * Service stuff 
	 * ================================================
	 */

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("Budius", "Service. onStartCommand");
		if (mLocationRequest == null) {
			mLocationRequest = LocationRequest.create();

			long interval, fastest_interval;
			interval = DialogSettings.getInterval(getApplicationContext());
			fastest_interval = interval / 3;
			if (fastest_interval < 1)
				fastest_interval = 1;

			mLocationRequest.setFastestInterval(fastest_interval * 1000);
			mLocationRequest.setInterval(interval * 1000);
			mLocationRequest
					.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		}

		if (mLocationClient == null) {
			mLocationClient = new LocationClient(this, this, this);
		}

		if (mFileHandler == null) {
			mFileHandler = new FileHandler(DialogSettings.getLogFile(this));
		}

		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i("Budius", "Service. onBind");
		if (mLoggerBinder == null)
			mLoggerBinder = new LoggerBinder();
		return mLoggerBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if (status != STS_CONNECTED_TO_GOOGLE_PLAY) {
			Log.i("Budius", "Service. onUnbind, destroying service");
			stopForeground(true);
			stopSelf();
		} else {
			Log.i("Budius", "Service. onUnbind, keeping service alive");
		}
		return false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("Budius", "Service. onDestroy");
		try {
			mLocationClient.removeLocationUpdates(this);
			mLocationClient.disconnect();
		} catch (Exception e) {
			Log.e("Budius", e.getMessage());
		}
	}

	private void setNewStatus(int newStatus) {
		if (status != newStatus) {
			status = newStatus;
			if (listener != null)
				listener.updateServiceStatus(status);
		}

	}

	/*-
	 * OnConnectionFailedListener stuff
	 * ================================================
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		setNewStatus(STS_FAIL_TO_CONNECT_TO_GOOGLE_PLAY);
		if (listener != null)
			listener.CallGooglePlay(result);
	}

	/*-
	 * ConnectionCallbacks stuff
	 * ================================================
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i("Budius", "Starting foreground");
		startForeground(ID, getNotification());
		setNewStatus(STS_CONNECTED_TO_GOOGLE_PLAY);
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
	}

	@Override
	public void onDisconnected() {
		Log.i("Budius", "Stoping foreground");
		stopForeground(true);
		setNewStatus(STS_DISCONNECTED_FROM_GOOGLE_PLAY);
	}

	/*-
	 * LocationListener stuff
	 * ================================================
	 */
	@Override
	public void onLocationChanged(Location location) {
		lastLocation = location;
		
		Thread fileThread = new Thread(new AddToLogRunnable());
		fileThread.setPriority(Thread.MIN_PRIORITY);
		fileThread.start();

		updateNotification();

		if (listener != null)
			listener.newLocation(location);
	}

	public class LoggerBinder extends Binder {

		public void setCallGooglePlayListener(ServiceLoggerListener l) {
			listener = l;
			if (listener != null) {
				listener.updateServiceStatus(status);
				if (lastLocation != null)
					listener.newLocation(lastLocation);
			}
		}

		public void startLogging() {
			setNewStatus(STS_CONNECTING_TO_GOOGLE_PLAY);
			mLocationClient.connect();
		};

		public void stopLogging() {
			setNewStatus(STS_DISCONNECTED_FROM_GOOGLE_PLAY);
			mLocationClient.disconnect();
			Log.i("Budius", "Stoping foreground");
			stopForeground(true);
		};
	}

	private class AddToLogRunnable implements Runnable {

		@Override
		public void run() {
			mFileHandler.addToLog(lastLocation);
		}

	}

	public interface ServiceLoggerListener {
		public void CallGooglePlay(ConnectionResult result);

		public void updateServiceStatus(int serviceStatus);

		public void newLocation(Location location);
	}

	private Notification getNotification() {
		return getNotification("", -1);
	}

	private Notification getNotification(String contentText, long when) {
		Intent intent = new Intent(getApplicationContext(), ActivityMain.class);
		PendingIntent pIntent = PendingIntent.getActivity(
				getApplicationContext(), ID, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		Notification.Builder b = new Notification.Builder(
				getApplicationContext());

		b.setAutoCancel(false);
		if (when > 0)
			b.setWhen(when);
		b.setContentIntent(pIntent);
		b.setContentText(contentText);
		b.setContentTitle("Camera Raw GPS Logger");
		b.setOngoing(false);
		b.setOnlyAlertOnce(true);
		b.setTicker("Starting to log your location");
		b.setSmallIcon(R.drawable.ic_launcher);
		return b.build();
	}

	private void updateNotification() {
		NotificationManager mn = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		StringBuilder sb = new StringBuilder();
		sb.append("Lat: ");
		sb.append(lastLocation.getLatitude());
		sb.append("; Lng: ");
		sb.append(lastLocation.getLongitude());
		mn.notify(ID, getNotification(sb.toString(), lastLocation.getTime()));
	}
}
