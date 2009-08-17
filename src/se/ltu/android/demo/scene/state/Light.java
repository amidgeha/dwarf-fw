/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene.state;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

/**
 * A basic light source. One could easily create subclasses of this
 * one to control the behavior of directional, point and spot lights.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class Light {
	public final static int MAX_LIGHTS = 8;
	private static final String TAG = "Light";
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
	
	// set gl light number
	private int glLight = -1;
	
	// global number of lights checked out
	private static int curLights = 0;
	
	private static int getNumber() {
		if(curLights < MAX_LIGHTS) {
			curLights++;
			switch(curLights) {
			case 1:
				return GL10.GL_LIGHT0;
			case 2:
				return GL10.GL_LIGHT1;
			case 3:
				return GL10.GL_LIGHT2;
			case 4:
				return GL10.GL_LIGHT3;
			case 5:
				return GL10.GL_LIGHT4;
			case 6:
				return GL10.GL_LIGHT5;
			case 7:
				return GL10.GL_LIGHT6;
			case 8:
				return GL10.GL_LIGHT7;
			}
		}
		Log.w(TAG, "The maximum number of lights are already created");
		return -1;
	}
	
	/**
	 * Creates a new instance
	 */
	public Light() {
		glLight = getNumber();
	}
	
	/**
	 * @return a copy of this light
	 */
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
	 * the light. Should be called inside the scene traversal. 
	 */
	public void enable(GL10 gl) {
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
	}
	
	/**
	 * Set the ambient
	 * @param r red component
	 * @param g green component
	 * @param b blue component
	 * @param a alpha component
	 */
	public void setAmbient(float r, float g, float b, float a) {
		if(ambient4f == null) {
			ambient4f = new float[4];
		}
		ambient4f[0] = r;
		ambient4f[1] = g;
		ambient4f[2] = b;
		ambient4f[3] = a;
	}
	
	/**
	 * Set the ambient
	 * @param rgba4f color to set
	 */
	public void setAmbient(float[] rgba4f) {
		if(rgba4f == null) {
			ambient4f = null;
			return;
		}
		if(rgba4f.length == 4) {
			if(ambient4f == null) {
				ambient4f = new float[4];
			}
			for(int i = 0; i < 4; i++) {
				ambient4f[i] = rgba4f[i];
			}
		}
	}
	
	/**
	 * @return the ambient
	 */
	public float[] getAmbient() {
		return ambient4f;
	}
	
	/**
	 * Set the diffuse
	 * @param r red component
	 * @param g green component
	 * @param b blue component
	 * @param a alpha component
	 */
	public void setDiffuse(float r, float g, float b, float a) {
		if(diffuse4f == null) {
			diffuse4f = new float[4];
		}
		diffuse4f[0] = r;
		diffuse4f[1] = g;
		diffuse4f[2] = b;
		diffuse4f[3] = a;
	}
	
	/**
	 * Set the diffuse
	 * @param rgba4f color to set
	 */
	public void setDiffuse(float[] rgba4f) {
		if(rgba4f == null) {
			diffuse4f = null;
			return;
		}
		if(rgba4f.length == 4) {
			if(diffuse4f == null) {
				diffuse4f = new float[4];
			}
			for(int i = 0; i < 4; i++) {
				diffuse4f[i] = rgba4f[i];
			}
		}
	}
	
	/**
	 * @return the diffuse
	 */
	public float[] getDiffuse() {
		return diffuse4f;
	}
	
	/**
	 * Set the position.
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param z z coordinate
	 * @param w set to 0 for a directional light or set to 1 for a point light
	 */
	public void setPosition(float x, float y, float z, float w) {
		if(pos4f == null) {
			pos4f = new float[4];
		}
		pos4f[0] = x;
		pos4f[1] = y;
		pos4f[2] = z;
		pos4f[3] = w;
	}
	
	/**
	 * Set the position. The last element in the vector defines wheter the light
	 * is a directional light (0) or a point light (1).
	 * @param vector4f position to set
	 */
	public void setPosition(float[] vector4f) {
		if(vector4f == null) {
			pos4f = null;
			return;
		}
		if(vector4f.length == 4) {
			if(pos4f == null) {
				pos4f = new float[4];
			}
			for(int i = 0; i < 4; i++) {
				pos4f[i] = vector4f[i];
			}
		}
	}
	
	/**
	 * @return the position
	 */
	public float[] getPosition() {
		return pos4f;
	}
	
	/**
	 * Set the specular
	 * @param r red component
	 * @param g green component
	 * @param b blue component
	 * @param a alpha component
	 */
	public void setSpecular(float r, float g, float b, float a) {
		if(specular4f == null) {
			specular4f = new float[4];
		}
		specular4f[0] = r;
		specular4f[1] = g;
		specular4f[2] = b;
		specular4f[3] = a;
	}
	
	/**
	 * Set the specular
	 * @param rgba4f specular to set
	 */
	public void setSpecular(float[] rgba4f) {
		if(rgba4f == null) {
			specular4f = null;
			return;
		}
		if(rgba4f.length == 4) {
			if(specular4f == null) {
				specular4f = new float[4];
			}
			for(int i = 0; i < 4; i++) {
				specular4f[i] = rgba4f[i];
			}
		}
	}
	
	/**
	 * @return the specular
	 */
	public float[] getSpecular() {
		return specular4f;
	}
	
	/**
	 * Set the spot direction
	 * @param x x direction
	 * @param y y direction
	 * @param z z direction
	 */
	public void setSpotDirection(float x, float y, float z) {
		if(spot_dir3f == null) {
			spot_dir3f = new float[3];
		}
		spot_dir3f[0] = x;
		spot_dir3f[1] = y;
		spot_dir3f[2] = z;
	}
	
	/**
	 * Set the spot direction
	 * @param vector3f direction to set
	 */
	public void setSpotDirection(float[] vector3f) {
		if(vector3f == null) {
			spot_dir3f = null;
			return;
		}
		if(vector3f.length == 4) {
			if(spot_dir3f == null) {
				spot_dir3f = new float[3];
			}
			for(int i = 0; i < 4; i++) {
				spot_dir3f[i] = vector3f[i];
			}
		}
	}
	
	/**
	 * @return the spot direction
	 */
	public float[] getSpotDirection() {
		return spot_dir3f;
	}
	
	/**
	 * Set the spot cutoff angle
	 * @param cutoff angle to set
	 */
	public void setSpotCutoff(float cutoff) {
		has_spot_cutoff = true;
		spot_cutoff = cutoff;
	}
	
	/**
	 * @return the spot cutoff angle
	 */
	public float getSpotCutoff() {
		return spot_cutoff;
	}
	
	/**
	 * Set the spot exponent
	 * @param exponent exponent to set
	 */
	public void setSpotExponent(float exponent) {
		has_spot_exponent = true;
		spot_exponent = exponent;
	}
	
	/**
	 * @return the spot exponent
	 */
	public float getSpotExponent() {
		return spot_exponent;
	}
}
