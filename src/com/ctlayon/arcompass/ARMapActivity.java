package com.ctlayon.arcompass;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import com.ctlayon.arcompass.MyLocation.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.maps.GeoPoint;

public class ARMapActivity extends Activity {
	 
	private GoogleMap map;
	private LatLng mCurr;
	MyLocation myLocation;
		
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.map );
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
        
		LocationResult locationResult = new LocationResult(){
		    @Override
		    public void gotLocation(Location location){
		        Log.d( "location","Got Location" );
		        mCurr = new LatLng( location.getLatitude(), location.getLongitude() );
		        map.animateCamera( CameraUpdateFactory.newLatLngZoom( mCurr, 15 ) );
		        map.setOnMapClickListener( mapClickListener() );
		    }
		};
		
		myLocation = new MyLocation();
		myLocation.getLocation( this, locationResult );
        
		map = ( (MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();		
		map.setMyLocationEnabled( true );		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	private GoogleMap.OnMapClickListener mapClickListener() {
		
		GoogleMap.OnMapClickListener clickListener = new GoogleMap.OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng latLng) {
			
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
 
                markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                
                map.clear();
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                map.addMarker(markerOptions);
				             
                LatLng curr = mCurr;
                
                GeoPoint src = new GeoPoint( (int) ( curr.latitude * 1E6 ), (int) (curr.longitude * 1E6 ) );
                GeoPoint dst = new GeoPoint( (int) ( latLng.latitude * 1E6 ), (int) ( latLng.longitude * 1E6 ) );
                
                GoogleMapParser parser = new GoogleMapParser();
                parser.parseJSON( src, dst );
                
                ArrayList<GeoPoint> geoPolyLine = parser.poly;
                
                for( int i = 0; i < geoPolyLine.size() - 1; i += 2 ) {
                	
                	GeoPoint geoSrc = geoPolyLine.get( i );
                	GeoPoint geoDst = geoPolyLine.get( i + 1 );
                	
                	LatLng latlng1 = new LatLng( geoSrc.getLatitudeE6() / 1E6, geoSrc.getLongitudeE6() / 1E6 );
                	LatLng latlng2 = new LatLng( geoDst.getLatitudeE6() / 1E6, geoDst.getLongitudeE6() / 1E6 );
                	
                	PolylineOptions po = new PolylineOptions();
                	
                	po.add( latlng1, latlng2 );
                	po.width( 5 );
                	po.color( Color.RED );
                	po.zIndex( 100f );
                	
                	map.addPolyline( po );
                }
                
			}
		};
		return clickListener;
	}

}
