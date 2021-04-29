package holworthy.sleepapp;

public class DataPoint {
	private long timestamp;
	private float xAcceleration;
	private float yAcceleration;
	private float zAcceleration;

	public DataPoint(long timestamp, float xAcceleration, float yAcceleration, float zAcceleration) {
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

	public float getXAcceleration() {
		return xAcceleration;
	}

	public float getYAcceleration() {
		return yAcceleration;
	}

	public float getZAcceleration() {
		return zAcceleration;
	}
}
