/* SVN FILE: $Id$ */
package se.ltu.android.demo;

import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
class DemoGLSurfaceView extends GLSurfaceView implements SensorEventListener {
	private DemoRenderer mRenderer;
	private GestureDetector mGestureDetector;

	public DemoGLSurfaceView(DemoActivity context) {
		super(context);
		//setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);
		// We need a surface with a depth buffer and an alpha channel
		//setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		getHolder().setFormat(PixelFormat.RGBA_8888);
		mRenderer = new DemoRenderer();
		setRenderer(mRenderer);
		DemoGestureDetector demoGestDet = new DemoGestureDetector();
		mGestureDetector = new GestureDetector(context, demoGestDet);
		//setRenderMode(RENDERMODE_WHEN_DIRTY);
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
	
	private class DemoGestureDetector extends GestureDetector.SimpleOnGestureListener {

		@Override
		/*
		 * This method must return true, otherwise the tap events will not
		 * be triggered.
		 */
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
