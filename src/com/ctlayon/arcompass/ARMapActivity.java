package com.ctlayon.arcompass;

import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.SensorsComponentAndroid;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.LLACoordinate;

public class ARMapActivity extends MetaioSDKViewActivity implements SensorsComponentAndroid.Callback  {
	 
	private GoogleMap map;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//Load Tracking data
		boolean result = metaioSDK.setTrackingConfiguration("GPS");  
		MetaioDebug.log("Tracking data loaded: " + result); 
		
		final LatLng latlng = new LatLng( 
				mSensors.getLocation().getLatitude(), 
				mSensors.getLocation().getLongitude() );
		
		MetaioDebug.log( "Latlng: " + latlng.latitude + " " + latlng.longitude );
		
		map = ( (MapFragment) getFragmentManager().findFragmentById(R.id.map))
		        .getMap();
		
		map.setMyLocationEnabled( true );		
		map.animateCamera( CameraUpdateFactory.newLatLngZoom( latlng, 15 ) );
	}
	
	@Override
	protected void onPause() 
	{
		super.onPause();
		
		// remove callback
		if (mSensors != null)
		{
			mSensors.registerCallback(null);
		}
		
		
	}

	@Override
	protected void onResume() 
	{
		super.onResume();

		// Register callback to receive sensor updates
		if (mSensors != null)
		{
			mSensors.registerCallback(this);
		}
		
	}


	@Override
	public void onLocationSensorChanged(LLACoordinate location)
	{
	//	updateGeometriesLocation(location);
	}

	@Override
	protected int getGUILayout() 
	{
		return R.layout.map;
	}

	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() 
	{
		return null;
	}

	@Override
	protected void loadContent() 
	{		
		updateGeometriesLocation( mSensors.getLocation() );
	}
		
	private void updateGeometriesLocation(LLACoordinate location)
	{
		MetaioDebug.log( "updateGeometriesLocation call" );		
	}

	@Override
	public void onGravitySensorChanged(float[] gravity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onHeadingSensorChanged(float[] orientation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onGeometryTouched(IGeometry geometry) {
		
	}
}
