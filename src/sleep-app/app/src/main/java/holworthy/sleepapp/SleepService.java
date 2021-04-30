package holworthy.sleepapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SleepService extends Service implements Runnable, SensorEventListener {

	private boolean isRunning = false;
	private ArrayList<DataPoint> dataPoints;
	private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	@Override
	public void onCreate() {
		super.onCreate();
		System.out.println("GOT CREATED");
	}

	@Override
	public void onDestroy() {
		System.out.println("GOT DESTROYED");
		super.onDestroy();
	}

	public class MyBinder extends Binder {
		public SleepService getService() {
			return SleepService.this;
		}
	}

	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	public void start() {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			NotificationChannel notificationChannel = new NotificationChannel("services", "Services", NotificationManager.IMPORTANCE_LOW);
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.createNotificationChannel(notificationChannel);
		}

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "services");
		builder.setContentTitle("Recording Sleep");
		builder.setContentText("The app will run in the background");
		builder.setSmallIcon(R.drawable.bed_icon);
		builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
		Notification notification = builder.build();

		startForeground(123, notification);

		System.out.println("GOT STARTED");
		if(!isRunning) {
			Thread thread = new Thread(this);
			thread.start();
		}
	}

	public void stop() {
		System.out.println("GOT STOPPED");
		isRunning = false;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		System.out.println("GOT BIND: " + intent);
		return new MyBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		System.out.println("GOT UNBIND: " + intent);
		return super.onUnbind(intent);
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

	private void writeSleepFile(File sleepFile, ArrayList<DataPoint> dataPoints) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(sleepFile, true);
		DataPointOutputStream dataPointOutputStream = new DataPointOutputStream(fileOutputStream);
		for(DataPoint dataPoint : dataPoints)
			dataPointOutputStream.writeDataPoint(dataPoint);
		dataPointOutputStream.close();
		fileOutputStream.close();
	}

	@Override
	public void run() {
		isRunning = true;
		System.out.println("RUNNING IN THREAD");

		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sleepapp:lock");
		SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		wakeLock.acquire();
		dataPoints = new ArrayList<>();
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 50000);

		File sleepFile = makeSleepFile();
		if(sleepFile == null) {
			// TODO: handle this error
			return;
		}

		System.out.println("Made a sleep file " + sleepFile);
		while(isRunning) {
			try {
				Thread.sleep(60 * 100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("Writing to file...");

			ArrayList<DataPoint> dataPointsToWrite = dataPoints;
			dataPoints = new ArrayList<>();

			try {
				writeSleepFile(sleepFile, dataPointsToWrite);
			} catch (IOException e) {
				// TODO: handle this error or maybe just hope it never happens
				e.printStackTrace();
			}
		}
		System.out.println("Done writing");

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
