/* SVN FILE: $Id$ */
package se.ltu.android.demo.light;

import javax.microedition.khronos.opengles.GL10;

/**
 * A basic light source. One could easily create subclasses of this
 * one to control the behavior of directional, point and spot lights.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class Light {
	public final static int MAX_LIGHTS = 8;
	// position (x,y,z,w). directional light (w = 0) or point light (w = 1)
	private float[] pos4f;
	private float[] ambient4f;
	private float[] diffuse4f;
	private float[] specular4f;
	
	// spot attributes
	private float[] spot_dir3f;
	private boolean has_spot_cutoff = false;
	private float spot_cutoff;
	private boolean has_spot_exponent = false;
	private float spot_exponent;
	
	// global light state
	private GlobalLightState lightState;
	private int glLight = -1;
	
	public Light() {
		lightState = GlobalLightState.getInstance();
	}
	
	public Light copy() {
		Light copy = new Light();
		copy.pos4f = pos4f;
		copy.ambient4f = ambient4f;
		copy.diffuse4f = diffuse4f;
		copy.specular4f = specular4f;
		copy.spot_dir3f = spot_dir3f;
		copy.spot_cutoff = spot_cutoff;
		copy.spot_exponent = spot_exponent;
		return copy;
	}
	
	/**
	 * Set the glLight attributes for this light source and enables
	 * the light.
	 * Should be called inside the scene traversal. 
	 */
	public void enable(GL10 gl) {
		glLight = lightState.checkOut();
		if(glLight == -1) {
			return;
		}
		if(pos4f != null) {
			gl.glLightfv(glLight, GL10.GL_POSITION, pos4f, 0);
		}
		if(ambient4f != null) {
			gl.glLightfv(glLight, GL10.GL_AMBIENT, ambient4f, 0);
		}
		if(specular4f != null) {
			gl.glLightfv(glLight, GL10.GL_SPECULAR, specular4f, 0);
		}
		if(diffuse4f != null) {
			gl.glLightfv(glLight, GL10.GL_DIFFUSE, diffuse4f, 0);
		}
		
		if(spot_dir3f != null) {
			gl.glLightfv(glLight, GL10.GL_SPOT_DIRECTION, spot_dir3f, 0);
		}
		if(has_spot_cutoff) {
			gl.glLightf(glLight, GL10.GL_SPOT_CUTOFF, spot_cutoff);
		}
		if(has_spot_exponent) {
			gl.glLightf(glLight, GL10.GL_SPOT_EXPONENT, spot_exponent);
		}
		gl.glEnable(glLight);
	}
	
	/**
	 * Disables the glLight for this light source.
	 * Should be called inside the scene traversal.
	 * @param gl
	 */
	public void disable(GL10 gl) {
		if(glLight == -1) {
			return;
		}
		gl.glDisable(glLight);
		lightState.checkOut();
	}
	
	public void setAmbient(float r, float g, float b, float a) {
		setComponents(ambient4f, r, g, b, a);
	}
	
	public void setAmbient(float[] rgba4f) {
		setArray(ambient4f, rgba4f, 4);
	}
	
	public void setDiffuse(float r, float g, float b, float a) {
		setComponents(diffuse4f, r, g, b, a);
	}
	
	public void setDiffuse(float[] rgba4f) {
		setArray(diffuse4f, rgba4f, 4);
	}
	
	public void setPosition(float x, float y, float z, float w) {
		setComponents(pos4f, x, y, z, w);
	}
	
	public void setPosition(float[] vector4f) {
		setArray(pos4f, vector4f, 4);
	}
	
	public void setSpecular(float r, float g, float b, float a) {
		setComponents(specular4f, r, g, b, a);
	}
	
	public void setSpecular(float[] rgba4f) {
		setArray(specular4f, rgba4f, 4);
	}
	
	public void setSpotDirection(float x, float y, float z) {
		setComponents(spot_dir3f, x, y, z);
	}
	
	public void setSpotDirection(float[] vector3f) {
		setArray(spot_dir3f, vector3f, 3);
	}
	
	public void setSpotCutoff(float cutoff) {
		has_spot_cutoff = true;
		spot_cutoff = cutoff;
	}
	
	public void setSpotExponent(float exponent) {
		has_spot_exponent = true;
		spot_exponent = exponent;
	}
	
	// convenience methods below
	private void setArray(float[] storage, float[] data, int length) {
		if(data == null) {
			storage = null;
			return;
		}
		if(data.length == length) {
			if(storage == null) {
				storage = new float[4];
			}
			for(int i = 0; i < length; i++) {
				storage[i] = data[i];
			}
		}
	}
	
	private void setComponents(float[] storage, float c0, float c1, float c2) {
		if(storage == null) {
			storage = new float[3];
		}
		storage[0] = c0;
		storage[1] = c1;
		storage[2] = c2;
	}
	
	private void setComponents(float[] storage, float c0, float c1, float c2, float c3) {
		if(storage == null) {
			storage = new float[4];
		}
		storage[0] = c0;
		storage[1] = c1;
		storage[2] = c2;
		storage[3] = c3;
	}
}
