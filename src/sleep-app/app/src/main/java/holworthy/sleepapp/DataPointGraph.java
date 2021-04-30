package holworthy.sleepapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class DataPointGraph extends View {

	private Paint bgPaint;
	private Paint fgPaint1;
	private Paint fgPaint2;
	private ArrayList<DataPoint> data;

	public DataPointGraph(Context context) {
		super(context);
		data = new ArrayList<>();
		bgPaint = new Paint();
		bgPaint.setColor(Color.BLUE);
		fgPaint1 = new Paint();
		fgPaint1.setColor(Color.RED);
		fgPaint1.setStrokeWidth(2);
		fgPaint1.setAntiAlias(true);
		fgPaint2 = new Paint();
		fgPaint2.setColor(Color.YELLOW);
		fgPaint2.setStrokeWidth(2);
		fgPaint2.setAntiAlias(true);
	}

	public DataPointGraph(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		data = new ArrayList<>();
		bgPaint = new Paint();
		bgPaint.setColor(Color.BLUE);
		fgPaint1 = new Paint();
		fgPaint1.setColor(Color.RED);
		fgPaint1.setStrokeWidth(2);
		fgPaint1.setAntiAlias(true);
		fgPaint2 = new Paint();
		fgPaint2.setColor(Color.YELLOW);
		fgPaint2.setStrokeWidth(2);
		fgPaint2.setAntiAlias(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

		if(data.size() > 2) {
			float total = 0;
			for (DataPoint dataPoint : data)
				total += dataPoint.getAcceleration();
			float mean = total / data.size();
			total = 0;
			for (DataPoint dataPoint : data) {
				float diff = dataPoint.getAcceleration() - mean;
				total += diff * diff;
			}
			float variance = total / data.size();
			float standardDeviation = (float) Math.sqrt(variance);

			DataPoint first = data.get(0);
			DataPoint last = data.get(data.size() - 1);

			float max = 0;
			float min = 1000;
			for (DataPoint dataPoint : data) {
				if (dataPoint.getAcceleration() > max)
					max = dataPoint.getAcceleration();
				if (dataPoint.getAcceleration() < min)
					min = dataPoint.getAcceleration();
			}

			for (int i = 0; i < data.size() - 1; i++) {
				DataPoint one = data.get(i);
				DataPoint two = data.get(i + 1);
				canvas.drawLine(
					(float) ((float) (one.getTimestamp() - first.getTimestamp()) / (last.getTimestamp() - first.getTimestamp())) * getWidth(),
					(float) ((float) (one.getAcceleration() - min) / (max - min)) * getHeight(),
					(float) ((float) (two.getTimestamp() - first.getTimestamp()) / (last.getTimestamp() - first.getTimestamp())) * getWidth(),
					(float) ((float) (two.getAcceleration() - min) / (max - min)) * getHeight(),
					(one.getAcceleration() > mean + standardDeviation || one.getAcceleration() < mean - standardDeviation || two.getAcceleration() > mean + standardDeviation || two.getAcceleration() < mean - standardDeviation) ? fgPaint2 : fgPaint1
				);
			}
		}
	}

	public void setData(ArrayList<DataPoint> data) {
		this.data = data;
		invalidate();
	}
}
