/* SVN FILE: $Id$ */
package se.ltu.android.demo.util;

import javax.microedition.khronos.opengles.GL10;

import android.util.FloatMath;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class GLExtras {
	public static final float DEG_TO_RAD = 0.01745329238474369f;
	
	
    /**
     * We create our own function to generate the projection matrix. The operations
     * are (roughly) the same but we have access to the matrix locally instead of
     * multiplying it to the current matrix.
     * 
     * http://pyopengl.sourceforge.net/documentation/manual/gluPerspective.3G.html
     * @see android.opengl.GLU#gluPerspective(GL10, float, float, float, float)
     */
    public static void gluPerspective(float fovy, float aspect, float zNear, 
    		float zFar, float[] result) {
    	// Temporary variables
    	result[6] = (fovy * DEG_TO_RAD)/2;
    	result[7] = FloatMath.cos(result[6]) / FloatMath.sin(result[6]); // f = cot(fovy/2)
		
		// Remember, column major matrix
    	result[0] = result[7] / aspect;
    	result[1] = 0.0f;
    	result[2] = 0.0f;
    	result[3] = 0.0f;
		
    	result[4] = 0.0f;
    	result[5] = result[7];
    	result[6] = 0.0f;
    	result[7] = 0.0f;
		
    	result[8] = 0.0f;
    	result[9] = 0.0f;
    	result[10] = (zFar+zNear)/(zNear-zFar);
    	result[11] = -1.0f;
		
    	result[12] = 0.0f;
    	result[13] = 0.0f;
    	result[14] = (2*zFar*zNear)/(zNear-zFar);
    	result[15] = 0.0f;
	}
}
