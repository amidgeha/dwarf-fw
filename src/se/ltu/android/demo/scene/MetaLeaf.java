/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene;

import javax.microedition.khronos.opengles.GL10;

import se.ltu.android.demo.scene.state.Material;

/**
 * A leaf with empty implementations for all abstract
 * methods in Spatial. This object cannot be instantiated
 * but is designed to be extended to other types of leafs
 * that do not require rendering.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public abstract class MetaLeaf extends Spatial {

	/**
	 * @param name name of leaf
	 */
	public MetaLeaf(String name) {
		super(name);
	}

	/**
	 * Empty implementation. Does nothing on this object.
	 */
	@Override
	public void draw(GL10 gl) {
	}

	/**
	 * Empty implementation. Does nothing on this object.
	 */
	@Override
	public void forgetHardwareBuffers() {}

	/**
	 * Empty implementation. Does nothing on this object.
	 */
	@Override
	public void freeHardwareBuffers(GL10 gl) {}

	/**
	 * Empty implementation. Does nothing on this object.
	 */
	@Override
	public void generateHardwareBuffers(GL10 gl) {}

	/**
	 * Empty implementation. Does nothing on this object.
	 */
	@Override
	public void setMaterial(Material material) {}

	/**
	 * Empty implementation. Does nothing on this object.
	 */
	@Override
	public void updateModelBound() {}

	/**
	 * Empty implementation. Does nothing on this object.
	 */
	@Override
	public void updateWorldBound(boolean propagate) {}

}
