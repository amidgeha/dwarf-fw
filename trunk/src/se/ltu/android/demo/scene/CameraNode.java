/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene;

import javax.microedition.khronos.opengles.GL10;

import se.ltu.android.demo.camera.Camera;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class CameraNode extends Spatial {
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

	/**
	 * Empty implementation. Does nothing on a Camera. Getting the view information
	 * from the camera is handled in the renderer.
	 * @see se.ltu.android.demo.scene.Spatial#draw(javax.microedition.khronos.opengles.GL10)
	 */
	@Override
	public void draw(GL10 gl) {
		// nothing to draw
	}

	/**
	 * Empty implementation. Does nothing on a Camera.
	 * @see se.ltu.android.demo.scene.Spatial#freeHardwareBuffers(javax.microedition.khronos.opengles.GL10)
	 */
	@Override
	public void freeHardwareBuffers(GL10 gl) {
	}
	
	/**
	 * Empty implementation. Does nothing on a Camera.
	 * @see se.ltu.android.demo.scene.Spatial#forgetHardwareBuffers()
	 */
	@Override
	public void forgetHardwareBuffers() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Empty implementation. Does nothing on a Camera.
	 * @see se.ltu.android.demo.scene.Spatial#generateHardwareBuffers(javax.microedition.khronos.opengles.GL10)
	 */
	@Override
	public void generateHardwareBuffers(GL10 gl) {
	}

	/**
	 * Empty implementation. Does nothing on a Camera.
	 * @see se.ltu.android.demo.scene.Spatial#updateModelBound()
	 */
	@Override
	public void updateModelBound() {
	}

	/**
	 * Empty implementation. Does nothing on a Camera and since there
	 * is no change of the world bound in a camera node, there is no
	 * need for a propagation.
	 * @see se.ltu.android.demo.scene.Spatial#updateWorldBound(boolean)
	 */
	@Override
	public void updateWorldBound(boolean propagate) {
	}
	
	@Override
	public void updateTransform() {
		// TODO The camera should be affected by parents world translation
	}
}
