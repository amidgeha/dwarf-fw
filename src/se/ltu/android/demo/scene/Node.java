/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

import se.ltu.android.demo.intersection.AABBox;
import se.ltu.android.demo.intersection.PickResult;
import se.ltu.android.demo.intersection.Ray;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class Node extends Spatial {
	private final static String TAG = "Node";
	private ArrayList<Spatial> children;
	
	public Node(String name) {
		super(name);
		children = new ArrayList<Spatial>();
	}

	@Override
	public void draw(GL10 gl) {
		int len = children.size();
		Spatial child;
		for (int i = 0; i < len; i++) {
			child = children.get(i);
			child.draw(gl);
		}
	}

	public void attachChild(Spatial child) {
		children.add(child);
		if(child.hasParent()) {
			child.detachFromParent();
		}
		child.parent = this;
	}
	
	public void detachChild(Spatial child) {
		children.remove(child);
		child.parent = null;
	}
	
	public boolean hasChildren() {
		return children.size() > 0;
	}
	
	public ArrayList<Spatial> getChildren() {
		return children;
	}
	
	public void updateBound() {
		int len = children.size();
		Spatial child;
		AABBox cBound;
		
		bound.minX = bound.maxX = 0;
		bound.minY = bound.maxY = 0;
		bound.minZ = bound.maxZ = 0;
		
		for(int i = 0; i < len; i++) {
			child = children.get(i);
			child.updateBound();
			cBound = child.getBound();
			
			if(i == 0) {
				bound.minX = cBound.minX;
				bound.minY = cBound.minY;
				bound.minZ = cBound.minZ;
				bound.maxX = cBound.maxX;
				bound.maxY = cBound.maxY;
				bound.maxZ = cBound.maxZ;
			} else {
				if(cBound.minX < bound.minX)
					bound.minX = cBound.minX;
				if(cBound.minY < bound.minY)
					bound.minY = cBound.minY;
				if(cBound.minZ < bound.minY)
					bound.minZ = cBound.minZ;
				if(cBound.maxX > bound.maxX)
					bound.maxX = cBound.maxX;
				if(cBound.maxY > bound.maxY)
					bound.maxY = cBound.maxY;
				if(cBound.maxZ > bound.maxZ)
					bound.maxZ = cBound.maxZ;
			}
		}
		bound.transform(transM);
	}
	
	/**
	 * Updates the world transform for this node its children
	 */
	@Override
	public void updateTransform() {
		super.updateTransform();
		
		int len = children.size();
		for (int i = 0; i < len; i++) {
			children.get(i).updateTransform();
		}
	}

	@Override
	public void calculatePick(Ray ray, PickResult result) {
		if(result == null) {
			Log.w(TAG, "PickResult is null in "+name);
			return;
		}
		
		if(ray.intersects(bound)) {
			int len = children.size();
			for(int i = 0; i < len; i++) {
				children.get(i).calculatePick(ray, result);
			}
		}
	}
}
