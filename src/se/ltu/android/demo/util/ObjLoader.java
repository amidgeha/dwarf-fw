/* SVN FILE: $Id$ */
package se.ltu.android.demo.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;

import android.content.res.AssetFileDescriptor;
import android.opengl.Matrix;
import android.util.Log;

import se.ltu.android.demo.scene.TriMesh;
import se.ltu.android.demo.scene.intersection.AABBox;

/**
 * A simple Obj-loader. It parses vertices, UV coordinates, normals and face descriptions.
 * It currently only works for triangulated faces.<br><br>
 * Since this model loader is quite slow and memory intensive, consider saving the resulting
 * TriMesh to a binary format by using the export method.
 * 
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class ObjLoader {
	private final static String TAG = "ObjLoader";

	// the result will end up in these buffers
	private CharBuffer indices;
	private FloatBuffer vertices;
	private FloatBuffer texcoords;
	private FloatBuffer normals;

	// data read from the model file
	private float[] tmpVertices;
	private float[] tmpNormals; // normalized
	private float[] tmpTexCoords;

	// storage for face values
	private ArrayList<FaceValues> faceSortedValues;
	private ArrayList<FaceValues> faceValues;

	// for allocating buffers
	private int nVertices = 0;
	private int nFaces = 0;
	private int nTexCoords = 0;
	private int nNormals = 0;
	private int nVertsPerFace = 0;

	// for bounding volume (or centering or stuff in future implementations?)
	private float minX = 0;
	private float minY = 0;
	private float minZ = 0;
	private float maxX = 0;
	private float maxY = 0;
	private float maxZ = 0;

	private int yeah = 0;
	private int doh = 0;

	/**
	 * Loads a model from an OBJ-file into a new TriMesh
	 * 
	 * @param name
	 *            name of the new TriMesh
	 * @param fd a file descriptor for the model to be imported
	 * @return the new TriMesh or null if the file could not be imported
	 * @throws IOException if there was an error loading the model
	 */
	public TriMesh loadModel(String name, AssetFileDescriptor fd) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(fd.createInputStream()), 8192);
		TriMesh mesh = null;

		// count occurrences first
		count(in);
		if (nVertices == 0) {
			Log.e(TAG, "No vertices found in file");
			return null;
		}
		if (nFaces == 0) {
			Log.e(TAG, "No face descriptions found in file");
			return null;
		}
		if (nVertsPerFace != 3) {
			Log.e(TAG, "Can not parse non-triangluar faces");
			return null;
		}

		// this is the same regardless of the cases below
		indices = BufferUtils.createCharBuffer(nFaces * nVertsPerFace);
		in = new BufferedReader(new InputStreamReader(fd.createInputStream()), 8192);
		
		if (nNormals == 0 && nTexCoords == 0) {
			// we can directly use faces as indices
			vertices = BufferUtils.createFloatBuffer(3 * nVertices);
			// loads indices to index buffer and vertices to vertex buffer
			load(in, true);
			mesh = new TriMesh(name, vertices, indices);
		} else {
			// need to be more clever
			tmpVertices = new float[3 * nVertices];
			if (nNormals > 0) {
				tmpNormals = new float[3 * nNormals];
			}
			if (nTexCoords > 0) {
				tmpTexCoords = new float[3 * nTexCoords];
			}
			faceSortedValues = new ArrayList<FaceValues>(nVertices);
			faceValues = new ArrayList<FaceValues>(nVertices);
			
			// loads indices to index buffer and triples to faceValues
			// indices now points to rows in faceValues
			load(in, false);
			
			int nFaceValues = faceValues.size();
			vertices = BufferUtils.createFloatBuffer(3 * nFaceValues);
			if(nNormals > 0) {
				normals = BufferUtils.createFloatBuffer(3 * nFaceValues);
			}
			if(nTexCoords > 0) {
				texcoords = BufferUtils.createFloatBuffer(2 * nFaceValues);
			}
			float[] fv_data;
			for(int i = 0; i < faceValues.size(); i++) {
				fv_data = faceValues.get(i).data;
				vertices.put(fv_data, 0, 3);
				if(nNormals > 0) {
					normals.put(fv_data, 3, 3);
				}
				if(nTexCoords > 0) {
					texcoords.put(fv_data, 6, 2);
				}
			}
			
			mesh = new TriMesh(name, vertices, indices);
			if(nNormals > 0) {
				mesh.setNormals(normals);
			}
			if(nTexCoords > 0) {
				mesh.setTexCoords(texcoords);
			}
		}

		// create the TriMesh
		AABBox bound = new AABBox();
		bound.minX = minX;
		bound.minY = minY;
		bound.minZ = minZ;
		bound.maxX = maxX;
		bound.maxY = maxY;
		bound.maxZ = maxZ;
		mesh.setModelBound(bound);

		// free loads of resources...
		tmpVertices = null;
		tmpNormals = null;
		tmpTexCoords = null;
		faceSortedValues = null;
		faceValues = null;
		
		Log.d(TAG, "Unique vertices: "+doh);
		Log.d(TAG, "Similar vertices: "+yeah);
		return mesh;
	}

	/**
	 * Count the number of vertices, indices etc that are present in an
	 * OBJ-file. Needed to allocate buffers with a minimum capacity.
	 * 
	 * @param file
	 *            target OBJ-file
	 * @throws IOException
	 */
	private void count(BufferedReader in) throws IOException {
		String line;
		while (((line = in.readLine()) != null)) {
			line = line.trim();
			if (line.length() > 0) {
				if (line.charAt(0) == 'v' && line.charAt(1) == ' ')
					nVertices++;
				if (line.charAt(0) == 'v' && line.charAt(1) == 't')
					nTexCoords++;

				if (line.charAt(0) == 'v' && line.charAt(1) == 'n')
					nNormals++;

				if (line.charAt(0) == 'f' && line.charAt(1) == ' ') {
					if (nVertsPerFace == 0) {
						nVertsPerFace = line.split("\\s+").length - 1;
					}
					nFaces++;
				}
			}
		}
	}

	private void load(BufferedReader in, boolean toBuffer) throws NumberFormatException, IOException {
		String line;
		String[] lineSplit;
		String[] faceSplit;
		float[] data3f = new float[3];	// used for vertices, texcoords and normals
		int iRawVertices = 0;
		int iRawNormals = 0;
		int iRawTexCoords = 0;
		float invLength;
		int[] tmpData = null;
		if(!toBuffer) {
			tmpData = new int[3];
		}
		FaceValues fv;

		while (((line = in.readLine()) != null)) {
			line = line.trim();
			if (line.length() > 1) {
				if (line.charAt(0) == 'v' && line.charAt(1) == ' ') {
					
					lineSplit = line.split("\\s+");
					data3f[0] = Float.parseFloat(lineSplit[1]);
					data3f[1] = Float.parseFloat(lineSplit[2]);
					data3f[2] = Float.parseFloat(lineSplit[3]);
					
					if (iRawVertices == 0) {
						minX = maxX = data3f[0];
						minY = maxY = data3f[1];
						minZ = maxZ = data3f[2];
					} else {
						if (data3f[0] < minX)
							minX = data3f[0];
						if (data3f[0] > maxX)
							maxX = data3f[0];
						if (data3f[1] < minY)
							minY = data3f[1];
						if (data3f[1] > maxY)
							maxY = data3f[1];
						if (data3f[2] < minZ)
							minZ = data3f[2];
						if (data3f[2] > maxZ)
							maxZ = data3f[2];
					}
					
					if(toBuffer) {
						vertices.put(data3f);
					} else {
						tmpVertices[iRawVertices] = data3f[0];
						tmpVertices[iRawVertices+1] = data3f[1];
						tmpVertices[iRawVertices+2] = data3f[2];
						iRawVertices += 3;
					}
					
				}
				else if (line.charAt(0) == 'v' && line.charAt(1) == 't') {
					
					if(!toBuffer) {
					
						lineSplit = line.split("\\s+");
						data3f[0] = Float.parseFloat(lineSplit[1]);
						data3f[1] = Float.parseFloat(lineSplit[2]);
						
						tmpTexCoords[iRawTexCoords] = data3f[0];
						tmpTexCoords[iRawTexCoords+1] = data3f[1];
						iRawVertices += 2;
					
					}
					
				}
				else if (line.charAt(0) == 'v' && line.charAt(1) == 'n') {
					
					if(!toBuffer) {
					
						lineSplit = line.split("\\s+");
						data3f[0] = Float.parseFloat(lineSplit[1]);
						data3f[1] = Float.parseFloat(lineSplit[2]);
						data3f[2] = Float.parseFloat(lineSplit[3]);
					
						// normalize... 
						invLength =  1/Matrix.length(data3f[0], data3f[1], data3f[2]);
						data3f[0] *= invLength;
						data3f[1] *= invLength;
						data3f[2] *= invLength;
					
						tmpNormals[iRawNormals] = data3f[0];
						tmpNormals[iRawNormals+1] = data3f[1];
						tmpNormals[iRawNormals+2] = data3f[2];
						iRawNormals += 3;
					}
					
				}
				else if (line.charAt(0) == 'f' && line.charAt(1) == ' ') {
					
					// avoid unnecessary NumberFormatExpection
					line = line.replace("//", "/0/");
					lineSplit = line.split("\\s+");
					
					// go through each face triangle data
					for(int i = 1; i < 4; i++) {
						// faceSplit = "vertex/texcoord/normal"
						faceSplit = lineSplit[i].split("/");
						
						if(toBuffer) {
							indices.put((char) (Integer.parseInt(faceSplit[0])-1));
						} else {
							tmpData[0] = 3 * (Integer.parseInt(faceSplit[0]) - 1);
							tmpData[1] = 2 * (Integer.parseInt(faceSplit[1]) - 1);
							tmpData[2] = 3 * (Integer.parseInt(faceSplit[2]) - 1);
							fv = new FaceValues();
							fv.data[0] = tmpVertices[tmpData[0]];
							fv.data[1] = tmpVertices[tmpData[0]+1];
							fv.data[2] = tmpVertices[tmpData[0]+2];
							if(tmpNormals != null) {
								fv.data[3] = tmpNormals[tmpData[2]];
								fv.data[4] = tmpNormals[tmpData[2]+1];
								fv.data[5] = tmpNormals[tmpData[2]+2];
							}
							if(tmpTexCoords != null) {
								fv.data[6] = tmpTexCoords[tmpData[1]];
								fv.data[7] = tmpTexCoords[tmpData[1]+1];
							}
							indices.put(addVertex(fv));
						}
					}
				}
				
			} // if (line.length() > 0)
		} // while (((line = in.readLine()) != null));
	} // load(BufferedReader in)

	private char addVertex(FaceValues values) {
		// find the vertex with binary search
		int i = Collections.binarySearch(faceSortedValues, values);
		if (i < 0) {
			// the vertex was not found and can be inserted
			// at position -(i + 1)
			faceSortedValues.add(-(i + 1), values);
			values.index = (char)faceValues.size();
			faceValues.add(values);
			doh++;
			return values.index;
		}
		yeah++;
		return faceSortedValues.get(i).index;
	}

	private class FaceValues implements Comparable<FaceValues> {
		/*
		 * Data array is structured as 
		 * 1. Vertex (3 separate x,y,z coordinates)
		 * 2. Normal (3 separate x,y,z coordinates)
		 * 3. Texture Coordinates (2 separate s,t coordinates)
		 */
		float[] data;
		char index;

		/**
		 * 
		 */
		public FaceValues() {
			data = new float[8];
		}

		@Override
		public int compareTo(FaceValues other) {
			for (int i = 0; i < data.length; i++) {
				if (this.data[i] < other.data[i]) {
					return -1;
				}
				if (this.data[i] > other.data[i]) {
					return 1;
				}
			}
			return 0;
		}
	}
}