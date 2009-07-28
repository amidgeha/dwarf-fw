/* SVN FILE: $Id$ */
package se.ltu.android.demo.util;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.Matrix;
import android.util.FloatMath;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class GLExtras {
	public static final float DEG_TO_RAD = 0.01745329238474369f;

	/**
	 * We create our own function to generate the projection matrix. The
	 * operations are (roughly) the same but we have access to the matrix
	 * locally instead of multiplying it to the current matrix.
	 * 
	 * http://pyopengl.sourceforge.net/documentation/manual/gluPerspective.3G.
	 * html
	 * 
	 * @see android.opengl.GLU#gluPerspective(GL10, float, float, float, float)
	 */
	public static void gluPerspective(float fovy, float aspect, float zNear,
			float zFar, float[] result) {
		// Temporary variables
		result[6] = (fovy * DEG_TO_RAD) / 2;
		result[7] = FloatMath.cos(result[6]) / FloatMath.sin(result[6]); // f =
																			// cot(fovy/2)

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
		result[10] = (zFar + zNear) / (zNear - zFar);
		result[11] = -1.0f;

		result[12] = 0.0f;
		result[13] = 0.0f;
		result[14] = (2 * zFar * zNear) / (zNear - zFar);
		result[15] = 0.0f;
	}

	/**
	 * Define a viewing transformation in terms of an eye point, a center of view, and an up vector.
	 * @param eyex eye x coordinate
	 * @param eyey eye y coordinate
	 * @param eyez eye z coordinate
	 * @param centerx view center x coordinate
	 * @param centery view center y coordinate
	 * @param centerz view center z coordinate
	 * @param upx up vector x coordinate
	 * @param upy up vector y coordinate
	 * @param upz up vector z coordinate
	 * @param result resulting model view matrix
	 */
	public static void gluLookAt(
    		float eyex, float eyey, float eyez,
    		float centerx, float centery, float centerz,
    		float upx, float upy, float upz, float[] result) {
    
	    float[] m = new float[16];
    	float[] x = new float[3]; 
    	float[] y = new float[3];
    	float[] z = new float[3];
    	float mag;

    	/* Make rotation matrix */

    	  
    	/* Z vector */
    	   
    	z[0] = eyex - centerx;
    	z[1] = eyey - centery;
    	z[2] = eyez - centerz;
    	
    	mag = FloatMath.sqrt(z[0] * z[0] + z[1] * z[1] + z[2] * z[2]);
    	if (mag > 0) {			/* mpichler, 19950515 */
    		mag = 1/mag;
    		z[0] *= mag;
    		z[1] *= mag;
    		z[2] *= mag;
    	}
    	
    	/* Y vector */
    	y[0] = upx;
    	y[1] = upy;
    	y[2] = upz;

    	/* X vector = Y cross Z */    	
    	x[0] = y[1] * z[2] - y[2] * z[1];
    	x[1] = -y[0] * z[2] + y[2] * z[0];
    	x[2] = y[0] * z[1] - y[1] * z[0];
    	
    	/* Recompute Y = Z cross X */    	
    	y[0] = z[1] * x[2] - z[2] * x[1];
    	y[1] = -z[0] * x[2] + z[2] * x[0];
    	y[2] = z[0] * x[1] - z[1] * x[0];
    	
    	/* mpichler, 19950515 */
    	
    	/* 
    	 * cross product gives area of parallelogram, which is < 1.0 for
    	 * non-perpendicular unit-length vectors; so normalize x, y here
    	 */

    	mag = FloatMath.sqrt(x[0] * x[0] + x[1] * x[1] + x[2] * x[2]);
    	   
    	if (mag > 0) {
    		mag = 1/mag;
    		x[0] *= mag;
    		x[1] *= mag;
    		x[2] *= mag;
    	}

    	mag = FloatMath.sqrt(y[0] * y[0] + y[1] * y[1] + y[2] * y[2]);
    	if (mag > 0) {
    		mag = 1/mag;
    		y[0] *= mag;
    		y[1] *= mag;
    		y[2] *= mag;
    	}

    	m[0] = x[0];
    	m[4] = x[1];
    	m[8] = x[2];
    	m[12] = 0.0f;
    	m[1] = y[0];
    	m[5] = y[1];
    	m[9] = y[2];
    	m[13] = 0.0f;
    	m[2] = z[0];
    	m[6] = z[1];
    	m[10] = z[2];
    	m[14] = 0.0f;
    	m[3] = 0.0f;
    	m[7] = 0.0f;
    	m[11] = 0.0f;
    	m[15] = 1.0f;
    	
    	/*
    	define M(row,col)  m[col*4+row]
    	M(0, 0) = x[0];
    	M(0, 1) = x[1];
    	M(0, 2) = x[2];
    	M(0, 3) = 0.0;
    	M(1, 0) = y[0];
    	M(1, 1) = y[1];
    	M(1, 2) = y[2];
    	M(1, 3) = 0.0;
    	M(2, 0) = z[0];
    	M(2, 1) = z[1];
    	M(2, 2) = z[2];
    	M(2, 3) = 0.0;
    	M(3, 0) = 0.0;
    	M(3, 1) = 0.0;
    	M(3, 2) = 0.0;
    	M(3, 3) = 1.0;
    	undef M
    	*/
    	
    	Matrix.multiplyMM(result, 0, m, 0, result, 0);
    	Matrix.translateM(result, 0, -eyex, -eyey, -eyez);
    	//glMultMatrixd(m);

    	/* Translate Eye to Origin */
    	//glTranslated(-eyex, -eyey, -eyez);
    }
}
