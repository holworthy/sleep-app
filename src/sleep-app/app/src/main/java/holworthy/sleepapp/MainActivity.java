package holworthy.sleepapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MainActivity extends AppCompatActivity {

	private Button analyseButton;
	private Button startStopButton;
	private AlertDialog storageAlert;
	private SleepService sleepService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setMessage("Could not create sleep file. Make sure you have given this app storage permissions.");
		builder.setPositiveButton("Ok", (dialogInterface, id) -> storageAlert.cancel());
		builder.setCancelable(false);
		storageAlert = builder.create();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			requestPermissions(new String[]{
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.FOREGROUND_SERVICE
			}, 1);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			requestPermissions(new String[]{
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.READ_EXTERNAL_STORAGE
			}, 0);
		}

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			NotificationChannel notificationChannel = new NotificationChannel("services", "Services", NotificationManager.IMPORTANCE_LOW);
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.createNotificationChannel(notificationChannel);
		}

		analyseButton = findViewById(R.id.analyseButton);
		analyseButton.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, SleepListViewActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		});

		startStopButton = findViewById(R.id.startStopButton);

		Intent sleepServiceIntent = new Intent(this, SleepService.class);
		class MyServiceConnection implements ServiceConnection {
			@Override
			public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
				sleepService = ((SleepService.MyBinder) iBinder).getService();
				updateStartStopButton();
				startStopButton.setEnabled(true);
			}

			@Override
			public void onServiceDisconnected(ComponentName componentName) {
				sleepService = null;
			}
		}
		MyServiceConnection myServiceConnection = new MyServiceConnection();
		startService(sleepServiceIntent);
		bindService(sleepServiceIntent, myServiceConnection, BIND_AUTO_CREATE | BIND_ABOVE_CLIENT);

		startStopButton.setOnClickListener(v -> {
			if(!sleepService.isRecording())
				sleepService.startRecording();
			else
				sleepService.stopRecording();
			updateStartStopButton();
		});
		startStopButton.setEnabled(false);
	}

	public static File[] getSleepFiles() {
		File sdcard = Environment.getExternalStorageDirectory();
		File sleepFolder = new File(sdcard, "/sleepapp");
		if(!sleepFolder.exists())
			return new File[]{};
		return sleepFolder.listFiles();
	}

	public static DataPoints readSleepFile(File sleepFile) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(sleepFile);
		DataPointInputStream dataPointInputStream = new DataPointInputStream(fileInputStream);
		DataPoints dataPoints = new DataPoints();
		while(dataPointInputStream.available() > 0)
			dataPoints.add(dataPointInputStream.readDataPoint());
		dataPointInputStream.close();
		fileInputStream.close();
		return dataPoints;
	}

	public static long getSleepFileLength(File sleepFile) throws IOException {
		RandomAccessFile randomAccessFile = new RandomAccessFile(sleepFile, "r");
		randomAccessFile.seek(0);
		long start = randomAccessFile.readLong();
		randomAccessFile.seek(randomAccessFile.length() - 20);
		long end = randomAccessFile.readLong();
		randomAccessFile.close();
		return end - start;
	}

	private void updateStartStopButton() {
		if(sleepService.isRecording()) {
			startStopButton.setText("Stop");
			startStopButton.getBackground().setTint(Color.parseColor("#CC0000"));
		} else {
			startStopButton.setText("Start");
			startStopButton.getBackground().setTint(Color.parseColor("#00CC00"));
		}
	}
}
