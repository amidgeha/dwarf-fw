/* SVN FILE: $Id$ */
package se.ltu.android.demo;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

/**
 * @author �ke Svedin <ake.svedin@gmail.com>
 * @version $Revision$
 * @lastmodified $Date$
 */
public class DemoActivity extends Activity {
	static final String TAG = "TestGL";
	private DemoGLSurfaceView mGLView;
	private SensorManager mSensorManager;
	private ArrayList<Sensor> sensors;
	private SensorHandler mSensorHandler;
	private DemoGameThread mGameThread;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // disable window title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // disable status bar
        int flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setFlags(flags, flags);
        
        // prepare listening to sensors
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensors = new ArrayList<Sensor>();
        //sensors.add((mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION)).get(0));
        //sensors.add((mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER)).get(0));
        //sensors.add((mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD)).get(0));
        mSensorHandler = new SensorHandler();
        //mSensorHandler.start();
        
        // set opengl view
        mGLView = new DemoGLSurfaceView(this, mSensorHandler);
        setContentView(mGLView);
        mGLView.requestFocus();
        mGLView.setFocusableInTouchMode(true);
        
        mGameThread = new DemoGameThread(mGLView);
        mGameThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
        mGameThread.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
        mGameThread.onResume();
        
        // register listener for each sensor in sensors
        for (Sensor sensor : sensors) {
			mSensorManager.registerListener(mGLView, 
					sensor, SensorManager.SENSOR_DELAY_GAME);
		}
    }
    
    @Override
    protected void onStop() {
    	// unregister listener for all sensors
    	mSensorManager.unregisterListener(mGLView);
        super.onStop();
    }
}
