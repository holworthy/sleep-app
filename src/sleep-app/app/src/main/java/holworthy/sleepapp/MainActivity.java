package holworthy.sleepapp;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
	private SensorManager sensorManager;
	private boolean recordingSleep;
	private Button startButton;
	private Button stopButton;
	private ArrayList<DataPoint> dataPoints;
	private PowerManager powerManager;
	private PowerManager.WakeLock wakeLock;
	private SimpleDateFormat simpleDateFormat;
	private Button analyseButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sleepapp:lock");
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		dataPoints = new ArrayList<>();
		recordingSleep = false;

		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

		startButton = findViewById(R.id.startButton);
		startButton.setOnClickListener(v -> {
			recordingSleep = true;
			sensorManager.registerListener(MainActivity.this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 50000);
			if(!wakeLock.isHeld())
				wakeLock.acquire();

			(new Thread(() -> {
				File sleepFile = makeSleepFile();
				System.out.println("Made a sleep file " + sleepFile);

				while(recordingSleep) {
					try {
						Thread.sleep(60 * 1000);
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
			})).start();
		});

		stopButton = findViewById(R.id.stopButton);
		stopButton.setOnClickListener(v -> {
			recordingSleep = false;
			sensorManager.unregisterListener(MainActivity.this);
			if(wakeLock.isHeld())
				wakeLock.release();
		});

		analyseButton = findViewById(R.id.analyseButton);
		analyseButton.setOnClickListener(v -> {
			Intent intent = new Intent(this, SleepListViewActivity.class);
			startActivity(intent);
		});
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

	public static File[] getSleepFiles() {
		File sdcard = Environment.getExternalStorageDirectory();
		File sleepFolder = new File(sdcard, "/sleepapp");
		if(!sleepFolder.exists())
			return new File[0];
		return sleepFolder.listFiles();
	}

	private ArrayList<DataPoint> readSleepFile(File sleepFile) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(sleepFile);
		DataPointInputStream dataPointInputStream = new DataPointInputStream(fileInputStream);
		ArrayList<DataPoint> dataPoints = new ArrayList<>();
		while(dataPointInputStream.available() > 0)
			dataPoints.add(dataPointInputStream.readDataPoint());
		dataPointInputStream.close();
		fileInputStream.close();
		return dataPoints;
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
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && recordingSleep) {
			float[] values = event.values;
			dataPoints.add(new DataPoint(System.currentTimeMillis(), values[0], values[1], values[2]));
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
