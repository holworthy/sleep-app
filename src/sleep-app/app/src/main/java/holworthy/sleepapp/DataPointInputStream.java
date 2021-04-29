package holworthy.sleepapp;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DataPointInputStream extends DataInputStream {
	public DataPointInputStream(InputStream in) {
		super(in);
	}

	public DataPoint readDataPoint() throws IOException {
		return new DataPoint(
			readLong(),
			readFloat(),
			readFloat(),
			readFloat()
		);
	}
}
