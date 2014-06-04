package com.example.hello2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class Map {
	private static final String TAG = "TM_Map";
	public String name;
	public int map;
	public int width;
	public int height;
	private HashMap<Integer,MapPoint> locations = new HashMap<Integer,MapPoint>();
	private HashMap<String,WifiAP> wifiAPs = new HashMap<String,WifiAP>();
	private MapPoint currentMapPoint;
	private int currentLocation=-1;
	private int rotation=0; //in degrees
	private int orientationOffset=0; //in degrees
	public int invertX=1;
	public int invertY=1;
	private int x=0;
	private int y=0;
	public float m2px=1; //multiplier used to convert meters to pixels
	
	public Map() {
	}
	
	public Map(String name, int map,int w, int h, int rot, int orOff) {
		this.sharedConstructor(name, map, w, h, rot, orOff, 1, 1);
	}
	
	public Map(String name, int map,int w, int h, int rot, int orOff, int iX, int iY) {
		this.sharedConstructor(name, map, w, h, rot, orOff, iX, iY);
	}
	
	private void sharedConstructor(String name, int map,int w, int h, int rot, int orOff, int iX, int iY) {
		this.name=name;
		this.map=map;
		this.width=w;
		this.height=h;
		this.rotation=rot;
		this.setOrientationOffset(orOff);
		this.invertX=iX;
		this.invertY=iY;
	}
	
	/**
	 * set orientation offset (degrees!)
	 * @param off orientation offset (degrees!)
	 */
	public void setOrientationOffset(int off) {
		Display display = ((WindowManager) MainActivity.getInstance().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		this.orientationOffset=off+display.getRotation()*90;
	}
	
	/**
	 * returns orientation offset (degrees)
	 * @return
	 */
	public int getOrientationOffsetDegrees() {
		return this.orientationOffset;
	}
	
	/**
	 * 
	 * @return
	 */
	public float getOrientationOffsetRadians() {
		return this.orientationOffset*3.14f/180f;
	}
	
	/**
	 * get rotation in radians 
	 * @return rotation in radians
	 */
	public float getRotationRadians() {
		return this.rotation*3.14f/180f;
	}
	
	/**
	 * get rotation in degrees
	 * @return rotation in degrees
	 */
	public float getRotationDegrees() {
		return this.rotation;
	}
	
	/**
	 * set rotation (degrees!)
	 * @param rot rotation in degrees
	 */
	public void setRotation(int rot) {
		this.rotation=rot;
	}
	
	/**
	 * adds a new map starting point
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param label label/name
	 * @return id of starting point
	 */
	public int addMapPoint(int x, int y, String label) {
		int id = this.locations.size();
		this.locations.put(id ,new MapPoint(x, y, label,id));
		return id;
	}
	
	/**
	 * set starting position based on start point id
	 * @param id start point id
	 */
	public void setPosition(int id) {
		if(this.locations.containsKey(id)) {
			this.currentLocation = id;
			this.currentMapPoint = this.locations.get(this.currentLocation);
			this.x=this.currentMapPoint.x;
			this.y=this.currentMapPoint.y;
		}
	}
	
	/**
	 * changes current start point based on label name given
	 * @param find label of start point to set
	 * @return Boolean success?
	 */
	public Boolean setPosition(String find) {
		for (MapPoint tempMP : this.locations.values()) {
	    	if(find.equals(tempMP.label)) {
	    		this.setPosition(tempMP.id);
	    		return true;
	    	}
	    }
		return false;
	}
	
	/**
	 * set starting position based on unscaled map coordinates
	 * @param x x coordinate
	 * @param y y coordinate
	 */
	public void setPosition(int x, int y) {
		this.currentLocation=-1;
		this.currentMapPoint=null;
		this.x=x;
		this.y=y;
//		Log.d(TAG,"setStartPoint() "+this.x+", "+this.y);
	}
	
	/**
	 * returns the start point x coordinate
	 * @return 
	 */
	public int getStartX() {
		return this.x;
	}
	
	/**
	 * returns the start point y coordinate
	 * @return
	 */
	public int getStartY() {
		return this.y;
	}
	
	/**
	 * returns the start point as a PointF object
	 * @return
	 */
	public PointF getStartPoint() {
		return new PointF(this.getStartX(), this.getStartY());
	}
	
	/**
	 * returns list of start points associated with this map
	 * @return list of start points
	 */
	public List<String> getMapPointList() {
		List<String> list = new ArrayList<String>();
	    for (MapPoint value : this.locations.values()) {
	    	list.add(value.label);
	    }
	    return list;
	}
	
	/**
	 * basic bssid verification
	 * checks whether last octet was discarded
	 * @param bssid bssid to verify
	 * @return verification successful
	 */
	public Boolean verifyBssid(String bssid) {
		Boolean ret = bssid.length()==14;
		if(!ret) {
			Log.w(TAG,"verifyBssid() incorrect bssid length! ("+bssid+")");
		}
		return ret;
	}

	/**
	 * add wifi AP
	 * used to fix location based on wifi signal RSS
	 * @param bssid the bssid of the wifi AP without the last octet
	 * @param x x position on unscaled map
	 * @param y y position on unscaled map
	 */
	public void addWifiAP(String bssid, int x, int y) {
		if(this.verifyBssid(bssid))
			this.wifiAPs.put(bssid, new WifiAP(bssid, x, y));
	}
	
	/**
	 * checks whether this map contains a specified bssid
	 * @param bssid
	 * @return
	 */
	public Boolean hasWifiAP(String bssid) {
		return this.wifiAPs.containsKey(bssid);
	}
	
	/**
	 * returns WifiAP with specified bssid
	 * @param bssid the bssid of the wifi AP without the last octet
	 * @return WifiAP
	 */
	public WifiAP getWifiAP(String bssid) {
		if(this.hasWifiAP(bssid))
			return this.wifiAPs.get(bssid);
		else
			return null;
	}
}
