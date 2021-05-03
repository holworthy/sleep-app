package holworthy.sleepapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DataPointGraph extends View {

	private Paint bgPaint;
	private Paint fgPaint1;
	private Paint fgPaint2;
	private float[] data;
//	private DataPoints data;
//
//	public DataPointGraph(Context context) {
//		super(context);
//		data = new float[0];
//		bgPaint = new Paint();
//		bgPaint.setColor(Color.BLUE);
//		fgPaint1 = new Paint();
//		fgPaint1.setColor(Color.RED);
//		fgPaint1.setStrokeWidth(2);
//		fgPaint1.setAntiAlias(true);
//		fgPaint2 = new Paint();
//		fgPaint2.setColor(Color.YELLOW);
//		fgPaint2.setStrokeWidth(2);
//		fgPaint2.setAntiAlias(true);
//	}

	public DataPointGraph(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
//		data = new DataPoints();
		data = new float[0];
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

		if(data.length > 2) {
//			float mean = data.getAccelerationMean();
//			float standardDeviation = data.getAccelerationStandardDeviation();
//
//			DataPoint first = data.get(0);
//			DataPoint last = data.get(data.size() - 1);
//
//			float max = data.getHighestAccelerationPoint().getAcceleration();
//			float min = data.getLowestAccelerationPoint().getAcceleration();
//
//			for (int i = 0; i < data.size() - 1; i++) {
//				DataPoint one = data.get(i);
//				DataPoint two = data.get(i + 1);
//				canvas.drawLine(
//					((float) (one.getTimestamp() - first.getTimestamp()) / (last.getTimestamp() - first.getTimestamp())) * getWidth(),
//					((one.getAcceleration() - min) / (max - min)) * getHeight(),
//					((float) (two.getTimestamp() - first.getTimestamp()) / (last.getTimestamp() - first.getTimestamp())) * getWidth(),
//					((two.getAcceleration() - min) / (max - min)) * getHeight(),
//					(one.getAcceleration() > mean + standardDeviation || one.getAcceleration() < mean - standardDeviation || two.getAcceleration() > mean + standardDeviation || two.getAcceleration() < mean - standardDeviation) ? fgPaint2 : fgPaint1
//				);
//			}

			float min = Float.MAX_VALUE;
			float max = 0;

			for(int i = 0; i < data.length; i++) {
				if(data[i] < min)
					min = data[i];
				if(data[i] > max)
					max = data[i];
			}

			for(int i = 0; i < data.length - 1; i++) {
				canvas.drawLine(
					(float) i / data.length * getWidth(),
					getHeight() - ((data[i] - min) / (max - min) * getHeight()),
					(float) (i + 1) / data.length * getWidth(),
					getHeight() - ((data[i + 1] - min) / (max - min) * getHeight()),
					fgPaint1
				);
			}
		}
	}

	public void setData(float[] data) {
//	public void setData(DataPoints data) {
		this.data = data;
		invalidate();
	}
}
