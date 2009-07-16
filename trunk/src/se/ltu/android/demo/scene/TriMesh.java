/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import se.ltu.android.demo.intersection.AABBox;

import android.util.Log;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class TriMesh extends Spatial {
	public static final String TAG = "TriMesh";
	public static final int MODE_TRIANGLES = GL10.GL_TRIANGLES;
	public static final int MODE_TRIANGLE_STRIP = GL10.GL_TRIANGLE_STRIP;
	public static final int MODE_TRIANGLE_FAN = GL10.GL_TRIANGLE_FAN;
	
	protected AABBox modelBound;
	protected int drawMode = MODE_TRIANGLES;
	protected int vertexCount = 0;
	/**
	 * Local center point of this geometry in x,y,z.
	 */
	protected float[] center = {0.0f, 0.0f, 0.0f};
	protected ShortBuffer indices;
	//private, so no one can circumvent the hasDirtyModelBound check
	private FloatBuffer vertices;
	protected FloatBuffer normals;
	protected FloatBuffer colors;
	protected FloatBuffer texcoords;
	protected boolean hasDirtyModelBound = true;
	protected TriMesh cloneTarget = null;
	
	public TriMesh(String name) {
		super(name);
		modelBound = new AABBox();
	}
	
	/**
	 * Creates a clone of this mesh. 
	 * The clone shares vertices, indices etc
	 * with the original but the buffers are write protected.
	 * If you change the original TriMesh's buffers, those changes
	 * will be visible in the clone.
	 * @return the cloned TriMesh
	 */
	public TriMesh cloneMesh() {
		return cloneMesh(name);
		
	}
	
	/**
	 * Creates a clone of this mesh. The clone shares vertices, indices etc
	 * with the original but the buffers are write protected.
	 * If you change the original TriMesh's buffers, those changes
	 * will be visible in the clone.
	 * @param name name of the clone
	 * @return the cloned TriMesh
	 */
	public TriMesh cloneMesh(String name) {
		TriMesh clone = new TriMesh(name);
		clone.cloneTarget = this;
		clone.modelBound = modelBound;
		clone.drawMode = drawMode;
		clone.vertexCount = vertexCount;
		clone.vertices = vertices.asReadOnlyBuffer();
		clone.indices = indices.asReadOnlyBuffer();
		clone.normals = normals.asReadOnlyBuffer();
		clone.colors = colors.asReadOnlyBuffer();
		clone.texcoords = texcoords.asReadOnlyBuffer();
		
		clone.setLocalTranslation(locTranslation);
		clone.setLocalRotation(locRotation);
		clone.setLocalScale(locScale);
		return clone;
	}
	
	@Override
	public void draw(GL10 gl) {	
		// test for null first so we can return without manipulating the stack
		if(vertices == null) {
			Log.e(TAG, "Vertices are null in: "+name);
			return;
		}
		if(indices == null) {
			Log.e(TAG, "Vertices are null in: "+name);
			return;
		}
		gl.glPushMatrix();
		gl.glMultMatrixf(transM, 0);
		
		vertices.rewind();
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertices);
		
		// enable non-mandatory arrays if found
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
		
		// do the drawing
		indices.rewind();
    	gl.glDrawElements(drawMode, indices.limit(), GL10.GL_UNSIGNED_SHORT, indices);
    	
    	// disable non-mandatory arrays
    	if(colors != null) {
    		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    	}
    	if(normals != null) {
    		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
    	}
    	
    	gl.glPopMatrix();
	}
	
	/**
	 * 
	 * @param colorArray
	 * @return
	 */
	public void setColors(float[] colorArray) {
		int size = colorArray.length;
		if(size != vertexCount * 4) {
			Log.e(TAG, "Invalid array length (Expected: "
					+vertexCount*4+", Found: "+size+") in "+name);
			return;
		}
		if(colors == null || colors.capacity() != size) {
			colors = allocateFloatBuffer(size);
		}
		colors.clear();
		colors.put(colorArray);
		return;
	}
	
	public void setIndices(short[] indexArray) {
		int size = indexArray.length;
		if(indices == null || indices.capacity() != size) {
			indices = allocateShortBuffer(size);
		}
		indices.clear();
		indices.put(indexArray);
		return;
	}
	
	/**
	 * 
	 * @param normals
	 * @return
	 */
	public void setNormals(float[] normalArray) {
		int size = normalArray.length;
		if(size != vertexCount * 3) {
			Log.e(TAG, "Invalid normal array length (Expected: "
					+4+", Found: "+size+") in "+name);
			return;
		}
		if(normals == null || normals.capacity() != size) {
			normals = allocateFloatBuffer(size);
		}
		normals.clear();
		normals.put(normalArray);
		return;
	}
	
	/**
	 * 
	 * @param color4f
	 * @return
	 */
	public void setSolidColor(float[] color4f) {
		int size = color4f.length;
		if(size != 4) {
			Log.e(TAG, "Invalid array length (Expected: "
					+4+", Found: "+size+") in "+name);
			return;
		}
		if(colors == null || colors.capacity() != vertexCount*4) {
			colors = allocateFloatBuffer(4*vertexCount);
		}
		colors.clear();
		for(int i = 0; i < vertexCount; i++) {
			colors.put(color4f);
		}
		return;
	}
	
	/**
	 * 
	 * @param texcoords
	 * @return
	 */
	public void setTexCoords(float[] texcoordsArray) {
		int size = texcoordsArray.length;
		if(size != vertexCount * 2) {
			Log.e(TAG, "Invalid texture coordinate array length (Expected: "
					+vertexCount*2+", Found: "+size+") in "+name);
			return;
		}
		if(texcoords == null || texcoords.capacity() != size) {
			texcoords = allocateFloatBuffer(size);
		}
		texcoords.clear();
		texcoords.put(texcoordsArray);
	}
	
	/**
	 * 
	 * @param normals
	 * @return
	 */
	protected void setVertices(float[] vertexArray) {
		int size = vertexArray.length;
		if(vertices != null) {
			Log.e(TAG, "Setting vertices twice is forbidden! In "+name);
			return;
		}
		if(size % 3 != 0) {
			Log.e(TAG, "Invalid vertex array length (Found: "
					+size+", not divisable by 3) in "+name);
			return;
		}
		vertexCount = size/3;
		vertices = allocateFloatBuffer(size);
		vertices.clear();
		vertices.put(vertexArray);
		hasDirtyModelBound = true;
		return;
	}
	
	/**
	 * Updates the bounding volume for this mesh
	 * This method uses the mesh's world transformation matrix so
	 * ensure that the matrix is valid (or call updateTransform() on
	 * this mesh before calling this method).
	 */
	@Override
	public void updateModelBound() {
		// For a clone, we update the targets bound
		if(cloneTarget != null) {
			cloneTarget.updateModelBound();
			return;
		}
		
		// For an original TriMesh, we only update
		// when our vertices has changed
		if(!hasDirtyModelBound) {
			return;
		}
		
		float tmpX,tmpY,tmpZ;
		int limit = vertices.limit();
		
		for(int pos = 0; pos < limit; pos += 3) {
			tmpX = vertices.get(pos);
			tmpY = vertices.get(pos+1);
			tmpZ = vertices.get(pos+2);
			if(pos == 0) {
				modelBound.minX = modelBound.maxX = tmpX;
				modelBound.minY = modelBound.maxY = tmpY;
				modelBound.minZ = modelBound.maxZ = tmpZ;
			} else {
				if(tmpX < modelBound.minX)
					modelBound.minX = tmpX;
				if(tmpY < modelBound.minY)
					modelBound.minY = tmpY;
				if(tmpZ < modelBound.minZ)
					modelBound.minZ = tmpZ;
				if(tmpX > modelBound.maxX)
					modelBound.maxX = tmpX;
				if(tmpY > modelBound.maxY)
					modelBound.maxY = tmpY;
				if(tmpZ > modelBound.maxZ)
					modelBound.maxZ = tmpZ;
			}
		}
		
		hasDirtyModelBound = false;
	}

	@Override
	public void updateWorldBound(boolean propagate) {
		if(hasDirtyModelBound) {
			updateModelBound();
		}
		worldBound.transform(transM, modelBound);
		if(propagate && parent != null) {
			parent.updateWorldBound(this);
		}
	}
	
	private FloatBuffer allocateFloatBuffer(int size) {
		ByteBuffer bb = ByteBuffer.allocateDirect(4*size);
		bb.order(ByteOrder.nativeOrder());
		return bb.asFloatBuffer();
	}
	
	private ShortBuffer allocateShortBuffer(int size) {
		ByteBuffer bb = ByteBuffer.allocateDirect(2*size);
		bb.order(ByteOrder.nativeOrder());
		return bb.asShortBuffer();
	}
}
