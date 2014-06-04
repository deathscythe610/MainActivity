package com.example.hello2;

/*
 * TouchImageView.java
 * By: Michael Ortiz
 * Updated By: Patrick Lackemacher
 * -------------------
 * Extends Android ImageView to include pinch zooming and panning.
 */


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

public class TouchImageView extends ImageView {

    Matrix matrix = new Matrix();

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1f;
    float maxScale = 4f;
    float initScale = 1f;
    float[] m;
    float positionX=0;
    float positionY=0;
    
    float redundantXSpace, redundantYSpace;
    
    float width, height; //imageview width and height
    static final int CLICK = 3;

	private static final String TAG = "TM_TouchImageView";
    float curScale = 1f;
    float right, bottom, 
    origWidth, origHeight, 
    bmWidth, bmHeight; //width and height of bitmap
    
    ScaleGestureDetector mScaleDetector;
    
    Context context;

	private Boolean pannable=false;
	public Boolean fullMapRotation=true;

	private float addX;
	private float addY;
	
    public TouchImageView(Context context) {
        super(context);
        sharedConstructing(context);
    }
    
    public TouchImageView(Context context, AttributeSet attrs) {
    	super(context, attrs);
    	sharedConstructing(context);
    }
    
    private void sharedConstructing(Context context) {
    	super.setClickable(true);
        this.context = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        matrix.setTranslate(1f, 1f);
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);
        this.curScale=this.initScale;

        setOnTouchListener(new OnTouchListener() {

//            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	mScaleDetector.onTouchEvent(event);

            	if(!pannable)
            		return false;
            	matrix.getValues(m);
            	float x = m[Matrix.MTRANS_X];
            	float y = m[Matrix.MTRANS_Y];
            	PointF curr = new PointF(event.getX(), event.getY());
            	
            	switch (event.getAction()) {
	            	case MotionEvent.ACTION_DOWN:
	                    last.set(event.getX(), event.getY());
	                    start.set(last);
	                    mode = DRAG;
	                    break;
	            	case MotionEvent.ACTION_MOVE:
	            		if (mode == DRAG) {
	            			float deltaX = curr.x - last.x;
	            			float deltaY = curr.y - last.y;
	            			float scaleWidth = Math.round(origWidth * curScale);
	            			float scaleHeight = Math.round(origHeight * curScale);
            				if (scaleWidth < width) {
	            				deltaX = 0;
	            				if (y + deltaY > 0)
		            				deltaY = -y;
	            				else if (y + deltaY < -bottom)
		            				deltaY = -(y + bottom); 
            				} else if (scaleHeight < height) {
	            				deltaY = 0;
	            				if (x + deltaX > 0)
		            				deltaX = -x;
		            			else if (x + deltaX < -right)
		            				deltaX = -(x + right);
            				} else {
	            				if (x + deltaX > 0)
		            				deltaX = -x;
		            			else if (x + deltaX < -right)
		            				deltaX = -(x + right);
		            			
	            				if (y + deltaY > 0)
		            				deltaY = -y;
		            			else if (y + deltaY < -bottom)
		            				deltaY = -(y + bottom);
	            			}
                        	matrix.postTranslate(deltaX, deltaY);
                        	last.set(curr.x, curr.y);
	                    }
	            		break;
	            		
	            	case MotionEvent.ACTION_UP:
	            		mode = NONE;
	            		int xDiff = (int) Math.abs(curr.x - start.x);
	                    int yDiff = (int) Math.abs(curr.y - start.y);
	                    if (xDiff < CLICK && yDiff < CLICK)
	                        performClick();
	            		break;
	            		
	            	case MotionEvent.ACTION_POINTER_UP:
	            		mode = NONE;
	            		break;
            	}
                setImageMatrix(matrix);
                invalidate();
                return true; // indicate event was handled
            }

        });
    }
    
    @Override
    public void setImageBitmap(Bitmap bm) { 
        super.setImageBitmap(bm);
        if(bm != null) {
        	bmWidth = bm.getWidth();
        	bmHeight = bm.getHeight();
        }
    }
    
    public void setMaxZoom(float x)
    {
    	maxScale = x;
    }
    
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    	@Override
    	public boolean onScaleBegin(ScaleGestureDetector detector) {
//    		if (true || pannable) {
	    		mode = ZOOM;
	    		return true;
//    		} else {
//    			return false;
//    		}
    	}
    	
		@Override
	    public boolean onScale(ScaleGestureDetector detector) {
			float mScaleFactor = detector.getScaleFactor();
		 	float origScale = curScale;
//		 	Log.d(TAG,"mScaleFactor: "+mScaleFactor);
	        curScale *= mScaleFactor;
	        if (curScale > maxScale) {
	        	curScale = maxScale;
	        	mScaleFactor = maxScale / origScale;
	        } else if (curScale < minScale) {
	        	curScale = minScale;
	        	mScaleFactor = minScale / origScale;
	        }
        	right = width * curScale - width - (2 * redundantXSpace * curScale);
            bottom = height * curScale - height - (2 * redundantYSpace * curScale);
        	if (origWidth * curScale <= width || origHeight * curScale <= height) {
        		matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2);
            	if (mScaleFactor < 1) {
            		matrix.getValues(m);
            		float x = m[Matrix.MTRANS_X];
                	float y = m[Matrix.MTRANS_Y];
                	if (mScaleFactor < 1) {
        	        	if (Math.round(origWidth * curScale) < width) {
        	        		if (y < -bottom)
            	        		matrix.postTranslate(0, -(y + bottom));
        	        		else if (y > 0)
            	        		matrix.postTranslate(0, -y);
        	        	} else {
	                		if (x < -right) 
	        	        		matrix.postTranslate(-(x + right), 0);
	                		else if (x > 0) 
	        	        		matrix.postTranslate(-x, 0);
        	        	}
                	}
            	}
        	} else {
            	matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
            	matrix.getValues(m);
            	float x = m[Matrix.MTRANS_X];
            	float y = m[Matrix.MTRANS_Y];
            	if (mScaleFactor < 1) {
    	        	if (x < -right) 
    	        		matrix.postTranslate(-(x + right), 0);
    	        	else if (x > 0) 
    	        		matrix.postTranslate(-x, 0);
    	        	if (y < -bottom)
    	        		matrix.postTranslate(0, -(y + bottom));
    	        	else if (y > 0)
    	        		matrix.postTranslate(0, -y);
            	}
        	}
	        return true;
	        
	    }
		
		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			mode = NONE;
			super.onScaleEnd(detector);
		}
	}
    
	@SuppressLint("NewApi")
	@Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        if(this.fullMapRotation) {
	        int maxDim = (int)Math.max(height,width);
			this.getLayoutParams().height=maxDim;
			this.getLayoutParams().width=maxDim;
			this.addX+=(width-maxDim)/2;
			this.addY+=(height-maxDim)/2;
			Log.d(TAG,"Addx/y: "+this.addX+", "+this.addY);
//			this.setTranslationX(this.addX);
			this.setTranslationY(this.addY);
        }
		
        matrix.setScale(curScale, curScale);

        // Center the image
        redundantYSpace = (float)height - (curScale * (float)bmHeight) ;
        redundantXSpace = (float)width - (curScale * (float)bmWidth);
        redundantYSpace /= (float)2;
        redundantXSpace /= (float)2;

//        matrix.postTranslate(redundantXSpace, redundantYSpace);
//        Log.d(TAG,"Redundant space: "+redundantXSpace+" / "+redundantYSpace);
        
        origWidth = width - 2 * redundantXSpace;
        origHeight = height - 2 * redundantYSpace;
        right = width * curScale - width - (2 * redundantXSpace * curScale);
        bottom = height * curScale - height - (2 * redundantYSpace * curScale);
//        Log.d(TAG,"Onmeasure centerX: "+this.centerX+", centerY: "+this.centerY);
//        Log.d(TAG,"Onmeasure width: "+width+", bmWidth: "+bmWidth+", origWidth: "+this.origWidth);
//        Log.d(TAG,"Onmeasure right: "+right+", bottom: "+this.bottom);
          Log.d(TAG,"Onmeasure width: "+this.width+", height: "+this.height);
//        setImageMatrix(matrix);
    }
    
    /**
     * pixels to center image on are given in bitmapCoordinates
     * passing 0,0 centers in upper-left corner
     * passing bmpWidth,bmpHeight centers in lower-right corner
     * @param centerX pixel to center image on
     * @param centerY pixel to center image on
     */
    public void centerImage(float centerX, float centerY) {
    	if(this.pannable) {
    		return;
    	}
    	
    	positionX = -centerX*this.curScale+this.width/2f;//-centerX/2f*this.saveScale+this.width/2f;// center at current location
    	if(!this.fullMapRotation) {
	    	if (this.bmWidth*this.curScale<this.width) // bitmap smaller than screen
	    		positionX=(width - (this.curScale * this.bmWidth))/2f; // center bitmap at center
	    	else { // bitmap bigger than screen
	    		if (positionX<this.width-this.bmWidth*this.curScale) //whitespace on right
	    			positionX=this.width-this.bmWidth*this.curScale;
	    		if (positionX>0) //whitespace on left
	    			positionX=0;
	    	}
    	}
        positionY = -centerY*this.curScale+(this.height)/2f;// center at current location
        if(!this.fullMapRotation) {
	        if (this.bmHeight*this.curScale<this.height) // bitmap smaller than screen
				positionY=(height - (this.curScale * this.bmHeight))/2f; // center bitmap at center
			else { // bitmap bigger than screen
				if (positionY<this.height-this.bmHeight*this.curScale) //whitespace on right
					positionY=this.height-this.bmHeight*this.curScale;
				if (positionY>0) //whitespace on left
					positionY=0;
			}
        }
//        Log.d(TAG,"tx: "+positionX+", ty: "+positionY+", bmWidth*scale="+(this.bmWidth*this.curScale));
        matrix.setScale(this.curScale,this.curScale);
        this.matrix.postTranslate(positionX,positionY);
        setImageMatrix(matrix);
    }
    
    public void setPannable(Boolean pannable) {
    	this.pannable=pannable;
    }
    
}
