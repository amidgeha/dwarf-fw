/* SVN FILE: $Id$ */
package se.ltu.android.demo;

import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.view.MotionEvent;

/**
 * @author Åke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
class DemoGLSurfaceView extends GLSurfaceView implements SensorEventListener {
	private final static String TAG = "DemoGLSurfaceView";
	private DemoRenderer mRenderer;
	private float mx;
	private float my;

	public DemoGLSurfaceView(DemoActivity context) {
		super(context);
		//setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);
		// We need a surface with a depth buffer and an alpha channel
		//setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		getHolder().setFormat(PixelFormat.RGBA_8888);
		mRenderer = new DemoRenderer();
		setRenderer(mRenderer);
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
		}
		return true;
	};

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		long downtime = event.getEventTime() - event.getDownTime();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mx = event.getX();
			my = event.getY();
			Log.d(TAG, "Touch down");
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			Log.d(TAG, "Touch up "+downtime);
			if (downtime > 0 && downtime < 400) {
				DemoGameThread.onTap(mx, my);
			}
			break;
		}
		return true;
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
}
