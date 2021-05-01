package holworthy.sleepapp;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;

public class DataPoints implements Iterable<DataPoint> {
	private ArrayList<DataPoint> arrayList;
	private boolean hasChanged = true;
	private DataPoint lowestAccelerationPoint = null;
	private DataPoint highestAccelerationPoint = null;
	private float accelerationMean = 0;
	private float accelerationVariance = 0;
	private float accelerationStandardDeviation = 0;

	public DataPoints() {
		arrayList = new ArrayList<>();
	}

	public void add(DataPoint dataPoint) {
		arrayList.add(dataPoint);
		hasChanged = true;
	}

	public void remove(DataPoint dataPoint) {
		arrayList.remove(dataPoint);
		hasChanged = true;
	}

	public DataPoint get(int index) {
		return arrayList.get(index);
	}

	public int size() {
		return arrayList.size();
	}

	private void recalculateIfNecessary() {
		if(hasChanged)
			recalculate();
	}

	private void recalculate() {
		if(arrayList.size() > 0) {
			lowestAccelerationPoint = highestAccelerationPoint = arrayList.get(0);
			for(DataPoint potentialPoint : arrayList) {
				if(potentialPoint.getAcceleration() < lowestAccelerationPoint.getAcceleration())
					lowestAccelerationPoint = potentialPoint;
				if(potentialPoint.getAcceleration() > highestAccelerationPoint.getAcceleration())
					highestAccelerationPoint = potentialPoint;
			}

			float total = 0;
			for (DataPoint dataPoint : arrayList)
				total += dataPoint.getAcceleration();
			accelerationMean = total / arrayList.size();
			total = 0;
			for (DataPoint dataPoint : arrayList) {
				float diff = dataPoint.getAcceleration() - accelerationMean;
				total += diff * diff;
			}
			accelerationVariance = total / arrayList.size();
			accelerationStandardDeviation = (float) Math.sqrt(accelerationVariance);
		} else {
			lowestAccelerationPoint = highestAccelerationPoint = null;
			accelerationMean = 0;
			accelerationVariance = 0;
			accelerationStandardDeviation = 0;
		}
	}

	public DataPoint getLowestAccelerationPoint() {
		recalculateIfNecessary();
		return lowestAccelerationPoint;
	}

	public DataPoint getHighestAccelerationPoint() {
		recalculateIfNecessary();
		return highestAccelerationPoint;
	}

	public float getAccelerationMean() {
		recalculateIfNecessary();
		return accelerationMean;
	}

	public float getAccelerationVariance() {
		recalculateIfNecessary();
		return accelerationVariance;
	}

	public float getAccelerationStandardDeviation() {
		recalculateIfNecessary();
		return accelerationStandardDeviation;
	}

	public DataPoints getFixed() {
		DataPoints fixed = new DataPoints();
		DataPoint last = get(size() - 1);
		int current = 0;
		for(long timestamp = get(0).getTimestamp(); timestamp <= last.getTimestamp(); timestamp += 50) {
			while(get(current + 1).getTimestamp() < timestamp)
				current++;
			fixed.add(DataPoint.lerp(get(current), get(current + 1), timestamp));
		}
		return fixed;
	}

	@NonNull
	@Override
	public Iterator<DataPoint> iterator() {
		return new Iterator<DataPoint>() {
			private int cursor = 0;

			@Override
			public boolean hasNext() {
				return cursor < arrayList.size();
			}

			@Override
			public DataPoint next() {
				return get(cursor++);
			}
		};
	}
}
