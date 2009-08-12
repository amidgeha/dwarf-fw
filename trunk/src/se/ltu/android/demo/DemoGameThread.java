/* SVN FILE: $Id$ */
package se.ltu.android.demo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

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
import se.ltu.android.demo.scene.camera.Camera;
import se.ltu.android.demo.scene.intersection.PickResult;
import se.ltu.android.demo.scene.intersection.Ray;
import se.ltu.android.demo.scene.shapes.*;
import se.ltu.android.demo.scene.state.Light;
import se.ltu.android.demo.scene.state.Material;
import se.ltu.android.demo.sensors.SensorHandler;
import se.ltu.android.demo.util.ObjLoader;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class DemoGameThread extends Thread implements AnimationListener {
	private final static String TAG = "GameThread";
	private final static int TARGET_FPS = 25;
	private static final float SELECT_COLOR_INC = 1.4f;
	public static long timePerFrame = 1000;
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
	
	private long timeTarget;
	private Node world;
	private TriMesh[][] board_data = new TriMesh[8][8];
	private DemoGLSurfaceView mGLView;
	private boolean isRunning = true;
	private boolean isPaused = false;
	private TriMesh pickedMesh;
	private Material savedMat;
	private Material selectedMat = new Material();
	private Camera[] camList; 	// list of available cameras
	private int iCam = 0; 		// camera pointer
	private boolean moving_piece = false;
	private boolean moving_camera = false;
	private CameraNode camNode = new CameraNode("Camera");	//used for animation camera movement
	private float[] modelM = new float[16];
	private int iCamSensor = 0;

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
		//Debug.startMethodTracing("mtrace");
		while (isRunning) {
			while (isPaused && isRunning) {
				try {
					sleep(100);
				} catch (InterruptedException e) {
				}
			}
			// this thread have some control over the frame rate
			// especially timePerFrame will be useful in
			// animations
			timePerFrame = System.currentTimeMillis() - lastTime;
			lastTime = System.currentTimeMillis();
			update();
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
		// updateState();
		updateInput();
		// updateAI();
		// updatePhysics();
		world.update(timePerFrame); // updates animations
		updateCamera();
		// updateSound();
		mGLView.requestRender();
	}

	private void updateCamera() {
		if (iCam == iCamSensor) {
			SensorHandler.getRotM4(modelM);
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
				mGLView.getRenderer().setCamera(camList[iCam]);
			}
			trackInput[3] = 0;
			if (trackInput[2] != 0) {
				/*
				if(iCam == iCamSensor) {
					// special case because of inverse rotation matrix ?
					trackInput[1] = -trackInput[1];
				}
				*/
				camList[iCam].translate(0, 0, trackInput[1]);
				trackInput[0] = 0;
				trackInput[1] = 0;
				trackInput[2] = 0;
			}
		}
	}

	private void handleSingleTap(PickResult result) {
		Log.d(TAG, "Single tap");
		Spatial spatial = result.getFirst();
		if (spatial.hasPieceData()) {
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

	/**
	 * 
	 */
	private void unselectPick() {
		pickedMesh.setMaterial(savedMat);
		pickedMesh = null;		
	}
	
	private void selectPick(TriMesh spatial) {
		float[] color4f;
		pickedMesh = spatial;
		savedMat = pickedMesh.getMaterial();
		selectedMat.copyFrom(savedMat);
		color4f = selectedMat.getAmbient();
		increaseColor(color4f, SELECT_COLOR_INC);
		color4f = selectedMat.getDiffuse();
		increaseColor(color4f, SELECT_COLOR_INC);
		pickedMesh.setMaterial(selectedMat);
	}

	private void increaseColor(float[] color4f, float amount) {
		color4f[0] = Math.min(color4f[0] * amount, 1.0f);
		color4f[1] = Math.min(color4f[1] * amount, 1.0f);
		color4f[2] = Math.min(color4f[2] * amount, 1.0f);
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
		
		Material lightMat = new Material();
		lightMat.setAmbient(1, 0.5f, 0, 1);
		lightMat.setDiffuse(1, 0.5f, 0, 1);
		lightMat.setSpecular(0.9f, 0.9f, 0.9f, 1.0f);
		lightMat.setShininess(40);
		
		Material darkMat = new Material();
		darkMat.setAmbient(0, 0.4f, 0.8f, 1);
		darkMat.setDiffuse(0, 0.4f, 0.8f, 1);
		darkMat.setSpecular(0.9f, 0.9f, 0.9f, 1.0f);
		darkMat.setShininess(40);
		
		try {
			char col = 'c';
			int row = 2;
			TriMesh pawn = new TriMesh("Pawn");
			InputStream stream = mGLView.getContext().getResources().openRawResource(R.raw.pawn);
			DataInputStream dis = new DataInputStream(stream);
			pawn.importModel(dis);
			pawn.setLocalTranslation(PieceData.getColPos(col), PieceData
					.getRowPos(row), -2.8f);
			pawn.setPieceData(new PieceData(col, row));
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
			knight.setPieceData(new PieceData(col, row));
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
			king.setPieceData(new PieceData(col, row));
			king.setMaterial(lightMat);
			board_data[PieceData.getColIndex(col)][PieceData.getRowIndex(row)] = king;
			world.attachChild(king);
			
			col = 'd';
			row = 6;
			mesh = pawn.cloneMesh("Cloned pawn");
			mesh.setLocalTranslation(PieceData.getColPos(col), PieceData
					.getRowPos(row), -2.8f);
			mesh.setPieceData(new PieceData(col, row));
			mesh.setMaterial(darkMat);
			board_data[PieceData.getColIndex(col)][PieceData.getRowIndex(row)] = mesh;
			world.attachChild(mesh);
			
			col = 'e';
			row = 6;
			mesh = mesh.cloneMesh("Cloned knight");
			mesh.setLocalTranslation(PieceData.getColPos(col), PieceData
					.getRowPos(row), -2.8f);
			mesh.setPieceData(new PieceData(col, row));
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
		world.attachChild(camNode);
		world.updateTransform();
		world.updateWorldBound(false);
	}

	/**
	 * @param inputObj
	 * @param outputMod
	 * @throws IOException 
	 */
	@SuppressWarnings("unused")
	private void convertModel(int inputObj, String outputMod) throws IOException {
		ObjLoader loader = new ObjLoader();
        InputStream stream1 = mGLView.getContext().getResources().openRawResource(inputObj);
        InputStream stream2 = mGLView.getContext().getResources().openRawResource(inputObj);
		TriMesh tPawn = loader.loadModel("Pawn", stream1, stream2);
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
