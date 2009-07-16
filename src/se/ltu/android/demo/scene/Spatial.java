/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.Matrix;
import android.util.Log;

import se.ltu.android.demo.intersection.AABBox;
import se.ltu.android.demo.intersection.PickResult;
import se.ltu.android.demo.intersection.Ray;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public abstract class Spatial {
	private final static String TAG = "Spatial";
	protected Node parent;
	protected String name = "unnamed node";
	protected AABBox worldBound;
	
	/**
	 * Transformation matrix
	 */
	protected float[] transM = new float[16];
	protected float[] locTranslation = null;
	protected float[] locRotation = null;
	protected float[] locScale = null;
		
	public Spatial(String name) {
		this.name = name;
		worldBound = new AABBox();
		Matrix.setIdentityM(transM, 0);
	}
	
	/**
	 * Draw the geometry or go through the children if it's not a geometry
	 * @param gl
	 */
	public abstract void draw(GL10 gl);
	
	public String getName() {
		return name;
	}

	public boolean hasParent() {
		return parent != null;
	}
	
	public void detachFromParent() {
		parent.detachChild(this);
	}
	
	public float[] getLocalTranslation() {
		return locTranslation;
	}
	
	public float[] getLocalRotation() {
		return locRotation;
	}
	
	public float[] getLocalScale() {
		return locScale;
	}
	
	public float[] getTransform() {
		return transM;
	}
	
	public void setLocalTranslation(float x, float y, float z) {
		if(locTranslation == null) {
			locTranslation = new float[3];
		}
		locTranslation[0] = x;
		locTranslation[1] = y;
		locTranslation[2] = z;
	}
	
	public void setLocalTranslation(float[] translation) {
		if(translation != null) {
			if(locTranslation == null) {
				locTranslation = new float[3];
			}
			locTranslation[0] = translation[0];
			locTranslation[1] = translation[1];
			locTranslation[2] = translation[2];
		}
	}
	
	public void setLocalRotation(float angle, float x, float y, float z) {
		if(locRotation == null) {
			locRotation = new float[4];
		}
		locRotation[0] = angle;
		locRotation[1] = x;
		locRotation[2] = y;
		locRotation[3] = z;
	}
	
	public void setLocalRotation(float[] rotation) {
		if(rotation != null) {
			if(locRotation == null) {
				locRotation = new float[4];
			}
			locRotation[0] = rotation[0];
			locRotation[1] = rotation[1];
			locRotation[2] = rotation[2];
		}
	}
	
	public void setLocalScale(float[] scale) {
		if(scale != null) {
			if(locScale == null) {
				locScale = new float[3];
			}
			locScale[0] = scale[0];
			locScale[1] = scale[1];
			locScale[2] = scale[2];
		}
	}
	
	public void setLocalScale(float x, float y, float z) {
		if(locScale == null) {
			locScale = new float[3];
		}
		locScale[0] = x;
		locScale[1] = y;
		locScale[2] = z;
	}
	
	public void setTransform(float[] transM) {
		if(transM.length != 16) {
			return;
		}
		for(int i = 0; i < 16; i++) {
			this.transM[i] = transM[i];
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Spatial))
			return false;
		Spatial other = (Spatial) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	/**
	 * Updates the bounding volume for this spatial
	 */
	public abstract void updateModelBound();
	
	/**
	 * Updates the world bound for this spatial and
	 * propagate the changes up to the root if wanted.
	 * @param propagate set to true if we want to propagate the
	 * changes up to the root 
	 */
	public abstract void updateWorldBound(boolean propagate);
	
	/**
	 * Updates the world transformation matrix for this spatial
	 */
	public void updateTransform() {
		if(parent != null) {
			for(int i = 0; i < 16; i++) {
				transM[i] = parent.transM[i];
			}
		} else {
			Matrix.setIdentityM(transM, 0);
		}
		
		if (locTranslation != null) {
			Matrix.translateM(transM, 0, locTranslation[0], locTranslation[1], locTranslation[2]);
		}
		if (locRotation != null) {
			Matrix.rotateM(transM, 0, locRotation[0], locRotation[1], locRotation[2], locRotation[3]);
		}
		if (locScale != null) {
			Matrix.scaleM(transM, 0, locScale[0], locScale[1], locScale[2]);
		}
	}
	
	public AABBox getWorldBound() {
		return worldBound;
	}
	
	public void calculatePick(Ray ray, PickResult result) {
		if(result == null) {
			Log.w(TAG, "PickResult is null in "+name);
			return;
		}
		
		float distance = 0;
		if(ray.intersects(worldBound, distance)) {
			result.add(this, distance);
		}
	}
}
