/* SVN FILE: $Id$ */
package se.ltu.android.demo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetFileDescriptor;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import se.ltu.android.demo.scene.Board;
import se.ltu.android.demo.scene.CameraLeaf;
import se.ltu.android.demo.scene.LightNode;
import se.ltu.android.demo.scene.Node;
import se.ltu.android.demo.scene.PieceData;
import se.ltu.android.demo.scene.Spatial;
import se.ltu.android.demo.scene.TriMesh;
import se.ltu.android.demo.scene.animation.AnimationListener;
import se.ltu.android.demo.scene.animation.KeyFrame;
import se.ltu.android.demo.scene.animation.KeyFrameAnimation;
import se.ltu.android.demo.scene.camera.Camera;
import se.ltu.android.demo.scene.intersection.PickResult;
import se.ltu.android.demo.scene.intersection.Ray;
import se.ltu.android.demo.scene.shapes.*;
import se.ltu.android.demo.scene.state.Light;
import se.ltu.android.demo.scene.state.Material;
import se.ltu.android.demo.sensors.SensorHandler;
import se.ltu.android.demo.util.ObjLoader;

/**
 * A thread that updates the world based on input and sensor events. All kind
 * of game states and logic should be placed here. Very heavy calculations should
 * still be placed in a separate asynchronous task.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class DemoGameThread extends Thread implements AnimationListener {
	private final static String TAG = "GameThread";
	private final static int TARGET_FPS = 25;
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
	private long timePerFrame = 1000;
	private long timeTarget;
	private Node world;
	private TriMesh[][] board_data = new TriMesh[8][8];
	private DemoGLSurfaceView mGLView;
	private boolean isRunning = true;
	private boolean isPaused = false;
	private TriMesh pickedMesh;
	
	// 
	private boolean moving_piece = false;
	private boolean moving_camera = false;
	
	// Camera variables
	private Camera[] camList; 	// list of available cameras
	private int iCam = 0; 		// camera pointer
	private CameraLeaf camLeaf = new CameraLeaf("Camera");	//used for animation camera movement
	private float[] modelM = new float[16];
	private int iCamSensor = 0;	// marks which camera that 
	
	// Materials for the pieces
	private Material lightMat;
	private Material darkMat;
	private Material lightMatPicked;
	private Material darkMatPicked;

	/**
	 * Creates the game thread
	 * @param glview
	 */
	public DemoGameThread(DemoGLSurfaceView glview) {
		setName("GameThread");
		mGLView = glview;
		timeTarget = 1000 / TARGET_FPS;
		camList = new Camera[4];

		camList[0] = new Camera();
		camList[0].setIdentity();
		camList[0].setPosition(0, 0, 9);

		camList[1] = new Camera(); // used for rotation
		iCamSensor = 1;
		
		camList[2] = new Camera();
		camList[2].lookAt(5, -5, 3, 0, 0, -2.9f, 0, 0, 1);

		camList[3] = new Camera();
		camList[3].lookAt(6, 0, 6, 0, 0, -2.9f, 0, 0, 1);
	}

	/**
	 * Starts the game loop
	 */
	@Override
	public void run() {
		mGLView.getRenderer().useVBOs(true);
		createWorld();
		mGLView.getRenderer().setCamera(camList[iCam]);
		mGLView.getRenderer().setScene(world);

		long lastTime = System.currentTimeMillis();
		long timeSleep;
		// Good place to put a method trace
		//Debug.startMethodTracing("mtrace");
		while (isRunning) {
			while (isPaused && isRunning) {
				try {
					sleep(100);
				} catch (InterruptedException e) {
				}
			}
			/*
			 * This thread have some control over the frame rate
			 * especially timePerFrame will be useful in animations
			 */
			timePerFrame = System.currentTimeMillis() - lastTime;
			lastTime = System.currentTimeMillis();
			update();
			/* 
			 * We want to sleep because we don't need to update as fast as possible.
			 * This way the renderer gets more time and we get a slightly higher frame rate.
			 * The last part on timeSleep (+ lastTime - System.currentTimeMillis()) is rather
			 * important to get smooth animations.
			 */
			timeSleep = timeTarget - timePerFrame + lastTime - System.currentTimeMillis();
			if (timeSleep > 0) {
				try {
					sleep(timeSleep);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private void update() {
		updateInput();
		world.update(timePerFrame); // updates animations
		updateCamera();
		mGLView.requestRender();
	}

	private void updateCamera() {
		if (iCam == iCamSensor && SensorHandler.getRotM4(modelM)) {
			camList[iCam].setRotationM(modelM);
		}
	}

	private void updateInput() {
		checkTap();
		checkTrack();
	}

	/**
	 * @return
	 */
	private void checkTap() {
		Ray pickRay = null;
		PickResult result = null;
		int nTaps = 0;

		synchronized (tapCoords) {
			if (tapCoords[2] == 1 && !moving_piece) {
				nTaps = 1;
				pickRay = camList[iCam].calculatePickRay(tapCoords[0],
						tapCoords[1]);
			}
			if (tapCoords[2] == 2 && !moving_camera) {
				// only move camera 0 and iCamSensor (straight above and sensor camera)
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
		if (result == null || !result.hasResult()) {
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
				mGLView.getRenderer().setCamera(camList[iCam]);
			}
			trackInput[3] = 0;
			if (trackInput[2] != 0) {
				float[] pos = camList[iCam].getPosition();
				float newpos = pos[2] + trackInput[1];
				switch(iCam) {
				case 0:
					newpos = clamp(-1, 15, newpos);
					break;
				case 1:
					newpos = clamp(-15, 15, newpos);
					break;
					default:
						newpos = clamp(-1, 9, newpos);
				}
				if(iCam == 0 || iCam == 1) {
					camList[iCam].setPosition(pos[0], pos[1], newpos);
				} else {
					camList[iCam].lookAt(pos[0], pos[1], newpos, 0, 0, -2.9f, 0, 0, 1);
				}
				trackInput[0] = 0;
				trackInput[1] = 0;
				trackInput[2] = 0;
			}
		}
	}

	/**
	 * Clamp value between lower and upper
	 */
	private float clamp(float lower, float upper, float value) {
		if(value > upper) {
			return upper;
		}
		if(value < lower) {
			return lower;
		}
		return value;
	}

	private void handleSingleTap(PickResult result) {
		Log.d(TAG, "Single tap");
		Spatial spatial = result.getClosest();
		if (spatial.hasData() && (spatial.getData() instanceof PieceData)) {
			checkPickPiece((TriMesh)spatial);
			return;
		} 
		if (spatial.hasParent() && spatial.getParent().getName().equals("Board")) {
			// picked a square
			float[] to = spatial.getLocalTranslation();
			int col = Math.round(to[0] + 3.5f);
			int row = Math.round(to[1] + 3.5f);
			if (board_data[col][row] != null) {
				// picked a square that is occupied by a piece
				checkPickPiece(board_data[col][row]);
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
				
				unselectPick();
			}
		}
	}
	
	private void handleDoubleTap(PickResult result) {
		Log.d(TAG, "Double tap");
		Spatial spatial = result.getClosest();
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
			camLeaf.setCamera(camList[iCam]);
			float[] from = camLeaf.getLocalTranslation();
			
			// create animation
			KeyFrame frame = new KeyFrame(2000);
			frame.setTranslation(to[0], to[1], from[2]);
			KeyFrameAnimation anim = new KeyFrameAnimation(this);
			anim.addFrame(frame);
			anim.setInterpolator(new AccelerateDecelerateInterpolator());
			camLeaf.addController(anim);
		}
	}

	private void checkPickPiece(TriMesh spatial) {
		if (pickedMesh != null) {
			if(spatial.equals(pickedMesh)) {
				// picked the previously selected piece
				unselectPick();
				return;
			}
			// picked a new piece and we have a previous selection
			unselectPick();
			selectPick(spatial);
			return;
		}
		// picked a piece and we have no previous selection
		selectPick(spatial);
	}

	private void unselectPick() {
		if(((PieceData)pickedMesh.getData()).isDark()) {
			pickedMesh.setMaterial(darkMat);
		} else {
			pickedMesh.setMaterial(lightMat);
		}
		pickedMesh = null;		
	}
	
	private void selectPick(TriMesh spatial) {
		pickedMesh = spatial;
		if(((PieceData)pickedMesh.getData()).isDark()) {
			pickedMesh.setMaterial(darkMatPicked);
		} else {
			pickedMesh.setMaterial(lightMatPicked);
		}
	}

	/**
	 * Pauses the game loop
	 */
	public void onPause() {
		isPaused = true;
	}

	/**
	 * Resumes the game loop
	 */
	public void onResume() {
		isPaused = false;
	}

	private void createWorld() {
		// world = new Node("Root Node");
		TriMesh mesh;
		Board board;
		Quad quad;
		
		Material defaultMat = new Material();
		defaultMat.setUseColorMaterial(true);

		Light light = new Light();
		light.setPosition(new float[] { -1, 1, 1, 0 });
		light.setAmbient(new float[] { 0.1f, 0.1f, 0.1f, 1 });
		light.setDiffuse(new float[] { 0.8f, 0.8f, 0.8f, 1 });
		light.setSpecular(new float[] { 1, 1, 1, 1 });
		world = new LightNode("Root & Light", light);

		quad = new Quad("floor", 12.0f, 12.0f);
		quad.setLocalTranslation(0.0f, 0.0f, -3f);
		quad.setSolidColor(new float[] { 0.4f, 0.4f, 0.4f, 1.0f});
		quad.setPickable(false);
		quad.setMaterial(defaultMat);
		world.attachChild(quad);

		board = new Board("Board");
		board.setMaterial(defaultMat);
		board.setLocalTranslation(0, 0, -2.9f);
		
		lightMat = new Material();
		lightMat.setAmbient(1, 0.5f, 0, 1);
		lightMat.setDiffuse(1, 0.5f, 0, 1);
		lightMat.setSpecular(0.9f, 0.9f, 0.9f, 1.0f);
		lightMat.setShininess(40);
		
		darkMat = new Material();
		darkMat.setAmbient(0, 0.4f, 0.8f, 1);
		darkMat.setDiffuse(0, 0.4f, 0.8f, 1);
		darkMat.setSpecular(0.9f, 0.9f, 0.9f, 1.0f);
		darkMat.setShininess(40);
		
		lightMatPicked = new Material();
		lightMatPicked.copyFrom(lightMat);
		lightMatPicked.setAmbient(1, 0.86f, 0.31f, 1);
		lightMatPicked.setDiffuse(1, 0.86f, 0.31f, 1);
		
		darkMatPicked = new Material();
		darkMatPicked.copyFrom(darkMat);
		darkMatPicked.setAmbient(0.26f, 0.82f, 1, 1);
		darkMatPicked.setDiffuse(0.26f, 0.82f, 1, 1);
		
		PieceData pData;
		
		try {
			char col = 'c';
			int row = 2;
			TriMesh pawn = new TriMesh("Pawn");
			InputStream stream = mGLView.getContext().getResources().openRawResource(R.raw.pawn);
			DataInputStream dis = new DataInputStream(stream);
			pawn.importModel(dis);
			pawn.setLocalTranslation(PieceData.getColPos(col), PieceData
					.getRowPos(row), -2.8f);
			pData = new PieceData(col, row);
			pData.setDark(false);
			pawn.setData(pData);
			pawn.setMaterial(lightMat);
			board_data[PieceData.getColIndex(col)][PieceData.getRowIndex(row)] = pawn;
			world.attachChild(pawn);
			
			col = 'd';
			row = 3;
			TriMesh knight = new TriMesh("Knight");
			stream = mGLView.getContext().getResources().openRawResource(R.raw.knight);
			dis = new DataInputStream(stream);
			knight.importModel(dis);
			knight.setLocalTranslation(PieceData.getColPos(col), PieceData
					.getRowPos(row), -2.8f);
			pData = new PieceData(col, row);
			pData.setDark(false);
			knight.setData(pData);
			knight.setMaterial(lightMat);
			board_data[PieceData.getColIndex(col)][PieceData.getRowIndex(row)] = knight;
			world.attachChild(knight);
			
			col = 'e';
			row = 2;
			TriMesh king = new TriMesh("King");
			stream = mGLView.getContext().getResources().openRawResource(R.raw.king);
			dis = new DataInputStream(stream);
			king.importModel(dis);
			king.setLocalTranslation(PieceData.getColPos(col), PieceData
					.getRowPos(row), -2.8f);
			pData = new PieceData(col, row);
			pData.setDark(false);
			king.setData(pData);
			king.setMaterial(lightMat);
			board_data[PieceData.getColIndex(col)][PieceData.getRowIndex(row)] = king;
			world.attachChild(king);
			
			col = 'd';
			row = 6;
			mesh = pawn.cloneMesh("Cloned pawn");
			mesh.setLocalTranslation(PieceData.getColPos(col), PieceData
					.getRowPos(row), -2.8f);
			mesh.setData(new PieceData(col, row));
			pData = new PieceData(col, row);
			pData.setDark(true);
			mesh.setData(pData);
			mesh.setMaterial(darkMat);
			board_data[PieceData.getColIndex(col)][PieceData.getRowIndex(row)] = mesh;
			world.attachChild(mesh);
			
			col = 'e';
			row = 6;
			mesh = mesh.cloneMesh("Cloned knight");
			mesh.setLocalTranslation(PieceData.getColPos(col), PieceData
					.getRowPos(row), -2.8f);
			mesh.setData(new PieceData(col, row));
			pData = new PieceData(col, row);
			pData.setDark(true);
			mesh.setData(pData);
			mesh.setMaterial(darkMat);
			board_data[PieceData.getColIndex(col)][PieceData.getRowIndex(row)] = mesh;
			world.attachChild(mesh);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		// convert models
		try {
			convertModel(R.raw.pawn2, "/sdcard/pawn.mod");
			convertModel(R.raw.knight2, "/sdcard/knight.mod");
			convertModel(R.raw.king2, "/sdcard/king.mod");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

		world.attachChild(board);
		world.attachChild(camLeaf);
		world.updateTransform();
		world.updateWorldBound(false);
	}

	/**
	 * Converts a model from OBJ to our TriMesh's binary format.
	 * @param inputObj a resource identifier for an OBJ-file (like R.raw.whatever)
	 * @param outputMod an output file location (like "/sdcard/whatever.mod").
	 * @throws IOException if there was any error
	 */
	@SuppressWarnings("unused")
	private void convertModel(int inputObj, String outputMod) throws IOException {
		ObjLoader loader = new ObjLoader();
        AssetFileDescriptor fd = mGLView.getContext().getResources().openRawResourceFd(inputObj);
		TriMesh tPawn = loader.loadModel("Pawn", fd);
		FileOutputStream fos = new FileOutputStream(outputMod);
		DataOutputStream dos = new DataOutputStream(fos);
		tPawn.exportModel(dos);
		dos.flush();
		dos.close();
		Log.d(TAG, "Converted obj to mod: "+outputMod);
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

	/**
	 * Register a track ball click on this thread.
	 */
	public static void onTrackballClick() {
		synchronized (trackInput) {
			trackInput[3] = 1;
		}
	}
	
	/**
	 * Register a track ball move on this thread
	 * @param x relative movement x coordinate
	 * @param y relative movement y coordinate
	 */
	public static void onTrackballMove(float x, float y) {
		synchronized (trackInput) {
			trackInput[0] += x;
			trackInput[1] += y;
			trackInput[2] = 1;
		}
	}

	/**
	 * Handles the end of an animation
	 */
	@Override
	public void onAnimationEnd(KeyFrameAnimation anim, Spatial spatial) {
		if(spatial != null) {
			if(spatial instanceof CameraLeaf) {
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
