package holworthy.sleepapp;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Utils {
	@SuppressLint("SimpleDateFormat")
	public static final SimpleDateFormat filenameFormatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	public static final String sleepFolderName = "sleepapp";

	public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss");

	public static String getTimeString(long timestamp){
		return dateFormatter.format(timestamp);
	}

	public static String filenameFromTimestamp(long timestamp) {
		return filenameFormatter.format(timestamp);
	}

	public static String timeStringFromDuration(long duration){
		long hours = duration / (1000 * 60 * 60);
		long minutes = (duration / (1000 * 60)) % 60;
		String message = hours == 0 && minutes == 0 ? "0 minutes" : hours == 0 ? Utils.plural(minutes, "minute") : minutes == 0 ? Utils.plural(hours, "hour") : Utils.plural(hours, "hour") + " " + Utils.plural(minutes, "minute");
		return message;
	}

	public static String plural(long quantity, String singular) {
		return quantity + " " + (quantity == 1 ? singular : singular + "s");
	}

	public static File getSleepFolder() {
		return new File(Environment.getExternalStorageDirectory(), "/" + sleepFolderName);
	}

	public static File[] getSleepFiles() {
		File sleepFolder = getSleepFolder();
		if(!sleepFolder.exists())
			return new File[0];
		return sleepFolder.listFiles((dir, filename) -> filename.endsWith(".slp"));
	}

	public static File[] getSleepAnalysisFiles() {
		File sleepFolder = getSleepFolder();
		if(!sleepFolder.exists())
			return new File[0];
		return sleepFolder.listFiles((dir, filename) -> filename.endsWith(".slpa"));
	}

	public static DataPoints readSleepFile(File sleepFile) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(sleepFile);
		DataPointInputStream dataPointInputStream = new DataPointInputStream(fileInputStream);
		DataPoints dataPoints = new DataPoints();
		while(dataPointInputStream.available() > 0)
			dataPoints.add(dataPointInputStream.readDataPoint());
		dataPointInputStream.close();
		fileInputStream.close();
		return dataPoints;
	}

	public static long getSleepFileDuration(File sleepFile) throws IOException {
		RandomAccessFile randomAccessFile = new RandomAccessFile(sleepFile, "r");
		randomAccessFile.seek(0);
		long start = randomAccessFile.readLong();
		randomAccessFile.seek(randomAccessFile.length() - 20);
		long end = randomAccessFile.readLong();
		randomAccessFile.close();
		return end - start;
	}

	public static File makeSleepFile() {
		getSleepFolder().mkdirs();
		File sleepFile = new File(getSleepFolder(), filenameFormatter.format(new Date()) + ".slp");
		try {
			if(!sleepFile.createNewFile())
				return null;
		} catch (IOException e) {
			return null;
		}
		return sleepFile;
	}

	public static File makeSleepAnalysisFile(File sleepFile) {
		File sleepAnalysisFile = new File(getSleepFolder(), sleepFile.getName() + "a");
		try {
			if(!sleepAnalysisFile.exists() && !sleepAnalysisFile.createNewFile())
				return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return sleepAnalysisFile;
	}

	public static File analyseSleepFile(File sleepFile) {
		DataPoints dataPoints = null;
		try {
			dataPoints = Utils.readSleepFile(sleepFile);
		} catch (IOException e) {

		}

		if(dataPoints == null || dataPoints.size() < 2) {
			// TODO: make notification because analysis failed
		} else {
			// space the data points evenly at 50ms apart
			dataPoints = dataPoints.getFixed();

			// gravity filter
			float gx = 0, gy = 0, gz = 0;
			for (DataPoint dataPoint : dataPoints) {
				gx = 0.9f * gx + 0.1f * dataPoint.getXAcceleration();
				gy = 0.9f * gy + 0.1f * dataPoint.getYAcceleration();
				gz = 0.9f * gz + 0.1f * dataPoint.getZAcceleration();
				dataPoint.setXAcceleration(dataPoint.getXAcceleration() - gx);
				dataPoint.setYAcceleration(dataPoint.getYAcceleration() - gy);
				dataPoint.setZAcceleration(dataPoint.getZAcceleration() - gz);
			}

			// mean
			float total = 0;
			for(DataPoint dataPoint : dataPoints)
				total += dataPoint.getAcceleration();
			float mean = total / dataPoints.size();

			// calculate standard deviation
			total = 0;
			for(DataPoint dataPoint : dataPoints) {
				float diff = dataPoint.getAcceleration() - mean;
				total += diff * diff;
			}
			float variance = total / dataPoints.size();
			float standardDeviation = (float) Math.sqrt(variance);

			// calculate fall asleep points
			int lastAwakeMovement = 0;
			ArrayList<Long> fallAsleepPoints = new ArrayList<>();
			for(int i = 0; i < dataPoints.size(); i++) {
				if(dataPoints.get(i).getAcceleration() > mean + 5 * standardDeviation)
					lastAwakeMovement = i;
				if(lastAwakeMovement < i - 20 * 60 * 10) {
					fallAsleepPoints.add(dataPoints.get(i - 20 * 60 * 5).getTimestamp());
					break;
				}
			}

			lastAwakeMovement = dataPoints.size() - 1;
			ArrayList<Long> wakeUpPoints = new ArrayList<>();
			for(int i = dataPoints.size() - 1; i >= 0; i--) {
				if(dataPoints.get(i).getAcceleration() > mean + 5 * standardDeviation)
					lastAwakeMovement = i;
				if(lastAwakeMovement > i + 20 * 60 * 10) {
					wakeUpPoints.add(dataPoints.get(i + 20 * 60 * 5).getTimestamp());
					break;
				}
			}

			// sum movement in each minute
			log("splitting into minutes");
			int minutes = (int) ((dataPoints.get(dataPoints.size() - 1).getTimestamp() - dataPoints.get(0).getTimestamp()) / (1000 * 60));
			float[] minuteSums = new float[minutes];
			for (int minute = 0; minute < minutes; minute++) {
				minuteSums[minute] = 0;
				for (int i = minute * 60 * 20; i < (minute + 1) * 60 * 20; i++) {
					minuteSums[minute] += dataPoints.get(i).getAcceleration();
				}
			}

			// calculate minute mean
			log("mean");
			total = 0;
			for (int minute = 0; minute < minutes; minute++)
				total += minuteSums[minute];
			float minuteMean = total / minutes;

			// calculate minute standard deviation
			log("standard deviation");
			total = 0;
			for (int minute = 0; minute < minutes; minute++) {
				float diff = minuteSums[minute] - mean;
				total += diff * diff;
			}
			float minuteVariance = total / minutes;
			float minuteStandardDeviation = (float) Math.sqrt(variance);

			// calculate min and max minutes
			log("min and max values");
			float min = Float.MAX_VALUE, max = 0;
			for (int minute = 0; minute < minutes; minute++) {
				if (minuteSums[minute] < min)
					min = minuteSums[minute];
				if (minuteSums[minute] > max)
					max = minuteSums[minute];
			}

			// find significant minutes
			log("significant values");
			ArrayList<MinuteSum> significantMinutes = new ArrayList<>();
			for (int minute = 0; minute < minutes; minute++)
				if (minuteSums[minute] > mean + standardDeviation)
					significantMinutes.add(new MinuteSum(dataPoints.get(minute * 60 * 20).getTimestamp(), minuteSums[minute]));

			log("saving analysis file");
			try {
				File sleepAnalysisFile = Utils.makeSleepAnalysisFile(sleepFile);

				// write sleep analysis file
				FileOutputStream fileOutputStream = new FileOutputStream(sleepAnalysisFile);
				DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

				// write start time
				dataOutputStream.writeLong(dataPoints.get(0).getTimestamp());
				// write end time
				dataOutputStream.writeLong(dataPoints.get(dataPoints.size() - 1).getTimestamp());
				// mean
				dataOutputStream.writeFloat(minuteMean);
				// standard deviation
				dataOutputStream.writeFloat(minuteStandardDeviation);
				// min
				dataOutputStream.writeFloat(min);
				// max
				dataOutputStream.writeFloat(max);
				// sleep points
				dataOutputStream.writeInt(fallAsleepPoints.size());
				for(int i = 0; i < fallAsleepPoints.size(); i++)
					dataOutputStream.writeLong(fallAsleepPoints.get(i));
				// wake points
				dataOutputStream.writeInt(wakeUpPoints.size());
				for(int i = 0; i < wakeUpPoints.size(); i++)
					dataOutputStream.writeLong(wakeUpPoints.get(i));
				// significant points
				dataOutputStream.writeInt(significantMinutes.size());
				for (MinuteSum minuteSum : significantMinutes) {
					dataOutputStream.writeLong(minuteSum.getTimestamp());
					dataOutputStream.writeFloat(minuteSum.getSum());
				}

				dataOutputStream.flush();
				dataOutputStream.close();
				fileOutputStream.close();

				return sleepAnalysisFile;
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: notification for error
			}
		}

		return null;
	}

	public static long getSleepAnalysisFileStartTimestamp(File sleepAnalysisFile) throws IOException {
		RandomAccessFile randomAccessFile = new RandomAccessFile(sleepAnalysisFile, "r");
		randomAccessFile.seek(0);
		long start = randomAccessFile.readLong();
		randomAccessFile.close();
		return start;
	}

	public static long getSleepAnalysisFileDuration(File sleepAnalysisFile) throws IOException {
		RandomAccessFile randomAccessFile = new RandomAccessFile(sleepAnalysisFile, "r");
		randomAccessFile.seek(0);
		long start = randomAccessFile.readLong();
		long end = randomAccessFile.readLong();
		randomAccessFile.close();
		return end - start;
	}

	private static void log(String message) {
		if(true) {
			String logMessage = filenameFormatter.format(System.currentTimeMillis()) + ": " + message + "\n";
			System.out.print(logMessage);
			try {
				FileWriter fileWriter = new FileWriter(new File(Environment.getExternalStorageDirectory(), "/sleeplog.txt"), true);
				fileWriter.write(logMessage);
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
