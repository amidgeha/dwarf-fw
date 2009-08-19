/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene.shapes;

import se.ltu.android.demo.scene.TriMesh;

/**
 * A quadrilateral.
 * 
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class Quad extends TriMesh {
	private final static char[] INDICES = {
		0,1,2,3
	};
	private final static float[] NORMALS = {
			0,0,1,0,0,1,0,0,1,0,0,1
	};
	private final static float[] TEXCOORDS = {
			0,0,1,0,1,1,0,1
	};
	
	private float extX;
	private float extY;
	
	/**
	 * Constructs a quad with given dimensions
	 * @param name for identifying purposes
	 * @param width length in x-axis
	 * @param height length in y-axis
	 */
	public Quad(String name, float width, float height) {
		super(name);
		extX = width/2.0f;
		extY = height/2.0f;
		construct();
	}

	private void construct() {
		drawMode = MODE_TRIANGLE_FAN;
		float minX = center[0] - extX;
		float maxX = center[0] + extX;
		float minY = center[1] - extY;
		float maxY = center[1] + extY;
		
		float[] vertices = {
				minX, minY, 0,
				maxX, minY, 0,
				maxX, maxY, 0,
				minX, maxY, 0
		};
		
		setVertices(vertices);
		setIndices(INDICES);
		setNormals(NORMALS);
		setTexCoords(TEXCOORDS);
	}
}
