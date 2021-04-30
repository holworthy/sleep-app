package holworthy.sleepapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

	private Button startButton;
	private Button stopButton;
	private Button analyseButton;
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

		analyseButton = findViewById(R.id.analyseButton);
		analyseButton.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, SleepListViewActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		});

		startButton = findViewById(R.id.startButton);
		stopButton = findViewById(R.id.stopButton);


		Intent sleepServiceIntent = new Intent(this, SleepService.class);
		class MyServiceConnection implements ServiceConnection {
			@Override
			public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
				sleepService = ((SleepService.MyBinder) iBinder).getService();

				if(sleepService.isRunning()) {
					stopButton.setEnabled(true);
					startButton.setEnabled(false);
				} else {
					startButton.setEnabled(true);
					stopButton.setEnabled(false);
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName componentName) {

			}
		}
		MyServiceConnection myServiceConnection = new MyServiceConnection();
		startService(sleepServiceIntent);
		bindService(sleepServiceIntent, myServiceConnection, BIND_AUTO_CREATE);

		startButton.setOnClickListener(v -> {
			startButton.setEnabled(false);
			stopButton.setEnabled(true);
			sleepService.start();
		});
		startButton.setEnabled(false);

		stopButton.setOnClickListener(v -> {
			stopButton.setEnabled(false);
			startButton.setEnabled(true);
			sleepService.stop();
		});
		stopButton.setEnabled(false);
	}

	public static File[] getSleepFiles() {
		File sdcard = Environment.getExternalStorageDirectory();
		File sleepFolder = new File(sdcard, "/sleepapp");
		if(!sleepFolder.exists())
			return new File[]{};
		return sleepFolder.listFiles();
	}

	public static ArrayList<DataPoint> readSleepFile(File sleepFile) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(sleepFile);
		DataPointInputStream dataPointInputStream = new DataPointInputStream(fileInputStream);
		ArrayList<DataPoint> dataPoints = new ArrayList<>();
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
}
