/* SVN FILE: $Id$ */
package se.ltu.android.demo.intersection;

/**
 * A ray
 * 
 * The ray with intersection tests is heavily based upon the following:
 * 
 * "Fast Ray / Axis-Aligned Bounding Box Overlap Tests using Ray Slopes" 
 * by Martin Eisemann, Thorsten Grosch, Stefan Müller and Marcus Magnor
 * Computer Graphics Lab, TU Braunschweig, Germany and
 * University of Koblenz-Landau, Germany
 * 
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class Ray {
	/*
	 * Pre-calculated classification of this ray where:
	 * M = Minus
	 * P = Plus
	 * O = Zero 
	 */
	private static final int CLASSIFICATION_MMM = 0;
	private static final int CLASSIFICATION_MMP = 1;
	private static final int CLASSIFICATION_MPM = 2;
	private static final int CLASSIFICATION_MPP = 3;
	private static final int CLASSIFICATION_PMM = 4;
	private static final int CLASSIFICATION_PMP = 5;
	private static final int CLASSIFICATION_PPM = 6;
	private static final int CLASSIFICATION_PPP = 7;
	private static final int CLASSIFICATION_POO = 8;
	private static final int CLASSIFICATION_MOO = 9;
	private static final int CLASSIFICATION_OPO = 10;
	private static final int CLASSIFICATION_OMO = 11;
	private static final int CLASSIFICATION_OOP = 12;
	private static final int CLASSIFICATION_OOM = 13;
	private static final int CLASSIFICATION_OMM = 14;
	private static final int CLASSIFICATION_OMP = 15;
	private static final int CLASSIFICATION_OPM = 16;
	private static final int CLASSIFICATION_OPP = 17;
	private static final int CLASSIFICATION_MOM = 18;
	private static final int CLASSIFICATION_MOP = 19;
	private static final int CLASSIFICATION_POM = 20;
	private static final int CLASSIFICATION_POP = 21;
	private static final int CLASSIFICATION_MMO = 22;
	private static final int CLASSIFICATION_MPO = 23;
	private static final int CLASSIFICATION_PMO = 24;
	private static final int CLASSIFICATION_PPO = 25;
	// END classifications
	
	private float x, y, z;		// ray origin	
	private float i, j, k;		// ray direction	
	private float ii, ij, ik;	// inverses of direction components
	
	// ray slope
	private int classification;
	private float ibyj, jbyi, kbyj, jbyk, ibyk, kbyi; //slope
	private float c_xy, c_xz, c_yx, c_yz, c_zx, c_zy;
	
	/**
	 * Constructs a new ray
	 * @param x origin x-coordinate
	 * @param y origin y-coordinate
	 * @param z origin z-coordinate
	 * @param i direction x-coordinate
	 * @param j direction y-coordinate
	 * @param k direction z-coordinate
	 */
	public Ray(float x, float y, float z, float i, float j, float k) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.i = i;
		this.j = j;
		this.k = k;
		
		// inverses of direction component
		this.ii = 1.0f/i;
		this.ij = 1.0f/j;
		this.ik = 1.0f/k;

		//ray slope
		this.ibyj = this.i * this.ij;
		this.jbyi = this.j * this.ii;
		this.jbyk = this.j * this.ik;
		this.kbyj = this.k * this.ij;
		this.ibyk = this.i * this.ik;
		this.kbyi = this.k * this.ii;
		this.c_xy = this.y - this.jbyi * this.x;
		this.c_xz = this.z - this.kbyi * this.x;
		this.c_yx = this.x - this.ibyj * this.y;
		this.c_yz = this.z - this.kbyj * this.y;
		this.c_zx = this.x - this.ibyk * this.z;
		this.c_zy = this.y - this.jbyk * this.z;
		
		//ray slope classification
		if(i < 0) {
			if(j < 0) {
				if(k < 0) {
					this.classification = CLASSIFICATION_MMM;
				} else if(k > 0){
					this.classification = CLASSIFICATION_MMP;
				} else { //(k >= 0)
					this.classification = CLASSIFICATION_MMO;
				}
			} else { //(j >= 0)
				if(k < 0) {
					this.classification = CLASSIFICATION_MPM;
					if(j==0)
						this.classification = CLASSIFICATION_MOM;
				} else { //(k >= 0)
					if((j==0) && (k==0))
						this.classification = CLASSIFICATION_MOO;	
					else if(k==0)
						this.classification = CLASSIFICATION_MPO;
					else if(j==0)
						this.classification = CLASSIFICATION_MOP;
					else
						this.classification = CLASSIFICATION_MPP;
				}
			}
		} else { //(i >= 0)
			if(j < 0) {
				if(k < 0) {
					this.classification = CLASSIFICATION_PMM;
					if(i==0)
						this.classification = CLASSIFICATION_OMM;
				} else { //(k >= 0)			
					if((i==0) && (k==0))
						this.classification = CLASSIFICATION_OMO;
					else if(k==0)
						this.classification = CLASSIFICATION_PMO;
					else if(i==0)
						this.classification = CLASSIFICATION_OMP;
					else
						this.classification = CLASSIFICATION_PMP;
				}
			} else { //(j >= 0)
				if(k < 0) {
					if((i==0) && (j==0))
						this.classification = CLASSIFICATION_OOM;
					else if(i==0)
						this.classification = CLASSIFICATION_OPM;
					else if(j==0)
						this.classification = CLASSIFICATION_POM;
					else
						this.classification = CLASSIFICATION_PPM;
				} else { //(k > 0)
					if(i==0) {
						if(j==0)
							this.classification = CLASSIFICATION_OOP;
						else if(k==0)
							this.classification = CLASSIFICATION_OPO;
						else
							this.classification = CLASSIFICATION_OPP;
					} else {
						if((j==0) && (k==0))
							this.classification = CLASSIFICATION_POO;
						else if(j==0)
							this.classification = CLASSIFICATION_POP;
						else if(k==0)
							this.classification = CLASSIFICATION_PPO;
						else
							this.classification = CLASSIFICATION_PPP;
					}
				}			
			}
		}
	} // public Ray(float x, float y, float z, float i, float j, float k)
	
	/**
	 * Calculates whether or not the ray intersects an axis-aligned bounding box.
	 * Same as calling <code>intersects(box, null)</code>
	 * @param box an axis-aligned bounding box
	 * @return true if this ray intersects the box
	 */
	public boolean intersects(AABBox box){
		return intersects(box, null);
	}
	
	/**
	 * Calculates whether or not the ray intersects an axis-aligned bounding box.
	 * @param box an axis-aligned bounding box
	 * @param distance the resulting distance from the origin of this ray to the 
	 * intersection point of the box, only valid if this method returns true.
	 * @return true if this ray intersects the box
	 */
	public boolean intersects(AABBox box, float[] distance){

		switch (classification)
		{
		case CLASSIFICATION_MMM:
			{
			if ((x < box.minX) || (y < box.minY) || (z < box.minZ)
				|| (jbyi * box.minX - box.maxY + c_xy > 0)
				|| (ibyj * box.minY - box.maxX + c_yx > 0)
				|| (jbyk * box.minZ - box.maxY + c_zy > 0)
				|| (kbyj * box.minY - box.maxZ + c_yz > 0)
				|| (kbyi * box.minX - box.maxZ + c_xz > 0)
				|| (ibyk * box.minZ - box.maxX + c_zx > 0)
				)
				return false;
			
				// compute the intersection distance[0]
			if(distance != null) {
				distance[0] = (box.maxX - x) * ii;
				float dist1 = (box.maxY - y) * ij;
				if(dist1 > distance[0])
					distance[0] = dist1;
				float dist2 = (box.maxZ - z) * ik;
				if(dist2 > distance[0])
					distance[0] = dist2;
			}

			return true;
		}


		case CLASSIFICATION_MMP:
			{		
			if ((x < box.minX) || (y < box.minY) || (z > box.maxZ)
				|| (jbyi * box.minX - box.maxY + c_xy > 0)
				|| (ibyj * box.minY - box.maxX + c_yx > 0)
				|| (jbyk * box.maxZ - box.maxY + c_zy > 0)
				|| (kbyj * box.minY - box.minZ + c_yz < 0)
				|| (kbyi * box.minX - box.minZ + c_xz < 0)
				|| (ibyk * box.maxZ - box.maxX + c_zx > 0)
				)
				return false;
			
			if(distance != null) {
				distance[0] = (box.maxX - x) * ii;
				float t1 = (box.maxY - y) * ij;
				if(t1 > distance[0])
					distance[0] = t1;
				float t2 = (box.minZ - z) * ik;
				if(t2 > distance[0])
					distance[0] = t2;
			}
			
				return true;
			}

		case CLASSIFICATION_MPM:
			{		
			if ((x < box.minX) || (y > box.maxY) || (z < box.minZ)
				|| (jbyi * box.minX - box.minY + c_xy < 0) 
				|| (ibyj * box.maxY - box.maxX + c_yx > 0)
				|| (jbyk * box.minZ - box.minY + c_zy < 0) 
				|| (kbyj * box.maxY - box.maxZ + c_yz > 0)
				|| (kbyi * box.minX - box.maxZ + c_xz > 0)
				|| (ibyk * box.minZ - box.maxX + c_zx > 0)
				)
				return false;
			
			if(distance != null) {
			distance[0] = (box.maxX - x) * ii;
			float t1 = (box.minY - y) * ij;
			if(t1 > distance[0])
				distance[0] = t1;
			float t2 = (box.maxZ - z) * ik;
			if(t2 > distance[0])
				distance[0] = t2;
			}

			return true;
			}

		case CLASSIFICATION_MPP:
			{
			if ((x < box.minX) || (y > box.maxY) || (z > box.maxZ)
				|| (jbyi * box.minX - box.minY + c_xy < 0) 
				|| (ibyj * box.maxY - box.maxX + c_yx > 0)
				|| (jbyk * box.maxZ - box.minY + c_zy < 0)
				|| (kbyj * box.maxY - box.minZ + c_yz < 0) 
				|| (kbyi * box.minX - box.minZ + c_xz < 0)
				|| (ibyk * box.maxZ - box.maxX + c_zx > 0)
				)
				return false;
			
			if(distance != null) {
			distance[0] = (box.maxX - x) * ii;
				float t1 = (box.minY - y) * ij;
				if(t1 > distance[0])
					distance[0] = t1;
				float t2 = (box.minZ - z) * ik;
				if(t2 > distance[0])
					distance[0] = t2;
			}

				return true;
			}

		case CLASSIFICATION_PMM:
			{
			if ((x > box.maxX) || (y < box.minY) || (z < box.minZ)
				|| (jbyi * box.maxX - box.maxY + c_xy > 0)
				|| (ibyj * box.minY - box.minX + c_yx < 0)
				|| (jbyk * box.minZ - box.maxY + c_zy > 0)
				|| (kbyj * box.minY - box.maxZ + c_yz > 0)
				|| (kbyi * box.maxX - box.maxZ + c_xz > 0)
				|| (ibyk * box.minZ - box.minX + c_zx < 0)
				)
				return false;
			
			if(distance != null) {
			distance[0] = (box.minX - x) * ii;
				float t1 = (box.maxY - y) * ij;
				if(t1 > distance[0])
					distance[0] = t1;
				float t2 = (box.maxZ - z) * ik;
				if(t2 > distance[0])
					distance[0] = t2;
			}

				return true;
			}
			

		case CLASSIFICATION_PMP:
			{
			if ((x > box.maxX) || (y < box.minY) || (z > box.maxZ)
				|| (jbyi * box.maxX - box.maxY + c_xy > 0)
				|| (ibyj * box.minY - box.minX + c_yx < 0)
				|| (jbyk * box.maxZ - box.maxY + c_zy > 0)
				|| (kbyj * box.minY - box.minZ + c_yz < 0)
				|| (kbyi * box.maxX - box.minZ + c_xz < 0)
				|| (ibyk * box.maxZ - box.minX + c_zx < 0)
				)
				return false;

			if(distance != null) {
			distance[0] = (box.minX - x) * ii;
				float t1 = (box.maxY - y) * ij;
				if(t1 > distance[0])
					distance[0] = t1;
				float t2 = (box.minZ - z) * ik;
				if(t2 > distance[0])
					distance[0] = t2;
			}

				return true;
			}

		case CLASSIFICATION_PPM:
			{
			if ((x > box.maxX) || (y > box.maxY) || (z < box.minZ)
				|| (jbyi * box.maxX - box.minY + c_xy < 0)
				|| (ibyj * box.maxY - box.minX + c_yx < 0)
				|| (jbyk * box.minZ - box.minY + c_zy < 0) 
				|| (kbyj * box.maxY - box.maxZ + c_yz > 0)
				|| (kbyi * box.maxX - box.maxZ + c_xz > 0)
				|| (ibyk * box.minZ - box.minX + c_zx < 0)
				)
				return false;
			
			if(distance != null) {
			distance[0] = (box.minX - x) * ii;
				float t1 = (box.minY - y) * ij;
				if(t1 > distance[0])
					distance[0] = t1;
				float t2 = (box.maxZ - z) * ik;
				if(t2 > distance[0])
					distance[0] = t2;
			}

				return true;
			}

		case CLASSIFICATION_PPP:
			{
			if ((x > box.maxX) || (y > box.maxY) || (z > box.maxZ)
				|| (jbyi * box.maxX - box.minY + c_xy < 0)
				|| (ibyj * box.maxY - box.minX + c_yx < 0)
				|| (jbyk * box.maxZ - box.minY + c_zy < 0)
				|| (kbyj * box.maxY - box.minZ + c_yz < 0)
				|| (kbyi * box.maxX - box.minZ + c_xz < 0)
				|| (ibyk * box.maxZ - box.minX + c_zx < 0)
				)
				return false;
			
			if(distance != null) {
			distance[0] = (box.minX - x) * ii;
				float t1 = (box.minY - y) * ij;
				if(t1 > distance[0])
					distance[0] = t1;
				float t2 = (box.minZ - z) * ik;
				if(t2 > distance[0])
					distance[0] = t2;
			}

				return true;
			}

		case CLASSIFICATION_OMM:
			{
			if((x < box.minX) || (x > box.maxX)
				|| (y < box.minY) || (z < box.minZ)
				|| (jbyk * box.minZ - box.maxY + c_zy > 0)
				|| (kbyj * box.minY - box.maxZ + c_yz > 0)
				)
				return false;

			if(distance != null) {
			distance[0] = (box.maxY - y) * ij;
			float t2 = (box.maxZ - z) * ik;
			if(t2 > distance[0])
				distance[0] = t2;
			}

			return true;
			}

		case CLASSIFICATION_OMP:
			{
			if((x < box.minX) || (x > box.maxX)
				|| (y < box.minY) || (z > box.maxZ)
				|| (jbyk * box.maxZ - box.maxY + c_zy > 0)
				|| (kbyj * box.minY - box.minZ + c_yz < 0)
				)
				return false;

			if(distance != null) {
			distance[0] = (box.maxY - y) * ij;
			float t2 = (box.minZ - z) * ik;
			if(t2 > distance[0])
				distance[0] = t2;
			}

			return true;
			}

		case CLASSIFICATION_OPM:
			{
			if((x < box.minX) || (x > box.maxX)
				|| (y > box.maxY) || (z < box.minZ)
				|| (jbyk * box.minZ - box.minY + c_zy < 0) 
				|| (kbyj * box.maxY - box.maxZ + c_yz > 0)
				)
				return false;

			if(distance != null) {
			distance[0] = (box.minY - y) * ij;		
			float t2 = (box.maxZ - z) * ik;
			if(t2 > distance[0])
				distance[0] = t2;
			}

			return true;
			}

		case CLASSIFICATION_OPP:
			{
			if((x < box.minX) || (x > box.maxX)
				|| (y > box.maxY) || (z > box.maxZ)
				|| (jbyk * box.maxZ - box.minY + c_zy < 0)
				|| (kbyj * box.maxY - box.minZ + c_yz < 0)
				)
				return false;
			
			if(distance != null) {
			distance[0] = (box.minY - y) * ij;		
			float t2 = (box.minZ - z) * ik;
			if(t2 > distance[0])
				distance[0] = t2;
			}

			return true;
			}
			

		case CLASSIFICATION_MOM:
			{
			if((y < box.minY) || (y > box.maxY)
				|| (x < box.minX) || (z < box.minZ) 
				|| (kbyi * box.minX - box.maxZ + c_xz > 0)
				|| (ibyk * box.minZ - box.maxX + c_zx > 0)
				)
				return false;
			
			if(distance != null) {
			distance[0] = (box.maxX - x) * ii;
			float t2 = (box.maxZ - z) * ik;
			if(t2 > distance[0])
				distance[0] = t2;
			}

			return true;
			}
			

		case CLASSIFICATION_MOP:
			{
			if((y < box.minY) || (y > box.maxY)
				|| (x < box.minX) || (z > box.maxZ) 
				|| (kbyi * box.minX - box.minZ + c_xz < 0)
				|| (ibyk * box.maxZ - box.maxX + c_zx > 0)
				)
				return false;

			if(distance != null) {
			distance[0] = (box.maxX - x) * ii;
			float t2 = (box.minZ - z) * ik;
			if(t2 > distance[0])
				distance[0] = t2;
			}

			return true;
			}

		case CLASSIFICATION_POM:
			{
			if((y < box.minY) || (y > box.maxY)
				|| (x > box.maxX) || (z < box.minZ)
				|| (kbyi * box.maxX - box.maxZ + c_xz > 0)
				|| (ibyk * box.minZ - box.minX + c_zx < 0)
				)
				return false;
			
			if(distance != null) {
			distance[0] = (box.minX - x) * ii;
			float t2 = (box.maxZ - z) * ik;
			if(t2 > distance[0])
				distance[0] = t2;
			}

			return true;
			}
				

		case CLASSIFICATION_POP:
			{
			if((y < box.minY) || (y > box.maxY)
				|| (x > box.maxX) || (z > box.maxZ)
				|| (kbyi * box.maxX - box.minZ + c_xz < 0)
				|| (ibyk * box.maxZ - box.minX + c_zx < 0)
				)
				return false;

			if(distance != null) {
			distance[0] = (box.minX - x) * ii;
			float t2 = (box.minZ - z) * ik;
			if(t2 > distance[0])
				distance[0] = t2;
			}

			return true;
			}	

		case CLASSIFICATION_MMO:
			{
			if((z < box.minZ) || (z > box.maxZ)
				|| (x < box.minX) || (y < box.minY)  
				|| (jbyi * box.minX - box.maxY + c_xy > 0)
				|| (ibyj * box.minY - box.maxX + c_yx > 0)
				)
				return false;

			if(distance != null) {
			distance[0] = (box.maxX - x) * ii;
			float t1 = (box.maxY - y) * ij;
			if(t1 > distance[0])
				distance[0] = t1;
			}

			return true;
			}	

		case CLASSIFICATION_MPO:
			{
			if((z < box.minZ) || (z > box.maxZ)
				|| (x < box.minX) || (y > box.maxY) 
				|| (jbyi * box.minX - box.minY + c_xy < 0) 
				|| (ibyj * box.maxY - box.maxX + c_yx > 0)
				)
				return false;
			
			if(distance != null) {
			distance[0] = (box.maxX - x) * ii;
			float t1 = (box.minY - y) * ij;
			if(t1 > distance[0])
				distance[0] = t1;
			}
			
			return true;
			}
			

		case CLASSIFICATION_PMO:
			{
			if((z < box.minZ) || (z > box.maxZ)
				|| (x > box.maxX) || (y < box.minY) 
				|| (jbyi * box.maxX - box.maxY + c_xy > 0)
				|| (ibyj * box.minY - box.minX + c_yx < 0) 
				)
				return false;

			if(distance != null) {
			distance[0] = (box.minX - x) * ii;
			float t1 = (box.maxY - y) * ij;
			if(t1 > distance[0])
				distance[0] = t1;
			}
			
			return true;
			}

		case CLASSIFICATION_PPO:
			{
			if((z < box.minZ) || (z > box.maxZ)
				|| (x > box.maxX) || (y > box.maxY) 
				|| (jbyi * box.maxX - box.minY + c_xy < 0)
				|| (ibyj * box.maxY - box.minX + c_yx < 0)
				)
				return false;
		
			if(distance != null) {
			distance[0] = (box.minX - x) * ii;
			float t1 = (box.minY - y) * ij;
			if(t1 > distance[0])
				distance[0] = t1;
			}

			return true;
			}
			

		case CLASSIFICATION_MOO:
			{
			if((x < box.minX)
				|| (y < box.minY) || (y > box.maxY)
				|| (z < box.minZ) || (z > box.maxZ)
				)
				return false;

			if(distance != null) {
			distance[0] = (box.maxX - x) * ii;
			}
			return true;
			}

		case CLASSIFICATION_POO:
			{
			if((x > box.maxX)
				|| (y < box.minY) || (y > box.maxY)
				|| (z < box.minZ) || (z > box.maxZ)
				)
				return false;

			if(distance != null) {
			distance[0] = (box.minX - x) * ii;
			}
			return true;
			}

		case CLASSIFICATION_OMO:
			{
			if((y < box.minY)
				|| (x < box.minX) || (x > box.maxX)
				|| (z < box.minZ) || (z > box.maxZ)
				)
				return false;
			
			if(distance != null) {
			distance[0] = (box.maxY - y) * ij;
			}
			return true;
			}

		case CLASSIFICATION_OPO:
			{
			if((y > box.maxY)
				|| (x < box.minX) || (x > box.maxX)
				|| (z < box.minZ) || (z > box.maxZ)
				)
				return false;

			if(distance != null) {
			distance[0] = (box.minY - y) * ij;
			}
			return true;
			}


		case CLASSIFICATION_OOM:
			{
			if((z < box.minZ)
				|| (x < box.minX) || (x > box.maxX)
				|| (y < box.minY) || (y > box.maxY)
				)
				return false;

			if(distance != null) {
			distance[0] = (box.maxZ - z) * ik;
			}
			return true;
			}

		case CLASSIFICATION_OOP:
			{
			if((z > box.maxZ)
				|| (x < box.minX) || (x > box.maxX)
				|| (y < box.minY) || (y > box.maxY)
				)
				return false;

			if(distance != null) {
			distance[0] = (box.minZ - z) * ik;
			}
			return true;
			}	
		}

		return false;
	}
}
