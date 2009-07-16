/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class IndexBuffer {
	private ShortBuffer indexBuffer;
	
	public IndexBuffer() {
		
	}
	
	public IndexBuffer(short[] indices) {
		setIndices(indices);
	}
	
	/**
	 * Set the index buffer from a list of shorts. 
	 * Every three indices define a new triangle.
	 * @param indices list of triangle indices
	 */
	public void setIndices(short[] indices) {
		ByteBuffer bb = ByteBuffer.allocateDirect(indices.length * 2);
		bb.order(ByteOrder.nativeOrder());
		indexBuffer = bb.asShortBuffer();
		indexBuffer.put(indices);
	}
	
	public ShortBuffer getIndices() {
		return indexBuffer;
	}
}
