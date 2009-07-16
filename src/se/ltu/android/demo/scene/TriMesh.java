/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class TriMesh extends Spatial {
	public static final String TAG = "TriMesh";
	public static final int TRIANGLES = GL10.GL_TRIANGLES;
	public static final int TRIANGLE_STRIP = GL10.GL_TRIANGLE_STRIP;
	public static final int TRIANGLE_FAN = GL10.GL_TRIANGLE_FAN;
	
	public static final float[] RED = {1,0,0,1};
	public static final float[] GREEN = {0,1,0,1};
	public static final float[] BLUE = {0,0,1,1};
	
	protected int draw_mode = TRIANGLES;
	/**
	 * Local center point of this geometry in x,y,z.
	 */
	protected float[] center = {0.0f, 0.0f, 0.0f};
	protected Vector<IndexBuffer> indexBuffers;
	protected VertexBuffer vertexBuffer;
	
	public TriMesh(String name) {
		super(name);
	}
	
	public void setVertexBuffer(VertexBuffer vb) {
		vertexBuffer = vb;
	}
	
	public void addIndexBuffer(IndexBuffer ib) {
		if(indexBuffers == null) {
			indexBuffers = new Vector<IndexBuffer>();
		}
		indexBuffers.add(ib);
	}
	
	public void setIndexBuffers(Vector<IndexBuffer> ibs) {
		indexBuffers = ibs;
	}
	
	/**
	 * Create a new index buffer containing only one array
	 * @param ib
	 */
	public void setIndexBuffer(IndexBuffer ib) {
		indexBuffers = new Vector<IndexBuffer>(1);
		indexBuffers.add(ib);
	}
	
	public Vector<IndexBuffer> getIndexBuffers() {
		return indexBuffers;
	}
	
	public VertexBuffer getVertexBuffer() {
		return vertexBuffer;
	}
	
	public void setSolidColor(float[] color) {
		vertexBuffer.setSolidColor(color);
	}

	@Override
	public void draw(GL10 gl) {
		FloatBuffer vertices = vertexBuffer.getCoordinates();
		FloatBuffer colors = vertexBuffer.getColors();
		FloatBuffer normals = vertexBuffer.getNormals();
		
		// test for null first so we can return without manipulating the stack
		// TODO check indices here too
		if(vertices == null) {
			Log.e(TAG, "Vertices are null in: "+name);
			return;
		}
		
		gl.glPushMatrix();
		gl.glMultMatrixf(transM, 0);
		
		vertices.rewind();
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertices);
		if(colors != null) {
			colors.rewind();
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			gl.glColorPointer(4, GL10.GL_FLOAT, 0, colors);
		}
		if(normals != null) {
			normals.rewind();
			gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
			gl.glNormalPointer(GL10.GL_FLOAT, 0, normals);
		}
    	for(IndexBuffer ib : indexBuffers) {
    		ShortBuffer indices = ib.getIndices();
    		if(indices == null) {
    			Log.e(TAG, "Indices is null in: "+name);
    		}
    		indices.rewind();
    		gl.glDrawElements(draw_mode, indices.limit(), GL10.GL_UNSIGNED_SHORT, indices);
    	}
    	if(colors != null) {
    		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    	}
    	if(normals != null) {
    		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
    	}
    	
    	gl.glPopMatrix();
	}
	
	public void updateBound() {
		float tmpX,tmpY,tmpZ;
		FloatBuffer vertices = vertexBuffer.getCoordinates();
		int limit = vertices.limit();
		
		for(int pos = 0; pos < limit; pos += 3) {
			tmpX = vertices.get(pos);
			tmpY = vertices.get(pos+1);
			tmpZ = vertices.get(pos+2);
			if(pos == 0) {
				bound.minX = bound.maxX = tmpX;
				bound.minY = bound.maxY = tmpY;
				bound.minZ = bound.maxZ = tmpZ;
			} else {
				if(tmpX < bound.minX)
					bound.minX = tmpX;
				if(tmpY < bound.minY)
					bound.minY = tmpY;
				if(tmpZ < bound.minZ)
					bound.minZ = tmpZ;
				if(tmpX > bound.maxX)
					bound.maxX = tmpX;
				if(tmpY > bound.maxY)
					bound.maxY = tmpY;
				if(tmpZ > bound.maxZ)
					bound.maxZ = tmpZ;
			}
		}
		bound.transform(transM);
	}
}
