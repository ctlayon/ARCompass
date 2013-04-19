package com.ctlayon.arcompass;

import android.content.Context;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;

import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValues;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

public class ARCompass extends MetaioSDKViewActivity {

	private IGeometry mCompassModel;
	private MetaioSDKCallbackHandler mCallbackHandler;
	
	private boolean mIsCloseToModel = false;
	private boolean mUpdate = false;
	
	private int frameCount = 0;

	String trackingConfigFile = null;	
	SensorManager sensorManager;
	
	float pitch = 25;
	float roll = 60;
	float heading = (float) Math.toRadians( 90 );
	
	@Override
	protected int getGUILayout() 
	{
		return R.layout.compass_rectified; 
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		mCallbackHandler = new MetaioSDKCallbackHandler();
		super.onCreate(savedInstanceState);
		
		sensorManager = (SensorManager) getSystemService( Context.SENSOR_SERVICE );
		updateOrientation(heading, pitch, roll);
		
	}

	// sensorlistener that is called everytime the heading, roll or pitch is changed

	private final SensorListener sensorListener = new SensorListener() {

        public void onSensorChanged(int sensor, float[] values) {
            updateOrientation( values[SensorManager.DATA_Z], values[SensorManager.DATA_Y], values[SensorManager.DATA_X] );
        }

        public void onAccuracyChanged(int sensor, int accuracy) {
        }

    };

    /**
     * Updates the 3D models pose, as well as the phones heading
     */
	private void updateOrientation(float pRoll, float pPitch, float pHeading) {
		heading = (float) Math.toRadians( pHeading + 90 );
		pitch = (float) Math.toRadians( pPitch );
		roll = (float) Math.toRadians( pRoll );
	
		if ( mCompassModel != null) {
			TrackingValues currentPose = metaioSDK.getTrackingValues(1);
			currentPose.setRotation( new Rotation( new Vector3d( 0, 0, heading ) ) );
			mCompassModel.setRotation( new Rotation( new Vector3d( 0, 0, heading ) ) );
			
		}
	}
	
	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler()
	{
		return mCallbackHandler;
	}

	@Override
	public void onDrawFrame() 
	{
		// TODO: turn frameCounter into a timer task
		
		super.onDrawFrame();
		
		if (metaioSDK != null) {
			
			// statistically speaking if the framecount is greater than 500
			// it's time to resync it with the tracking object
			
			if( frameCount > 500 ) {
				mCompassModel.setRotation( new Rotation( 0f, 0f, (float) heading ) );
				metaioSDK.startInstantTracking( "INSTANT_2D" );
				frameCount = 0;
			}
			
			// checks if you gets too close to the object
			// if you do it tries to redraw it
			
			else if ( mCompassModel.isVisible() ) {
				checkDistanceToTarget();
				if( mIsCloseToModel && mUpdate) {
					mCompassModel.setRotation( new Rotation( 0f, 0f, (float) heading ) );
					metaioSDK.startInstantTracking( "INSTANT_2D" );
					mUpdate = false;
					frameCount = 0;
				} else if( !mIsCloseToModel && mUpdate ) {
					mCompassModel.setRotation(new Rotation(0f, 0f, (float) heading ) );
					metaioSDK.startInstantTracking("INSTANT_2D");
					mUpdate = false;
					frameCount = 0;
				}
				
			} 
			
			// if the object has been seen for 150 frames
			// go ahead and redraw it on the current frame
			
			else if( frameCount > 150 ) {
				mCompassModel.setRotation(new Rotation(0f, 0f, (float) heading ) );
				metaioSDK.startInstantTracking("INSTANT_2D");
				frameCount = 0;
			}	
			
			frameCount++;
		}
	}

	@Override
	protected void onStart() 
	{
		super.onStart();
		
		// hide GUI until SDK is ready
		
		if (!mRendererInitialized)
			mGUIView.setVisibility(View.GONE);	
		
	}
	@Override protected void onResume() 
	{
		super.onResume();
		
		sensorManager.registerListener(sensorListener,
                SensorManager.SENSOR_ORIENTATION,
                SensorManager.SENSOR_DELAY_FASTEST);
	}
	@Override
	protected void onStop() 
	{
		sensorManager.unregisterListener(sensorListener);
		super.onStop();
	}

	public void onButtonClick(View v)
	{
		finish();
	}
		
	@Override
	protected void loadContent() 
	{
		String compassPath = AssetsManager.getAssetPath( "Assets5/compassAR.md2" );		
		
		if ( compassPath != null ) {
			mCompassModel = metaioSDK.createGeometry( compassPath );
			if (mCompassModel != null) {
				mCompassModel.setScale( new Vector3d( .05f, .05f, .05f ) );
				MetaioDebug.log( "Loaded geometry " + compassPath );
			} else {
				MetaioDebug.log( Log.ERROR, "Error loading geometry: " + compassPath );
			}
		}
		
	}
	
  
	@Override
	protected void onGeometryTouched(IGeometry geometry) {
		MetaioDebug.log( "Heading is: " + heading );
	}
	
	final class MetaioSDKCallbackHandler extends IMetaioSDKCallback
	{
	
		@Override
		public void onSDKReady() 
		{
			// show GUI
			runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					mGUIView.setVisibility(View.VISIBLE);
				}
			});
		}
		

		@Override
		public void onInstantTrackingEvent(boolean success, String file)
		{
			if(success) {
				MetaioDebug.log("MetaioSDKCallbackHandler.onInstantTrackingEvent: "+file);
				metaioSDK.setTrackingConfiguration(file);
			} else {
				MetaioDebug.log("Failed to create instant tracking configuration!");
			}
		}
	}
	
	
	/**
	 * Calculates how far away the object is from the camera update
	 * Updates mIsCloseToObject and mUpdate
	 */
	private void checkDistanceToTarget() {
		
		TrackingValues currentPose = metaioSDK.getTrackingValues(1);
		
		// if the quality value > 0, it means we're currently tracking

		if (currentPose.getQuality() > 0) {
			
			Vector3d poseTranslation = currentPose.getTranslation();
			
			// calculate the distance as sqrt( x^2 + y^2 + z^2 )
			
			float distanceToTarget = FloatMath.sqrt(
					poseTranslation.getX() * poseTranslation.getX() + 
					poseTranslation.getY() * poseTranslation.getY() + 
					poseTranslation.getZ() * poseTranslation.getZ() );
			
			float threshold = 150;
			
			if (mIsCloseToModel) { 
				if ( distanceToTarget > ( threshold + 10 ) ) {
					mIsCloseToModel = false;
					mUpdate = true;
				}
			}
			else {
				if (distanceToTarget < threshold) {					
					mIsCloseToModel = true;
					mUpdate = false;
				}
			}
			
		}
	}
}