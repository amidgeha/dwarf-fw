/* SVN FILE: $Id$ */
package se.ltu.android.demo.camera;

import se.ltu.android.demo.intersection.Ray;
import android.opengl.Matrix;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class Camera {
	public static final float DEG_TO_RAD = 0.01745329238474369f;

	private static final String TAG = "Camera";
	
	// Projection matrix.. keep static so all instances of camera
	// share the same projection
	private static float[] project = {
		1,0,0,0,
		0,1,0,0,
		0,0,1,0,
		0,0,0,1
	};
	
	// keep some variables that are good for calculating a picking ray
	private static float near_height;
	private static float zNear;
	private static float aspect;
	private static float height;
	private static float half_width;
	private static float half_height;
	
	// Model View matrix and instance variables 
	private float[] model = {
		1,0,0,0,
		0,1,0,0,
		0,0,1,0,
		0,0,0,1	
	};
	private float[] invModel = new float[16];
	private float[] position = new float[3];
	
	public Camera() {
		
	}
	
	/**
	 * Set the projection matrix, similar to gluPerspective.
	 * @param fovy Field of view angle in y coordinate
	 * @param width Width of screen
	 * @param height Height of screen
	 * @param zNear Distance to near-plane
	 * @param zFar Distance to far-plane
	 */
	public static void setPerspective(float fovy, float width, float height, float zNear, float zFar) {		
		Camera.near_height = (float) Math.tan((fovy * DEG_TO_RAD) / 2);
		Camera.zNear = zNear;
		Camera.height = height;
		Camera.half_width = width / 2;
		Camera.half_height = height / 2;
		Camera.aspect = width / height;
		project[5] = 1 / Camera.near_height;  // = cot(fovy/2)

		// Remember, column major matrix
		project[0] = project[5] / aspect;
		project[1] = 0.0f;
		project[2] = 0.0f;
		project[3] = 0.0f;

		project[4] = 0.0f;
		//project[5] = 1 / near_height;  // already set
		project[6] = 0.0f;
		project[7] = 0.0f;

		project[8] = 0.0f;
		project[9] = 0.0f;
		project[10] = (zFar + zNear) / (zNear - zFar);
		project[11] = -1.0f;

		project[12] = 0.0f;
		project[13] = 0.0f;
		project[14] = (2 * zFar * zNear) / (zNear - zFar);
		project[15] = 0.0f;
	}
	
	/**
	 * 
	 * @param result
	 * @return 
	 */
	public static float[] getProjectionM() {
		return project;
	}
	
	public void setModelM(float[] m) {
		synchronized(model) {
			for(int i = 0; i < 16; i++) {
				model[i] = m[i];
			}
		}
	}
	
	/**
	 * Sets the cameras rotation matrix. This is similar
	 * to setModelM but it keeps the cameras current position.
	 */
	public void setRotationM(float[] m) {
		synchronized(model) {
			for(int i = 0; i < 16; i++) {
				model[i] = m[i];
			}
			Matrix.translateM(model, 0, -position[0], -position[1], -position[2]);
		}
	}
	
	public float[] getModelM() {
		synchronized(model) {
			return model;
		}
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
	 */
	public void lookAt(
    	float eyex, float eyey, float eyez,
    	float centerx, float centery, float centerz,
    	float upx, float upy, float upz) {
    
    	float[] x = new float[3]; 
    	float[] y = new float[3];
    	float[] z = new float[3];
    	float mag;

    	// Make rotation matrix
    	  
    	// Z vector
    	z[0] = eyex - centerx;
    	z[1] = eyey - centery;
    	z[2] = eyez - centerz;
    	
    	mag = Matrix.length(z[0], z[1], z[2]);
    	if (mag > 0) {			// mpichler, 19950515
    		mag = 1/mag;
    		z[0] *= mag;
    		z[1] *= mag;
    		z[2] *= mag;
    	}
    	
    	// Y vector
    	y[0] = upx;
    	y[1] = upy;
    	y[2] = upz;

    	// X vector = Y cross Z    	
    	x[0] = y[1] * z[2] - y[2] * z[1];
    	x[1] = -y[0] * z[2] + y[2] * z[0];
    	x[2] = y[0] * z[1] - y[1] * z[0];
    	
    	// Recompute Y = Z cross X    	
    	y[0] = z[1] * x[2] - z[2] * x[1];
    	y[1] = -z[0] * x[2] + z[2] * x[0];
    	y[2] = z[0] * x[1] - z[1] * x[0];
    	
    	// mpichler, 19950515
    	
    	// cross product gives area of parallelogram, which is < 1.0 for
    	// non-perpendicular unit-length vectors; so normalize x, y here

    	mag = Matrix.length(x[0], x[1], x[2]);
    	if (mag > 0) {
    		mag = 1/mag;
    		x[0] *= mag;
    		x[1] *= mag;
    		x[2] *= mag;
    	}

    	mag = Matrix.length(y[0], y[1], y[2]);
    	if (mag > 0) {
    		mag = 1/mag;
    		y[0] *= mag;
    		y[1] *= mag;
    		y[2] *= mag;
    	}

    	synchronized(model) {
	    	model[0] = x[0];
	    	model[4] = x[1];
	    	model[8] = x[2];
	    	model[12] = 0.0f;
	    	model[1] = y[0];
	    	model[5] = y[1];
	    	model[9] = y[2];
	    	model[13] = 0.0f;
	    	model[2] = z[0];
	    	model[6] = z[1];
	    	model[10] = z[2];
	    	model[14] = 0.0f;
	    	model[3] = 0.0f;
	    	model[7] = 0.0f;
	    	model[11] = 0.0f;
	    	model[15] = 1.0f;
	    	
	    	//Matrix.multiplyMM(model, 0, m, 0, model, 0);
	    	// Translate Eye to Origin 
	    	position[0] = eyex;
	    	position[1] = eyey;
	    	position[2] = eyez;
	    	Matrix.translateM(model, 0, -position[0], -position[1], -position[2]);
    	}
    }

	/**
	 * Translate the cameras position with the given coordinates
	 * @param x translation x coordinate
	 * @param y translation y coordinate
	 * @param z translation z coordinate
	 */
	public void translate(float x, float y, float z) {
		synchronized(model) {
			position[0] += x;
			position[1] += y;
			position[2] += z;
			Matrix.translateM(model, 0, -x, -y, -z);
		}
	}
	
	/**
	 * Translate the cameras position with the given coordinates
	 * @param vector3f an array of size three, containing x,y and z coordinates
	 */
	public void translate(float[] vector3f) {
		if(vector3f == null || vector3f.length != 3) {
			return;
		}
		synchronized(model) {
			position[0] += vector3f[0];
			position[1] += vector3f[1];
			position[2] += vector3f[2];
			Matrix.translateM(model, 0, -vector3f[0], -vector3f[1], -vector3f[2]);
		}
	}
	
	/**
	 * Set the cameras model view matrix to the identity matrix
	 */
	public void setIdentity() {
		synchronized(model) {
			Matrix.setIdentityM(model, 0);
			position[0] = 0;
			position[1] = 0;
			position[2] = 0;
		}
	}
	
	/**
	 * Set the absolute position of this camera
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param z z coordinate
	 */
	public void setPosition(float x, float y, float z) {
		synchronized(model) {
			// revert last position
			Matrix.translateM(model, 0, position[0], position[1], position[2]);
			// set new position
			position[0] = x;
	    	position[1] = y;
	    	position[2] = z;
	    	Matrix.translateM(model, 0, -position[0], -position[1], -position[2]);
		}
	}
	
	/**
	 * Calculates a pick ray based on the given screen coordinates and
	 * the current projection matrix and model view matrix.
	 * 
	 * The screen coordinates are expected to have (0,0) at the upper left 
	 * part of the screen and the y-axis is reversed compared to the OpenGL y-axis. 
	 * @param pickX screen x coordinate
	 * @param pickY screen y coordinate
	 */
    public Ray calculatePickRay(float pickX, float pickY) {
    	// coordinates centered on the screen
    	// -1 <= x <= 1 and -1 <= y <= 1
    	float unit_x = (pickX - half_width)/half_width;
    	float unit_y = ((height - pickY) - half_height)/half_height;
    	
    	//Log.d(TAG, "Pick: ("+pickX+", "+pickY+") - Unit: ("+unit_x+", "+unit_y+")");
		
		float[] rayRawPos = {0.0f, 0.0f, 0.0f, 1.0f};
		float[] rayRawDir = {unit_x * near_height * aspect, unit_y * near_height, -zNear, 0.0f};
		float[] rayPos = new float[4];
		float[] rayDir = new float[4];
		
		// multiply the position and vector with the inverse model matrix
		// to get world coordinates
		synchronized(model) {
			Matrix.invertM(invModel, 0, model, 0);
		}
		Matrix.multiplyMV(rayPos, 0, invModel, 0, rayRawPos, 0);
		Matrix.multiplyMV(rayDir, 0, invModel, 0, rayRawDir, 0);

		//Log.d(TAG, tmp="  Raw Ray pos: ("+rayRawPos[0]+", "+rayRawPos[1]+", "+rayRawPos[2]+") - dir: ("+rayRawDir[0]+", "+rayRawDir[1]+", "+rayRawDir[2]+")");
		//Log.d(TAG, tmp="World Ray pos: ("+rayPos[0]+", "+rayPos[1]+", "+rayPos[2]+") - dir: ("+rayDir[0]+", "+rayDir[1]+", "+rayDir[2]+")");
		return new Ray(rayPos[0], rayPos[1], rayPos[2], rayDir[0], rayDir[1], rayDir[2]);
	}
}
