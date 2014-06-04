package com.example.hello2;

public class MapPoint {
	public int x;
	public int y;
	public String label;
	public int id;
	
	public MapPoint(int x, int y, String label, int id) {
		this.x=x;
		this.y=y;
		this.label=label;
		this.id=id;
	}
	
	public MapPoint(int x, int y, String label) {
		this.x=x;
		this.y=y;
		this.label=label;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	@Override
	public String toString() {
		return this.label+" ("+this.x+", "+this.y+")";
	}
}
