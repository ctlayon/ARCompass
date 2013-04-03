package com.ctlayon.arcompass;

import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.SensorsComponentAndroid;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.IRadar;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

public class ARCompass extends MetaioSDKViewActivity implements SensorsComponentAndroid.Callback  {
	 
	/**
	 * Geometries
	 */
	private IGeometry mGeometrySouth;
	private IGeometry mGeometryWest;
	private IGeometry mGeometryNorth;
	private IGeometry mGeometryEast;
	
	private IRadar mRadar;

	/**
	 * Offset from current location
	 */
	private static final double OFFSET = 0.00002;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		
		//Load Tracking data
		boolean result = metaioSDK.setTrackingConfiguration("GPS");  
		MetaioDebug.log("Tracking data loaded: " + result);  
	}
	
	@Override
	protected void onPause() 
	{
		super.onPause();
		
		// remove callback
		if (mSensors != null)
		{
			mSensors.registerCallback(null);
		//	mSensorsManager.pause();
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
			//mSensorsManager.resume();
		}
		
	}


	@Override
	public void onLocationSensorChanged(LLACoordinate location)
	{
		updateGeometriesLocation(location);
	}


	public void onButtonClick(View v)
	{
		finish();
	}

	@Override
	protected int getGUILayout() 
	{
		return R.layout.compass;
	}

	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() 
	{
		return null;
	}

	@Override
	protected void loadContent() 
	{
		String filepath = AssetsManager.getAssetPath("Assets5/POI_bg.png");
		if (filepath != null) 
		{
			mGeometryNorth = metaioSDK.loadImageBillboard( createBillboardTexture( "North" ) );
			mGeometrySouth = metaioSDK.loadImageBillboard( createBillboardTexture( "South" ) );
			mGeometryWest  = metaioSDK.loadImageBillboard( createBillboardTexture( "West" ) );
			mGeometryEast  = metaioSDK.loadImageBillboard( createBillboardTexture( "East" ) );				
		}
		
		updateGeometriesLocation( mSensors.getLocation() );
		
		// create radar
		mRadar = metaioSDK.createRadar();
		mRadar.setBackgroundTexture( AssetsManager.getAssetPath( "Assets5/radar.png" ) );
		mRadar.setObjectsDefaultTexture( AssetsManager.getAssetPath( "Assets5/yellow.png" ) );
		mRadar.setRelativeToScreen( IGeometry.ANCHOR_TL );
						
		// add geometries to the radar
		mRadar.add(mGeometryNorth);
		mRadar.add(mGeometrySouth);
		mRadar.add(mGeometryWest);
		mRadar.add(mGeometryEast);		
	}
	
	private String createBillboardTexture(String billBoardTitle)
    {
           try
           {
                  final String texturepath = getCacheDir() + "/" + billBoardTitle + ".png";
                  Paint mPaint = new Paint();

                  // Load background image (256x128), and make a mutable copy
                  Bitmap billboard = null;
                  
                  //reading billboard background
                  String filepath = AssetsManager.getAssetPath("Assets5/POI_bg.png");
                  Bitmap mBackgroundImage = BitmapFactory.decodeFile(filepath);
                  
                  billboard = mBackgroundImage.copy(Bitmap.Config.ARGB_8888, true);


                  Canvas c = new Canvas(billboard);

                  mPaint.setColor(Color.WHITE);
                  mPaint.setTextSize(24);
                  mPaint.setTypeface(Typeface.DEFAULT);

                  float y = 40;
                  float x = 30;

                  // Draw POI name
                  if (billBoardTitle.length() > 0)
                  {
                        String n = billBoardTitle.trim();

                        final int maxWidth = 160;

                        int i = mPaint.breakText(n, true, maxWidth, null);
                        c.drawText(n.substring(0, i), x, y, mPaint);

                        // Draw second line if valid
                        if (i < n.length())
                        {
                               n = n.substring(i);
                               y += 20;
                               i = mPaint.breakText(n, true, maxWidth, null);

                               if (i < n.length())
                               {
                                      i = mPaint.breakText(n, true, maxWidth - 20, null);
                                      c.drawText(n.substring(0, i) + "...", x, y, mPaint);
                               } else
                               {
                                      c.drawText(n.substring(0, i), x, y, mPaint);
                               }
                        }

                  }


                  // writing file
                  try
                  {
                	  FileOutputStream out = new FileOutputStream(texturepath);
                      billboard.compress(Bitmap.CompressFormat.PNG, 90, out);
                      MetaioDebug.log("Texture file is saved to "+texturepath);
                      return texturepath;
                  } catch (Exception e) {
                      MetaioDebug.log("Failed to save texture file");
                	  e.printStackTrace();
                   }
                 
                  billboard.recycle();
                  billboard = null;

           } catch (Exception e)
           {
                  MetaioDebug.log("Error creating billboard texture: " + e.getMessage());
                  MetaioDebug.printStackTrace(Log.DEBUG, e);
                  return null;
           }
           return null;
    }
	
	private void updateGeometriesLocation(LLACoordinate location)
	{
		if (mGeometrySouth != null)
		{
			location.setLatitude( location.getLatitude() - OFFSET );
			MetaioDebug.log("geometrySouth.setTranslationLLA: "+location);
			mGeometrySouth.setTranslationLLA(location);
			location.setLatitude( location.getLatitude() + OFFSET );
		}
		
		if (mGeometryNorth != null)
		{
			location.setLatitude(location.getLatitude() + OFFSET );
			MetaioDebug.log("geometryNorth.setTranslationLLA: "+location);
			mGeometryNorth.setTranslationLLA(location);
			location.setLatitude(location.getLatitude() - OFFSET );
		}
		
		if (mGeometryWest != null)
		{
			location.setLongitude( location.getLongitude() - OFFSET );
			MetaioDebug.log( "geometryWest.setTranslationLLA: "+location);
			mGeometryWest.setTranslationLLA( location );
			location.setLongitude(location.getLongitude() + OFFSET);
		}
		
		if (mGeometryEast != null)
		{
			location.setLongitude( location.getLongitude() + OFFSET );
			MetaioDebug.log( "geometryEast.setTranslationLLA: " + location );
			mGeometryEast.setTranslationLLA( location );
			location.setLongitude( location.getLongitude() - OFFSET );
		}
		
	}
	
	@Override
	protected void onGeometryTouched(final IGeometry geometry) 
	{
		MetaioDebug.log("Geometry selected: "+geometry);
		
		mSurfaceView.queueEvent(new Runnable()
		{

			@Override
			public void run() 
			{
				mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPath("Assets5/yellow.png"));
				mRadar.setObjectTexture(geometry, AssetsManager.getAssetPath("Assets5/red.png"));
			}
		
				
		});
	}

	@Override
	public void onGravitySensorChanged(float[] gravity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onHeadingSensorChanged(float[] orientation) {
		// TODO Auto-generated method stub
		
	}
}
