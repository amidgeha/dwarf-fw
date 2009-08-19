/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene.intersection;

import se.ltu.android.demo.scene.Spatial;

/**
 * Contains the result from testing ray intersections against a scenes elements.<br><br>
 * <bold>Note:</bold>Currently it only holds the closest intersecting spatial since there
 * was no need for more at the time.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class PickResult {
	Spatial spat;
	float dist;

	/**
	 * Add an intersecting spatial to the result
	 * @param spatial intersecting spatial
	 * @param distance distance to the intersection point
	 */
	public void add(Spatial spatial, float distance) {
		if(spat == null || distance < this.dist) {
			this.spat = spatial;
			this.dist = distance;
		}
	}
	
	/**
	 * @return the closest spatial
	 */
	public Spatial getClosest() {
		return spat;
	}
	
	/**
	 * @return true if there is at least one result
	 */
	public boolean hasResult() {
		return spat != null;
	}
	
	public String toString() {
		if(spat != null) {
			return "s: "+spat.toString() + ", d: "+dist;
		}
		return "empty";
	}
}
