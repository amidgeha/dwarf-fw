/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene;

import android.opengl.Matrix;
import se.ltu.android.demo.scene.camera.Camera;

/**
 * A Camera object embedded in a scene element leaf. Changing the leafs spatial
 * position will also change the Cameras position.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class CameraLeaf extends MetaLeaf {
	private Camera cam;
	
	/**
	 * CreCameraLeafra node without a Camera to wrap.
	 * You must specify the Camera later with <code>setCamera</code>.
	 * @param name name of the camera
	 */
	public CameraLeaf(String name) {
		super(name);
		setPickable(false);
	}
		/** Creates a camera node which wraps given camera
	 * @param name name of the camera
	 * @param cam Camera object to wrap
	 */
	public CameraLeaf(String name, Camera cam) {
		this(name);
		this.cam = cam;
	}
		
	@Override
	public void setLocalTranslation(float x, float y, float z) {
		if(cam == null) {
			return;
		}
		cam.setPosition(x, y, z);
	}
	
	@Override
	public void setLocalTranslation(float[] translation) {
		if(cam == null) {
			return;
		}
		if(translation != null && translation.length == 3) {
			cam.setPosition(translation[0], translation[1], translation[2]);
		}
	}
		 
	@Override
	public float[] getLocalTranslation() {
		if(cam == null) {
			return null;
		}
		return cam.getPosition();
	}
	
	/**
	 * Sets this nodes camera
	 * @param cam camera that will be linked to from this node
	 */
	public void setCamera(Camera cam) {
		this.cam = cam;
	}
	
	/**
	 * Updates the world transformation matrix for this spatial
	 * <strong>Note:</strong> this is currently unsupported.
	 */
	@Override
	public void updateTransform() {
		// TODO The camera should be affected by parents world translation
	}
}
