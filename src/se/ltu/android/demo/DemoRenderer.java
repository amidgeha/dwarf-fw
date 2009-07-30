/* SVN FILE: $Id$ */
package se.ltu.android.demo;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import se.ltu.android.demo.intersection.Ray;
import se.ltu.android.demo.scene.Node;
import se.ltu.android.demo.scene.shapes.*;
import se.ltu.android.demo.util.BufferUtils;
import se.ltu.android.demo.util.GLColor;
import se.ltu.android.demo.util.GLExtras;

import android.opengl.GLU;
import android.opengl.Matrix;
import android.os.Debug;
import android.util.FloatMath;
import android.util.Log;

/**
 * @author �ke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class DemoRenderer implements GLSurfaceView.Renderer {
	private Node scene;	
	private final float FOVY = 60.0f;
	private final float ZNEAR = 1.0f;
	private final float ZFAR = 20.0f;
	private final static String TAG = "RENDERER";
	private static final int CAMERA_1 = 0;
	private static final int CAMERA_2 = 1;
	private static final int CAMERA_3 = 2;
	
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
	private int cam_mode = CAMERA_1;
	private boolean use_vbos = false;
	private boolean has_vbos = false;
	private Object pickLock = new Object();
	private float[] pickLine;
	
	public DemoRenderer(SensorHandler handler) {
		mSensorHandler = handler;
		lastFrame = System.currentTimeMillis();
	}

	public void surfaceCreated(GL10 gl) {
		// Disable default features to increase performance
		gl.glDisable(GL10.GL_DITHER);
		
		// One-time OpenGL initialization based on context...
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                 GL10.GL_NICEST);
		
        gl.glClearColor(0, 0, 0, 1);      
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glCullFace(GL10.GL_BACK);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glEnable(GL10.GL_LIGHTING);
        gl.glEnable(GL10.GL_COLOR_MATERIAL);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glShadeModel(GL10.GL_SMOOTH);
    }

    public void sizeChanged(GL10 gl, int w, int h) {
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
    
    public void shutdown(GL10 gl) {
		if (use_vbos) {
			//scene.freeHardwareBuffers(gl);
        }
    }

	public void drawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        
        // setup camera
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        Matrix.setIdentityM(modelM, 0);
        if(cam_mode == CAMERA_1) {
        	pos[0] = 0;
        	pos[1] = 0;
        	pos[2] = 9;
        	Matrix.translateM(modelM, 0, -pos[0], -pos[1], -pos[2]);
        }
        else if(cam_mode == CAMERA_2) {
        	pos[0] = 5;
        	pos[1] = -5;
        	pos[2] = 3;
        	GLExtras.gluLookAt(pos[0], pos[1], pos[2], 0, 0, -2.9f, 0, 0, 1, modelM);
        	//Matrix.translateM(modelM, 0, 0, 0, -12);
        }
        else {
        	pos[0] = 6;
        	pos[1] = 0;
        	pos[2] = 6;
        	GLExtras.gluLookAt(pos[0], pos[1], pos[2], 0, 0, -2.9f, 0, 0, 1, modelM);
        }
        gl.glLoadMatrixf(modelM, 0);
        /*
        if(cam_mode == CAMERA_LOCKED) {
        	gl.glLoadMatrixf(modelM, 0);
        } else {
        	mSensorHandler.getRotM4(modelM);
            //Matrix.translateM(modelM, 0, -pos[0], -pos[1], -pos[2]);
        }*/
             
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        
        // draw the world
        if(scene != null) {
        	if(use_vbos && !has_vbos) {
    			// FIXME messy...
    			has_vbos = true;
    			scene.generateHardwareBuffers(gl);
    		}
        	//Debug.startMethodTracing("Draw scene");
        	scene.draw(gl);
        	if(pickLine != null) {
	        	FloatBuffer line = BufferUtils.createFloatBuffer(6);
	        	line.rewind();
	        	line.put(pickLine);
	        	line.rewind();
	        	gl.glVertexPointer(3, GL10.GL_FLOAT, 0, line);
	        	FloatBuffer lineColor = BufferUtils.createFloatBuffer(8);
	        	lineColor.rewind();
	        	lineColor.put(new float[]{1,1,1,1,1,1,1,1});
	        	lineColor.rewind();
	        	gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
	        	gl.glColorPointer(4, GL10.GL_FLOAT, 0, lineColor);
	        	CharBuffer indices = BufferUtils.createCharBuffer(2);
	        	indices.rewind();
	        	indices.put(new char[]{0,1});
	        	indices.rewind();
	        	gl.glLineWidth(5.0f);
	        	gl.glDrawElements(GL10.GL_LINES, 2, GL10.GL_UNSIGNED_SHORT, indices);
	        	gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        	}
        	//Debug.stopMethodTracing();
        }
        /*
        long now = System.currentTimeMillis();
        if(now - lastFrame >= 1000l) {
            Log.d(TAG, tmp=fps + " fps");
            fps = 0;
            lastFrame = now;
        } else {
        	fps++;
        }
        */
        
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
    	
    	//Log.d(TAG, "Pick: ("+pickX+", "+pickY+") - Unit: ("+unit_x+", "+unit_y+")");
    	
		float angle = FOVY * GLExtras.DEG_TO_RAD / 2.0f;
		// defined as glFrustrumf(), that is, half the height.
		float near_height = (float)(ZNEAR * Math.tan(angle)); // FloatMath.sin(angle)/FloatMath.cos(angle);
		
		float[] rayRawPos = {0.0f, 0.0f, 0.0f, 1.0f};
		float[] rayRawDir = {unit_x * near_height * aspect, unit_y * near_height, -ZNEAR, 0.0f};
		float[] rayPos = new float[4];
		float[] rayDir = new float[4];
		
		// multiply the position and vector with the inverse model matrix
		// to get world coordinates
		float[] invModelM = new float[16];
		Matrix.invertM(invModelM, 0, modelM, 0);
		Matrix.multiplyMV(rayPos, 0, invModelM, 0, rayRawPos, 0);
		Matrix.multiplyMV(rayDir, 0, invModelM, 0, rayRawDir, 0);
    	/*float invlen = 1/Matrix.length(rayDir[0], rayDir[1], rayDir[2]);
    	rayDir[0] *= invlen;
    	rayDir[1] *= invlen;
    	rayDir[2] *= invlen;*/
		Log.d(TAG, tmp="  Raw Ray pos: ("+rayRawPos[0]+", "+rayRawPos[1]+", "+rayRawPos[2]+") - dir: ("+rayRawDir[0]+", "+rayRawDir[1]+", "+rayRawDir[2]+")");
		Log.d(TAG, tmp="World Ray pos: ("+rayPos[0]+", "+rayPos[1]+", "+rayPos[2]+") - dir: ("+rayDir[0]+", "+rayDir[1]+", "+rayDir[2]+")");
		pickLine = new float[]{rayPos[0],rayPos[1],rayPos[2],rayPos[0]+20*rayDir[0],rayPos[1]+20*rayDir[1],rayPos[2]+20*rayDir[2]};
		Log.d(TAG, "Line: ("+pickLine[0]+", "+pickLine[1]+", "+pickLine[2]+") -> ("+pickLine[3]+", "+pickLine[4]+", "+pickLine[5]+")");
		synchronized(pickLock) {
			pickRay = new Ray(rayPos[0], rayPos[1], rayPos[2], rayDir[0], rayDir[1], rayDir[2]);
		}
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
		synchronized(pickLock) {
			Ray retRay = pickRay;
			pickRay = null;
			return retRay;
		}
	}

	/**
	 * 
	 */
	public void changeCamera() {
		if(cam_mode == CAMERA_1) {
			cam_mode = CAMERA_2;
		} else if(cam_mode == CAMERA_2) {
			cam_mode = CAMERA_3;
		} else {
			cam_mode = CAMERA_1;
		}
	}
	
	public void useVBOs(boolean value) {
		use_vbos = true;
	}

	/* (non-Javadoc)
	 * @see se.ltu.android.demo.GLSurfaceView.Renderer#getConfigSpec()
	 */
	@Override
	public int[] getConfigSpec() {
		int[] configSpec = {
                EGL10.EGL_RED_SIZE,      8,
                EGL10.EGL_GREEN_SIZE,    8,
                EGL10.EGL_BLUE_SIZE,     8,
                EGL10.EGL_ALPHA_SIZE,    8,
                EGL10.EGL_DEPTH_SIZE,   16,
                EGL10.EGL_NONE
        };
        return configSpec;
	}
}