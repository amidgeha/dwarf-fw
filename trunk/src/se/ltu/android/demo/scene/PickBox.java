/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene;

import se.ltu.android.demo.scene.intersection.AABBox;

/**
 * Represents a bounding box that can be placed anywhere in
 * a scene. As an example, it is good for creating one or more 
 * pickable areas inside a solid mesh.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class PickBox extends MetaLeaf {
	AABBox modelBound;
	
	/**
	 * Creates a new instance with the given name and the given bounding volume
	 * @param name name of the instance
	 * @param bound custom bounding volume
	 */
	public PickBox(String name, AABBox bound) {
		super(name);
		modelBound = bound;
	}

	/**
	 * Sets the bounding volume.
	 * @param bound custom bounding volume
	 */
	public void setModelBound(AABBox bound) {
		modelBound = bound;
	}

	/**
	 * Empty implementation. Does nothing on a PickBox. Use <code>setModelBound()</code>
	 * if you want to change the model bound.
	 * @see se.ltu.android.demo.scene.Spatial#updateModelBound()
	 */
	@Override
	public void updateModelBound() {
	}

	/**
	 * Updates the world bound based on the model bound set for this object.
	 */
	@Override
	public void updateWorldBound(boolean propagate) {
		worldBound.transform(transM, modelBound);
		if(propagate && parent != null) {
			parent.updateWorldBound(this);
		}
	}
}
