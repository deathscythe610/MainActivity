package com.example.hello2;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.LogManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;

public class MapInfo extends Info{
	private static final String TAG = "TM_MapInfo";
	private static final int wifiFixRadius = 6; //radius in meters to fix location based on wifi location
	private int markerSize = 4;
	private Paint hereMarkerPaint;
	private Paint hereArrowPaint;
	private HashMap<String,Map> mapList = new HashMap<String,Map>();
	private Map curMap;
	private TouchImageView mapView;
	private Bitmap mapBitmap; //contains pure map
	private Canvas hereMarkerCanvas; //contains marker
	private Bitmap combinedBitmap; //contains map + marker
	private long lastUpdate=0;
	private PointF mapPoint = new PointF(0,0);
	private PointF deltaPoint = new PointF(0,0);
	private Boolean fullMapRotation = true;
	private boolean skipFirst;
	
	public MapInfo(){
		super();
		this.loadMaps();
		
		this.hereMarkerPaint = new Paint();
		this.hereMarkerPaint.setColor(Color.RED);
//		this.hereMarkerPaint.setAlpha(100);
		this.hereArrowPaint = new Paint();
		this.hereArrowPaint.setColor(Color.BLUE);
		this.hereArrowPaint.setAlpha(100);
	}
	
	@Override
	void createUiMap(){
		View layout = (View)MainActivity.getInstance().findViewForPositionInPager(0);
		if(layout==null) {
			Log.d(TAG,"layout null");
			return;
		}
//		uiMap.put("logInfo", (TextView) layout.findViewById(R.id.logInfo));
	}
	
	@Override
	void init() {
		View layout = (View)MainActivity.getInstance().findViewForPositionInPager(0);
		if(layout==null) {
			Log.d(TAG,"layout null");
			return;
		}
		this.populateMapStartPointsSpinner(layout);
		this.setStartPointSpinnerListener(layout);
		
		
		CheckBox mapViewLock = (CheckBox)layout.findViewById(R.id.mapViewLock);
		mapViewLock.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				MainActivity.getInstance().setMapViewLock(isChecked);
			}
		});;
		this.mapView = (TouchImageView) layout.findViewById(R.id.mapView);
		this.mapView.fullMapRotation=this.fullMapRotation;
		
//		System.gc();
		BitmapFactory.Options options = new BitmapFactory.Options(); 
		options.inPurgeable = true;
		this.mapBitmap = BitmapFactory.decodeResource(MainActivity.getInstance().getResources(), this.curMap.map,options);
		this.redraw();
	}
	
	@Override
	void update() {
		if(this.mapBitmap!=null && this.lastUpdate>0)// && Misc.getTime()-this.lastUpdate>1000)
		{
//			this.mapView.destroyDrawingCache();
			this.hereMarkerCanvas=null;
			this.combinedBitmap.recycle();
			this.deltaPoint = new PointF(MainActivity.getInstance().deadReckoning.getDistanceX()*this.curMap.m2px*this.curMap.invertX, MainActivity.getInstance().deadReckoning.getDistanceY()*this.curMap.m2px*this.curMap.invertY);
			this.calculateMapPoint();
			this.redraw();
		}
	}
	
	public void redraw() {
		this.combinedBitmap = Bitmap.createBitmap(this.mapBitmap.getWidth(), this.mapBitmap.getHeight(), Bitmap.Config.RGB_565);
		this.hereMarkerCanvas = new Canvas(this.combinedBitmap);
		this.hereMarkerCanvas.drawBitmap(this.mapBitmap, 0, 0, null);
		
		this.hereMarkerCanvas.drawCircle(this.getScaledX(),this.getScaledY(), this.markerSize/(0.5f*this.mapView.curScale), this.hereMarkerPaint);
		float o = MainActivity.getInstance().sensorInfo.orientationFusion.getFusedZOrientation()*180/3.14f+this.curMap.getRotationDegrees();
//		float xEnd = this.getScaledX()+this.curMap.invertX*l*FloatMath.sin(o);
//		float yEnd = this.getScaledY()+this.curMap.invertY*l*FloatMath.cos(o);
//		hereMarkerCanvas.drawLine(this.getScaledX(),this.getScaledY(), xEnd, yEnd, this.hereMarkerPaint);
        this.hereMarkerCanvas.rotate(o, this.getScaledX(), this.getScaledY());
        this.hereMarkerCanvas.drawPath(this.getArrow(), this.hereArrowPaint);
//		this.mapView.setImageDrawable(new BitmapDrawable(MainActivity.getInstance().getResources(), this.combinedBitmap));
		this.mapView.setImageBitmap(this.combinedBitmap);
//		Log.d(TAG,"trueScaleX: "+getTrueScaleX()+", trueScaleY: "+getTrueScaleY());
//		Log.d(TAG,"setRotation: "+(-o));
		if(this.mapView.fullMapRotation) {
			this.mapView.setRotation(-o);//*180/3.14f
		}
		this.mapView.centerImage(this.getScaledX(),this.getScaledY());
		
    	this.lastUpdate=Misc.getTime();
    	DataLogManager.addLine("mapPath", this.getX()+", "+this.getY());
	}
	
	/**
	 * draws an arrow located at the current user location (not rotated)
	 * @return
	 */
	private Path getArrow() {
		Path mPath = new Path();
        mPath.lineTo(20, 20);
        mPath.lineTo(0, -50);
        mPath.lineTo(-20, 20);
        mPath.offset(this.getScaledX(), this.getScaledY());
        mPath.close();
        return mPath;
	}
	
	/**
	 * get current X coordinate in original unoptimized bitmap coordinate system
	 * @return
	 */
	private float getX() {
		return this.mapPoint.x;
	}
	
	/**
	 * get current Y coordinate in original unoptimized bitmap coordinate system
	 * @return
	 */
	private float getY() {
		return this.mapPoint.y;
	}
	
	/**
	 * get current X coordinate in scaled/optimized bitmap coordinate system
	 * @return
	 */
	private float getScaledX() {
		return this.getX()*this.getBitmapScaleX();
	}
	
	/**
	 * get current Y coordinate in scaled/optimized bitmap coordinate system
	 * @return
	 */
	private float getScaledY() {
		return this.getY()*this.getBitmapScaleY();
	}
	
	/**
	 * get bitmap scale (x) resulting from bitmap optimalization
	 * @return
	 */
	private float getBitmapScaleX() {
		return this.mapBitmap.getWidth()/(float)this.curMap.width;
	}
	
	/**
	 * get bitmap scale (y) resulting from bitmap optimalization
	 * @return
	 */
	private float getBitmapScaleY() {
		return this.mapBitmap.getHeight()/(float)this.curMap.height;
	}
	
	public void setPannable(Boolean pannable) {
		this.mapView.setPannable(pannable);
	}
	
	/**
	 * rotates deltaPoint around startPoint by curMap.rotation (rad)
	 */
	public void calculateMapPoint() {
		float rot = this.curMap.getRotationRadians();
		float tempx = this.curMap.getStartX()
				+ (FloatMath.cos(rot) * this.deltaPoint.x
				- FloatMath.sin(rot) * this.deltaPoint.y ); 
		float tempy = this.curMap.getStartY()
				+ (FloatMath.sin(rot) * this.deltaPoint.x
				+ FloatMath.cos(rot) * this.deltaPoint.y); 
		this.mapPoint.x=tempx;
		this.mapPoint.y=tempy;
//		Log.d(TAG,"curpoint x: "+this.mapPoint.x+", y: "+this.mapPoint.y);
	}
	
	public void recycleBitmap() {
//		Log.d(TAG,"recycleBitmap() called");
		if(this.mapBitmap!=null) {
			this.mapView.setImageDrawable(null);
			this.hereMarkerCanvas=null;
			this.combinedBitmap.recycle();
			this.combinedBitmap=null;
			this.mapBitmap.recycle();
			this.mapBitmap=null;
			System.gc();
		}
	}
	
	/**
	 * load maps with map points from xml resource
	 */
	private void loadMaps() {
		try {
			Resources res = MainActivity.getInstance().getResources();
			XmlResourceParser xpp = res.getXml(R.xml.map);
			xpp.next();
			int eventType = xpp.getEventType();
			String tagName;
			Map tempMap = new Map();
			Boolean defaultMap=false;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				tagName = xpp.getName();
				if(eventType == XmlPullParser.START_TAG) {
//					Log.d(TAG,"tag: "+temp);
					if(tagName.equals("map")) {
						tempMap = new Map();
						tempMap.name = xpp.getAttributeValue(null, "name");
						tempMap.map = xpp.getAttributeResourceValue(null, "src", -1);
						tempMap.width = xpp.getAttributeIntValue(null, "width", 0);
						tempMap.height = xpp.getAttributeIntValue(null, "height", 0);
						tempMap.setRotation(xpp.getAttributeIntValue(null, "rotation", 0));
						tempMap.setOrientationOffset(xpp.getAttributeIntValue(null, "orientationOffset", 0));
						tempMap.invertX = xpp.getAttributeIntValue(null, "invertx", 1);
						tempMap.invertY = xpp.getAttributeIntValue(null, "inverty", 1);
						tempMap.m2px = xpp.getAttributeFloatValue(null, "m2px", 1);
						defaultMap=xpp.getAttributeBooleanValue(null, "default", false);
					} else if (tagName.equals("map_point")) {
						int tempId = tempMap.addMapPoint(xpp.getAttributeIntValue(null, "x",-1),xpp.getAttributeIntValue(null, "y",-1),xpp.getAttributeValue(null, "name"));
						if(xpp.getAttributeBooleanValue(null, "default", false)) {
							tempMap.setPosition(tempId);
						}
					} else if (tagName.equals("wifi_ap")) {
						tempMap.addWifiAP(xpp.getAttributeValue(null, "bssid"),xpp.getAttributeIntValue(null, "x",-1),xpp.getAttributeIntValue(null, "y",-1));
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					if(tagName.equals("map")) {
						this.mapList.put(tempMap.name, tempMap);
						if (defaultMap==true) {
							this.curMap=tempMap;
						}
					}
				}
			    eventType = xpp.next();
			}
		} catch (IOException ex) {
			Log.e(TAG, ex.toString());
		} catch (XmlPullParserException ex) {
			Log.e(TAG, ex.toString());
		}
	}
	
	/**
	 * populates the start point reset spinner with data from current map
	 * @param layout the layout containing mapStartPointSpinner
	 */
	private void populateMapStartPointsSpinner(View layout) {
		Spinner sp = (Spinner)layout.findViewById(R.id.mapStartPointSpinner);
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(MainActivity.getInstance(),android.R.layout.simple_spinner_dropdown_item,this.curMap.getMapPointList());
		sp.setAdapter(dataAdapter);
	}
	
	/**
	 * sets the map start point spinner OnItemSelectedListener
	 * resets DeadReckoning data, and sets new start point
	 * @param layout the layout containing mapStartPointSpinner
	 */
	private void setStartPointSpinnerListener(View layout) {
		Spinner sp = (Spinner)layout.findViewById(R.id.mapStartPointSpinner);
		skipFirst=true;
		sp.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
				if(skipFirst) {
					skipFirst=false;
					return;
				}
//				Log.d(TAG," OnItemSelectedListener::onItemSelected() call");
				MainActivity.getInstance().deadReckoning.reset();
				Boolean success = curMap.setPosition(parent.getItemAtPosition(pos).toString());
				if(success)
					Misc.toast(R.string.mapStartPointResetSuccess);
				else
					Misc.toast(R.string.mapStartPointResetFailed);
			  }

			public void onNothingSelected(AdapterView<?> arg0) {
				Misc.toast(R.string.mapStartPointResetFailed);
			}
		});
	}

	public void reloadSettings(boolean fmr) {
		this.fullMapRotation=fmr;
	}
	
	/**
	 * fix location using provided wifi AP bssid
	 * called when wifi AP has strong strength
	 * fix location when user is more than wifiFixRadius from AP
	 * if outside of radius, puts user on radius, 
	 * on intersection of the circumference and the line connecting current position
	 * with wifi ap position
	 * @param bssid BSSID of AP
	 * @return Boolean was the location fixed?
	 */
	public Boolean wifiLocationFix(String bssid) {
		if(this.curMap.hasWifiAP(bssid)) {
			WifiAP temp = this.curMap.getWifiAP(bssid);
			float orgX = this.getX();
			float orgY = this.getY();
			float dx = orgX - temp.x;
			float dy = orgY - temp.y;
			double vectorLength = this.vectorLength(dx, dy);
			if(vectorLength>wifiFixRadius*this.curMap.m2px) { //fix location
				double fixMultiplier = wifiFixRadius*this.curMap.m2px/vectorLength;
				dx*=fixMultiplier;
				dy*=fixMultiplier;
				MainActivity.getInstance().deadReckoning.reset();
				this.curMap.setPosition((int)((temp.x+dx)*this.getBitmapScaleX()), (int)((temp.y+dy)*this.getBitmapScaleY()));
				Misc.toast("wifi location fixed");
				DataLogManager.addLine("wififix", orgX+", "+orgY+", " + this.curMap.getStartX()+", "+this.curMap.getStartY());
				return true;
			}
		}
		return false;
	}
	
	/**
	 * returns distance between current point and given coordinates
	 * operates in original bitmap size
	 * @param x
	 * @param y
	 * @return distance between current point and given coordinates (px)
	 */
	public double distanceFrom(int x, int y) {
		return this.vectorLength(x-this.getX(), y-this.getY());
	}
	
	/**
	 * returns length of given vector
	 * @param x
	 * @param y
	 * @return length(magnitude) of given vector
	 */
	private double vectorLength(float x, float y) {
		return Math.sqrt(Math.pow(x,2)+Math.pow(y,2));
	}
	
	public Map getCurMap() {
		return this.curMap;
	}
	
}
