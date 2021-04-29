package holworthy.sleepapp;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
	private SensorManager sensorManager;
	private boolean recordingSleep;
	private Button startButton;
	private Button stopButton;
	private ArrayList<DataPoint> dataPoints;
	private PowerManager powerManager;
	private PowerManager.WakeLock wakeLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "sleepapp:lock");
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		dataPoints = new ArrayList<>();
		recordingSleep = false;

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
