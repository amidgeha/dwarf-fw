/* SVN FILE: $Id$ */
package se.ltu.android.demo;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.opengles.GL10;

import se.ltu.android.demo.camera.Camera;
import se.ltu.android.demo.scene.Node;

import android.util.Log;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class DemoRenderer implements GLSurfaceView.Renderer {
	private final static String TAG = "RENDERER";
	private Node scene;	
	private final float FOVY = 60.0f;
	private final float ZNEAR = 1.0f;
	private final float ZFAR = 20.0f;
	
	long lastFrame = 0;
	int fps = 0;
	private boolean use_vbos = false;
	private Camera camera;
	
	public DemoRenderer() {
		lastFrame = System.currentTimeMillis();
		camera = new Camera();	// need a default one if one is not set for us
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
        
        if(scene != null) {
        	if(use_vbos) {
    			// FIXME messy...
    			scene.forgetHardwareBuffers();
    			scene.generateHardwareBuffers(gl);
    		}
        }
        
    }

    public void sizeChanged(GL10 gl, int w, int h) {   	
    	gl.glViewport(0, 0, w, h);
    	Camera.setPerspective(FOVY, w, h, ZNEAR, ZFAR);
        gl.glMatrixMode(GL10.GL_PROJECTION); 
        gl.glLoadMatrixf(Camera.getProjectionM(), 0);
    }
    
    public void shutdown(GL10 gl) {
		if (scene != null && use_vbos) {
			scene.freeHardwareBuffers(gl);
        }
    }

	public void drawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        
        // setup camera
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        synchronized(camera) {
        	gl.glLoadMatrixf(camera.getModelM(), 0);
        }
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        
        // draw the world
        if(scene != null) {
        	if(use_vbos) {
        		// creates hardware buffers for objects
        		// that do not already have any
    			scene.generateHardwareBuffers(gl);
    		}
        	scene.draw(gl);
        }
        long now = System.currentTimeMillis();
        if(now - lastFrame >= 1000l) {
            Log.d(TAG, fps + " fps");
            fps = 0;
            lastFrame = now;
        } else {
        	fps++;
        }
    }

	public void setScene(Node scene) {
		this.scene = scene;
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

	/**
	 * @param camera
	 */
	public void setCamera(Camera camera) {
		synchronized(camera) {
			this.camera = camera;
		}
	}
}
