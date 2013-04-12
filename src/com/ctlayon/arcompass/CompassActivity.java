package com.ctlayon.arcompass;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;

@SuppressWarnings("deprecation")
public class CompassActivity extends Activity {

	float pitch = 25;
	float roll = 60;
	float heading = 90;

	CompassView compassView;
	SensorManager sensorManager;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate( bundle );
		setContentView( R.layout.compass_2d );

		compassView = (CompassView) this.findViewById( R.id.compassView );
		sensorManager = (SensorManager) getSystemService( Context.SENSOR_SERVICE );
		updateOrientation(heading, pitch, roll);
	}

	private final SensorListener sensorListener = new SensorListener() {

            public void onSensorChanged(int sensor, float[] values) {
                updateOrientation( values[SensorManager.DATA_Z], values[SensorManager.DATA_Y], values[SensorManager.DATA_X] );
            }

            public void onAccuracyChanged(int sensor, int accuracy) {
            }

        };

	private void updateOrientation(float pRoll, float pPitch, float pHeading) {
		heading = pHeading;
		pitch = pPitch;
		roll = pRoll;

		if (compassView != null) {
			compassView.setBearing(heading);
			compassView.invalidate();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(sensorListener,
                                       SensorManager.SENSOR_ORIENTATION,
                                       SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	protected void onStop() {
		sensorManager.unregisterListener(sensorListener);
		super.onStop();
	}
}
