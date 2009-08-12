/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene.state;

import javax.microedition.khronos.opengles.GL10;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class GlobalLightState {
	public static final int MAX_LIGHTS = 8;
	private int curLights = 0;
	
	// Private constructor prevents instantiation from other classes
	private GlobalLightState() {
	}

	private static class SingletonHolder {
		private static final GlobalLightState INSTANCE = new GlobalLightState();
	}

	public static GlobalLightState getInstance() {
		return SingletonHolder.INSTANCE;
	}

	/**
	 * Checks how many lights that are currently "checked out"
	 * and returns the next free GL10.GL_LIGHTx constant.
	 * 
	 * @return Returns an integer greater than zero if we have not exceeded
	 * the maximum number of lights. Returns -1 otherwise.
	 */
	protected int checkOut() {
		if(curLights < MAX_LIGHTS) {
			curLights++;
			switch(curLights) {
			case 1:
				return GL10.GL_LIGHT0;
			case 2:
				return GL10.GL_LIGHT1;
			case 3:
				return GL10.GL_LIGHT2;
			case 4:
				return GL10.GL_LIGHT3;
			case 5:
				return GL10.GL_LIGHT4;
			case 6:
				return GL10.GL_LIGHT5;
			case 7:
				return GL10.GL_LIGHT6;
			case 8:
				return GL10.GL_LIGHT7;
			}
		}
		return -1;
	}
	
	/**
	 * Tell the light state that the last given number from
	 * checkOut() is free again.
	 */
	protected void checkIn() {
		curLights--;
	}
}
