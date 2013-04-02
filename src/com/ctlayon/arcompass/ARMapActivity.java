package com.ctlayon.arcompass;

import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;


public class ARMapActivity extends MapActivity {
	
	private boolean isRouteDisplayed = false;

	@Override
	protected boolean isRouteDisplayed() {		
		return isRouteDisplayed;
	}
	
	@Override
	public void onCreate( Bundle savedInstanceState ) {
	    super.onCreate( savedInstanceState );
	    setContentView( R.layout.map );
	    
	    MapView mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	}
	

}
