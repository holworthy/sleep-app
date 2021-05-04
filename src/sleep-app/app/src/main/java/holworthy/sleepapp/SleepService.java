package holworthy.sleepapp;

import android.annotation.SuppressLint;
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
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SleepService extends Service implements SensorEventListener {
	public static final boolean USE_LOG = true;
	public static final long SAVE_DELAY = 30000;

	private PowerManager.WakeLock wakeLock;
	private SensorManager sensorManager;

	private boolean isRecording = false;
	private File sleepFile;
	private DataPoints dataPoints;
	@SuppressLint("SimpleDateFormat")
	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	private SharedPreferences sharedPreferences;
	private long lastSaveTimestamp = 0;

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
		log("service created");
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sleepapp:lock");
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		log("service started " + intent + ", " + flags);
		boolean supposedToBeRecording = sharedPreferences.getBoolean("isRecording", false);
		log("service supposed to be running? " + supposedToBeRecording + ", isRecording? " + isRecording);
		if(!isRecording && supposedToBeRecording)
			startRecording();
		return START_STICKY;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		log("service bind " + intent);
		return new MyBinder();
	}

	@Override
	public void onDestroy() {
		log("service destroyed");
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		log("service unbind " + intent);
		if(!isRecording)
			stopSelf();
		return true;
	}

	@Override
	public void onRebind(Intent intent) {
		log("service rebind " + intent);
		super.onRebind(intent);
	}

	@SuppressLint("WakelockTimeout")
	public void startRecording() {
		log("service start recording");
		if(!isRecording) {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "services");
			builder.setContentTitle("Recording Your Sleep");
			builder.setContentText("The app will run in the background");
			builder.setSmallIcon(R.drawable.bed_icon);
			builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0));
			Notification notification = builder.build();
			startForeground(1, notification);

			isRecording = true;
			sleepFile = makeSleepFile();

			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean("isRecording", true);
			editor.apply();

			wakeLock.acquire();
			dataPoints = new DataPoints();
			sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 50000);
		}
	}

	public void stopRecording() {
		log("service stop recording");
		if(isRecording) {
			sensorManager.unregisterListener(this);
			save();
			stopForeground(true);
			wakeLock.release();

			isRecording = false;
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean("isRecording", false);
			editor.apply();
		}
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

	private void writeSleepFile(File sleepFile, DataPoints dataPoints) {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(sleepFile, true);
			DataPointOutputStream dataPointOutputStream = new DataPointOutputStream(fileOutputStream);
			for(DataPoint dataPoint : dataPoints)
				dataPointOutputStream.writeDataPoint(dataPoint);
			dataPointOutputStream.close();
			fileOutputStream.close();
		} catch (Exception e) {
			log("couldn't save to file");
			e.printStackTrace();
		}
	}

	private void save() {
		lastSaveTimestamp = System.currentTimeMillis();
		Thread thread = new Thread(() -> {
			log("saving to file");

			DataPoints dataPointsToWrite = dataPoints;
			dataPoints = new DataPoints();
			writeSleepFile(sleepFile, dataPointsToWrite);
		}, "Save-Thread");
		thread.start();
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

			if(System.currentTimeMillis() - lastSaveTimestamp > SAVE_DELAY)
				save();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onLowMemory() {
		log("low mem");
		super.onLowMemory();
	}

	private void log(String message) {
		if(USE_LOG) {
			String logMessage = simpleDateFormat.format(System.currentTimeMillis()) + ": " + message + "\n";
			System.out.print(logMessage);
			try {
				FileWriter fileWriter = new FileWriter("/sdcard/sleeplog.txt", true);
				fileWriter.write(logMessage);
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
