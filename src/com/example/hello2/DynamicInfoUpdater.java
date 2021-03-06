package com.example.hello2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;
import java.util.Map.Entry;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class DynamicInfoUpdater {
	private static final String TAG = "TM_DynamicInfoUpdater";
	
	private Handler diuHandler=null;
	private int uiUpdateRate;
	private Map<Integer,Info> infoClassMap = new HashMap<Integer,Info>();
	
	public DynamicInfoUpdater(Map<Integer,Info> info) {
		this.infoClassMap=info;
		this.init();
	}
	
	public void restart(int uiUpdateRate) {
		this.uiUpdateRate=uiUpdateRate;
		this.restart();
	}
	
	protected void restart() {
		if(diuHandler!=null) {
        	diuHandler.removeCallbacks(diuRunner);
        	diuRunner.run();
        }
	}
	
	private void init() {
    	diuHandler = new Handler();
        diuRunner.run();
	}
	
	protected void update() {
    	Iterator<Entry<Integer, Info>> iter = this.infoClassMap.entrySet().iterator();
    	while (iter.hasNext()) {
    		Entry<Integer, Info> pair = iter.next();
        	Info tempClass = pair.getValue();
        	tempClass.update();
    		Iterator<Entry<String, TextView>> it = tempClass.uiMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, TextView> pairs = (Map.Entry<String, TextView>)it.next();
//                Log.d(TAG,pairs.toString());
                if(tempClass.valuesMap.containsKey(pairs.getKey())) {
                	TextView temp = pairs.getValue();
                	if(temp!=null) {
                		temp.setText(tempClass.valuesMap.get(pairs.getKey()));
                	} else {
                		Log.d(TAG,pairs.toString());
                	}
                }
            }
    	}
    	
    }
	
	protected void initUI(int pos) {
		if(this.infoClassMap.containsKey(pos)) {
			Info tempClass = this.infoClassMap.get(pos);
			tempClass.init();
			tempClass.createUiMap();
		}
	}
    
//    class UpdateDynamicInfoTask extends TimerTask {
//	    public void run() {
//	    	update();
//	    }
//    }
    
    Runnable diuRunner = new Runnable()
    {
		public void run() {
              update();
              diuHandler.postDelayed(diuRunner, uiUpdateRate);
         }
    };
}
