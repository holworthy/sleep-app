package holworthy.sleepapp;

public class DataPoint {
	private long timestamp;
	private long xAcceleration;
	private long yAcceleration;
	private long zAcceleration;

	public DataPoint(long timestamp, long xAcceleration, long yAcceleration, long zAcceleration) {
		this.timestamp = timestamp;
		this.xAcceleration = xAcceleration;
		this.yAcceleration = yAcceleration;
		this.zAcceleration = zAcceleration;
	}

	public static DataPoint lerp(DataPoint a, DataPoint b, long timestamp) {
		if(a.timestamp > timestamp || b.timestamp < timestamp)
			return null;
		float t = (timestamp - a.timestamp) / (float) (b.timestamp - a.timestamp);
		return new DataPoint(
			timestamp,
			(long) (a.xAcceleration + (b.xAcceleration - a.xAcceleration) * t),
			(long) (a.yAcceleration + (b.yAcceleration - a.yAcceleration) * t),
			(long) (a.zAcceleration + (b.zAcceleration - a.zAcceleration) * t)
		);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public long getXAcceleration() {
		return xAcceleration;
	}

	public long getYAcceleration() {
		return yAcceleration;
	}

	public long getZAcceleration() {
		return zAcceleration;
	}
}
