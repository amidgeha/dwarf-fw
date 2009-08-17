/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene;

import javax.microedition.khronos.opengles.GL10;

import se.ltu.android.demo.scene.state.Light;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class LightNode extends Node {
	protected Light light;

	/**
	 * @param name
	 */
	public LightNode(String name) {
		super(name);
	}
	
	public LightNode(String name, Light light) {
		super(name);
		this.light = light;
	}

	public Light getLight() {
		return light;
	}
	
	/**
	 * Set the light for this node. Replaces any previously set
	 * light.
	 */
	public void setLight(Light light) {
		this.light = light;
	}

	/* (non-Javadoc)
	 * @see se.ltu.android.demo.scene.Node#draw(javax.microedition.khronos.opengles.GL10)
	 */
	@Override
	public void draw(GL10 gl) {
		// TODO allow transformations and check for type of light source
		light.enable(gl);
		super.draw(gl);
	}
}
