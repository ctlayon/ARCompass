package com.ctlayon.arcompass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

public class CompassView extends View {

	private Paint markerPaint;
	private Paint textPaint;
	private int textHeight;
	private float bearing;
	
	int[] borderGradientColors;
	float[] borderGradientPositions;    

    public CompassView(Context context) {
    	super(context);
    	initCompassView();
    }   

    public CompassView(Context context, AttributeSet attrs) {
    	super(context, attrs);
    	initCompassView();
    }

    public CompassView(Context context, AttributeSet attrs, int defaultStyle) {
    	super(context, attrs, defaultStyle);
    	initCompassView();
    }
    
    @SuppressLint("ResourceAsColor")
	protected void initCompassView() {
	    this.setFocusable( true );
	    Resources r = this.getResources();
	    	
	    this.textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    this.textPaint.setColor(r.getColor(R.color.text_color));
	    this.textPaint.setFakeBoldText(true);
	    this.textPaint.setSubpixelText(true);
	    this.textPaint.setTextAlign(Align.LEFT);
	
	    this.textHeight = (int)textPaint.measureText("yY");
	
	    this.markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    this.markerPaint.setColor(r.getColor(R.color.marker_color));
	    this.markerPaint.setAlpha(200);
	    this.markerPaint.setStrokeWidth(1);
	    this.markerPaint.setStyle(Paint.Style.STROKE);
	    this.markerPaint.setShadowLayer(2, 1, 1, r.getColor(R.color.shadow_color));
	
	    this.borderGradientColors = new int[4];
	    this.borderGradientPositions = new float[4];
	
	    this.borderGradientColors[3] = r.getColor(R.color.outer_border);
	    this.borderGradientColors[2] = r.getColor(R.color.inner_border_one);
	    this.borderGradientColors[1] = r.getColor(R.color.inner_border_two);
	    this.borderGradientColors[0] = r.getColor(R.color.inner_border);
	    this.borderGradientPositions[3] = 0.0f;
	    this.borderGradientPositions[2] = 1 - 0.03f;
	    this.borderGradientPositions[1] = 1 - 0.06f;
	    this.borderGradientPositions[0] = 1.0f;
	  
    }
  
    @Override    
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { 
    	
    	int measuredWidth = measure( widthMeasureSpec );
    	int measuredHeight = measure( heightMeasureSpec );
        
    	int d = Math.min( measuredWidth, measuredHeight );
        
    	// make it 50% of the screen width or height
    	d = (int) ( d * 0.5f );
    	setMeasuredDimension( d, d );    
    }
      
    /**
     * Measures the size of the screen based on the specs
     */
    private int measure( int measureSpec ) {
    	
    	int result = 0; 
    	
    	int specMode = MeasureSpec.getMode( measureSpec );
    	int specSize = MeasureSpec.getSize( measureSpec ); 

    	if ( specMode == MeasureSpec.UNSPECIFIED ) {
    		result = 200; // default value
    	} else {
    		result = specSize;
    	} 
    	
    	return result;
    }

    /**
     * Draws the compass to the screen
     */
    @Override 
    protected void onDraw(Canvas canvas) {
	  
    	int px = getMeasuredHeight() / 2;
    	int py = getMeasuredWidth() / 2;
    	
    	Point center = new Point(px, py);

    	int radius = Math.min(px, py)-2;

    	RectF outerBox = new RectF(
    			center.x - radius, 
                center.y - radius, 
                center.x + radius, 
                center.y + radius );

    	RadialGradient borderGradient = new RadialGradient(
    			px, py, radius,
    			borderGradientColors, 
    			borderGradientPositions,
    			TileMode.CLAMP);
    	
    	Paint pgb = new Paint();
    	pgb.setShader( borderGradient );

    	Path outerRingPath = new Path();
    	outerRingPath.addOval( outerBox, Direction.CW );

    	canvas.drawPath( outerRingPath, pgb );	 	
	 	canvas.rotate( -1 * bearing, px, py );

	 	double increment = 22.5;
	  
	 	// Draw Heading Text
	 	
	 	for (double i = 0; i < 360; i += increment)
	 	{
	 		CompassDirection cd = CompassDirection.values()[(int)(i / 22.5)];
	 		String headString = cd.toString();

	 		float headStringWidth = textPaint.measureText(headString);
	 		PointF headStringCenter = new PointF( center.x - headStringWidth / 2, outerBox.top + 1 + textHeight );

	 		if (i % increment == 0)
	 			canvas.drawText( headString,  headStringCenter.x, headStringCenter.y, textPaint );
	 		else
	 			canvas.drawLine( center.x, (int) outerBox.top, center.x, (int) outerBox.top + 3, markerPaint );

	 		canvas.rotate( (int) increment, center.x, center.y );
	 	}
	 	
		canvas.restore();
	}
  
	public void setBearing(float pBearing) {
		bearing = pBearing;
	}
  
	public float getBearing() {
	    return bearing;
	}
	
	private enum CompassDirection 
    { 
	    N, NNE, NE, ENE,
        E, ESE, SE, SSE,
        S, SSW, SW, WSW,
        W, WNW, NW, NNW 
    }
}
