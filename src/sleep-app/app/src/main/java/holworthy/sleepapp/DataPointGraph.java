package holworthy.sleepapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.io.File;
import java.io.IOException;

public class DataPointGraph extends View {

	private Paint bgPaint;
	private Paint fgPaint1;
	private Paint fgPaint2;
	private Paint timePaint;
	private DataPoints data;

	public DataPointGraph(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		data = new DataPoints();
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
		timePaint = new Paint();
		timePaint.setColor(Color.GREEN);
		timePaint.setAlpha(255);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

		if(data.size() > 2) {
			// space the points equally
			DataPoints fixedDataPoints = data.getFixed();

			// gravity filter
			float gx = 0, gy = 0, gz = 0;
			for (DataPoint dataPoint : fixedDataPoints) {
				gx = 0.9f * gx + 0.1f * dataPoint.getXAcceleration();
				gy = 0.9f * gy + 0.1f * dataPoint.getYAcceleration();
				gz = 0.9f * gz + 0.1f * dataPoint.getZAcceleration();
				dataPoint.setXAcceleration(dataPoint.getXAcceleration() - gx);
				dataPoint.setYAcceleration(dataPoint.getYAcceleration() - gy);
				dataPoint.setZAcceleration(dataPoint.getZAcceleration() - gz);
			}

			float mean = fixedDataPoints.getAccelerationMean();
			float standardDeviation = fixedDataPoints.getAccelerationStandardDeviation();

			System.out.println("mean: " + mean);
			System.out.println("stdd: " + standardDeviation);

			float[] gravityFix = new float[fixedDataPoints.size()];
			for (int i = 0; i < fixedDataPoints.size(); i++)
				gravityFix[i] = fixedDataPoints.get(i).getAcceleration();// < mean ? 3 * mean - 2 * fixedDataPoints.get(i).getAcceleration() : fixedDataPoints.get(i).getAcceleration();

			float min = Float.MAX_VALUE;
			float max = 0;

			for (int i = 0; i < gravityFix.length; i++) {
				if (gravityFix[i] < min)
					min = gravityFix[i];
				if (gravityFix[i] > max)
					max = gravityFix[i];
			}

			System.out.println("min and max: " + min + ", " + max);

			for (int i = 0; i < gravityFix.length - 1; i++) {
				canvas.drawLine(
					(float) i / gravityFix.length * getWidth(),
					getHeight() - ((gravityFix[i] - min) / (max - min) * getHeight()),
					(float) (i + 1) / gravityFix.length * getWidth(),
					getHeight() - ((gravityFix[i + 1] - min) / (max - min) * getHeight()),
					fgPaint1
				);
			}

			// draw a line every 15 minutes
			long duration = fixedDataPoints.get(fixedDataPoints.size() - 1).getTimestamp() - fixedDataPoints.get(0).getTimestamp();
			long fifteenMinutes = duration / (1000 * 60 * 15);
			for (int i = 1; i < fifteenMinutes; i++) {
				canvas.drawLine((float) i / fifteenMinutes * getWidth(), 0, (float) i / fifteenMinutes * getWidth(), getHeight(), timePaint);
			}

			float significantLine = getHeight() - ((mean + 2 * standardDeviation) / (max - min)) * getHeight();
			canvas.drawLine(0, significantLine, getWidth(), significantLine, timePaint);

		}
	}

	public void setData(File file) {
		Thread thread = new Thread(() -> {
			try {
				DataPoints dataPoints = MainActivity.readSleepFile(file);
				setData(dataPoints);
			} catch (IOException e) {
				e.printStackTrace();
			}

		});
		thread.start();
	}

	public void setData(DataPoints data) {
		this.data = data;
		invalidate();
	}
}
