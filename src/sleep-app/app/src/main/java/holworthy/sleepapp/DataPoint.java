package holworthy.sleepapp;

public class DataPoint {
	public long timestamp;
	public long xAcceleration;
	public long yAcceleration;
	public long zAcceleration;

	public DataPoint(long timestamp, long xAcceleration, long yAcceleration, long zAcceleration) {
		this.timestamp = timestamp;
		this.xAcceleration = xAcceleration;
		this.yAcceleration = yAcceleration;
		this.zAcceleration = zAcceleration;
	}

	public static DataPoint lerp(DataPoint a, DataPoint b, long timestamp) {
		assert a.timestamp < b.timestamp && a.timestamp <= timestamp && b.timestamp >= timestamp;
		float t = (timestamp - a.timestamp) / (float) (b.timestamp - a.timestamp);
		return new DataPoint(
			timestamp,
			(long) (a.xAcceleration + (b.xAcceleration - a.xAcceleration) * t),
			(long) (a.yAcceleration + (b.yAcceleration - a.yAcceleration) * t),
			(long) (a.zAcceleration + (b.zAcceleration - a.zAcceleration) * t)
		);
	}
}
