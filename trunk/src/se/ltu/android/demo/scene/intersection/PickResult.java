/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene.intersection;

import java.util.TreeMap;

import se.ltu.android.demo.scene.Spatial;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
// TODO probable memory hog...
public class PickResult {
	TreeMap<Float, Spatial> map;
	
	public PickResult() {
		map = new TreeMap<Float, Spatial>();
	}

	public void add(Spatial spatial, float distance) {
		map.put(distance, spatial);
	}
	
	public Spatial getFirst() {
		return map.get(map.firstKey());
	}

	public int size() {
		return map.size();
	}
	
	public String toString() {
		return map.toString();
	}
}
