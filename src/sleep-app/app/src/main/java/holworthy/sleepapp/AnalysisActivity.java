package holworthy.sleepapp;

import android.app.NotificationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class AnalysisActivity extends AppCompatActivity {
	private TextView topLeftText;
	private TextView topRightText;
	private TextView bottomLeftText;
	private TextView bottomRightText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analysis);

		ActionBar actionBar = getSupportActionBar();
		if(actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		File sleepAnalysisFile = (File) getIntent().getSerializableExtra("file");
		if(sleepAnalysisFile == null) {
			finish();
			return;
		}

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(3);

		DataPointGraph graph = findViewById(R.id.graph);
		graph.setAnalysisFile(sleepAnalysisFile);

		topLeftText = findViewById(R.id.TopLeftText);
		topLeftText.setText("Fall asleep hour");

		topRightText = findViewById(R.id.TopRightText);
		topRightText.setText("Wake Up Hour");

		Thread thread = new Thread(() -> {
			try {
				RandomAccessFile randomAccessFile = new RandomAccessFile(sleepAnalysisFile, "r");
				randomAccessFile.seek(32);
				int sleepPointCount = randomAccessFile.readInt();
				ArrayList<Long> sleepPoints = new ArrayList<>();
				synchronized (sleepPoints) {
					for (int i = 0; i < sleepPointCount; i++)
						sleepPoints.add(randomAccessFile.readLong());
				}
				int wakePointCount = randomAccessFile.readInt();
				ArrayList<Long> wakePoints = new ArrayList<>();
				synchronized (wakePoints) {
					for (int i = 0; i < wakePointCount; i++)
						wakePoints.add(randomAccessFile.readLong());
				}
				randomAccessFile.close();

				final String fallAsleepMessage = "Fall asleep hour:\n" + Utils.getTimeString(sleepPoints.get(0));
				topLeftText.post( () -> topLeftText.setText(fallAsleepMessage));

				final String wakeUpMessage = "Wake up hour:\n" + Utils.getTimeString(wakePoints.get(wakePoints.size() - 1));
				topRightText.post( () -> topRightText.setText(wakeUpMessage));

				final String totalSleepMessage = "Total sleep time:\n"+ Utils.timeStringFromDuration(wakePoints.get(wakePoints.size() - 1) - sleepPoints.get(0));
				bottomLeftText.post( () -> bottomLeftText.setText(totalSleepMessage));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();

			}
		}, "Read-Thread");
		thread.start();

		bottomLeftText = findViewById(R.id.BottomLeftText);
		bottomLeftText.setText("Total sleep time");

		bottomRightText = findViewById(R.id.BottomRightText);
		bottomRightText.setText("Time in bed");
		Thread thread2 = new Thread(() -> {
			long duration;
			try {
				duration = Utils.getSleepAnalysisFileDuration(sleepAnalysisFile);
			} catch (Exception e) {
				duration = 0;
			}
			long hours = duration / (1000 * 60 * 60);
			long minutes = (duration / (1000 * 60)) % 60;
			StringBuilder message = new StringBuilder();
			message.append("Time in bed:\n");
			String time = hours == 0 && minutes == 0 ? "0 minutes" : hours == 0 ? Utils.plural(minutes, "minute") : minutes == 0 ? Utils.plural(hours, "hour") : Utils.plural(hours, "hour") + " " + Utils.plural(minutes, "minute");
			message.append(time);
			bottomRightText.post(() -> bottomRightText.setText(message.toString()));
		});
	thread2.start();

//		int seconds = dataPoints.size() / 20;
//		for(int chunk = 0; chunk < seconds; chunk++) {
//			float sum = 0;
//			for(int i = chunk * 20; i < (chunk + 1) * 20; i++)
//				sum += dataPoints.get(i).getAcceleration();
//		}


//			for (int i = 0; i < gravityFix.length - 1; i++) {
//				canvas.drawLine(
//					(float) i / gravityFix.length * getWidth(),
//					getHeight() - ((gravityFix[i] - min) / (max - min) * getHeight()),
//					(float) (i + 1) / gravityFix.length * getWidth(),
//					getHeight() - ((gravityFix[i + 1] - min) / (max - min) * getHeight()),
//					fgPaint1
//				);
//			}

		// draw a line every 15 minutes
//			long duration = fixedDataPoints.get(fixedDataPoints.size() - 1).getTimestamp() - fixedDataPoints.get(0).getTimestamp();
//			long fifteenMinutes = duration / (1000 * 60 * 15);
//			for (int i = 1; i < fifteenMinutes; i++) {
//				canvas.drawLine((float) i / fifteenMinutes * getWidth(), 0, (float) i / fifteenMinutes * getWidth(), getHeight(), timePaint);
//			}

//			float significantLine = getHeight() - ((mean + 2 * standardDeviation) / (max - min)) * getHeight();
//			canvas.drawLine(0, significantLine, getWidth(), significantLine, timePaint);

	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		finish();
		return true;
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
}