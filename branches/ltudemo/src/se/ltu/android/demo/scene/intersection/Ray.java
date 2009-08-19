/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene.intersection;

/**
 * A basic ray with intersection test. 
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class Ray {	
	private float x, y, z;		// ray origin	
	private float i, j, k;		// ray direction	
	private float ii, ij, ik;	// inverses of direction components
	private boolean sgn_ii, sgn_ij, sgn_ik;
	
	/**
	 * Constructs a new ray
	 * @param x origin x-coordinate
	 * @param y origin y-coordinate
	 * @param z origin z-coordinate
	 * @param i direction x-coordinate
	 * @param j direction y-coordinate
	 * @param k direction z-coordinate
	 */
	public Ray(float x, float y, float z, float i, float j, float k) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.i = i;
		this.j = j;
		this.k = k;
		
		// inverses of direction component
		this.ii = 1.0f/this.i;
		this.ij = 1.0f/this.j;
		this.ik = 1.0f/this.k;
		this.sgn_ii = (ii >= 0);
		this.sgn_ij = (ij >= 0);
		this.sgn_ik = (ik >= 0);
	} // public Ray(float x, float y, float z, float i, float j, float k)
	
	/**
	 * Calculates whether or not the ray intersects an axis-aligned bounding box.
	 * Same as calling <code>intersects(box, null)</code>
	 * @param box an axis-aligned bounding box
	 * @return true if this ray intersects the box
	 */
	public boolean intersects(AABBox box){
		return intersects(box, null);
	}
	
	/**
	 * Calculates whether or not the ray intersects an axis-aligned bounding box.
	 * @param box an axis-aligned bounding box
	 * @param distance the resulting distance from the origin of this ray to the 
	 * intersection point of the box, only valid if this method returns true.
	 * @return true if this ray intersects the box
	 */
	public boolean intersects(AABBox box, float[] distance) {
		float tmin, tmax, tymin, tymax, tzmin, tzmax;
		float t0 = Float.NEGATIVE_INFINITY;
		float t1 = Float.POSITIVE_INFINITY;
		
		if (sgn_ii) {
			tmin = (box.minX - x) * ii;
			tmax = (box.maxX - x) * ii;
		}
		else {
			tmin = (box.maxX - x) * ii;
			tmax = (box.minX - x) * ii;
		}
		if (sgn_ij) {
			tymin = (box.minY - y) * ij;
			tymax = (box.maxY - y) * ij;
		}
		else {
			tymin = (box.maxY - y) * ij;
			tymax = (box.minY - y) * ij;
		}
		if ( (tmin > tymax) || (tymin > tmax) ) {
			return false;
		}
		if (tymin > tmin) {
			tmin = tymin;
		}
		if (tymax < tmax) {
			tmax = tymax;
		}
		
		if (sgn_ik) {
			tzmin = (box.minZ - z) * ik;
			tzmax = (box.maxZ - z) * ik;
		}
		else {
			tzmin = (box.maxZ - z) * ik;
			tzmax = (box.minZ - z) * ik;
		}
	  if ( (tmin > tzmax) || (tzmin > tmax) ) {
	    return false;
	  }
	  if (tzmin > tmin) {
	    tmin = tzmin;
	  }
	  if (tzmax < tmax) {
	    tmax = tzmax;
	  }
	  
	  if(tmin < t1 && tmax > t0) {
		  if(distance != null && distance.length > 0) {
			  distance[0] = tmin;
		  }
		  return true;
	  }
	  return false;
	}
}
