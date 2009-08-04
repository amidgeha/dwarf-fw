/* SVN FILE: $Id$ */
package se.ltu.android.demo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;

import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import se.ltu.android.demo.camera.Camera;
import se.ltu.android.demo.intersection.AABBox;
import se.ltu.android.demo.intersection.PickResult;
import se.ltu.android.demo.intersection.Ray;
import se.ltu.android.demo.light.Light;
import se.ltu.android.demo.model.Object3D;
import se.ltu.android.demo.scene.Board;
import se.ltu.android.demo.scene.CameraNode;
import se.ltu.android.demo.scene.LightNode;
import se.ltu.android.demo.scene.Node;
import se.ltu.android.demo.scene.PieceData;
import se.ltu.android.demo.scene.Spatial;
import se.ltu.android.demo.scene.TriMesh;
import se.ltu.android.demo.scene.animation.AnimationListener;
import se.ltu.android.demo.scene.animation.KeyFrame;
import se.ltu.android.demo.scene.animation.KeyFrameAnimation;
import se.ltu.android.demo.scene.shapes.*;
import se.ltu.android.demo.util.GLColor;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class DemoGameThread extends Thread implements AnimationListener {
	private final static String TAG = "GameThread";
	private final static int TARGET_FPS = 60;
	private final static float TRACKBALL_MOVE_MULTIPLIER = 1f;
	public static long timePerFrame = 1000;
	private long timeTarget;
	private Node world;
	private DemoGLSurfaceView mGLView;
	private boolean isRunning = true;
	private boolean isPaused = false;
	private Spatial pickedMesh;
	private Box box;
	private Camera[] camList; // camera list
	private int iCam = 0; // camera pointer
	private boolean moving_piece = false;
	private boolean moving_camera = false;
	private CameraNode camNode = new CameraNode("Camera");	//used for animation camera movement
	/**
	 * [0] = x coordinate<br>
	 * [1] = y coordinate<br>
	 * [2] = type tap (1 = single, 2 = double, otherwise coordinates are ignored)
	 */
	private static float[] tapCoords = new float[3];
	/**
	 * [0] = x coordinate<br>
	 * [1] = y coordinate<br>
	 * [2] = coordinate change (0 = false, true otherwise)<br>
	 * [3] = on click (0 = false, true otherwise)<br>
	 */
	private static float[] trackInput = new float[4];
	private float[] modelM = new float[16];
	private int iCamSensor = 0;
	private Spatial[][] board_data = new TriMesh[8][8];

	public DemoGameThread(DemoGLSurfaceView glview) {
		setName("GameThread");
		mGLView = glview;
		timeTarget = 1000 / TARGET_FPS;
		camList = new Camera[4];

		camList[0] = new Camera();
		camList[0].setIdentity();
		camList[0].setPosition(0, 0, 9);

		camList[1] = new Camera();
		camList[1].lookAt(5, -5, 3, 0, 0, -2.9f, 0, 0, 1);

		camList[2] = new Camera();
		camList[2].lookAt(6, 0, 6, 0, 0, -2.9f, 0, 0, 1);

		camList[3] = new Camera(); // used for rotation
		iCamSensor = 3;
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
			timeSleep = timeTarget - timePerFrame
					+ (lastTime - System.currentTimeMillis());
			if (timeSleep > 0) {
				try {
					sleep(timeSleep);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private void update() {
		// updateState();
		updateInput();
		// updateAI();
		// updatePhysics();
		world.update(timePerFrame); // updates animations
		updateCamera();
		// updateSound();
		// mGLView.requestRender();
	}

	private void updateCamera() {
		if (iCam == iCamSensor) {
			SensorHandler.getRotM4(modelM);
			camList[iCam].setRotationM(modelM);
		}
		mGLView.getRenderer().setCamera(camList[iCam]);
	}

	private void updateInput() {
		checkTap();
		checkTrack();
	}

	/**
	 * @return
	 */
	private void checkTap() {
		Ray pickRay = null; // = mGLView.getRenderer().getPickRay();
		PickResult result = null;
		int nTaps = 0;

		synchronized (tapCoords) {
			if (tapCoords[2] == 1 && !moving_piece) {
				nTaps = 1;
				pickRay = camList[iCam].calculatePickRay(tapCoords[0],
						tapCoords[1]);
			}
			if (tapCoords[2] == 2 && !moving_camera) {
				// only move camera 0 and 3 (above and sensor camera)
				if(iCam == 0 || iCam == iCamSensor) {
					nTaps = 2;
					pickRay = camList[iCam].calculatePickRay(tapCoords[0],
							tapCoords[1]);
				}
			}
			tapCoords[2] = 0;
		}
		if (pickRay != null) {
			result = new PickResult();
			world.calculatePick(pickRay, result);
		}
		if (result == null || result.size() < 1) {
			return;
		}
		if(nTaps == 1) {
			handleSingleTap(result);
		}
		if(nTaps == 2) {
			handleDoubleTap(result);
		}
	}

	private void checkTrack() {
		synchronized (trackInput) {
			if (trackInput[3] != 0 && !moving_camera) {
				// switch camera
				iCam++;
				if (iCam == camList.length) {
					iCam = 0;
				}
			}
			trackInput[3] = 0;
			if (trackInput[2] != 0) {
				camList[iCam].translate(0, 0, trackInput[1]);
				trackInput[0] = 0;
				trackInput[1] = 0;
				trackInput[2] = 0;
			}
		}
	}

	private void handleSingleTap(PickResult result) {
		Log.d(TAG, "Single tap");
		if (box != null) {
			box.detachFromParent();
			box = null;
		}
		Spatial spatial = result.getFirst();
		if (spatial.hasPieceData()) {
			checkPickPiece(spatial);
			return;
		} 
		if (spatial.hasParent() && spatial.getParent().getName().equals("Board")) {
			// picked a square
			float[] to = spatial.getLocalTranslation();
			int col = Math.round(to[0] + 3.5f);
			int row = Math.round(to[1] + 3.5f);
			if (board_data[col][row] != null) {
				// picked a square that is occupied by a piece
				spatial = board_data[col][row];
				checkPickPiece(spatial);
				return;
			}
			if (pickedMesh != null) {
				// picked an empty square and we have a previous selection
				moving_piece = true;
				float[] from = pickedMesh.getLocalTranslation();
				
				// create animation
				KeyFrame frame = new KeyFrame(2000);
				frame.setTranslation(to[0], to[1], from[2]);
				KeyFrameAnimation anim = new KeyFrameAnimation(this);
				anim.addFrame(frame);
				anim.setInterpolator(new AccelerateDecelerateInterpolator());
				pickedMesh.addController(anim);
				
				// update board
				board_data[col][row] = pickedMesh;
				col = Math.round(from[0] + 3.5f);
				row = Math.round(from[1] + 3.5f);
				board_data[col][row] = null;
				
				pickedMesh = null;
			}
		}
	}
	
	/**
	 * @param result
	 */
	private void handleDoubleTap(PickResult result) {
		Log.d(TAG, "Double tap");
		Spatial spatial = result.getFirst();
		if (spatial.hasParent() && spatial.getParent().getName().equals("Board")) {
			// picked a square
			float[] to = spatial.getLocalTranslation();
			int col = Math.round(to[0] + 3.5f);
			int row = Math.round(to[1] + 3.5f);
			if (board_data[col][row] != null) {
				// picked a square that is occupied by a piece
				return;
			}
			
			// square with no piece, set to go
			moving_camera = true;
			camNode.setCamera(camList[iCam]);
			float[] from = camNode.getLocalTranslation();
			
			// create animation
			KeyFrame frame = new KeyFrame(2000);
			frame.setTranslation(to[0], to[1], from[2]);
			KeyFrameAnimation anim = new KeyFrameAnimation(this);
			anim.addFrame(frame);
			anim.setInterpolator(new AccelerateDecelerateInterpolator());
			camNode.addController(anim);
		}
	}

	private void checkPickPiece(Spatial spatial) {
		if (pickedMesh != null) {
			if(spatial.equals(pickedMesh)) {
				// picked the previously selected piece
				pickedMesh = null;
				return;
			}
			// picked a new piece and we have a previous selection
			pickedMesh = spatial;
			AABBox bound = pickedMesh.getWorldBound();
			box = new Box("bounding", bound.minX, bound.minY, bound.minZ,
					bound.maxX, bound.maxY, bound.maxZ);
			box.setPickable(false);
			world.attachChild(box);
			return;
		}
		// picked a piece and we have no previous selection
		pickedMesh = spatial;
		AABBox bound = pickedMesh.getWorldBound();
		box = new Box("bounding", bound.minX, bound.minY, bound.minZ,
				bound.maxX, bound.maxY, bound.maxZ);
		box.setPickable(false);
		world.attachChild(box);
	}

	public void onPause() {
		isPaused = true;
	}

	public void onResume() {
		isPaused = false;
	}

	private void createWorld() {
		// world = new Node("Root Node");
		TriMesh mesh;
		Board board;
		Quad quad;
		Box box;

		Light light = new Light();
		light.setPosition(new float[] { -1, 1, 1, 0 });
		light.setAmbient(new float[] { 0.1f, 0.1f, 0.1f, 1 });
		light.setDiffuse(new float[] { 0.8f, 0.8f, 0.8f, 1 });
		light.setSpecular(new float[] { 1, 1, 1, 1 });
		world = new LightNode("Root & Light", light);

		/*
		 * box = new Box("northW", 12.0f, 1.0f, 6.0f);
		 * box.setLocalTranslation(0.f, 6.5f, 0.f);
		 * box.setSolidColor(GLColor.CYAN); box.setPickable(false);
		 * world.attachChild(box);
		 * 
		 * box = new Box("southW", 12.0f, 1.0f, 6.0f);
		 * box.setLocalTranslation(0.f, -6.5f, 0.f);
		 * box.setSolidColor(GLColor.CYAN); box.setPickable(false);
		 * world.attachChild(box);
		 * 
		 * box = new Box("westW", 1.0f, 12.0f, 6.0f);
		 * box.setLocalTranslation(-6.5f, 0.f, 0.f);
		 * box.setSolidColor(GLColor.BLUE); box.setPickable(false);
		 * world.attachChild(box);
		 * 
		 * box = new Box("eastW", 1.0f, 12.0f, 6.0f);
		 * box.setLocalTranslation(6.5f, 0.f, 0.f);
		 * box.setSolidColor(GLColor.BLUE); box.setPickable(false);
		 * world.attachChild(box);
		 * 
		 * box = new Box("Cpillar1", 1.0f, 1.0f, 6.0f);
		 * box.setLocalTranslation(-6.5f, -6.5f, 0.f); box.setPickable(false);
		 * world.attachChild(box);
		 * 
		 * box = new Box("Cpillar2", 1.0f, 1.0f, 6.0f);
		 * box.setLocalTranslation(6.5f, -6.5f, 0.f); box.setPickable(false);
		 * world.attachChild(box);
		 * 
		 * box = new Box("Cpillar3", 1.0f, 1.0f, 6.0f);
		 * box.setLocalTranslation(6.5f, 6.5f, 0.f); box.setPickable(false);
		 * world.attachChild(box);
		 * 
		 * box = new Box("Cpillar4", 1.0f, 1.0f, 6.0f);
		 * box.setLocalTranslation(-6.5f, 6.5f, 0.f); box.setPickable(false);
		 * world.attachChild(box);
		 */

		quad = new Quad("floor", 12.0f, 12.0f);
		quad.setLocalTranslation(0.0f, 0.0f, -3f);
		quad.setSolidColor(new float[] { 0.4f, 0.4f, 0.4f, 1.0f });
		quad.setPickable(false);
		world.attachChild(quad);

		board = new Board("Board");
		board.setLocalTranslation(0, 0, -2.9f);

		InputStream stream1 = mGLView.getContext().getResources()
				.openRawResource(R.raw.knight);
		InputStream stream2 = mGLView.getContext().getResources()
				.openRawResource(R.raw.knight);

		try {
			mesh = new Object3D().loadModel("piece", stream1, stream2);
			mesh.setLocalTranslation(PieceData.getColPos('b'), PieceData
					.getRowPos(2), -2.8f);
			mesh.setSolidColor(GLColor.ORANGE);
			mesh.setPieceData(new PieceData('b', 2));
			board_data[PieceData.getColIndex('b')][PieceData.getRowIndex(2)] = mesh;

			world.attachChild(mesh);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// box = new Box("piece", 0.60f, 0.60f, 0.80f);

		/*
		 * box = new Box("GreenBox", 3.0f,3.0f,3.0f);
		 * box.setLocalTranslation(-3f, -3f, 0f);
		 * box.setSolidColor(GLColor.GREEN); room.attachChild(box);
		 * 
		 * box = new Box("RedBox", 1.0f,1.0f,1.0f); box.setLocalTranslation(0f,
		 * 0f, 0f); box.setLocalRotation(45, 0, 1, 0);
		 * box.setSolidColor(GLColor.RED); room.attachChild(box);
		 * 
		 * TriMesh mesh = box.cloneMesh("ClonedMesh");
		 * mesh.getLocalTranslation()[2] += 1.5f; room.attachChild(mesh);
		 * 
		 * box.setColors(new float[]{ 1,0,0,1, 1,0,0,1, 1,0,0,1, 1,0,0,1,
		 * //front 1,0,0,1, 1,0,0,1, 1,0,0,1, 1,0,0,1, //back
		 * 0.5f,0.5f,0.5f,1.0f, 0.5f,0.5f,0.5f,1.0f, 0.5f,0.5f,0.5f,1.0f,
		 * 0.5f,0.5f,0.5f,1.0f, //right 0.5f,0.5f,0.5f,1.0f,
		 * 0.5f,0.5f,0.5f,1.0f, 0.5f,0.5f,0.5f,1.0f, 0.5f,0.5f,0.5f,1.0f, //left
		 * 0,0,1,1, 0,0,1,1, 0,0,1,1, 0,0,1,1, //top 0,0,1,1, 0,0,1,1, 0,0,1,1,
		 * 0,0,1,1, //bottom
		 * 
		 * });
		 */

		world.attachChild(board);
		world.attachChild(camNode);
		// world.attachChild(lNode);
		world.updateTransform();
		world.updateWorldBound(false);

		// board.getChildren()
	}

	/**
	 * Register a double tap on this thread
	 * 
	 * @param x
	 *            screen x coordinate
	 * @param y
	 *            screen y coordinate
	 */
	public static void onDoubleTap(float x, float y) {
		synchronized (tapCoords) {
			tapCoords[0] = x;
			tapCoords[1] = y;
			tapCoords[2] = 2;
		}
	}
	
	/**
	 * Register a single tap on this thread
	 * 
	 * @param x
	 *            screen x coordinate
	 * @param y
	 *            screen y coordinate
	 */
	public static void onSingleTap(float x, float y) {
		synchronized (tapCoords) {
			tapCoords[0] = x;
			tapCoords[1] = y;
			tapCoords[2] = 1;
		}
	}

	public static void onTrackballClick() {
		synchronized (trackInput) {
			trackInput[3] = 1;
		}
	}
	
	public static void onTrackballMove(float x, float y) {
		synchronized (trackInput) {
			trackInput[0] += x;
			trackInput[1] += y;
			trackInput[2] = 1;
		}
	}

	@Override
	public void onAnimationEnd(KeyFrameAnimation anim, Spatial spatial) {
		if(spatial != null) {
			if(spatial instanceof CameraNode) {
				// it's a camera
				moving_camera = false;
			} else {
				// it's a piece
				moving_piece = false;
			}
			spatial.removeController(anim);
		}
	}
}
