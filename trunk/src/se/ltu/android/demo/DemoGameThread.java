/* SVN FILE: $Id$ */
package se.ltu.android.demo;

import android.util.Log;
import se.ltu.android.demo.intersection.AABBox;
import se.ltu.android.demo.intersection.PickResult;
import se.ltu.android.demo.intersection.Ray;
import se.ltu.android.demo.scene.Node;
import se.ltu.android.demo.scene.Spatial;
import se.ltu.android.demo.scene.TriMesh;
import se.ltu.android.demo.scene.shapes.*;
import se.ltu.android.demo.util.GLColor;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class DemoGameThread extends Thread {
	private final static String TAG = "GameThread";
	private final static int TARGET_FPS = 30;
	public static long timePerFrame = 1000;
	private long timeTarget;
	private long timeLast = 0;
	private Node world;
	private DemoGLSurfaceView mGLView;
	private boolean isRunning = true;
	private boolean isPaused = false;
	private TriMesh pickedMesh;
	private Box box;

	public DemoGameThread(DemoGLSurfaceView glview) {
		mGLView = glview;
		timeTarget = 1000/TARGET_FPS;
	}
	
	public void run() {
		createWorld();
		mGLView.getRenderer().setScene(world);
		
		long timeLast;
		long timeNow;
		long timeSleep;
		while (isRunning) {
	        while (isPaused && isRunning) {
	            try {
	            	sleep(100);
				} catch (InterruptedException e) {
				}
	        }
	        // this thread have control over the frame rate
	        // especially timePerFrame will be useful in
	        // animations
	        timeLast = System.currentTimeMillis();
	        update();
	        timeNow = System.currentTimeMillis();
	        timePerFrame = timeNow - timeLast;
	        timeSleep = timeTarget - timePerFrame;
	        if(timeSleep > 0) {
	        	try {
					sleep(timeSleep);
				} catch (InterruptedException e) {
				}
	        }
	    }
	}
	
	private void update() {
		//updateState();
	    updateInput();
	    //updateAI();
	    //updatePhysics();
	    //updateAnimations();
	    //updateSound();
		mGLView.requestRender();
	}
	
	private void updateInput() {
		Ray pickRay = mGLView.getRenderer().getPickRay();
		if(pickRay != null) {
			PickResult result = new PickResult();
			world.calculatePick(pickRay, result);
			if(result.size() > 0) {
				Spatial spatial = result.getFirst();
				Log.d(TAG, "Picked: "+spatial.getName());
				if(box != null) {
					box.detachFromParent();
				}
				box = null;
				if(spatial instanceof TriMesh) {
					pickedMesh = (TriMesh)spatial;
					AABBox bound = pickedMesh.getWorldBound();
					box = new Box("bounding", bound.minX, bound.minY, bound.minZ, bound.maxX, bound.maxY, bound.maxZ);
					//box.setTransform(pickedBox.getTransform());
					world.attachChild(box);
				}
			} else {
				Log.d(TAG, "Picked none!");
			}
		}
	}

	public void onPause() {
		isPaused = true;		
	}
	
	public void onResume() {
		isPaused = false;
	}

	private void createWorld() {
    	world = new Node("Root Node");
    	Node room = new Node("room");
    	Quad quad;
    	Box box;
    	   	  	
    	box = new Box("northW", 12.0f, 1.0f, 6.0f);
    	box.setLocalTranslation(0.f, 6.5f, 0.f);
    	box.setSolidColor(GLColor.CYAN);
    	room.attachChild(box);
    	
    	box = new Box("southW", 12.0f, 1.0f, 6.0f);
    	box.setLocalTranslation(0.f, -6.5f, 0.f);
    	box.setSolidColor(GLColor.CYAN);
    	room.attachChild(box);
    	  	
    	box = new Box("westW", 1.0f, 12.0f, 6.0f);
    	box.setLocalTranslation(-6.5f, 0.f, 0.f);
    	box.setSolidColor(GLColor.BLUE);
    	room.attachChild(box);
    	
    	box = new Box("eastW", 1.0f, 12.0f, 6.0f);
    	box.setLocalTranslation(6.5f, 0.f, 0.f);
    	box.setSolidColor(GLColor.BLUE);
    	room.attachChild(box);
    	
    	box = new Box("Cpillar1", 1.0f, 1.0f, 6.0f);
    	box.setLocalTranslation(-6.5f, -6.5f, 0.f);
    	room.attachChild(box);
    	
    	box = new Box("Cpillar2", 1.0f, 1.0f, 6.0f);
    	box.setLocalTranslation(6.5f, -6.5f, 0.f);
    	room.attachChild(box);
    	
    	box = new Box("Cpillar3", 1.0f, 1.0f, 6.0f);
    	box.setLocalTranslation(6.5f, 6.5f, 0.f);
    	room.attachChild(box);
    	
    	box = new Box("Cpillar4", 1.0f, 1.0f, 6.0f);
    	box.setLocalTranslation(-6.5f, 6.5f, 0.f);
    	room.attachChild(box);
    		
    	quad = new Quad("floor", 12.0f, 12.0f);
    	quad.setLocalTranslation(0.0f, 0.0f, -3f);
    	quad.setSolidColor(new float[] {0.4f,0.4f,0.4f,1.0f});
    	room.attachChild(quad);
    	
    	box = new Box("GreenBox", 3.0f,3.0f,3.0f);
    	box.setLocalTranslation(-3f, -3f, 0f);
    	box.setSolidColor(GLColor.GREEN);
    	room.attachChild(box);
    	
    	box = new Box("RedBox", 1.0f,1.0f,1.0f);
    	box.setLocalTranslation(0f, 0f, 0f);
    	box.setLocalRotation(45, 0, 1, 0);
    	box.setSolidColor(GLColor.RED);
    	room.attachChild(box);
    	
    	TriMesh mesh = box.cloneMesh("ClonedMesh");
    	mesh.getLocalTranslation()[2] += 1.5f;
    	room.attachChild(mesh);
    	
    	box.setColors(new float[]{
    			1,0,0,1, 1,0,0,1, 1,0,0,1, 1,0,0,1, //front
    			1,0,0,1, 1,0,0,1, 1,0,0,1, 1,0,0,1, //back
    			0.5f,0.5f,0.5f,1.0f, 0.5f,0.5f,0.5f,1.0f, 0.5f,0.5f,0.5f,1.0f, 0.5f,0.5f,0.5f,1.0f, //right
    			0.5f,0.5f,0.5f,1.0f, 0.5f,0.5f,0.5f,1.0f, 0.5f,0.5f,0.5f,1.0f, 0.5f,0.5f,0.5f,1.0f, //left
    			0,0,1,1, 0,0,1,1, 0,0,1,1, 0,0,1,1, //top
    			0,0,1,1, 0,0,1,1, 0,0,1,1, 0,0,1,1, //bottom
    			
    	});
		
		world.attachChild(room);
		world.updateTransform();
		world.updateWorldBound(false);
	}
}
