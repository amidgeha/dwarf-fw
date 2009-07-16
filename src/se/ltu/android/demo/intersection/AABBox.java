/* SVN FILE: $Id$ */
package se.ltu.android.demo.intersection;

import android.util.Log;

/**
 * An Axis-Aligned Bounding Box that is defined by a minimum and a maximum
 * point.
 * 
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class AABBox {
	private final static String TAG = "AABBox";
	public float minX, minY, minZ, maxX, maxY, maxZ;
	
	/**
	 * Constructs the box with points at zero
	 */
	public AABBox() {
		minX = maxX = 0;
		minY = maxY = 0;
		minZ = maxZ = 0;
	}
	
	/**
	 * Constructs the box by giving it two points. This constructor will
	 * compare the point components to ensure that any component c0 is 
	 * smaller than c1.
	 * @param x0 first x-coordinate
	 * @param y0 first y-coordinate
	 * @param z0 first z-coordinate
	 * @param x1 second x-coordinate
	 * @param y1 second y-coordinate
	 * @param z1 second z-coordinate
	 */
	public AABBox(float x0, float y0, float z0, float x1, float y1, float z1) {
		if(x0 > x1) {
			minX = x1;
			maxX = x0;
		} else {
			minX = x0;
			maxX = x1;
		}
		if(y0 > y1) {
			minY = y1;
			maxY = y0;
		} else {
			minY = y0;
			maxY = y1;
		}
		if(z0 > z1) {
			minZ = z1;
			maxZ = z0;
		} else {
			minZ = z0;
			maxZ = z1;
		}
	}
	
	/**
	 * Apply a transformation matrix on this box
	 * As this box will still be axis aligned, the box might be larger.
	 * @param mat column-major transformation matrix to apply
	 */
	public void transform(float[] matrix) {
		if(matrix.length != 16) {
			Log.e(TAG, "The matrix size is wrong");
			return;
		}
		float av, bv;
		int col, row;
		float[] oldMin = {minX, minY, minZ};
		float[] oldMax = {maxX, maxY, maxZ};
		float[] newMin = {matrix[12], matrix[13], matrix[14]};
		float[] newMax = {matrix[12], matrix[13], matrix[14]};
		for (col = 0; col < 3; col++) {
			for (row = 0; row < 3; row++)
			{
				av = matrix[row+col*4] * oldMin[row];
				bv = matrix[row+col*4] * oldMax[row];
				if (av < bv)
				{
					newMin[col] += av;
					newMax[col] += bv;
				} else {
					newMin[col] += bv;
					newMax[col] += av;
				}
			}
		}
		// set the values to this box
		minX = newMin[0];
		minY = newMin[1];
		minZ = newMin[2];
		maxX = newMax[0];
		maxY = newMax[1];
		maxZ = newMax[2];
	}
	
	/**
	 * Apply a transformation matrix on another box and set the result
	 * on this box. This box current values will be overwritten while the
	 * other box will not be touched.
	 * @param mat column-major transformation matrix to apply
	 */
	public void transform(float[] matrix, AABBox other) {
		if(matrix.length != 16) {
			Log.e(TAG, "The matrix size is wrong");
			return;
		}
		float av, bv;
		int col, row;
		float[] oldMin = {other.minX, other.minY, other.minZ};
		float[] oldMax = {other.maxX, other.maxY, other.maxZ};
		float[] newMin = {matrix[12], matrix[13], matrix[14]};
		float[] newMax = {matrix[12], matrix[13], matrix[14]};
		for (col = 0; col < 3; col++) {
			for (row = 0; row < 3; row++)
			{
				av = matrix[row+col*4] * oldMin[row];
				bv = matrix[row+col*4] * oldMax[row];
				if (av < bv)
				{
					newMin[col] += av;
					newMax[col] += bv;
				} else {
					newMin[col] += bv;
					newMax[col] += av;
				}
			}
		}
		// set the values to this box
		minX = newMin[0];
		minY = newMin[1];
		minZ = newMin[2];
		maxX = newMax[0];
		maxY = newMax[1];
		maxZ = newMax[2];
	}
	
	public boolean isSet() {
		return minX == maxX && minY == maxY && minZ == maxZ;
	}
}


