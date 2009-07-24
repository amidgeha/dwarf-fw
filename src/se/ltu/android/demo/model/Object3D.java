/* SVN FILE: $Id$ */
package se.ltu.android.demo.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;

import se.ltu.android.demo.intersection.AABBox;
import se.ltu.android.demo.scene.TriMesh;
import se.ltu.android.demo.util.BufferUtils;

/**
 * A simple Obj-loader.
 * Currently it only parse vertices and vertex face data.
 * It does not parse texture coordinates, normal data.
 * 
 * This class is <b>not</b> thread-safe
 * 
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
/* 
 * This one is pretty dumb. To increase rendering performance, consider
 * something like http://www.cs.sunysb.edu/~stripe/
 * 
 * To reduce loading time, consider saving your Java object to
 * a binary format.
 */
public abstract class Object3D {
	private static CharBuffer indices;
	private static FloatBuffer vertices;
	private static FloatBuffer texcoords;
	private static FloatBuffer normals;
	
	// for allocating buffers
	private static int nVertices = 0;
	private static int nFaces = 0;
	private static int nTexCoords = 0;
	private static int nNormals = 0;
	private static int nVertsPerFace = 0;
	
	// for bounding volume (at least for now)
	private static float minX = 0;
	private static float minY = 0;
	private static float minZ = 0;
	private static float maxX = 0;
	private static float maxY = 0;
	private static float maxZ = 0;
	
	/**
	 * Loads a model from an OBJ-file into a new TriMesh
	 * @param filename absolute or relative path to the OBJ-file
	 * @param name name of the new TriMesh
	 * @return the new TriMesh
	 * @throws IOException
	 */
	public static TriMesh loadModel(String name, InputStream stream1, InputStream stream2) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(stream1));
		// count data and allocate buffers
		count(in);
		vertices = BufferUtils.createFloatBuffer(3 * nVertices);
		indices = BufferUtils.createCharBuffer(nFaces * nVertsPerFace);
		normals = BufferUtils.createFloatBuffer(3 * nNormals);
		texcoords = BufferUtils.createFloatBuffer(3 * nTexCoords);
		in = new BufferedReader(new InputStreamReader(stream2));
		// load the model
		load(in);
		
		// create the TriMesh
		TriMesh mesh = new TriMesh(name, vertices, indices);
		AABBox bound = new AABBox();
		bound.minX = minX;
		bound.minY = minY;
		bound.minZ = minZ;
		bound.maxX = maxX;
		bound.maxY = maxY;
		bound.maxZ = maxZ;
		mesh.setModelBound(bound);
		
		return mesh;
	}
	
	/**
	 * Count the number of vertices, indices etc that are
	 * present in an OBJ-file. Needed to allocate buffers
	 * with a minimum capacity.
	 * @param file target OBJ-file
	 * @throws IOException 
	 */
	private static void count(BufferedReader in) throws IOException {
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
	
	private static void load(BufferedReader in) throws NumberFormatException, IOException {
		String line;
		boolean firstpass = true;

		String[] lineSplit;
		String[] faceValues;
		float[] data3f = new float[3];	// used for vertices, texcoords and normals
		int[] vertex = new int[3];

		while (((line = in.readLine()) != null)) {
			line = line.trim();
			if (line.length() > 1) {
				if (line.charAt(0) == 'v' && line.charAt(1) == ' ') {
					
					lineSplit = line.split("\\s+");
					data3f[0] = Float.parseFloat(lineSplit[1]);
					data3f[1] = Float.parseFloat(lineSplit[2]);
					data3f[2] = Float.parseFloat(lineSplit[3]);
					vertices.put(data3f);
					
					if (firstpass) {
						minX = maxX = data3f[0];
						minY = maxY = data3f[1];
						minZ = maxZ = data3f[2];
						firstpass = false;
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
					
				}
				else if (line.charAt(0) == 'v' && line.charAt(1) == 't') {
					
					lineSplit = line.split("\\s+");
					data3f[0] = Float.parseFloat(lineSplit[1]);
					data3f[1] = Float.parseFloat(lineSplit[2]);
					texcoords.put(data3f, 0, 2);
					
				}
				else if (line.charAt(0) == 'v' && line.charAt(1) == 'n') {
					
					lineSplit = line.split("\\s+");
					data3f[0] = Float.parseFloat(lineSplit[1]);
					data3f[1] = Float.parseFloat(lineSplit[2]);
					data3f[2] = Float.parseFloat(lineSplit[3]);
					
					// TODO normalize, create vertex utility class?
					normals.put(data3f);
					
				}
				else if (line.charAt(0) == 'f' && line.charAt(1) == ' ') {
					
					lineSplit = line.split("\\s+");
					
					// add first three data
					
					faceValues = lineSplit[1].split("/");
					vertex[0] = Integer.parseInt(faceValues[0]);
					faceValues = lineSplit[2].split("/");
					vertex[1] = Integer.parseInt(faceValues[0]);
					faceValues = lineSplit[3].split("/");
					vertex[2] = Integer.parseInt(faceValues[0]);
					
					// TODO, could make it much more clever
					indices.put((char) vertex[0]);
					indices.put((char) vertex[1]);
					indices.put((char) vertex[2]);
					
					// add eventual extra face data
					for(int i = 3; i < nVertsPerFace; i++) {
						faceValues = lineSplit[i+1].split("/");
						vertex[0] = vertex[1];
						vertex[1] = vertex[2];
						vertex[2] = Integer.parseInt(faceValues[0]);
						
						indices.put((char) vertex[0]);
						indices.put((char) vertex[1]);
						indices.put((char) vertex[2]);
					}
				}
				
			} // if (line.length() > 0)
		} // while (((line = in.readLine()) != null));
	} // load(BufferedReader in)
}
