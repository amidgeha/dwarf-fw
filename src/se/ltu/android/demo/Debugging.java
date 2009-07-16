/* SVN FILE: $Id$ */
package se.ltu.android.demo;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public abstract class Debugging {
	
	/**
	 * Stringify an array with floats, for debugging purposes only!
	 */
	public static String stringify(float[] array) {
		return String.format("[%f,%f,%f]", array[0], array[1], array[2]);
	}
}
