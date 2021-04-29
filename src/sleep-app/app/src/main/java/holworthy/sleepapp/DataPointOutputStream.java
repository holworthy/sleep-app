package holworthy.sleepapp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DataPointOutputStream extends DataOutputStream {
	public DataPointOutputStream(OutputStream out) {
		super(out);
	}

	public void writeDataPoint(DataPoint dataPoint) throws IOException {
		writeLong(dataPoint.getTimestamp());
		writeFloat(dataPoint.getXAcceleration());
		writeFloat(dataPoint.getYAcceleration());
		writeFloat(dataPoint.getZAcceleration());
	}
}
