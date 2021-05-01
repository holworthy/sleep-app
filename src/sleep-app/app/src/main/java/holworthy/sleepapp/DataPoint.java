package holworthy.sleepapp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DataPoint {
	private long timestamp;
	private float xAcceleration;
	private float yAcceleration;
	private float zAcceleration;
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public DataPoint(long timestamp, float xAcceleration, float yAcceleration, float zAcceleration) {
		this.timestamp = timestamp;
		this.xAcceleration = xAcceleration;
		this.yAcceleration = yAcceleration;
		this.zAcceleration = zAcceleration;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public float getXAcceleration() {
		return xAcceleration;
	}

	public float getYAcceleration() {
		return yAcceleration;
	}

	public float getZAcceleration() {
		return zAcceleration;
	}

	public float getAcceleration() {
		return (float) Math.sqrt(getXAcceleration() * getXAcceleration() + getYAcceleration() * getYAcceleration() + getZAcceleration() * getZAcceleration());
	}

	@Override
	public String toString() {
		return "DataPoint{" +
			"timestamp=" + simpleDateFormat.format(new Date(timestamp)) +
			", xAcceleration=" + xAcceleration +
			", yAcceleration=" + yAcceleration +
			", zAcceleration=" + zAcceleration +
			'}';
	}

	public static DataPoint lerp(DataPoint a, DataPoint b, long timestamp) {
		if(a.timestamp > timestamp || b.timestamp < timestamp)
			return null;
		float t = (timestamp - a.timestamp) / (float) (b.timestamp - a.timestamp);
		return new DataPoint(
			timestamp,
			a.xAcceleration + (b.xAcceleration - a.xAcceleration) * t,
			a.yAcceleration + (b.yAcceleration - a.yAcceleration) * t,
			a.zAcceleration + (b.zAcceleration - a.zAcceleration) * t
		);
	}
}
