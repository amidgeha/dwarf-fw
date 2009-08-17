/* SVN FILE: $Id$ */
package se.ltu.android.demo.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * An utility class for java.nio.Buffers.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public abstract class BufferUtils {
	
	/**
	 * @param size number of bytes the buffer should hold
	 * @return the newly allocated byte buffer
	 */
	public static ByteBuffer createByteBuffer(int size) {
		ByteBuffer bb = ByteBuffer.allocateDirect(size);
		bb.order(ByteOrder.nativeOrder());
		return bb;
	}
	
	/**
	 * @param size number of chars the buffer should hold
	 * @return the newly allocated char buffer
	 */
	public static CharBuffer createCharBuffer(int size) {
		ByteBuffer bb = ByteBuffer.allocateDirect(2*size);
		bb.order(ByteOrder.nativeOrder());
		return bb.asCharBuffer();
	}
	
	/**
	 * @param size number of floats the buffer should hold
	 * @return the newly allocated float buffer
	 */
	public static FloatBuffer createFloatBuffer(int size) {
		ByteBuffer bb = ByteBuffer.allocateDirect(4*size);
		bb.order(ByteOrder.nativeOrder());
		return bb.asFloatBuffer();
	}
	
	/**
	 * @param size number of integers the buffer should hold
	 * @return the newly allocated integer buffer
	 */
	private static IntBuffer createIntBuffer(int size) {
		ByteBuffer bb = ByteBuffer.allocateDirect(4*size);
		bb.order(ByteOrder.nativeOrder());
		return bb.asIntBuffer();
	}

	/**
	 * Converts a float buffer to a fixed point int buffer
	 * @param floatBuffer a FloatBuffer
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
