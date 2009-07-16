/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * A vertex buffer holds the following information in separate
 * buffers for describing vertices and their attributes:
 * 
 * <ol>
 * <li>Coordinates</li>
 * <li>Normals</li>
 * <li>Colors</li>
 * <li>Texture Coordinates</li>
 * </ol>
 * 
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class VertexBuffer {
	private FloatBuffer coordBuffer = null;
	private FloatBuffer normalBuffer = null;
	private FloatBuffer colorBuffer = null;
	private FloatBuffer texBuffer = null;
	
	private int coordBufferSetSize;
	private int texBufferSetSize;
	private int vertexCount = 0;
	
	public VertexBuffer() {
		
	}
	
	/**
	 * Creates a new vertex buffer from an array of vertices.
	 * Assumes three coordinates per array element.
	 * @param vertices
	 */
	public VertexBuffer(float[] vertices) {
		setCoordinates(vertices, 3);
	}
	
	/**
	 * Creates a coordinate buffer from an array of fixed point integers
	 * TODO coords must come in sets of 2, 3 or 4 depending on setSize.
	 * @param setSize Specifies the number of coordinates per array element. Must be 2, 3 or 4
	 * @param verticesX coordinates of vertices
	 */
	public void setCoordinates(float[] vertices, int setSize) {	
		coordBufferSetSize = setSize;
		coordBuffer = createFloatBuffer(vertices);
		vertexCount = vertices.length / setSize;
	}
	
	/**
	 * Creates a color buffer from an array of fixed point integers
	 * TODO colors must come in sets of 4
	 * @param colorsX color of vertices
	 */
	public boolean setColors(float[] colors) {
		if(colors.length != vertexCount * 4) {
			return false;
		}
		if(colorBuffer == null || colorBuffer.capacity() != vertexCount * 4) {
			ByteBuffer bb = ByteBuffer.allocateDirect(4*colors.length);
			bb.order(ByteOrder.nativeOrder());
			colorBuffer = bb.asFloatBuffer();
		}
		colorBuffer.rewind();
		colorBuffer.put(colors);
		return true;
	}
	
	/**
	 * 
	 * @param color
	 */
	public boolean setSolidColor(float[] color) {
		if(color.length != 4) {
			return false;
		}
		if(colorBuffer == null || colorBuffer.capacity() != vertexCount*4) {
			ByteBuffer bb = ByteBuffer.allocateDirect(4*4*vertexCount);
			bb.order(ByteOrder.nativeOrder());
			colorBuffer = bb.asFloatBuffer();
		}
		colorBuffer.rewind();
		for(int i = 0; i < vertexCount; i++) {
			colorBuffer.put(color);
		}
		return true;
	}
	
	/**
	 * TODO normals must come in sets of 3
	 * @param normalsX
	 */
	public boolean setNormals(float[] normals) {
		if(normals.length != vertexCount * 3) {
			return false;
		}
		if(normalBuffer == null || normalBuffer.capacity() != vertexCount*3) {
			ByteBuffer bb = ByteBuffer.allocateDirect(4*normals.length);
			bb.order(ByteOrder.nativeOrder());
			normalBuffer = bb.asFloatBuffer();
		}
		normalBuffer.rewind();
		normalBuffer.put(normals);
		return true;
	}
	
	/**
	 * TODO coords must come in sets of 2, 3 or 4 depending on setSize.
	 * @param setSize Specifies the number of coordinates per array element. Must be 2, 3 or 4
	 * @param verticesX
	 */
	public boolean setTexCoords(float[] texcoords, int setSize) {
		if(texcoords.length != vertexCount * setSize) {
			return false;
		}
		if(texBuffer == null || texBuffer.capacity() != texcoords.length) {
			ByteBuffer bb = ByteBuffer.allocateDirect(4*texcoords.length);
			bb.order(ByteOrder.nativeOrder());
			texBuffer = bb.asFloatBuffer();
		}
		texBufferSetSize = setSize;
		texBuffer.rewind();
		texBuffer.put(texcoords);
		return true;
	}
	
	/**
	 * Creates an IntBuffer from an integer array
	 * @param data array with data
	 * @return the created IntBuffer
	 */
	private FloatBuffer createFloatBuffer(float[] data) {
		ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(data);
		return fb;
	}
	
	public FloatBuffer getCoordinates() {
		return coordBuffer;
	}
	
	public FloatBuffer getColors() {
		return colorBuffer;
	}

	public FloatBuffer getNormals() {
		return normalBuffer;
	}

	public int getCount() {
		return vertexCount;
	}
}
