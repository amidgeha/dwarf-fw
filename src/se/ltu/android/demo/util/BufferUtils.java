/* SVN FILE: $Id$ */
package se.ltu.android.demo.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public abstract class BufferUtils {
	
	public static FloatBuffer createFloatBuffer(int size) {
		ByteBuffer bb = ByteBuffer.allocateDirect(4*size);
		bb.order(ByteOrder.nativeOrder());
		return bb.asFloatBuffer();
	}
	
	public static CharBuffer createCharBuffer(int size) {
		ByteBuffer bb = ByteBuffer.allocateDirect(2*size);
		bb.order(ByteOrder.nativeOrder());
		return bb.asCharBuffer();
	}
	
	public static ByteBuffer createByteBuffer(int size) {
		ByteBuffer bb = ByteBuffer.allocateDirect(size);
		bb.order(ByteOrder.nativeOrder());
		return bb;
	}
	
}
