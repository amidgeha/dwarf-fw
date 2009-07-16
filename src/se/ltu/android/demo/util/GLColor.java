/* SVN FILE: $Id$ */
package se.ltu.android.demo.util;

/**
 * Provides some predefined colors in RGBa format which you can use
 * when setting the color on a scene element.
 * 
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public abstract class GLColor {
	public static final float[] BLACK = {0.0f,0.0f,0.0f,1.0f};
	public static final float[] DARK_GREY = {0.2f,0.2f,0.2f,1.0f};
	public static final float[] GREY = {0.5f,0.5f,0.5f,1.0f};
	public static final float[] LIGHT_GREY = {0.8f,0.8f,0.8f,1.0f};
	public static final float[] WHITE = {1.0f,1.0f,1.0f,1.0f};
	
	public static final float[] RED = {1.0f,0.0f,0.0f,1.0f};
	public static final float[] GREEN = {0.0f,1.0f,0.0f,1.0f};
	public static final float[] BLUE = {0.0f,0.0f,1.0f,1.0f};
	
	public static final float[] YELLOW = {1.0f,1.0f,0.0f,1.0f};
	public static final float[] MAGENTA = {1.0f,0.0f,1.0f,1.0f};
	public static final float[] CYAN = {0.0f,1.0f,1.0f,1.0f};
	
	// The bastard color compared to the above.. orange!!
	public static final float[] ORANGE = {1.0f,0.5f,0.0f,1.0f};
}
