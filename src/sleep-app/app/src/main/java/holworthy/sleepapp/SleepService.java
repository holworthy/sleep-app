package holworthy.sleepapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SleepService extends Service implements Runnable, SensorEventListener {

	private boolean isRecording = false;
	private File sleepFile;
	private DataPoints dataPoints;
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	private SharedPreferences sharedPreferences;

	public class MyBinder extends Binder {
		public SleepService getService() {
			return SleepService.this;
		}
	}

	public boolean isRecording() {
		return isRecording;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		System.out.println("GOT CREATED");
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		System.out.println("GOT STARTED " + intent);
		if(intent == null) {
			boolean supposedToBeRecording = sharedPreferences.getBoolean("isRecording", false);
			if(!isRecording && supposedToBeRecording)
				startRecording();
		}
		return START_STICKY;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		System.out.println("GOT BIND: " + intent);
		return new MyBinder();
	}

	@Override
	public void onDestroy() {
		System.out.println("GOT DESTROYED");
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		System.out.println("GOT UNBIND: " + intent);
		if(!isRecording)
			stopSelf();
		return true;
	}

	@Override
	public void onRebind(Intent intent) {
		System.out.println("GOT REBIND");
		super.onRebind(intent);
	}

	public void startRecording() {
		System.out.println("GOT STARTED");
		if(!isRecording) {
			System.out.println("ACTUALLY STARTED");

			NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "services");
			builder.setContentTitle("Recording Sleep");
			builder.setContentText("The app will run in the background");
			builder.setSmallIcon(R.drawable.bed_icon);
			builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
			Notification notification = builder.build();
			startForeground(123, notification);

			isRecording = true;
			sleepFile = makeSleepFile();

			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean("isRecording", true);
			editor.apply();

			Thread thread = new Thread(this, "Saving-Thread");
			thread.start();
		}
	}

	public void stopRecording() {
		System.out.println("GOT STOPPED");
		isRecording = false;

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("isRecording", false);
		editor.apply();
	}

	private File makeSleepFile() {
		File sdcard = Environment.getExternalStorageDirectory();
		File sleepFolder = new File(sdcard, "/sleepapp");
		File sleepFile = new File(sleepFolder, simpleDateFormat.format(new Date()) + ".slp");
		sleepFolder.mkdirs();
		try {
			if(!sleepFile.createNewFile())
				return null;
		} catch (IOException e) {
			return null;
		}
		return sleepFile;
	}

	private void writeSleepFile(File sleepFile, DataPoints dataPoints) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(sleepFile, true);
		DataPointOutputStream dataPointOutputStream = new DataPointOutputStream(fileOutputStream);
		for(DataPoint dataPoint : dataPoints)
			dataPointOutputStream.writeDataPoint(dataPoint);
		dataPointOutputStream.close();
		fileOutputStream.close();
	}

	@Override
	public void run() {
		System.out.println("RUNNING IN THREAD");

		File sleepFile = this.sleepFile;

		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sleepapp:lock");
		SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		wakeLock.acquire();
		dataPoints = new DataPoints();
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 50000);

		while(isRecording) {
			try {
				Thread.sleep(30 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("FILE WRITE");

			DataPoints dataPointsToWrite = dataPoints;
			dataPoints = new DataPoints();

			try {
				writeSleepFile(sleepFile, dataPointsToWrite);
			} catch (IOException e) {
				// TODO: handle this error or maybe just hope it never happens
				e.printStackTrace();
			}
		}
		System.out.println("THREAD DONE");

		sensorManager.unregisterListener(this);
		wakeLock.release();
		stopForeground(true);
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float[] values = sensorEvent.values;
			dataPoints.add(new DataPoint(
				System.currentTimeMillis() + (sensorEvent.timestamp - SystemClock.elapsedRealtimeNanos()) / 1000000,
				values[0],
				values[1],
				values[2]
			));
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
