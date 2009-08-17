/* SVN FILE: $Id$ */
package se.ltu.android.demo.scene.animation;

import se.ltu.android.demo.scene.Spatial;

/**
 * Any class that want to able to listen to changes on an animation should implement
 * this interface and then register itself on the animations setListener() method.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public interface AnimationListener {
	
	/**
	 * Tells the listeners that the animation has ended.
	 * @param anim the animation that started the event
	 * @param spatial the animated spatial
	 */
	public void onAnimationEnd(KeyFrameAnimation anim, Spatial spatial);
}
