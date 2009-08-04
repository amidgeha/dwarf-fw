/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene.animation;

import java.util.ArrayList;

import se.ltu.android.demo.scene.Spatial;

import android.util.Log;
import android.view.animation.Interpolator;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class KeyFrameAnimation {
	private ArrayList<KeyFrame> frames = new ArrayList<KeyFrame>();
	private long curTime;
	private int curIndex;
	private int nextIndex;
	private KeyFrame curFrame;
	private KeyFrame nextFrame;
	private long lastFrameTime = -1;
	private boolean isRunning = false;
	private float[] tmpTrans = new float[3];
	private boolean isPrepared;
	private Interpolator interpolator;
	private AnimationListener listener;
	
	/**
	 * Creates a new empty instance
	 */
	public KeyFrameAnimation() {
	}
	
	/**
	 * Creates a new empty instance that notifies the given
	 * listener on changes.
	 * @param listener AnimationListener to be notified of any changes
	 */
	public KeyFrameAnimation(AnimationListener listener) {
		this.listener = listener;
	}

	/**
	 * Set the animation listener
	 * 
	 * @param listener
	 *            an AnimationListener or null if this animation should not
	 *            notify any changes
	 */
	public void setListener(AnimationListener listener) {
		this.listener = listener;
	}

	/**
	 * Adds a frame to the animation path. If there already exists a frame with
	 * the same time as the added frame; that frame is first removed before the
	 * given frame is added.
	 * 
	 * @param frame
	 */
	public void addFrame(KeyFrame frame) {
		int len = frames.size();
		if (lastFrameTime < frame.time) {
			// add last instead of checking the whole array
			frames.add(frame);
			lastFrameTime = frame.time;
			return;
		}
		for (int i = 0; i < len; i++) {
			if (frames.get(i).time == frame.time) {
				// replace duplicate frame at the same position
				frames.remove(i);
				frames.add(i - 1, frame);
				return;
			}
			if (frames.get(i).time > frame.time) {
				// insert just before the larger element
				frames.add(i, frame);
				return;
			}
		}
	}

	/**
	 * Prepares the animation to be run. Any missing information in each frame
	 * is filled in and creates a first frame.
	 */
	public void prepare(Spatial spatial) {
		if (frames.size() == 0) {
			return;
		}
		if (frames.size() == 1 && frames.get(0).time == 0) {
			Log.e("TAG", "Animation is incomplete");
			return;
		}
		// insert the initial frame, if we miss one
		if (frames.get(0).time != 0) {
			KeyFrame startFrame = new KeyFrame(0);
			// startFrame.setRotation(spatial.getLocalRotation());
			// startFrame.setScale(spatial.getLocalScale());
			if (spatial.getLocalTranslation() != null) {
				startFrame.setTranslation(spatial.getLocalTranslation());
			} else {
				startFrame.setTranslation(0, 0, 0);
			}
			frames.add(0, startFrame);
		}
		// TODO interpolate missing information
		int len = frames.size();
		KeyFrame frame;
		for (int i = 0; i < len; i++) {
			frame = frames.get(i);
			if (frame.translation == null) {
				frame.setTranslation(0, 0, 0);
			}
		}

		isPrepared = true;
		reset();
	}

	/**
	 * Removes a frame from the animation path.
	 * 
	 * @param frame
	 *            frame to remove
	 * @return true if a frame was found and removed
	 */
	public boolean removeFrame(KeyFrame frame) {
		return frames.remove(frame);
	}

	/**
	 * Resets the animation back to the first frame and starts the animation.
	 */
	public void reset() {
		curTime = 0;
		curIndex = -1;
		nextIndex = 0;
		if (isPrepared) {
			frameChange(null);
			isRunning = true;
		}
	}

	/**
	 * Updates the animation based on the current time per frame This method is
	 * called from a spatial
	 * 
	 * @param tpf
	 *            current time per frame
	 */
	public void update(long tpf, Spatial caller) {
		if (isRunning) {
			curTime += tpf;

			// handle frame change
			if (curTime > nextFrame.time) {
				if (curTime > lastFrameTime) {
					caller.setLocalTranslation(
							frames.get(frames.size() - 1).translation);
				}
				frameChange(caller);
			}
			// ratio between frames
			float frameRatio = (curTime - curFrame.time)
					/ ((float) (nextFrame.time - curFrame.time));
			if (interpolator != null) {
				frameRatio = interpolator.getInterpolation(frameRatio);
			}

			float[] nextTrans = nextFrame.getTranslation();
			float[] curTrans = curFrame.getTranslation();
			tmpTrans[0] = curTrans[0] + (nextTrans[0] - curTrans[0])
					* frameRatio;
			tmpTrans[1] = curTrans[1] + (nextTrans[1] - curTrans[1])
					* frameRatio;
			tmpTrans[2] = curTrans[2] + (nextTrans[2] - curTrans[2])
					* frameRatio;
			synchronized (caller) {
				caller.setLocalTranslation(tmpTrans);
				caller.updateTransform();
				caller.updateWorldBound(false);
			}
		}
	}

	/**
	 * 
	 * @param ip
	 */
	public void setInterpolator(Interpolator ip) {
		this.interpolator = ip;
	}

	/**
	 * Handles a frame change
	 */
	// caller is only needed for notifying listener, could be null
	private void frameChange(Spatial caller) {
		if (curTime >= lastFrameTime) {
			// end of animation
			// TODO implement some kind of wrapping mechanism?
			isRunning = false;
			if(listener != null) {
				listener.onAnimationEnd(this, caller);
			}
			return;
		}
		curFrame = frames.get(++curIndex);
		nextFrame = frames.get(++nextIndex);
		while (curTime < curFrame.time || curTime > nextFrame.time) {
			curFrame = frames.get(++curIndex);
			nextFrame = frames.get(++nextIndex);
		}
	}
}
