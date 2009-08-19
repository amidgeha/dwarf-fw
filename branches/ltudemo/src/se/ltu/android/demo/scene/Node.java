/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

import se.ltu.android.demo.scene.intersection.AABBox;
import se.ltu.android.demo.scene.intersection.PickResult;
import se.ltu.android.demo.scene.intersection.Ray;
import se.ltu.android.demo.scene.state.Material;

/**
 * A spatial that supports any number of children attached to it. Most of
 * Spatials methods are implemented so that they affect this object and then
 * its children.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class Node extends Spatial {
	private final static String TAG = "Node";
	protected ArrayList<Spatial> children = new ArrayList<Spatial>();
	
	public Node(String name) {
		super(name);
	}

	@Override
	public void draw(GL10 gl) {
		synchronized(children) {
			int len = children.size();
			Spatial child;
			for (int i = 0; i < len; i++) {
				child = children.get(i);
				child.draw(gl);
			}
		}
	}

	/**
	 * Add a child to this node.
	 * @param child child to add
	 */
	public void attachChild(Spatial child) {
		synchronized(children) {
			children.add(child);
		}
		if(child.hasParent()) {
			child.detachFromParent();
		}
		child.parent = this;
	}
	
	/**
	 * Removes a child from this node
	 * @param child child to remove
	 */
	public void detachChild(Spatial child) {
		synchronized(children) {
			children.remove(child);
		}
		child.parent = null;
	}
	
	/**
	 * @return true if this node has at least one child
	 */
	public boolean hasChildren() {
		synchronized(children) {
			return children.size() > 0;
		}
	}
	
	/**
	 * @return the list of children attached to this node
	 */
	public ArrayList<Spatial> getChildren() {
		synchronized(children) {
			return children;
		}
	}
	
	/**
	 * Updates the model bound for all children of this node.
	 */
	@Override
	public void updateModelBound() {
		synchronized(children) {
			int len = children.size();	
			for(int i = 0; i < len; i++) {
				children.get(i).updateModelBound();;
			}
		}
	}
	
	/**
	 * Updates the world bound of this node and its children.
	 * @param propagate set to true if we want to propagate the
	 * changes up to the root
	 */
	@Override
	public void updateWorldBound(boolean propagate) {
		synchronized(children) {
			int len = children.size();
			Spatial child;
			AABBox cBound;
			
			worldBound.minX = worldBound.maxX = 0;
			worldBound.minY = worldBound.maxY = 0;
			worldBound.minZ = worldBound.maxZ = 0;
			
			for(int i = 0; i < len; i++) {
				child = children.get(i);
				child.updateWorldBound(false);
				cBound = child.getWorldBound();
				
				if(i == 0) {
					worldBound.minX = cBound.minX;
					worldBound.minY = cBound.minY;
					worldBound.minZ = cBound.minZ;
					worldBound.maxX = cBound.maxX;
					worldBound.maxY = cBound.maxY;
					worldBound.maxZ = cBound.maxZ;
				} else {
					if(cBound.minX < worldBound.minX)
						worldBound.minX = cBound.minX;
					if(cBound.minY < worldBound.minY)
						worldBound.minY = cBound.minY;
					if(cBound.minZ < worldBound.minY)
						worldBound.minZ = cBound.minZ;
					if(cBound.maxX > worldBound.maxX)
						worldBound.maxX = cBound.maxX;
					if(cBound.maxY > worldBound.maxY)
						worldBound.maxY = cBound.maxY;
					if(cBound.maxZ > worldBound.maxZ)
						worldBound.maxZ = cBound.maxZ;
				}
			}
		}
		//worldBound.transform(transM);
		
		if(propagate && parent != null) {
			parent.updateWorldBound(this);
		}
	}
	
	/**
	 * Updates the world bound for this node based on the
	 * world bound of the calling child and propagate the changes up 
	 * to the root
	 * @param child the spatial which world bound has changed
	 */
	protected void updateWorldBound(Spatial child) {
		AABBox cBound = child.getWorldBound();
		if(cBound.minX < worldBound.minX)
			worldBound.minX = cBound.minX;
		if(cBound.minY < worldBound.minY)
			worldBound.minY = cBound.minY;
		if(cBound.minZ < worldBound.minY)
			worldBound.minZ = cBound.minZ;
		if(cBound.maxX > worldBound.maxX)
			worldBound.maxX = cBound.maxX;
		if(cBound.maxY > worldBound.maxY)
			worldBound.maxY = cBound.maxY;
		if(cBound.maxZ > worldBound.maxZ)
			worldBound.maxZ = cBound.maxZ;
		
		if(parent != null) {
			parent.updateWorldBound(this);
		}
	}
	
	/**
	 * Updates the world transform for this node and its children
	 */
	@Override
	public void updateTransform() {
		super.updateTransform();
		
		synchronized (children) {
			int len = children.size();
			for (int i = 0; i < len; i++) {
				children.get(i).updateTransform();
			}
		}
	}

	@Override
	public void calculatePick(Ray ray, PickResult result) {
		if(result == null) {
			Log.w(TAG, "PickResult is null in "+name);
			return;
		}
		
		if(ray.intersects(worldBound)) {
			synchronized (children) {
				int len = children.size();
				if(pickable) {
					for(int i = 0; i < len; i++) {
						children.get(i).calculatePick(ray, result);
					}
				}
			}
		}
	}
	
	@Override
	public void update(long tpf) {
		super.update(tpf);
		synchronized (children) {
			int len = children.size();
			for (int i = 0; i < len; i++) {
				children.get(i).update(tpf);
			}
		}
	}

	@Override
	public void freeHardwareBuffers(GL10 gl) {
		synchronized (children) {
			int len = children.size();
			for (int i = 0; i < len; i++) {
				children.get(i).freeHardwareBuffers(gl);
			}
		}
	}
	

	@Override
	public void forgetHardwareBuffers() {
		synchronized (children) {
			int len = children.size();
			for (int i = 0; i < len; i++) {
				children.get(i).forgetHardwareBuffers();
			}
		}
	}

	@Override
	public void generateHardwareBuffers(GL10 gl) {
		synchronized (children) {
			int len = children.size();
			for (int i = 0; i < len; i++) {
				children.get(i).generateHardwareBuffers(gl);
			}
		}
	}

	/**
	 * Set the material for all children of this node
	 * @param material material to set
	 */
	@Override
	public void setMaterial(Material material) {
		synchronized (children) {
			int len = children.size();
			for (int i = 0; i < len; i++) {
				children.get(i).setMaterial(material);
			}
		}
	}
}
