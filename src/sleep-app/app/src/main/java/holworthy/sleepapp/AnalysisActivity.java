package holworthy.sleepapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
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
		actionBar.setDisplayHomeAsUpEnabled(true);

		File sleepFile = (File) getIntent().getSerializableExtra("file");
		System.out.println(sleepFile);
		if(sleepFile == null) {
			finish();
			return;
		}

		ArrayList<DataPoint> dataPoints;
		try {
			dataPoints = MainActivity.readSleepFile(sleepFile);
		} catch (IOException e) {
			e.printStackTrace();
			// TODO: error and then exit
			return;
		}

		if(dataPoints.size() == 0)
			return; // TODO: error

		dataPoints = fixData(dataPoints);

		LinearLayout graphWrapper = findViewById(R.id.GraphLayoutWrapper);
		DataPointGraph graph = new DataPointGraph(this, dataPoints);
		graph.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 800, 1f));
		graphWrapper.addView(graph);


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

	private ArrayList<DataPoint> fixData(ArrayList<DataPoint> brokenDataPoints) {
		ArrayList<DataPoint> fixedDataPoints = new ArrayList<>();
		DataPoint last = brokenDataPoints.get(brokenDataPoints.size() - 1);
		int current = 0;
		for(long timestamp = brokenDataPoints.get(0).getTimestamp(); timestamp <= last.getTimestamp(); timestamp += 50) {
			while(brokenDataPoints.get(current + 1).getTimestamp() < timestamp)
				current++;
			fixedDataPoints.add(DataPoint.lerp(brokenDataPoints.get(current), brokenDataPoints.get(current + 1), timestamp));
		}
		return fixedDataPoints;
	}
}