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
	private int sleepPointCount = 0;
	private float min = 0;
	private float max = 0;
	private ArrayList<Long> sleepPoints = new ArrayList<>();
	private int wakePointCount = 0;
	private ArrayList<Long> wakePoints = new ArrayList<>();
	private int significantPointCount = 0;
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
		fgPaint2.setAntiAlias(true);
		timePaint = new Paint();
		timePaint.setColor(Color.GREEN);
		timePaint.setAlpha(255);
	}

	public void setAnalysisFile(File analysisFile) {
		System.out.println(analysisFile);
		Thread thread = new Thread(() -> {
			try {
				// TODO: try with fileinputstream and datainputstream
				RandomAccessFile randomAccessFile = new RandomAccessFile(analysisFile, "r");
				startTimestamp = randomAccessFile.readLong();
				System.out.println("start:" + startTimestamp);
				endTimestamp = randomAccessFile.readLong();
				System.out.println("end:" + endTimestamp);
				mean = randomAccessFile.readFloat();
				System.out.println("mean:" + mean);
				standardDeviation = randomAccessFile.readFloat();
				System.out.println("stddev:" + standardDeviation);
				min = randomAccessFile.readFloat();
				System.out.println("min:" + min);
				max = randomAccessFile.readFloat();
				System.out.println("max:" + max);
				sleepPointCount = randomAccessFile.readInt();
				System.out.println("sleep:" + sleepPointCount);
				ArrayList<Long> sleepPoints = new ArrayList<>();
				wakePointCount = randomAccessFile.readInt();
				System.out.println("wake:" + wakePointCount);
				ArrayList<Long> wakePoints = new ArrayList<>();
				significantPointCount = randomAccessFile.readInt();
				System.out.println("sig:" + significantPointCount);
				significant = new ArrayList<>();
				for(int i = 0; i < significantPointCount; i++)
					significant.add(new MinuteSum(randomAccessFile.readLong(), randomAccessFile.readFloat()));
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

		System.out.println(significant.size());
		if(significant.size() > 0)
			for(MinuteSum minuteSum : significant)
				canvas.drawRect(
					(float) (minuteSum.getTimestamp() - startTimestamp) / duration * getWidth(),
					getHeight() - (minuteSum.getSum() - min) / (max - min) * getHeight(),
					(float) (minuteSum.getTimestamp() + 1000 * 60 - startTimestamp) / duration * getWidth(),
					getHeight(),
					fgPaint2
				);
	}
}
