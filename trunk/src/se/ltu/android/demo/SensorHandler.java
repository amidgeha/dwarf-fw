/* SVN FILE: $Id$ */
package se.ltu.android.demo;

import se.ltu.android.demo.filter.MPMovingAverageFilter;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * A class that will handle sensor data in a separate thread.
 * All kinds of signal manipulation should be done here.
 * 
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class SensorHandler extends Thread {
	private static final float ACC_BUFFER_OFFSET = 1.0f;
	private static final float MAG_BUFFER_OFFSET = 5.0f;
	
	private final String TAG = "SensorHandler";
	private float[] acc_raw = {0.0f, 0.0f, 0.0f};
	private float[] mag_raw = {0.0f, 0.0f, 0.0f};
	private float[] ori_raw = {0.0f, 0.0f, 0.0f};
	private float[] accBuffer = {0.0f, 0.0f, 0.0f};
	private float[] magBuffer = {0.0f, 0.0f, 0.0f};
	private Object oriLock = new Object();
	private Object accLock = new Object();
	private Object magLock = new Object();
	private MPMovingAverageFilter accFilter;
	private MPMovingAverageFilter magFilter;

	public Handler mHandler;
	
	public SensorHandler() {
		accFilter = new MPMovingAverageFilter(25, 3, 3);
		accFilter.initialize();
		magFilter = new MPMovingAverageFilter(25, 3, 3);
		magFilter.initialize();
	}

	public void run() {
		Looper.prepare();

		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				//Log.d(TAG, "Got message: "+msg.what);
			}
		};

		Looper.loop();
	}
	
	/**
	 * @param timestamp time in nanoseconds for event
	 * @param data array of values with length 3
	 */
	public void handleAccData(long timestamp, float[] data) {
		synchronized(accLock) {
			acc_raw[0] = data[0];
			acc_raw[1] = data[1];
			acc_raw[2] = data[2];
			
			if(
					acc_raw[0] < accBuffer[0] - ACC_BUFFER_OFFSET ||
					acc_raw[0] > accBuffer[0] + ACC_BUFFER_OFFSET ||
					acc_raw[1] < accBuffer[1] - ACC_BUFFER_OFFSET ||
					acc_raw[1] > accBuffer[1] + ACC_BUFFER_OFFSET ||
					acc_raw[2] < accBuffer[2] - ACC_BUFFER_OFFSET ||
					acc_raw[2] > accBuffer[2] + ACC_BUFFER_OFFSET
					)
			{
				accBuffer[0] = acc_raw[0];
				accBuffer[1] = acc_raw[1];
				accBuffer[2] = acc_raw[2];
			}
			
			accFilter.addSamples(accBuffer, timestamp);
		}
	}
	
	/**
	 * @param timestamp time in nanoseconds for event
	 * @param data array of values with length 3
	 */
	public void handleMagData(long timestamp, float[] data) {
		synchronized(magLock) {
			mag_raw[0] = data[0];
			mag_raw[1] = data[1];
			mag_raw[2] = data[2];

			if (  	
					mag_raw[0] < magBuffer[0] - MAG_BUFFER_OFFSET ||
					mag_raw[0] > magBuffer[0] + MAG_BUFFER_OFFSET ||
					mag_raw[1] < magBuffer[1] - MAG_BUFFER_OFFSET ||
					mag_raw[1] > magBuffer[1] + MAG_BUFFER_OFFSET ||
					mag_raw[2] < magBuffer[2] - MAG_BUFFER_OFFSET ||
					mag_raw[2] > magBuffer[2] + MAG_BUFFER_OFFSET
					)
			{
				magBuffer[0] = mag_raw[0];
				magBuffer[1] = mag_raw[1];
				magBuffer[2] = mag_raw[2];
			}
			
			magFilter.addSamples(magBuffer, timestamp);
		}
	}
	
	/**
	 * @param timestamp time in nanoseconds for event
	 * @param data array of values with length 3
	 */
	public void handleOriData(long timestamp, float[] data) {
		synchronized(oriLock) {
			ori_raw[0] = data[0];
			ori_raw[1] = data[1];
			ori_raw[2] = data[2];
		}
	}
	
	/**
	 * Get the orientation
	 * @param result will contain the orientation after the call
	 */
	public void getOri(float[] result) {
		synchronized(oriLock) {
			result[0] = ori_raw[0];
			result[1] = ori_raw[1];
			result[2] = ori_raw[2];
		}
	}
	
	/**
	 * Get the 4x4 rotation matrix
	 * @param result the matrix as float array with length 16
	 * @return true if the result was calculated and the result array was set
	 */
	public boolean getRotM4(float[] result) {
		synchronized(magLock) {
			synchronized(accLock) {
				float[] acc_res = new float[3];
				float[] mag_res = new float[3];
				accFilter.getResults(acc_res);
				magFilter.getResults(mag_res);
					return SensorManager.getRotationMatrix(result, null, acc_res, mag_res);
			}
		}
	}
}
