/* SVN FILE: $Id$ */
package se.ltu.android.demo.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public abstract class BufferUtils {
	
	public static ByteBuffer createByteBuffer(int size) {
		ByteBuffer bb = ByteBuffer.allocateDirect(size);
		bb.order(ByteOrder.nativeOrder());
		return bb;
	}
	
	public static CharBuffer createCharBuffer(int size) {
		ByteBuffer bb = ByteBuffer.allocateDirect(2*size);
		bb.order(ByteOrder.nativeOrder());
		return bb.asCharBuffer();
	}
	
	public static FloatBuffer createFloatBuffer(int size) {
		ByteBuffer bb = ByteBuffer.allocateDirect(4*size);
		bb.order(ByteOrder.nativeOrder());
		return bb.asFloatBuffer();
	}
	
	/**
	 * @param len
	 * @return
	 */
	private static IntBuffer createIntBuffer(int size) {
		ByteBuffer bb = ByteBuffer.allocateDirect(4*size);
		bb.order(ByteOrder.nativeOrder());
		return bb.asIntBuffer();
	}

	/**
	 * Converts a float buffer to a fixed point int buffer
	 * @param vertices a FloatBuffer
	 * @return an IntBuffer containing fixed points
	 */
	public static IntBuffer toFixedBuffer(FloatBuffer floatBuffer) {
		int len = floatBuffer.capacity();
		IntBuffer fixedBuffer = createIntBuffer(len);
		floatBuffer.clear();
		fixedBuffer.clear();
		for(int i = 0; i < len; i++) {
			fixedBuffer.put((int)(floatBuffer.get(i)*65536));
		}
		fixedBuffer.clear();
		return null;
	}
	
}
