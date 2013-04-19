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
	
	// ================= Map ===================== //
	
	private GoogleMap map;
	private LatLng mCurr;
	
	MyLocation myLocation;	
	
	// ================= GUI ===================== //
	
	private View mProgress;
	
	private TextView lblDest;
	private EditText destAddress;
	private Button go;
		
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate( savedInstanceState );		
		setContentView( R.layout.map );
		
		this.initGUI();
		
		// Gives the map permission to make queries ( Gingerbread fix )
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
        
		// location result is the call back method of MyLocation
		// it is called when the location is returned from GPS
		
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

	// ============= Click Listeners ============ //
	
	/**
	 * Creates a google map Click Listener
	 * @return Google Map Click Listener
	 */
	private GoogleMap.OnMapClickListener mapClickListener() {
		
		GoogleMap.OnMapClickListener clickListener = new GoogleMap.OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng latLng) {
			
				hideGUI();
				
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
 
                markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                
                // moves the camera to where you clicked
                
                map.clear();
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                map.addMarker(markerOptions);
				             
                LatLng curr = mCurr;
                
                GeoPoint src = new GeoPoint( (int) ( curr.latitude * 1E6 ), (int) (curr.longitude * 1E6 ) );
                GeoPoint dst = new GeoPoint( (int) ( latLng.latitude * 1E6 ), (int) ( latLng.longitude * 1E6 ) );
                
                // get data from google in convient format
                
                GoogleMapParser parser = new GoogleMapParser();
                parser.parseJSON( src, dst );
                
                drawRoute( parser );
                
			}
		};
		return clickListener;
	}

	/**
	 * Creates a google map Long Click Listener
	 * @return Google Map Long Click Listener
	 */
	private GoogleMap.OnMapLongClickListener mapLongClickListener() {
		
		OnMapLongClickListener cl = new OnMapLongClickListener() {
			
			@Override
			public void onMapLongClick(LatLng point) {
				showGUI();
			}
		};
		
		return cl;
	}

	/**
	 * Function that is called when the "GO" button is pressed
	 * It queries google for an Address / City's GPS location
	 * Parses it and then displays the directions on the map
	 * @param view that the button is located in
	 */
	public void onGoClick( View view) {

		// Hide the gui and display the spinning circle
		
		hideGUI();
		this.mProgress.setVisibility( View.VISIBLE );
		
		// callback handler for myLocation
		// is called when GPS returns location
		
		LocationResult locationResult = new LocationResult(){
		    @Override
		    public void gotLocation( Location location ) {        
		    	
		    	map.clear();
		    	
		        GeoPoint src = new GeoPoint( (int) ( location.getLatitude() * 1E6 ), (int) ( location.getLongitude() * 1E6 ) );
		        
		        GoogleMapParser parser = new GoogleMapParser();
		        parser.parseJSON( src, destAddress.getText().toString() );
		        
		        // done waiting on data hide the spinning circle
		        
		        mProgress.setVisibility( View.GONE );
		        
		        drawRoute( parser );		        
		    }
		};
		
		myLocation.getLocation( this, locationResult );
				
	}
	
	// ================= Helper Functions ================== //
	/**
	 * Displays the route that parser contains on the Google Map
	 * @param parser google parser that has already queried for data
	 */
	private void drawRoute( GoogleMapParser parser ) {
		
		// necessary null check in case user inputs bad data
		
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
	
	/**
	 * Hides the GUI
	 */
	private void hideGUI() {
		lblDest.setVisibility( View.GONE );
		destAddress.setVisibility( View.GONE );
		go.setVisibility( View.GONE );
	}

	/**
	 * Shows the GUI
	 */
	private void showGUI() {
		lblDest.setVisibility( View.VISIBLE );
		destAddress.setVisibility( View.VISIBLE );
		go.setVisibility( View.VISIBLE );
	}

	/**
	 * Initilizes the GUI interface
	 */
	private void initGUI() {
		
		// Displays the spinning circle
		
		this.mProgress = findViewById( R.id.progress );
		
		this.lblDest = (TextView) findViewById( R.id.txtDest );
		this.lblDest.setTextColor( Color.BLACK );
		
		this.destAddress = (EditText) findViewById( R.id.destAddress );	
		
		this.go = (Button) findViewById( R.id.btnGo );
		
		this.hideGUI();		
	}
	
	// ================ Overrides for Key Presses ================= //
	
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
	
}	
