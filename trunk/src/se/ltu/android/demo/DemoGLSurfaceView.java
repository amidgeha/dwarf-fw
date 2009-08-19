/* SVN FILE: $Id$ */
package se.ltu.android.demo;

import se.ltu.android.demo.sensors.SensorHandler;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Our application specific implementation of a GLSurfaceView. It holds the renderer
 * and it listens to all input and sensor events spawned by the underlying OS.
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class DemoGLSurfaceView extends GLSurfaceView implements SensorEventListener {
	private DemoRenderer mRenderer;
	private GestureDetector mGestureDetector;

	public DemoGLSurfaceView(DemoActivity context, boolean use_vbos) {
		super(context);

		mRenderer = new DemoRenderer();
		mRenderer.useVBOs(use_vbos);
		setRenderer(mRenderer);
		
		mGestureDetector = new GestureDetector(context, new DemoGestureDetector());
	}
	
	public DemoRenderer getRenderer() {
		return mRenderer;
	}

	@Override
	public boolean onTrackballEvent(final MotionEvent event) {
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				DemoGameThread.onTrackballClick();
				break;
			case MotionEvent.ACTION_MOVE:
				DemoGameThread.onTrackballMove(event.getX(), event.getY());
		}
		return true;
	};
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(final SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			SensorHandler.handleAccData(event.timestamp, event.values);
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			SensorHandler.handleMagData(event.timestamp, event.values);
			break;
		}
	}
	
	// See the class documentation for more types of supported gestures.
	private class DemoGestureDetector extends GestureDetector.SimpleOnGestureListener {

		/*
		 * This method must return true, otherwise the tap events will not
		 * be triggered.
		 */
		@Override
	    public boolean onDown(MotionEvent ev) {
	        return true;
	    }
		
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			DemoGameThread.onDoubleTap(e.getX(), e.getY());
			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			DemoGameThread.onSingleTap(e.getX(), e.getY());
			return true;
		}
		
	}
}
