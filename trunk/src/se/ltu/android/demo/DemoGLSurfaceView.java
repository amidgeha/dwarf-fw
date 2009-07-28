/* SVN FILE: $Id$ */
package se.ltu.android.demo;

import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Message;
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
	private SensorHandler mSensorHandler;
	private float mx;
	private float my;

	public DemoGLSurfaceView(DemoActivity context, SensorHandler handler) {
		super(context);
		//setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);
		// We need a surface with a depth buffer and an alpha channel
		//setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		getHolder().setFormat(PixelFormat.RGBA_8888);
		mSensorHandler = handler;
		mRenderer = new DemoRenderer(handler);
		setRenderer(mRenderer);
		//setRenderMode(RENDERMODE_WHEN_DIRTY);
	}
	
	public DemoRenderer getRenderer() {
		return mRenderer;
	}

	@Override
	public boolean onTrackballEvent(final MotionEvent event) {
		/*
		 * Message msg; //Log.d(TAG, Thread.currentThread().getName() +
		 * " got event " + event.getAction()); switch (event.getAction()) { case
		 * (MotionEvent.ACTION_DOWN): msg =
		 * Message.obtain(mSensorHandler.mHandler, SensorHandler.LOG_POSITION);
		 * msg.sendToTarget(); break; case (MotionEvent.ACTION_UP): msg =
		 * Message.obtain(mSensorHandler.mHandler, SensorHandler.LOG_ROTATION);
		 * msg.sendToTarget(); break; }
		 */
		return true;
	};

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		long downtime = event.getEventTime() - event.getDownTime();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mx = event.getX();
			my = event.getY();
			// Log.d(TAG, "Touch down");
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			Log.d(TAG, "Touch up "+downtime);
			if (downtime > 0 && downtime < 400) {
				setEvent(new Runnable() {
					public void run() {
						mRenderer.pick(mx, my);
					}
				});
				
			}
			if (downtime > 1000) {
				setEvent(new Runnable() {
					public void run() {
						mRenderer.changeCamera();
					}
				});
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
		Message msg;
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ORIENTATION:
			/*
			msg = Message.obtain(mSensorHandler.mHandler, new Runnable() {
				public void run() {
					mSensorHandler.handleOriData(event.timestamp, event.values);
				}
			});
			msg.sendToTarget();
			break;
			*/
		case Sensor.TYPE_ACCELEROMETER:
			/*
			msg = Message.obtain(mSensorHandler.mHandler, new Runnable() {
				public void run() {
					mSensorHandler.handleAccData(event.timestamp, event.values);
				}
			});
			msg.sendToTarget();
			*/
			mSensorHandler.handleAccData(event.timestamp, event.values);
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			/*
			msg = Message.obtain(mSensorHandler.mHandler, new Runnable() {
				public void run() {
					mSensorHandler.handleMagData(event.timestamp, event.values);
				}
			});
			msg.sendToTarget();
			*/
			mSensorHandler.handleMagData(event.timestamp, event.values);
			break;
		}
	}
}
