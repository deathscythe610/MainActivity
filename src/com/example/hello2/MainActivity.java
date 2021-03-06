package com.example.hello2;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.example.hello2.R.id;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.ToggleButton;
//tescik
public class MainActivity extends FragmentActivity{
	private static final String TAG = "TM_MainActivity";
	private static MainActivity instance=null;
	
	private DynamicInfoUpdater diu;
	private Map<Integer,Info> infoClassMap = new HashMap<Integer,Info>();
	protected SensorInfo sensorInfo;
	protected DeadReckoning deadReckoning;
	protected MapInfo mapInfo;
	
	public Runnable toastRunnable(final String text){

	    Runnable aRunnable = new Runnable(){
	        public void run(){
	        	Toast.makeText(MainActivity.getInstance(), text,Toast.LENGTH_SHORT).show();
	        }
	    };

	    return aRunnable;

	}
	
	private MyPagerAdapter pagerAdapter;
	private Timer deadReckoningTimer;
	
	private Boolean wifiLocationFixing = true;
	protected WifiManager wifiManager;
	private BroadcastReceiver broadcastReceiver=null;
	
	public static MainActivity getInstance() {
		if(MainActivity.instance==null) {
			Log.e(TAG,"MainActivity singleton not initialized!");
		}
		return MainActivity.instance;
	}
	
	/**
	 * called after each resume
	 */
	public void init() {
		deadReckoningTimer = new Timer();
		//Run DeadReckoning at 10 ms rate (100Hz)
		//Change to 40ms rate (25Hz) for Zhanhy proposed algorithm 
		deadReckoningTimer.scheduleAtFixedRate(new deadReckoningTask(), 0, 10);
		//deadReckoningTimer.scheduleAtFixedRate(new deadReckoningTask(), 0, 10);
		if(this.wifiLocationFixing) {
			this.wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
			if(!this.wifiManager.isWifiEnabled()){
	          this.wifiManager.setWifiEnabled(true);
	        }
			
			this.broadcastReceiver = new WiFiScanReceiver();
			this.registerReceiver(this.broadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		}
	}
		
    @Override
    public void onCreate(Bundle savedInstanceState) {this.getSupportFragmentManager();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        MainActivity.instance=this;
        pagerAdapter = new MyPagerAdapter(this);
        
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		int startupScreen = Integer.valueOf(sharedPrefs.getString("startup_screen","0"));
		pagerAdapter.setCurrentItem(startupScreen);
		this.mapInfo = new MapInfo();
        this.sensorInfo = new SensorInfo();
        this.deadReckoning = new DeadReckoning();
        this.infoClassMap.put(2, this.sensorInfo);
        this.infoClassMap.put(1,this.deadReckoning);
        this.infoClassMap.put(0,this.mapInfo);
        this.diu = new DynamicInfoUpdater(this.infoClassMap);

        this.reloadSettings();
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"
                + Environment.getExternalStorageDirectory())));
    }
    
    @Override
    public void onDestroy() {
    	Log.d(TAG,"onDestroy()");
    	super.onDestroy();
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.static_info, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
            	startActivity(new Intent(this, Preferences.class));
                return true;
            case R.id.exit:
            	finish();
            	return true;
            case R.id.startDataLogMain:
            	DataLogManager.allow("datalog");
            	return true;
            case R.id.startDataLogMapPath:
            	DataLogManager.allow("wififix");
            	String wififixLogName = DataLogManager.initLog("wififix", null);
            	DataLogManager.allow("mapPath");
            	DataLogManager.addLine("mapPath", "%%% wififixfile='"+wififixLogName+"';",false);            	
            	return true;
            case R.id.drCalibration:
            	this.deadReckoning.startCalibrationLogging();
				showDrCalibrationDialog();
				return true;
			case R.id.gyroscopeCalibration:
				this.sensorInfo.triggerGyroscopeCalibration();
				return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onBackPressed() {
    	finish();
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		reloadSettings();
		DataLogManager.resetAll();
		sensorInfo.init();
		this.mapInfo.init();
		this.init();
	}

	@Override
	protected void onPause() {
		Log.d(TAG,"onPause()");
		super.onPause();
		sensorInfo.sensorManager.unregisterListener(sensorInfo);
		this.sensorInfo.stopLogging();
		if(this.deadReckoningTimer!=null)
			this.deadReckoningTimer.cancel();
		this.unregisterWifiReceiver();
	}
	
	@Override
	protected void onStop() {
		Log.d(TAG,"onStop()");
		DataLogManager.saveAll();
		this.sensorInfo.stopLogging();
		if(this.deadReckoningTimer!=null)
			this.deadReckoningTimer.cancel();
		this.mapInfo.recycleBitmap();
		this.unregisterWifiReceiver();
		super.onStop();
	};
	
	protected void reloadSettings() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        int uiUpdateRate = Integer.valueOf(sharedPrefs.getString("ui_refresh_speed","1000"));
        DataLogManager.globalLogging = sharedPrefs.getBoolean("globalLogging", false);
        this.deadReckoning.setParameters(Float.parseFloat(sharedPrefs.getString("drThresholdMax", "1.0"))
        		,Float.parseFloat(sharedPrefs.getString("drThresholdMin", "-0.9"))
        		,Float.parseFloat(sharedPrefs.getString("drK", "0.7"))
        		);
        this.diu.restart(uiUpdateRate);
        this.sensorInfo.reloadSettings(Integer.valueOf(sharedPrefs.getString("sensor_refresh_speed","3")),
    		Float.valueOf(sharedPrefs.getString("gyroscopeXOffset","0.0")),
			Float.valueOf(sharedPrefs.getString("gyroscopeYOffset","0.0")),
			Float.valueOf(sharedPrefs.getString("gyroscopeZOffset","0.0")),
			Short.parseShort(sharedPrefs.getString("dr_orientation_source", "2")),
			Float.valueOf(sharedPrefs.getString("fuse_coefficient","0.95"))
		);
        this.mapInfo.reloadSettings(sharedPrefs.getBoolean("mapFullRotation", true));
        this.wifiLocationFixing=sharedPrefs.getBoolean("wifiLocationFixing", true);
	}
	
	protected void initUI(int pos) {
		this.diu.initUI(pos);
	}
	
    private void showDrCalibrationDialog() {
    	new AlertDialog.Builder(MainActivity.getInstance())
		.setTitle(R.string.drCalibrationTitle)
		.setMessage(R.string.drCalibrationMsg)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		    	deadReckoning.startCalibrationCalculations();
		    }
	    }).setNegativeButton(android.R.string.cancel,  new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
		    	deadReckoning.endCalibration();
		    }
	    }).show();
    }
    
    protected View findViewForPositionInPager(int position) {
    	return this.pagerAdapter.findViewForPosition(position);
    }
    
    class deadReckoningTask extends TimerTask {
		public void run() {
			//Change to Zhanhy_trigger function to implement Zhanhy algorithm
			deadReckoning.trigger_zhanhy(sensorInfo.getWorldAccelerationZ(),sensorInfo.getWorldAccelerationX(), sensorInfo.orientationFusion.getOrientation());
			//deadReckoning.trigger(sensorInfo.getWorldAccelerationZ(),sensorInfo.orientationFusion.getOrientation());
		}
	}
    
    protected void setMapViewLock(boolean locked) {
    	Log.d(TAG,"mapviewlocked: ");
    	this.pagerAdapter.setViewLock(locked);
    	this.mapInfo.setPannable(locked);
    }
    private void setTitleBarMemoryUsage() {
    	int usedMegs = (int)(Debug.getNativeHeapAllocatedSize() / 1048576L);
		String usedMegsString = String.format(" - Memory Used: %d MB", usedMegs);
		getWindow().setTitle(usedMegsString);
    }
    
    /**
     * unregister broadcastReceiver and catch IllegalArgumentException
     * exception happens when we try to unregister the receiver more than once
     */
    private void unregisterWifiReceiver() {
    	Log.d(TAG,"unregisterWifiReceiver()");
    	try{
    		this.unregisterReceiver(this.broadcastReceiver);
    	} catch(IllegalArgumentException ex) {
    		Log.d(TAG,"caught exception from unregistering");
    	}
    }
}
