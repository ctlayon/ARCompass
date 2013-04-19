package com.ctlayon.arcompass;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ctlayon.arcompass.MyLocation.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.maps.GeoPoint;

public class ARMapActivity extends Activity {
	 
	private GoogleMap map;
	private LatLng mCurr;
	
	private View mProgress;
	
	MyLocation myLocation;	
	
	private TextView lblDest;
	private EditText destAddress;
	private Button go;
		
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate( savedInstanceState );		
		setContentView( R.layout.map );
		
		this.mProgress = findViewById( R.id.progress );
		
		lblDest = (TextView) findViewById( R.id.txtDest );
		lblDest.setTextColor( Color.BLACK );
		
		destAddress = (EditText) findViewById( R.id.destAddress );	
		
		go = (Button) findViewById( R.id.btnGo );
		
		this.hideGUI();
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
        
		LocationResult locationResult = new LocationResult(){
		    @Override
		    public void gotLocation(Location location){
		        Log.d( "location","Got Location" );
		        mCurr = new LatLng( location.getLatitude(), location.getLongitude() );
		        map.animateCamera( CameraUpdateFactory.newLatLngZoom( mCurr, 15 ) );
		        map.setOnMapClickListener( mapClickListener() );
		        map.setOnMapLongClickListener( mapLongClickListener() );
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
	
	@Override
	public void onBackPressed() {
		if( lblDest.getVisibility() == View.VISIBLE ) {
			lblDest.setVisibility( View.GONE );
			destAddress.setVisibility( View.GONE );
			go.setVisibility( View.GONE );
		} else {
			super.onBackPressed();
		}
		
	}
	
	@Override
	public boolean onSearchRequested() {
		if( lblDest.getVisibility() == View.VISIBLE )
			hideGUI();
		else
			showGUI();
		
		return true;		
	}
	
	private GoogleMap.OnMapClickListener mapClickListener() {
		
		GoogleMap.OnMapClickListener clickListener = new GoogleMap.OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng latLng) {
			
				hideGUI();
				
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
                
                drawRoute( parser );
                
			}
		};
		return clickListener;
	}

	private GoogleMap.OnMapLongClickListener mapLongClickListener() {
		
		OnMapLongClickListener cl = new OnMapLongClickListener() {
			
			@Override
			public void onMapLongClick(LatLng point) {
				showGUI();
			}
		};
		
		return cl;
	}

	public void onGoClick( View view) {
		Log.d( "ClickListener", "CLICKED" );
		hideGUI();
		this.mProgress.setVisibility( View.VISIBLE );
		LocationResult locationResult = new LocationResult(){
		    @Override
		    public void gotLocation(Location location){
		        
		    	
		    	map.clear();
		    	
		        GeoPoint src = new GeoPoint( (int) ( location.getLatitude() * 1E6 ), (int) ( location.getLongitude() * 1E6 ) );
		        GoogleMapParser parser = new GoogleMapParser();
		        parser.parseJSON( src, destAddress.getText().toString() );
		        
		        mProgress.setVisibility( View.GONE );
		        drawRoute( parser );		        
		    }
		};
		
		myLocation.getLocation( this, locationResult );
				
	}
	
	private void drawRoute( GoogleMapParser parser ) {
		if( parser.poly == null ) {
			Toast.makeText( this, "Could not find address", Toast.LENGTH_SHORT).show();
			return;
		}
		
        ArrayList<GeoPoint> geoPolyLine = parser.poly;
        
        for( int i = 0; i < geoPolyLine.size() - 1; i ++ ) {
        	
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

	private void hideGUI() {
		lblDest.setVisibility( View.GONE );
		destAddress.setVisibility( View.GONE );
		go.setVisibility( View.GONE );
	}

	private void showGUI() {
		lblDest.setVisibility( View.VISIBLE );
		destAddress.setVisibility( View.VISIBLE );
		go.setVisibility( View.VISIBLE );
	}
}	
