/* SVN FILE: $Id$ */
package se.ltu.android.demo.filter;

/**
 * A Multiple-Pass (and somewhat weighted) Moving Average Filter.
 * 
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class MPMovingAverageFilter {
	/**
	 * Average time (in gigahertz) between two samples. Change this if you
	 * don't use the fastest rate.
	 * 
	 * On the Android Dev Phone 1:
	 * SENSOR_DELAY_FASTEST ~= 50Hz = 0.02s
	 * SENSOR_DELAY_GAME ~= unknown
	 * TODO get all sensor delays for G1
	 * @see android.hardware.SensorManager#SENSOR_DELAY_FASTEST
	 */
	public static float Frequency = 0.00000005f;
	private int nPasses = 1;
	private int nSamples = 1;
	private int nElements = 1;
	private boolean passthrough = true;
	private float[][] lastResult;
	private int steadyPass = 0;
	private float[][][] sampleHistory;
	private int iSamp = 0;
	private int iSampNext = 0;
	private int iLastPassResult = 0;
	private long lastTime = 0;
	private double divValue = 1;

	/**
	 * Creates a new filter with a set amount of samples and passes.
	 * The number of samples must be greater than 1 and the number of
	 * passes must be at least 1. Otherwise this filter will just work as a
	 * pass-through.
	 * 
	 * @param nSamples
	 *            number of samples
	 * @param nPasses
	 *            number of passes
	 * @param nElements
	 *            the number of elements to be filtered individually
	 */
	public MPMovingAverageFilter(int nSamples, int nPasses, int nElements) {
		if (nSamples > 1 && nPasses > 0) {
			this.nSamples = nSamples;
			this.nPasses = nPasses;
			passthrough = false;
		}
		if (nElements > 1) {
			this.nElements = nElements;
		}
		
		// iSampNext will be just ahead of iSamp
		nextIndex(iSampNext);
		
		// create arrays
		sampleHistory = new float[nPasses][nElements][nSamples];
		lastResult = new float[nPasses][nElements];
	}

	/**
	 * Creates a new filter with provided amount of samples and passes.
	 * The number of samples must be greater than 1 and the number of
	 * passes must be at least 1. Otherwise this filter will just work as a
	 * pass-through.
	 * 
	 * @param nSamples
	 *            number of samples
	 * @param nPasses
	 *            number of passes
	 */
	public MPMovingAverageFilter(int nSamples, int nPasses) {
		this(nSamples, nPasses, 1);
	}

	/**
	 * Creates a new filter with one pass and provided amount of samples.
	 * The number of samples must be greater than 1. Otherwise this filter
	 * will just work as a pass-through.
	 * 
	 * @param nSamples
	 *            number of samples
	 */
	public MPMovingAverageFilter(int nSamples) {
		this(nSamples, 1);
	}
	
	/**
	 * Initiates the filter just as if you had added zeros 
	 * until all passes have sampling history.
	 * <b>Hint</b>, this is <i>way</i> faster than actually adding
	 * those zeroes by hand.
	 */
	public void initialize() {
		iLastPassResult = nPasses-1;
		divValue = Math.pow(nSamples, iLastPassResult+1);
		steadyPass = nPasses;
	}
	
	/**
	 * Add a sample to the filter. This does not compute any weighting.
	 * @param sample Sample to add. The elements of this array will be filtered
	 * individually.
	 */
	public void addSamples(float sample[]) {
	
		if (sample.length != nElements) {
			try {
				throw new Exception("Warning! The sample arrays length must match the constructor");
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}

		if (passthrough) {
			for (int iElem = 0; iElem < nElements; iElem++) {
				lastResult[0][iElem] = sample[iElem];
			}
			return;
		}
		
		_add(sample);
	}

	/**
	 * Add a sample to the filter.
	 * 
	 * This method compares the given time with the last time a sample was added
	 * to see if there is a large gap. Then it tries to fill that gap by adding
	 * this sample one or several times.
	 * 
	 * @param sample Sample to add. The elements of this array will be filtered
	 * individually.
	 * @param long Time in <b>nanoseconds</b>. Such as those already given from the sensors.
	 */
	public void addSamples(float sample[], long time) {

		if (sample.length != nElements) {
			try {
				throw new Exception("Warning! The sample arrays length must match the constructor");
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}

		if (passthrough) {
			for (int iElem = 0; iElem < nElements; iElem++) {
				lastResult[0][iElem] = sample[iElem];
			}
			return;
		}
		
		/*
		 * Since samples can come whenever "they want", we want to
		 * fill the void between two samples...
		 * This WILL result in a rounding error! Too lazy to do a
		 * good weighted average solution :P
		 */
		int nAdds;
		if(lastTime == 0) {
			nAdds = 1;
		} else {
			// time in nanosecond
			nAdds = Math.round((time - lastTime)*Frequency);
		}
		lastTime = time;
		for(int i = 0; i < nAdds; i++) {
			_add(sample);
		}
	}

	private void _add(float[] sample) {
		float toAdd;
		for (int iPass = 0; iPass < nPasses; iPass++) {
			if (iPass >= steadyPass) {
				// we need to collect samples...
				if (iPass == steadyPass) {
					// ...but don't unless the previous pass is stable (or it's the first pass)
					for (int iElem = 0; iElem < nElements; iElem++) {
						// if first pass, add sample.
						// otherwise, add previous pass result
						toAdd = iPass == 0 ? sample[iElem] : lastResult[iPass - 1][iElem];
						lastResult[iPass][iElem] += toAdd;
						iLastPassResult = iPass;
						// populate sample history
						sampleHistory[iPass][iElem][iSamp] = toAdd;
					}
				}
				
			} else {
				// here we have enough samples to do the regular
				// moving average algorithm for this pass
				for (int iElem = 0; iElem < nElements; iElem++) {
					// if first pass, add sample. otherwise, add previous pass
					// result
					toAdd = iPass == 0 ? sample[iElem] : lastResult[iPass - 1][iElem];

					// add new value and remove the first
					lastResult[iPass][iElem] += toAdd - sampleHistory[iPass][iElem][iSampNext];

					// write over sample history
					sampleHistory[iPass][iElem][iSamp] = toAdd;
				}
			}
		}
		iSamp = nextIndex(iSamp);
		iSampNext = nextIndex(iSampNext);
		if(steadyPass < nPasses && iSamp == 0) {
			
			// with iSamp as 0, we have written a full sample history for one
			// pass. We can now mark that pass as "complete" and start
			// gathering samples for another pass
			steadyPass++;
			
			if(steadyPass <= nPasses) {
				// update divValue during pass changes
				divValue = Math.pow(nSamples, iLastPassResult+1);
			}
		}
	}

	/**
	 * Sets the given array with the latest results from this filter
	 * @param result results 
	 */
	public void getResults(float[] result) {
		if (result.length != nElements) {
			try {
				throw new Exception("Warning! The result arrays length must match the constructor");
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		if (passthrough) {
			for (int iElem = 0; iElem < nElements; iElem++) {
				result[iElem] = lastResult[0][iElem];
			}
		}
		// calculate our division while we´re still collecting enough samples
		if(steadyPass < nPasses && iSamp != 0) {
			divValue = Math.pow(nSamples, iLastPassResult+1) * (iSamp)/(double)nSamples;
		}
		for (int iElem = 0; iElem < nElements; iElem++) {
			result[iElem] = (float) (lastResult[iLastPassResult][iElem] / divValue);
		}
	}

	// nothing fancy, just a circular array index
	private int nextIndex(int index) {
		index++;
		if(index == nSamples) {
			index = 0;
		}
		return index;
	}
}