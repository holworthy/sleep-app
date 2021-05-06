package holworthy.sleepapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class DataPointGraph extends View {

	private Paint bgPaint;
	private Paint fgPaint1;
	private Paint fgPaint2;
	private Paint timePaint;
	private DataPoints data;

	private long startTimestamp = 0;
	private long endTimestamp = 0;
	private float mean = 0;
	private float standardDeviation = 0;
	private float min = 0;
	private float max = 0;
	private ArrayList<Long> sleepPoints = new ArrayList<>();
	private ArrayList<Long> wakePoints = new ArrayList<>();
	private ArrayList<MinuteSum> significant = new ArrayList<>();

	public DataPointGraph(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		data = new DataPoints();
		bgPaint = new Paint();
		bgPaint.setColor(Color.DKGRAY);
		fgPaint1 = new Paint();
		fgPaint1.setColor(Color.RED);
		fgPaint1.setStrokeWidth(2);
		fgPaint1.setAntiAlias(true);
		fgPaint2 = new Paint();
		fgPaint2.setColor(Color.YELLOW);
		fgPaint2.setStrokeWidth(2);
		fgPaint2.setAntiAlias(false);
		timePaint = new Paint();
		timePaint.setColor(Color.GREEN);
		timePaint.setAlpha(127);
	}

	public void setAnalysisFile(File analysisFile) {
		System.out.println(analysisFile);
		Thread thread = new Thread(() -> {
			try {
				RandomAccessFile randomAccessFile = new RandomAccessFile(analysisFile, "r");
				startTimestamp = randomAccessFile.readLong();
				endTimestamp = randomAccessFile.readLong();
				mean = randomAccessFile.readFloat();
				standardDeviation = randomAccessFile.readFloat();
				min = randomAccessFile.readFloat();
				max = randomAccessFile.readFloat();
				int sleepPointCount = randomAccessFile.readInt();
				sleepPoints = new ArrayList<>();
				synchronized (sleepPoints) {
					for (int i = 0; i < sleepPointCount; i++)
						sleepPoints.add(randomAccessFile.readLong());
				}
				int wakePointCount = randomAccessFile.readInt();
				wakePoints = new ArrayList<>();
				synchronized (wakePoints) {
					for (int i = 0; i < wakePointCount; i++)
						wakePoints.add(randomAccessFile.readLong());
				}
				int significantPointCount = randomAccessFile.readInt();
				significant = new ArrayList<>();
				synchronized (significant) {
					for (int i = 0; i < significantPointCount; i++)
						significant.add(new MinuteSum(randomAccessFile.readLong(), randomAccessFile.readFloat()));
				}
				randomAccessFile.close();
				invalidate();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, "Read-Thread");
		thread.start();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

		long duration = endTimestamp - startTimestamp;
		long fifteenMinutes = duration / (1000 * 60 * 15);
		for (int i = 1; i < fifteenMinutes; i++)
			canvas.drawLine((float) i / fifteenMinutes * getWidth(), 0, (float) i / fifteenMinutes * getWidth(), getHeight(), timePaint);

		synchronized (significant) {
			for (MinuteSum minuteSum : significant)
				canvas.drawRect(
					(float) (minuteSum.getTimestamp() - startTimestamp) / duration * getWidth(),
					getHeight() - (minuteSum.getSum() - min) / (max - min) * getHeight(),
					(float) (minuteSum.getTimestamp() + 1000 * 60 - startTimestamp) / duration * getWidth(),
					getHeight(),
					fgPaint2
				);
		}

		synchronized (sleepPoints) {
			for (Long sleepPoint : sleepPoints) {
				float x = (float) (sleepPoint - startTimestamp) / (endTimestamp - startTimestamp) * getWidth();
				canvas.drawLine(x, 0, x, getHeight(), fgPaint1);
			}
		}

		synchronized (wakePoints) {
			for (Long wakePoint : wakePoints) {
				float x = (float) (wakePoint - startTimestamp) / (endTimestamp - startTimestamp) * getWidth();
				canvas.drawLine(x, 0, x, getHeight(), fgPaint1);
			}
		}
	}
}
