/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene;

import se.ltu.android.demo.scene.camera.Camera;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class CameraNode extends MetaLeaf {
	private Camera cam;
	
	/**
	 * Create a camera node without a Camera to wrap.
	 * You must specify the Camera later with <code>setCamera</code>.
	 * @param name name of the camera
	 */
	public CameraNode(String name) {
		super(name);
		setPickable(false);
	}
	
	/**
	 * Creates a camera node which wraps given camera
	 * @param name name of the camera
	 * @param cam Camera object to wrap
	 */
	public CameraNode(String name, Camera cam) {
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
	
	@Override
	public void updateTransform() {
		// TODO The camera should be affected by parents world translation
	}
}
