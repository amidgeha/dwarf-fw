/* SVN FILE: $Id$ */
package se.ltu.android.demo;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import se.ltu.android.demo.camera.Camera;
import se.ltu.android.demo.intersection.AABBox;
import se.ltu.android.demo.intersection.PickResult;
import se.ltu.android.demo.intersection.Ray;
import se.ltu.android.demo.light.Light;
import se.ltu.android.demo.model.Object3D;
import se.ltu.android.demo.scene.Board;
import se.ltu.android.demo.scene.LightNode;
import se.ltu.android.demo.scene.Node;
import se.ltu.android.demo.scene.Spatial;
import se.ltu.android.demo.scene.TriMesh;
import se.ltu.android.demo.scene.animation.KeyFrame;
import se.ltu.android.demo.scene.animation.KeyFrameAnimation;
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
	private Node world;
	private DemoGLSurfaceView mGLView;
	private boolean isRunning = true;
	private boolean isPaused = false;
	private Spatial pickedMesh;
	private Box box;
	private Camera[] camList;	// camera list
	private int iCam = 0;		// camera pointer
	/**
	 * [0] = x coordinate (-1 = no tap registered)<br>
	 * [1] = y coordinate<br>
	 */
	private static float[] tapCoords = new float[2];
	/**
	 * [0] = x coordinate<br>
	 * [1] = y coordinate<br>
	 * [2] = coordinate change (0 = false, true otherwise)<br>
	 * [3] = on click (0 = false, true otherwise)<br>
	 */
	private static float[] trackInput = new float[4];
	private float[] modelM = new float[16];
	private int iCamSensor = 0;

	public DemoGameThread(DemoGLSurfaceView glview) {
		setName("GameThread");
		mGLView = glview;
		timeTarget = 1000/TARGET_FPS;
		camList = new Camera[4];
		
		camList[0] = new Camera();
		camList[0].setIdentity();
		camList[0].setPosition(0, 0, 9);
		
		camList[1] = new Camera();
		camList[1].lookAt(5, -5, 3, 0, 0, -2.9f, 0, 0, 1);
		
		camList[2] = new Camera();
		camList[2].lookAt(6, 0, 6, 0, 0, -2.9f, 0, 0, 1);
		
		camList[3] = new Camera();	// used for rotation
		iCamSensor  = 3;
	}
	
	public void run() {
		mGLView.getRenderer().useVBOs(true);
		createWorld();
		mGLView.getRenderer().setCamera(camList[iCam]);
		mGLView.getRenderer().setScene(world);
		
		long lastTime = System.currentTimeMillis();
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
	        timePerFrame = System.currentTimeMillis() - lastTime;
	        lastTime = System.currentTimeMillis();
	        update();
	        timeSleep = timeTarget - timePerFrame + (lastTime - System.currentTimeMillis());
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
	    world.update(timePerFrame);		// updates animations
	    updateCamera();
	    //updateSound();
		//mGLView.requestRender();
	}

	private void updateCamera() {
		if(iCam == iCamSensor) {
			SensorHandler.getRotM4(modelM);
			camList[iCam].setRotationM(modelM);
		}
		mGLView.getRenderer().setCamera(camList[iCam]);
	}

	private void updateInput() {
		checkPick();
		checkTrack();
	}

	private void checkTrack() {
		synchronized(trackInput) {
			if(trackInput[3] != 0) {
				// switch camera
				iCam++;
				if(iCam == camList.length) {
					iCam = 0;
				}
			}
			trackInput[3] = 0;
		}
	}

	private void checkPick() {
		Ray pickRay = null; // = mGLView.getRenderer().getPickRay();
		synchronized(tapCoords) {
			if(tapCoords[0] != -1) {
				pickRay = camList[iCam].calculatePickRay(tapCoords[0], tapCoords[1]);
			}
			tapCoords[0] = -1;
		}
		if(pickRay != null) {
			PickResult result = new PickResult();
			world.calculatePick(pickRay, result);
			if(box != null) {
				box.detachFromParent();
			}
			box = null;
			if(result.size() > 0) {
				Spatial spatial = result.getFirst();
				if(pickedMesh == null && spatial.getName().equals("piece")) {
					
					pickedMesh = spatial;
					AABBox bound = pickedMesh.getWorldBound();
					box = new Box("bounding", bound.minX, bound.minY, bound.minZ, bound.maxX, bound.maxY, bound.maxZ);
					box.setPickable(false);
					world.attachChild(box);
				} else if(pickedMesh != null && spatial.hasParent()
						&& spatial.getParent().getName().equals("Board")) {
					
					float[] from = pickedMesh.getLocalTranslation();
					float[] to = spatial.getLocalTranslation();
					KeyFrame frame = new KeyFrame(2000);
					frame.setTranslation(to[0], to[1], from[2]);
					KeyFrameAnimation anim = new KeyFrameAnimation();
					anim.addFrame(frame);
					anim.setInterpolator(new AccelerateDecelerateInterpolator());
					pickedMesh.addController(anim);
					pickedMesh = null;
					
				} else if(pickedMesh != null && spatial.getName().equals("piece")) {
					
					pickedMesh = null;
					
				}
				//Log.d(TAG, "Picked: "+spatial.getName());
			} else {
				pickedMesh = null;
				//Log.d(TAG, "Picked none!");
			}
		}
	}

	public void onPause() {
		isPaused = true;		
	}
	
	public void onResume() {
		isPaused = false;
	}
	
	/**
	 * Register a tap on this thread
	 * @param x screen x coordinate
	 * @param y screen y coordinate
	 */
	public static void onTap(float x, float y) {
		synchronized(tapCoords) {
			tapCoords[0] = x;
			tapCoords[1] = y;
		}
	}
	
	public static void onTrackballClick() {
		synchronized(trackInput) {
			trackInput[3] = 1;
		}
	}

	private void createWorld() {
    	//world = new Node("Root Node");
    	TriMesh mesh;
    	Board board;
    	Quad quad;
    	Box box;
    	
    	Light light = new Light();
		light.setPosition(new float[]{-1, 1, 1, 0});
		light.setAmbient(new float[]{0.1f, 0.1f, 0.1f, 1});
		light.setDiffuse(new float[]{0.8f, 0.8f, 0.8f, 1});
		light.setSpecular(new float[]{1, 1, 1, 1});
		world = new LightNode("Root & Light", light);
    	
		/*
    	box = new Box("northW", 12.0f, 1.0f, 6.0f);
    	box.setLocalTranslation(0.f, 6.5f, 0.f);
    	box.setSolidColor(GLColor.CYAN);
    	box.setPickable(false);
    	world.attachChild(box);
    	
    	box = new Box("southW", 12.0f, 1.0f, 6.0f);
    	box.setLocalTranslation(0.f, -6.5f, 0.f);
    	box.setSolidColor(GLColor.CYAN);
    	box.setPickable(false);
    	world.attachChild(box);
    	  	
    	box = new Box("westW", 1.0f, 12.0f, 6.0f);
    	box.setLocalTranslation(-6.5f, 0.f, 0.f);
    	box.setSolidColor(GLColor.BLUE);
    	box.setPickable(false);
    	world.attachChild(box);
    	
    	box = new Box("eastW", 1.0f, 12.0f, 6.0f);
    	box.setLocalTranslation(6.5f, 0.f, 0.f);
    	box.setSolidColor(GLColor.BLUE);
    	box.setPickable(false);
    	world.attachChild(box);
    	
    	box = new Box("Cpillar1", 1.0f, 1.0f, 6.0f);
    	box.setLocalTranslation(-6.5f, -6.5f, 0.f);
    	box.setPickable(false);
    	world.attachChild(box);
    	
    	box = new Box("Cpillar2", 1.0f, 1.0f, 6.0f);
    	box.setLocalTranslation(6.5f, -6.5f, 0.f);
    	box.setPickable(false);
    	world.attachChild(box);
    	
    	box = new Box("Cpillar3", 1.0f, 1.0f, 6.0f);
    	box.setLocalTranslation(6.5f, 6.5f, 0.f);
    	box.setPickable(false);
    	world.attachChild(box);
    	
    	box = new Box("Cpillar4", 1.0f, 1.0f, 6.0f);
    	box.setLocalTranslation(-6.5f, 6.5f, 0.f);
    	box.setPickable(false);
    	world.attachChild(box);
    	*/
    	
    	quad = new Quad("floor", 12.0f, 12.0f);
    	quad.setLocalTranslation(0.0f, 0.0f, -3f);
    	quad.setSolidColor(new float[] {0.4f,0.4f,0.4f,1.0f});
    	quad.setPickable(false);
    	world.attachChild(quad);
    	
    	board = new Board("Board");
    	board.setLocalTranslation(0, 0, -2.9f);
    	
    	InputStream stream1 = mGLView.getContext().getResources().openRawResource(R.raw.knight);
    	InputStream stream2 = mGLView.getContext().getResources().openRawResource(R.raw.knight);
    	
    	try {
			mesh = new Object3D().loadModel("piece", stream1, stream2);
			mesh.setLocalTranslation(-2.5f, -2.5f, -2.8f);
			//mesh.setLocalScale(2, 2, 2);
	    	mesh.setSolidColor(GLColor.ORANGE);
	    	world.attachChild(mesh);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	//box = new Box("piece", 0.60f, 0.60f, 0.80f);
    	
    	
    	/*
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
    	*/
		
		world.attachChild(board);
		//world.attachChild(lNode);
		world.updateTransform();
		world.updateWorldBound(false);
		
		//board.getChildren()
	}
}
