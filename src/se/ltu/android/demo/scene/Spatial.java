/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.Matrix;
import android.util.Log;

import se.ltu.android.demo.scene.animation.KeyFrameAnimation;
import se.ltu.android.demo.scene.intersection.AABBox;
import se.ltu.android.demo.scene.intersection.PickResult;
import se.ltu.android.demo.scene.intersection.Ray;
import se.ltu.android.demo.scene.state.Material;

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
	protected ArrayList<KeyFrameAnimation> animations;
	protected Boolean pickable = true; 
	
	/**
	 * Transformation matrix
	 */
	protected float[] transM = new float[16];
	protected float[] locTranslation = null;
	protected float[] locRotation = null;
	protected float[] locScale = null;
	
	private Object dataObject;
		
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
		if(translation != null && translation.length == 3) {
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
		if(rotation != null && rotation.length == 4) {
			if(locRotation == null) {
				locRotation = new float[4];
			}
			locRotation[0] = rotation[0];
			locRotation[1] = rotation[1];
			locRotation[2] = rotation[2];
		}
	}
	
	public void setLocalScale(float[] scale) {
		if(scale != null && scale.length == 3) {
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
		if(transM == null || transM.length != 16) {
			return;
		}
		for(int i = 0; i < 16; i++) {
			this.transM[i] = transM[i];
		}
	}

	/**
	 * Test this spatial for equality with another spatial.
	 * They are considered equal if the names are equal
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
		if (dataObject == null) {
			if (other.dataObject != null)
				return false;
		} else if (!dataObject.equals(other.dataObject))
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
	
	/**
	 * @return the world bound
	 */
	public AABBox getWorldBound() {
		return worldBound;
	}
	
	/**
	 * Check for intersections between this spatial and
	 * a ray. It must be passed PickResult where the results
	 * will end up.
	 * @param ray ray to test against
	 * @param result contains the results when the method returns
	 */
	public void calculatePick(Ray ray, PickResult result) {
		if(result == null) {
			Log.w(TAG, "PickResult is null in "+name);
			return;
		}
		
		if(pickable) {
			float[] distance = new float[1];
			if(ray.intersects(worldBound, distance)) {
				result.add(this, distance[0]);
			}
		}
	}
	
	/**
	 * Add an animation controller to this spatial
	 * @param anim animation controller to add
	 */
	public void addController(KeyFrameAnimation anim) {
		if(animations == null) {
			animations = new ArrayList<KeyFrameAnimation>();
		}
		anim.prepare(this);
		animations.add(anim);
	}
	
	/**
	 * Removes all animation controllers from this spatial
	 */
	public void clearControllers() {
		animations.clear();
	}
	
	/**
	 * Remove a specific animation controller
	 * @param anim animation controller to remove
	 */
	public void removeController(KeyFrameAnimation anim) {
		animations.remove(anim);
	}
	
	/**
	 * @return the parent node of this spatial
	 */
	public Node getParent() {
		return parent;
	}
	
	/**
	 * Updates the animation controllers of this spatial
	 * @param tpf time in milliseconds since last update
	 */
	public void update(long tpf) {
		if(animations != null) {
			int len = animations.size();
			for(int i = 0; i < len; i++) {
				animations.get(i).update(tpf, this);
			}
		}
	}
	
	/**
	 * Returns the name of this spatial
	 */
	public String toString() {
		return name;
	}

	/**
     * Deletes the hardware buffers allocated by this object (if any).
     */
	public abstract void freeHardwareBuffers(GL10 gl);

	/** 
     * When the OpenGL ES device is lost, GL handles become invalidated.
     * In that case, we just want to "forget" the old handles (without
     * explicitly deleting them) and make new ones.
     */
	public abstract void forgetHardwareBuffers();

	/** 
     * Allocates hardware buffers on the graphics card and fills them with
     * data if a buffer has not already been previously allocated.  Note that
     * this function uses the GL_OES_vertex_buffer_object extension, which is
     * not guaranteed to be supported on every device.
     * @param gl  A pointer to the OpenGL ES context.
     */
	public abstract void generateHardwareBuffers(GL10 gl);
	
	/**
	 * Set if this object should be tested for intersections with a
	 * pick ray and end up in a PickResult.<br>
	 * <br>
	 * Default is true.
	 * @param pickable true if this object should be pickable
	 */
	public void setPickable(boolean pickable) {
		this.pickable = pickable;
	}
	
	/**
	 * Tells whether or not this object is tested for intersections with
	 * a pick ray.
	 * @return true if this object is pickable
	 */
	public boolean isPickable() {
		return pickable;
	}

	/**
	 * Set an object that contains application specific information
	 * about this spatial
	 * @param data the data object to set
	 */
	public void setData(Object data) {
		this.dataObject = data;
	}

	/**
	 * @return the data object
	 */
	public Object getData() {
		return dataObject;
	}
	
	/**
	 * @return true if this object has a data object
	 */
	public boolean hasData() {
		return (dataObject != null);
	}
	
	/**
	 * @param material material to set or null to clear
	 */
	public abstract void setMaterial(Material material);
}
