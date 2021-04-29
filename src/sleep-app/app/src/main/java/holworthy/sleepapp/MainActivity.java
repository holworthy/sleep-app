package holworthy.sleepapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
		});

		stopButton = findViewById(R.id.stopButton);
		stopButton.setOnClickListener(v -> {
			recordingSleep = false;
			sensorManager.unregisterListener(MainActivity.this);
			if(wakeLock.isHeld())
				wakeLock.release();
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

	private File[] getSleepFiles() {
		File sdcard = Environment.getExternalStorageDirectory();
		File sleepFolder = new File(sdcard, "/sleepapp");
		if(!sleepFolder.exists())
			return new File[0];
		return sleepFolder.listFiles();
	}

	private DataPoint[] readSleepFile(File sleepFile) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(sleepFile);
		DataPointInputStream dataPointInputStream = new DataPointInputStream(fileInputStream);
		ArrayList<DataPoint> dataPoints = new ArrayList<>();
		while(dataPointInputStream.available() > 0)
			dataPoints.add(dataPointInputStream.readDataPoint());
		dataPointInputStream.close();
		fileInputStream.close();
		return (DataPoint[]) dataPoints.toArray();
	}

	private void writeSleepFile(File sleepFile, DataPoint[] dataPoints) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(sleepFile);
		DataPointOutputStream dataPointOutputStream = new DataPointOutputStream(fileOutputStream);
		for(DataPoint dataPoint : dataPoints)
			dataPointOutputStream.writeDataPoint(dataPoint);
		dataPointOutputStream.close();
		fileOutputStream.close();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			if (recordingSleep) {
				float[] values = event.values;
				dataPoints.add(new DataPoint(System.currentTimeMillis(), values[0], values[1], values[2]));
				System.out.println(dataPoints);
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
