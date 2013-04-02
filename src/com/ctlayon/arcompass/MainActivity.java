// Copyright 2007-2012 metaio GmbH. All rights reserved.
package com.ctlayon.arcompass;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.metaio.Example.R;
import com.metaio.sdk.MetaioDebug;
import com.metaio.tools.io.AssetsManager;


@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity implements WebFiles
{
	
	private WebView mWebView;
	private AssetsExtracter mTask;
	private View mProgress;
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		
		super.onCreate( savedInstanceState );		
		setContentView( R.layout.webview );
		 
		this.mProgress = findViewById( R.id.progress );
		
		// extract all the assets
		
		this.mTask = new AssetsExtracter();
		this.mTask.execute(0);
		
		MetaioDebug.enableLogging( true );
		
		this.mWebView = (WebView) findViewById( R.id.webview );
        
        WebSettings settings = mWebView.getSettings();
		
        settings.setRenderPriority( RenderPriority.HIGH );
        settings.setCacheMode( WebSettings.LOAD_NO_CACHE );
		settings.setJavaScriptEnabled( true );

		this.mWebView.setScrollBarStyle( View.SCROLLBARS_INSIDE_OVERLAY );        
		this.mWebView.setWebViewClient( new WebViewHandler() );		
		
	}
	
	@Override
	public void onBackPressed() {
		
		// if web view can go back, go back
		
		if ( mWebView.canGoBack() ) {
			mWebView.goBack();
		} else {
			super.onBackPressed();
		}
		
	}
	
	private class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			mProgress.setVisibility( View.VISIBLE );
		}
		
		@Override
		protected Boolean doInBackground( Integer... params ) {
			try {
				AssetsManager.extractAllAssets( getApplicationContext(), true );
			} catch (IOException e) {
				MetaioDebug.printStackTrace( Log.ERROR, e );
				return false;
			}
			
			return true;
		}
		
		@Override
		protected void onPostExecute( Boolean result ) {
			
			mProgress.setVisibility( View.GONE );
			
			if( result ) {
				mWebView.loadUrl( "file:///android_asset/WebWrapper/index.html" );  
			} else {
				MetaioDebug.log( Log.ERROR, "Error extracting assets, closing the application..." );
				finish();
			}
			
	    }
		
	}
	
	
	private class WebViewHandler extends WebViewClient {
		@Override
		public void onPageStarted( WebView view, String url, Bitmap favicon ) {
			mProgress.setVisibility( View.VISIBLE );
		}
		
		@Override
		public void onPageFinished( WebView view, String url ) {
			mProgress.setVisibility( View.GONE );
		}
		
	    @Override
	    public boolean shouldOverrideUrlLoading( WebView view, String url ) {
	    	
	    	String ID = url.substring( url.lastIndexOf( "=" ) + 1 );
	    	MetaioDebug.log( "ID detected: " + ID );
	    	
	    	if( url.startsWith( MAP_ID ) ) {
	    		MetaioDebug.log( "Native code to be loaded " + MAP_ID );
	    		
	    		Intent intent = new Intent(getApplicationContext(), MapActivity.class );
    			startActivity(intent);
    			
    			return true;
	    	} else if( url.startsWith( COMP_ID ) ) {
	    		MetaioDebug.log( "Native code to be loaded " + COMP_ID );
	    		
	    		return true;
	    	}
	    	return false;
	    	
	    }
	}
}

