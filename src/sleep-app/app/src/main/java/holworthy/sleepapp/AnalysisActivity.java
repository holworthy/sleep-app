package holworthy.sleepapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
		actionBar.setDisplayHomeAsUpEnabled(true);

		File sleepFile = (File) getIntent().getSerializableExtra("file");
		if(sleepFile == null) {
			finish();
			return;
		}

		DataPointGraph graph = findViewById(R.id.graph);

		Thread thread = new Thread(() -> {
			DataPoints dataPoints;
			try {
				dataPoints = MainActivity.readSleepFile(sleepFile);
			} catch (IOException e) {
				// TODO: handle better
				System.out.println("errors");
				finish();
				return;
			}

			if(dataPoints.size() < 2) {
				System.out.println("errors");
				finish();
				return; // TODO: error
			}

			DataPoints fixedDataPoints = dataPoints.getFixed();
			float mean = fixedDataPoints.getAccelerationMean();
			float standardDeviation = fixedDataPoints.getAccelerationStandardDeviation();

			DataPoints insignificant = new DataPoints();
			for(DataPoint dataPoint : fixedDataPoints)
				if(dataPoint.getAcceleration() > mean - standardDeviation && dataPoint.getAcceleration() < mean + standardDeviation)
					insignificant.add(dataPoint);
			float gravityMean = insignificant.getAccelerationMean();

			System.out.println(mean + ", " + gravityMean);

			float[] gravityFix = new float[fixedDataPoints.size()];
			for(int i = 0; i < fixedDataPoints.size(); i++)
				gravityFix[i] = fixedDataPoints.get(i).getAcceleration() < mean ? 3 * gravityMean - 2 * fixedDataPoints.get(i).getAcceleration() : fixedDataPoints.get(i).getAcceleration();

			float[] minutes = new float[gravityFix.length / (20 * 60)];
			for(int chunk = 0; chunk < gravityFix.length / (20 * 60); chunk++) {
				minutes[chunk] = 0;
				for(int i = chunk * 60 * 20; i < (chunk + 1) * 20 * 60; i++)
					minutes[chunk] += gravityFix[i];
			}

			System.out.println(Arrays.toString(minutes));

			graph.post(() -> graph.setData(minutes));
		});
		thread.start();

		topLeftText = findViewById(R.id.TopLeftText);
		topLeftText.setText("Fall asleep hour");

		topRightText = findViewById(R.id.TopRightText);
		topRightText.setText("Wake Up Hour");

		bottomLeftText = findViewById(R.id.BottomLeftText);
		bottomLeftText.setText("Total sleep time");

		bottomRightText = findViewById(R.id.BottomRightText);
		bottomRightText.setText("Another");

//		DataPointGraph dataPointGraph = new DataPointGraph(this, dataPoints);
//		setContentView(dataPointGraph);

//		int seconds = dataPoints.size() / 20;
//		for(int chunk = 0; chunk < seconds; chunk++) {
//			float sum = 0;
//			for(int i = chunk * 20; i < (chunk + 1) * 20; i++)
//				sum += dataPoints.get(i).getAcceleration();
//		}
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