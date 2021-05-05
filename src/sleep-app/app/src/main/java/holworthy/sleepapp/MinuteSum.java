package holworthy.sleepapp;

public class MinuteSum {
	private long timestamp;
	private float sum;

	public MinuteSum(long timestamp, float sum) {
		this.timestamp = timestamp;
		this.sum = sum;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public float getSum() {
		return sum;
	}
}
