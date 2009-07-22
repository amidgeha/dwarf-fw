/* SVN FILE: $Id$ */
package se.ltu.android.demo;

import java.io.IOException;
import java.io.InputStream;

import se.ltu.android.demo.intersection.AABBox;
import se.ltu.android.demo.intersection.PickResult;
import se.ltu.android.demo.intersection.Ray;
import se.ltu.android.demo.model.Object3D;
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
	private long timeLast = 0;
	private Node world;
	private DemoGLSurfaceView mGLView;
	private boolean isRunning = true;
	private boolean isPaused = false;
	private Spatial pickedMesh;
	private Box box;

	public DemoGameThread(DemoGLSurfaceView glview) {
		mGLView = glview;
		timeTarget = 1000/TARGET_FPS;
	}
	
	public void run() {
		createWorld();
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
	    //updateAnimations();
	    world.update(timePerFrame);
	    //updateSound();
		mGLView.requestRender();
	}
	
	private void updateInput() {
		Ray pickRay = mGLView.getRenderer().getPickRay();
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
					world.attachChild(box);
				} else if(pickedMesh != null && spatial.hasParent()
						&& spatial.getParent().getName().equals("Board")) {
					
					float[] from = pickedMesh.getLocalTranslation();
					float[] to = spatial.getLocalTranslation();
					KeyFrame frame = new KeyFrame(2000);
					frame.setTranslation(to[0], to[1], from[2]);
					KeyFrameAnimation anim = new KeyFrameAnimation();
					anim.addFrame(frame);
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

	private void createWorld() {
    	world = new Node("Root Node");
    	Node room = new Node("room");
    	TriMesh mesh;
    	Node board;
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
    	
    	board = createBoard();
    	
    	InputStream stream1 = mGLView.getContext().getResources().openRawResource(R.raw.pawn);
    	InputStream stream2 = mGLView.getContext().getResources().openRawResource(R.raw.pawn);
    	
    	try {
			mesh = Object3D.loadModel("piece", stream1, stream2);
			mesh.setLocalTranslation(-2.5f, -2.5f, 0.5f);
	    	mesh.setSolidColor(GLColor.ORANGE);
	    	room.attachChild(mesh);
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
		
    	room.attachChild(board);
		world.attachChild(room);
		world.updateTransform();
		world.updateWorldBound(false);
	}

	/**
	 * @return
	 */
	private Node createBoard() {
		Node board = new Node("Board");
		Box darkSquare = new Box("darkSquare", 1.0f, 1.0f, 0.2f);
		Box lightSquare = new Box("lightSquare", 1.0f, 1.0f, 0.2f);
		darkSquare.setSolidColor(new float[]{0.4f,0.2f,0.08f,1.0f});
		lightSquare.setSolidColor(new float[]{0.87f,0.62f,0.45f,1.0f});
		
		TriMesh curSquare = null;
		short oddeven = 1; // 1a (a dark square) starts as odd
		String name;
		// create chess board from 1a to 8h
		for(short row = 1; row < 9; row++) {
			for(short col = 0; col < 8; col++) {
				name = String.valueOf((char)('a'+col)) + row;
				if((oddeven & 1) == 0) {
					curSquare = lightSquare.cloneMesh(name);
				} else {
					curSquare = darkSquare.cloneMesh(name);
				}
				curSquare.setLocalTranslation(col-3.5f, row-4.5f, 0.0f);
				board.attachChild(curSquare);
				oddeven++;
			}
			oddeven++;
		}
		return board;
	}
}
