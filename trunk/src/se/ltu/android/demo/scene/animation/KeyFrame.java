/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene.animation;

import java.security.InvalidParameterException;

import android.util.Log;

/**
 * A key frame consists of a transformation and a point in time.
 * It's the building stone of a key frame animation path.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class KeyFrame implements Comparable<KeyFrame> {
	private final static String TAG = "Frame";
	protected float[] rotation;
	protected float[] translation;
	protected float[] scale;
	protected long time;

	/**
	 * Creates a new frame with the given time stamp.
	 * The time is relative to another frame and not based on
	 * the current system time. The time can never be negative. If
	 * a negative time value is given, the value is set to zero
	 * and a warning is logged.
	 * 
	 * @param time time in milliseconds
	 */
	public KeyFrame(long time) {
		if(time < 0) {
			Log.w(TAG, "Got a negative time stamp, setting it to zero");
			time = 0;
		}
		this.time = time;
	}
	
	/**
	 * Compares this frame with another frame based on the
	 * time set for each frame. The transformations are not tested.
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(KeyFrame other) {
		if(this.time < other.time) {
			return -1;
		}
		if(this.time == other.time) {
			return 0;
		}
		return 1;
	}
	
	/**
	 * Compares this frame with another frame. They are
	 * considered equal if they occur at the same time.
	 * Transformations are <b>not</b> checked.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof KeyFrame))
			return false;
		KeyFrame other = (KeyFrame) obj;
		if (time != other.time)
			return false;
		return true;
	}

	/**
	 * @return the rotation
	 */
	public float[] getRotation() {
		return rotation;
	}
	
	/**
	 * return the scale
	 * @return
	 */
	public float[] getScale() {
		return scale;
	}
	
	/**
	 * @return the time when this key frame should occur
	 */
	public long getTime() {
		return time;
	}
	
	/**
	 * @return return the translation
	 */
	public float[] getTranslation() {
		return translation;
	}
	
	/**
	 * Set the rotation specified by an angle and an axis of rotation
	 * @param angle angle in degrees
	 * @param x axis of rotation x coordinate
	 * @param y axis of rotation y coordinate
	 * @param z axis of rotation z coordinate
	 */
	public void setRotation(float angle, float x, float y, float z) {
		if(rotation == null) {
			rotation = new float[4];
		}
		rotation[0] = angle;
		rotation[1] = x;
		rotation[2] = y;
		rotation[3] = z;
	}
	
	/**
	 * Set the rotation specified by an angle and an axis of rotation
	 * @param rotation4f rotation to set
	 */
	public void setRotation(float[] rotation4f) {
		if(rotation4f == null) {
			rotation = null;
			return;
		}
		if(rotation4f.length != 3) {
			throw new InvalidParameterException(
					"Invalid length of array, got "+rotation4f.length+", expected 3");
		}
		if(rotation == null) {
			rotation = new float[3];
		}
		rotation[0] = rotation4f[0];
		rotation[1] = rotation4f[1];
		rotation[2] = rotation4f[2];
		rotation[3] = rotation4f[3];
	}
	
	/**
	 * Set the scale
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setScale(float x, float y, float z) {
		if(scale == null) {
			scale = new float[3];
		}
		scale[0] = x;
		scale[1] = y;
		scale[2] = z;
	}
	
	/**
	 * Set the scale
	 * @param scale3f scale to set
	 */
	public void setScale(float[] scale3f) {
		if(scale3f == null) {
			scale = null;
			return;
		}
		if(scale3f.length != 3) {
			throw new InvalidParameterException(
					"Invalid length of array, got "+scale3f.length+", expected 3");
		}
		if(scale == null) {
			scale = new float[3];
		}
		scale[0] = scale3f[0];
		scale[1] = scale3f[1];
		scale[2] = scale3f[2];
	}
	
	/**
	 * Set the translation
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param z z coordinate
	 */
	public void setTranslation(float x, float y, float z) {
		if(translation == null) {
			translation = new float[3];
		}
		translation[0] = x;
		translation[1] = y;
		translation[2] = z;
	}
	
	/**
	 * Set the translation
	 * @param trans3f translation to set
	 */
	public void setTranslation(float[] trans3f) {
		if(trans3f == null) {
			translation = null;
			return;
		}
		if(trans3f.length != 3) {
			throw new InvalidParameterException(
					"Invalid length of array, got "+trans3f.length+", expected 3");
		}
		if(translation == null) {
			translation = new float[3];
		}
		translation[0] = trans3f[0];
		translation[1] = trans3f[1];
		translation[2] = trans3f[2];
	}
}