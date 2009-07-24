/* SVN FILE: $Id$ */
package se.ltu.android.demo;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import se.ltu.android.demo.intersection.Ray;
import se.ltu.android.demo.scene.Node;
import se.ltu.android.demo.scene.shapes.*;
import se.ltu.android.demo.util.GLColor;
import se.ltu.android.demo.util.GLExtras;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.os.Debug;
import android.util.FloatMath;
import android.util.Log;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class DemoRenderer implements GLSurfaceView.Renderer {
	private Node scene;	
	private final float FOVY = 60.0f;
	private final float ZNEAR = 1.0f;
	private final float ZFAR = 20.0f;
	private final static String TAG = "RENDERER";
	
	// keeps track of current orientation
	float[] orient = {0.f, 0.f, 0.f};
	float[] pos = {5.0f, -5.0f, 0.0f};
	long lastFrame = 0;
	int fps = 0;
	//private float[] projM = new float[16];
	private float[] modelM = new float[16];
	private SensorHandler mSensorHandler;
	private String tmp;
	private int width;
	private int height;
	private float aspect;

	private float pickX = -1;
	private float pickY = -1;
	private Ray pickRay;
	
	public DemoRenderer(SensorHandler handler) {
		mSensorHandler = handler;
		lastFrame = System.currentTimeMillis();
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// Disable default features to increase performance
		gl.glDisable(GL10.GL_DITHER);
		
		// One-time OpenGL initialization based on context...
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                 GL10.GL_NICEST);
		
        gl.glClearColor(0, 0, 0, 1);      
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glEnable(GL10.GL_LIGHTING);
        gl.glEnable(GL10.GL_COLOR_MATERIAL);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glShadeModel(GL10.GL_SMOOTH);
    }

    public void onSurfaceChanged(GL10 gl, int w, int h) {
    	width = w;
    	height = h;
    	aspect = (float) w / h;
    	
    	gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);     
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, FOVY, aspect, ZNEAR, ZFAR);
        //GLExtras.gluPerspective(FOVY, aspect, ZNEAR, ZFAR, projM);
        //gl.glLoadMatrixf(projM, 0);
    }

	public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        
        // setup camera
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        //mSensorHandler.getRotM4(modelM);
        //Matrix.translateM(modelM, 0, -pos[0], -pos[1], -pos[2]);
        Matrix.setIdentityM(modelM, 0);
        Matrix.translateM(modelM, 0, 0, 0, -12);
    	gl.glLoadMatrixf(modelM, 0);
             
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        
        // draw the world
        if(scene != null) {
        	//Debug.startMethodTracing("Draw scene");
        	scene.draw(gl);
        	//Debug.stopMethodTracing();
        }
        long now = System.currentTimeMillis();
        if(now - lastFrame >= 1000l) {
            Log.d(TAG, tmp=fps + " fps");
            fps = 0;
            lastFrame = now;
        } else {
        	fps++;
        }
        
        calcTouchRay(gl);
    }

	/**
	 * Converts screen coordinates to coordinates on the near plane.
	 * Then a picking ray is defined as the line starting at the eye position
	 * and going towards the coordinate at the near plane.
	 */
    private void calcTouchRay(GL10 gl) {
    	// check for a possible value, else return
    	if(pickX == -1) {
    		return;
    	}
		
    	// coordinates centered on the screen 
    	float centered_y = (height - pickY) - height/2;
    	float centered_x = pickX - width/2;
    	// unit coordinates (-1.0f to 1.0f)
    	float unit_x = centered_x/(width/2);
    	float unit_y = centered_y/(height/2);
    	
		float angle = FOVY * GLExtras.DEG_TO_RAD / 2.0f;
		// defined as glFrustrumf(), that is, half the height.
		float near_height = ZNEAR * FloatMath.sin(angle)/FloatMath.cos(angle);
		
		float[] rayPos = {0.0f, 0.0f, 0.0f, 1.0f};
		float[] rayDir = {unit_x * near_height * aspect, unit_y * near_height, -ZNEAR, 0.0f};
		
		// multiply the position and vector with the inverse model matrix
		// to get world coordinates
		float[] invModelM = new float[16];
		Matrix.invertM(invModelM, 0, modelM, 0);
		Matrix.multiplyMV(rayPos, 0, invModelM, 0, rayPos, 0);
		Matrix.multiplyMV(rayDir, 0, invModelM, 0, rayDir, 0);
    	
		pickRay = new Ray(rayPos[0], rayPos[1], rayPos[2], rayDir[0], rayDir[1], rayDir[2]);
		//Log.d(TAG, tmp="Ray pos: "+pickRayPos[0]+", "+pickRayPos[1]+", "+pickRayPos[2]+
		//		" - dir:"+pickRayDir[0]+", "+pickRayDir[1]+", "+pickRayDir[2]);
		
		// set to an impossible value
		pickX = -1;
	}

	public void pick(float x, float y) {
		// set input variables and we will handle them in calcTouchRay
		pickX = x;
		pickY = y;
	}

	public void setScene(Node scene) {
		this.scene = scene;
	}

	// TODO this is really bad... do all rays in thread instead.
	public Ray getPickRay() {
		Ray retRay = pickRay;
		pickRay = null;
		return retRay;
	}
}
