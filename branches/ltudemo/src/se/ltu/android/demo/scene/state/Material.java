/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene.state;

import javax.microedition.khronos.opengles.GL10;

/**
 * Material for geometries in the scene
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class Material {
	public static float[] DEFAULT_AMBIENT = {0.2f, 0.2f, 0.2f, 1.0f};
	public static float[] DEFAULT_DIFFUSE = {0.8f, 0.8f, 0.8f, 1.0f};
	public static float[] DEFAULT_SPECULAR = {0.0f, 0.0f, 0.0f, 1.0f};
	public static float[] DEFAULT_EMISSION = {0.0f, 0.0f, 0.0f, 1.0f};
	public static float DEFAULT_SHININESS = 0.0f;
	
	private static float[] current_ambient;
	private static float[] current_diffuse;
	private static float[] current_specular;
	private static float[] current_emission;
	private static float current_shininess;
	private static boolean current_useColorMaterial;
	
	private float[] ambient;
	private float[] diffuse;
	private float[] specular;
	private float[] emission;
	private float shininess; // 0, 128
	private boolean useColorMaterial;
	
	/**
	 * Sets the material to the one specified
	 * @param gl
	 */
	public void applyState(GL10 gl) {
		if(current_useColorMaterial != useColorMaterial) {
			current_useColorMaterial = useColorMaterial;
			if(current_useColorMaterial) {
				gl.glEnable(GL10.GL_COLOR_MATERIAL);
			} else {
				gl.glDisable(GL10.GL_COLOR_MATERIAL);
			}
		}
		
		if(ambient != null) {
			if(current_ambient == null || !sameColor(current_ambient, ambient)) {
				current_ambient = ambient;
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, ambient, 0);
			}
		} else if(current_ambient != null && !sameColor(current_ambient, DEFAULT_AMBIENT)) {
			current_ambient = null;
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, DEFAULT_AMBIENT, 0);
		}
		
		if(diffuse != null) {
			if(current_diffuse == null || !sameColor(current_diffuse, diffuse)) {
				current_diffuse = diffuse;
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, diffuse, 0);
			}
		} else if(current_diffuse != null && !sameColor(current_diffuse, DEFAULT_DIFFUSE)) {
			current_diffuse = null;
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, DEFAULT_DIFFUSE, 0);
		}
		
		if(emission != null) {
			if(current_emission == null || !sameColor(current_emission, emission)) {
				current_emission = emission;
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_EMISSION, emission, 0);
			}
		} else if(current_emission != null && !sameColor(current_emission, DEFAULT_EMISSION)) {
			current_emission = null;
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_EMISSION, DEFAULT_EMISSION, 0);
		}
		
		if(specular != null) {
			if(current_specular == null || !sameColor(current_specular, specular)) {
				current_specular = specular;
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, specular, 0);
			}
		} else if(current_specular != null && !sameColor(current_specular, DEFAULT_SPECULAR)) {
			current_specular = null;
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, DEFAULT_SPECULAR, 0);
		}
		
		if(current_shininess != shininess) {
			current_shininess = shininess;
			gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, shininess);
		}
	}
	
	/**
	 * Sets the material to the OpenGL standards
	 * @param gl
	 */
	public static void removeState(GL10 gl) {
		
		if(current_useColorMaterial) {
			current_useColorMaterial = false;
			gl.glDisable(GL10.GL_COLOR_MATERIAL);
		}
		
		if(current_ambient != null && !sameColor(current_ambient, DEFAULT_AMBIENT)) {
			current_ambient = null;
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, DEFAULT_AMBIENT, 0);
		}
		
		if(current_diffuse != null && !sameColor(current_diffuse, DEFAULT_DIFFUSE)) {
			current_diffuse = null;
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, DEFAULT_DIFFUSE, 0);
		}
		
		if(current_emission != null && !sameColor(current_emission, DEFAULT_EMISSION)) {
			current_emission = null;
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_EMISSION, DEFAULT_EMISSION, 0);
		}
		
		if(current_specular != null && !sameColor(current_specular, DEFAULT_SPECULAR)) {
			current_specular = null;
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, DEFAULT_SPECULAR, 0);
		}
		
		if(current_shininess != DEFAULT_SHININESS) {
			current_shininess = DEFAULT_SHININESS;
			gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, DEFAULT_SHININESS);
		}
	}

	/**
	 * @param color4f the ambient to set
	 */
	public void setAmbient(float[] color4f) {
		if(color4f == null || color4f.length != 4) {
			return;
		}
		if(ambient == null) {
			ambient = new float[4];
		}
		this.ambient[0] = color4f[0];
		this.ambient[1] = color4f[1]; 
		this.ambient[2] = color4f[2]; 
		this.ambient[3] = color4f[3]; 
	}
	
	/**
	 * Set the ambient
	 * @param r red component
	 * @param g green component
	 * @param b blue component
	 * @param a alpha component
	 */
	public void setAmbient(float r, float g, float b, float a) {
		if(ambient == null) {
			ambient = new float[4];
		}
		this.ambient[0] = r;
		this.ambient[1] = g; 
		this.ambient[2] = b; 
		this.ambient[3] = a; 
	}

	/**
	 * @return the ambient
	 */
	public float[] getAmbient() {
		return ambient;
	}
	
	/**
	 * @param color4f the diffuse to set
	 */
	public void setDiffuse(float[] color4f) {
		if(color4f == null || color4f.length != 4) {
			return;
		}
		if(diffuse == null) {
			diffuse = new float[4];
		}
		this.diffuse[0] = color4f[0];
		this.diffuse[1] = color4f[1]; 
		this.diffuse[2] = color4f[2]; 
		this.diffuse[3] = color4f[3]; 
	}
	
	/**
	 * Set the diffuse
	 * @param r red component
	 * @param g green component
	 * @param b blue component
	 * @param a alpha component
	 */
	public void setDiffuse(float r, float g, float b, float a) {
		if(diffuse == null) {
			diffuse = new float[4];
		}
		this.diffuse[0] = r;
		this.diffuse[1] = g; 
		this.diffuse[2] = b; 
		this.diffuse[3] = a; 
	}

	/**
	 * @return the diffuse
	 */
	public float[] getDiffuse() {
		return diffuse;
	}
	
	/**
	 * @param color4f the emission to set
	 */
	public void setEmission(float[] color4f) {
		if(color4f == null || color4f.length != 4) {
			return;
		}
		if(emission == null) {
			emission = new float[4];
		}
		this.emission[0] = color4f[0];
		this.emission[1] = color4f[1]; 
		this.emission[2] = color4f[2]; 
		this.emission[3] = color4f[3]; 
	}
	
	/**
	 * Set the emission
	 * @param r red component
	 * @param g green component
	 * @param b blue component
	 * @param a alpha component
	 */
	public void setEmission(float r, float g, float b, float a) {
		if(emission == null) {
			emission = new float[4];
		}
		this.emission[0] = r;
		this.emission[1] = g; 
		this.emission[2] = b; 
		this.emission[3] = a; 
	}

	/**
	 * @return the emission
	 */
	public float[] getEmission() {
		return emission;
	}
	
	/**
	 * @param color4f the specular to set
	 */
	public void setSpecular(float[] color4f) {
		if(color4f == null || color4f.length != 4) {
			return;
		}
		if(specular == null) {
			specular = new float[4];
		}
		this.specular[0] = color4f[0];
		this.specular[1] = color4f[1]; 
		this.specular[2] = color4f[2]; 
		this.specular[3] = color4f[3]; 
	}
	
	/**
	 * Set the specular
	 * @param r red component
	 * @param g green component
	 * @param b blue component
	 * @param a alpha component
	 */
	public void setSpecular(float r, float g, float b, float a) {
		if(specular == null) {
			specular = new float[4];
		}
		this.specular[0] = r;
		this.specular[1] = g; 
		this.specular[2] = b; 
		this.specular[3] = a; 
	}

	/**
	 * @return the specular
	 */
	public float[] getSpecular() {
		return specular;
	}
	
	/**
	 * @param shininess the shininess to set
	 */
	public void setShininess(float shininess) {
		this.shininess = shininess;
	}

	/**
	 * @return the shininess
	 */
	public float getShininess() {
		return shininess;
	}
	
	/**
	 * @param b true if this material should use vertex colors
	 */
	public void setUseColorMaterial(boolean b) {
		useColorMaterial = b;
	}
	
	/**
	 * @return true if this material uses vertex colors
	 */
	public boolean usesColorMaterial() {
		return useColorMaterial;
	}
	
	// maybe not so clever
	private static boolean sameColor(float[] c1, float[] c2) {
		if(c1 == c2) {
			return true;
		}
		return (
				c1[0] == c2[0] &&
				c1[1] == c2[1] &&
				c1[2] == c2[2] &&
				c1[3] == c2[3]
		);
	}

	/**
	 * Set this materials attributes equal to another material attributes.
	 * @param other material to copy attributes from
	 */
	public void copyFrom(Material other) {
		setAmbient(other.ambient);
		setDiffuse(other.diffuse);
		setSpecular(other.specular);
		setEmission(other.emission);
		this.useColorMaterial = other.useColorMaterial;
		this.shininess = other.shininess;
	}
}
