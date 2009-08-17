/* SVN FILE: $Id$ */
package se.ltu.android.demo;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.opengles.GL10;

import se.ltu.android.demo.scene.Node;
import se.ltu.android.demo.scene.camera.Camera;

import android.util.Log;

/**
 * Our application specific implementation of GLSurfaceView.Renderer. It holds
 * the GL-context and does nothing but
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class DemoRenderer implements GLSurfaceView.Renderer {
	private final static String TAG = "RENDERER";
	private Node scene;	
	private final float FOVY = 60.0f;
	private final float ZNEAR = 0.1f;
	private final float ZFAR = 20.0f;
	
	long lastFrame = 0;
	int fps = 0;
	private boolean use_vbos = false;
	private Camera camera;
	
	public DemoRenderer() {
		lastFrame = System.currentTimeMillis();
		camera = new Camera();	// need a default one if one is not set for us
	}

	@Override
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
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glShadeModel(GL10.GL_SMOOTH);
        
        if(scene != null) {
        	if(use_vbos) {
    			// TODO messy...
    			scene.forgetHardwareBuffers();
    			scene.generateHardwareBuffers(gl);
    		}
        }
        
    }

	@Override
    public void sizeChanged(GL10 gl, int w, int h) {   	
    	gl.glViewport(0, 0, w, h);
    	Camera.setPerspective(FOVY, w, h, ZNEAR, ZFAR);
        gl.glMatrixMode(GL10.GL_PROJECTION); 
        gl.glLoadMatrixf(Camera.getProjectionM(), 0);
    }
    
	@Override
    public void shutdown(GL10 gl) {
		if (scene != null && use_vbos) {
			scene.freeHardwareBuffers(gl);
        }
    }

	@Override
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
        //printFPS();
    }

	/**
	 * Set the scene to render
	 * @param scene a node representing the scene
	 */
	public void setScene(Node scene) {
		this.scene = scene;
	}
	
	/**
	 * Tell the renderer to use Vertex Buffer Objects instead
	 * of java.nio.Buffers.
	 * @param value true if we want to use VBOs
	 */
	public void useVBOs(boolean value) {
		use_vbos = value;
	}

	@Override
	public int[] getConfigSpec() {
		// currently, we need a depth buffer but no alpha channel
		int[] configSpec = {
                EGL10.EGL_DEPTH_SIZE,   16,
                EGL10.EGL_NONE
        };
        return configSpec;
	}

	/**
	 * Set the camera of the camera
	 * @param camera Camera to use
	 */
	public void setCamera(Camera camera) {
		synchronized(camera) {
			this.camera = camera;
		}
	}
	
	// a rough FPS counter, if called in drawFrame().
	@SuppressWarnings("unused")
	private void printFPS() {
		long now = System.currentTimeMillis();
        if(now - lastFrame >= 1000l) {
            Log.d(TAG, fps + " fps");
            fps = 0;
            lastFrame = now;
        } else {
        	fps++;
        }
	}
}
