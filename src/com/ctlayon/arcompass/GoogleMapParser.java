package com.ctlayon.arcompass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.maps.GeoPoint;

public class GoogleMapParser {	
	
	public ArrayList<GeoPoint> poly;
	
	private GeoPoint geoPoint;
	private JSONObject jData;
	
	/**
	 * Default Constructor for Parser Classes
	 * Initializes all variables
	 */
	public GoogleMapParser() {
		this.jData = new JSONObject();
		this.poly = new ArrayList<GeoPoint>();
		this.geoPoint = null;
	}
	
	/**
	 * parseJson queries google for routing information in json format
	 * it then parses and stores the data in the public ArrayList poly.
	 * 
	 * The even entries in poly are src geopoints
	 * The odd  entries in poly are dst geopoints
	 *  
	 * @param src where the route is starting from
	 * @param dest where the route is ending
	 */
	public void parseJSON( GeoPoint src, GeoPoint dest ) {
		
		// Necessary null check for bad user input
		
		if( src == null || dest == null ) {
			poly = null;
			return;
		}

		// Build the query string
		
        StringBuilder urlString = new StringBuilder();
        urlString.append( "http://maps.googleapis.com/maps/api/directions/json?" );
        urlString.append( "origin=" ); // from
        urlString.append( Double.toString( (double) src.getLatitudeE6() / 1.0E6 ) );
        urlString.append(",");
        urlString.append( Double.toString( (double) src.getLongitudeE6() / 1.0E6 ) );
        urlString.append( "&destination=" ); // to
        urlString.append( Double.toString( (double) dest.getLatitudeE6() / 1.0E6 ) );
        urlString.append( "," );
        urlString.append( Double.toString( (double) dest.getLongitudeE6() / 1.0E6 ) );
        urlString.append( "&sensor=false" );
        
        // Send the query for JSON data
        
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet( urlString.toString() );
        
        HttpResponse response;
        
        try {
			response = httpClient.execute( httpget );
			if(response.getStatusLine().getStatusCode() == 200){
                HttpEntity entity = response.getEntity();
                //if its not empty
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    this.jData = new JSONObject( convertStreamToString( instream ) );
                    this.parse();
                    instream.close();
                }
            }
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * parseJson queries google for routing information in json format
	 * it then parses and stores the data in the public ArrayList poly.
	 * 
	 * The even entries in poly are src geopoints
	 * The odd  entries in poly are dst geopoints
	 *  
	 * @param src where the route is starting from
	 * @param pAddress geolocation of where the route is ending
	 */
	public void parseJSON( GeoPoint src, String pAddress ) {
		
		String cleanText = pAddress.replace( " ", "+" );
		cleanText = cleanText.replace( ",", "" );
			
		StringBuilder urlString = new StringBuilder();
        urlString.append( "http://maps.googleapis.com/maps/api/geocode/json?" );
        urlString.append( "address=" );
        urlString.append( cleanText );
        urlString.append( "&sensor=false" );
        
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet( urlString.toString() );
        Log.d("NETOWRK", urlString.toString() );
        HttpResponse response;
        
        try {
			response = httpClient.execute( httpget );
			if(response.getStatusLine().getStatusCode() == 200){
                HttpEntity entity = response.getEntity();
                //if its not empty
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    this.jData = new JSONObject( convertStreamToString( instream ) );
                    this.parseGeoCode();
                    this.parseJSON( src, geoPoint );
                    instream.close();
                }
            }
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Converts an input stream to a String
	 */
	private static String convertStreamToString(InputStream is) {

		// To convert the InputStream to String we use the BufferedReader.readLine()
		// method. We iterate until the BufferedReader return null which means
		// there's no more data to read. Each line will appended to a StringBuilder
		// and returned as String.
		
		        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		        StringBuilder sb = new StringBuilder();
		 
		        String line = null;
		        try {
		            while ((line = reader.readLine()) != null) {
		                sb.append(line + "\n");
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		} finally {
		    try {
		        is.close();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
		return sb.toString();
	 }

	/**
	 * Parses the data stored in jData to the arraylist poly
	 */
	private void parse() throws JSONException {
		
		// routes
		
		JSONArray routes = this.jData.getJSONArray( "routes" );
		 
		for( int i = 0; i < routes.length(); i++ ) {

			JSONObject r = routes.getJSONObject( i );
			
			// legs
			
			JSONArray legs = r.getJSONArray( "legs" );
			for( int j = 0; j < legs.length(); j++ ) {
				
				JSONObject l = legs.getJSONObject( j );
				
				// steps
				
				JSONArray steps = l.getJSONArray( "steps" );
				for( int k = 0; k < steps.length(); k++ ) {
					JSONObject s = steps.getJSONObject( k );
					JSONObject polyline = s.getJSONObject( "polyline" );
					decodePoly( polyline.getString( "points" ) );					
				}
				
			}
		}
			 
	}
	
	private void parseGeoCode() throws JSONException {
		
		// results
		
		JSONArray results = jData.getJSONArray( "results" );
		 
		for( int i = 0; i < results.length(); i++ ) {

			JSONObject r = results.getJSONObject( i );
			
			// geometry
			
			JSONObject geometry = r.getJSONObject( "geometry" );
			JSONObject location = geometry.getJSONObject( "location" );
			double lat = location.getDouble( "lat" );
			double lon = location.getDouble( "lng" );
			
			this.geoPoint = new GeoPoint( (int) (lat * 1E6), (int) (lon * 1E6) );
		}
		
	}
	
	/**
	 * Google, you're compression method is a joke
	 * that is all.
	 * @param encoded googles encoded string
	 */
	private void decodePoly(String encoded) {
		
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		GeoPoint p = null;
		
		while (index < len) {
			
			int b, shift = 0, result = 0;			
			
			// first 32 bits are latitude
			
			do {
				b = encoded.charAt( index++ ) - 63;
				result |= ( b & 0x1f ) << shift;
				shift += 5;
			} while (b >= 0x20);
			
			// Normalize
			
			int dlat = ( (result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			
			// second 32 bits are longitude
			
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			
			// Normalize
			
			int dlng = ( (result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			p = new GeoPoint((int) (((double) lat / 1E5) * 1E6),
				 (int) (((double) lng / 1E5) * 1E6));
			
			poly.add(p);
		}
	}
}
