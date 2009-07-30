/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene;

import javax.microedition.khronos.opengles.GL10;

import se.ltu.android.demo.intersection.AABBox;

/**
 * Represents a bounding box that can be placed anywhere in
 * a scene. As an example, it is good for creating one or more 
 * pickable areas inside a solid mesh.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class PickBox extends Spatial {
	AABBox modelBound;
	
	public PickBox(String name, AABBox bound) {
		super(name);
		modelBound = bound;
	}

	/**
	 * This method does nothing on this object
	 * @see se.ltu.android.demo.scene.Spatial#draw(javax.microedition.khronos.opengles.GL10)
	 */
	@Override
	public void draw(GL10 gl) {
	}

	/**
	 * This method does nothing on this object
	 * @see se.ltu.android.demo.scene.Spatial#freeHardwareBuffers(javax.microedition.khronos.opengles.GL10)
	 */
	@Override
	public void freeHardwareBuffers(GL10 gl) {
	}

	/**
	 * This method does nothing on this object
	 * @see se.ltu.android.demo.scene.Spatial#generateHardwareBuffers(javax.microedition.khronos.opengles.GL10)
	 */
	@Override
	public void generateHardwareBuffers(GL10 gl) {
	}
	
	/**
	 * Sets the bounding volume.
	 * @param bound custom bounding volume
	 */
	public void setModelBound(AABBox bound) {
		modelBound = bound;
	}

	/**
	 * This method does nothing on this object. Use setModelBound() if
	 * you want to change the model bound.
	 * @see se.ltu.android.demo.scene.Spatial#updateModelBound()
	 */
	@Override
	public void updateModelBound() {
	}

	/**
	 * Updates
	 * @see se.ltu.android.demo.scene.Spatial#updateWorldBound(boolean)
	 */
	@Override
	public void updateWorldBound(boolean propagate) {
		worldBound.transform(transM, modelBound);
		if(propagate && parent != null) {
			parent.updateWorldBound(this);
		}
	}

}
