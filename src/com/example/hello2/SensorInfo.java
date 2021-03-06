package com.example.hello2;

import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

public class SensorInfo extends Info  implements SensorEventListener{
	private static final String TAG = "TM_SensorInfo";
	protected int sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
	protected SensorManager sensorManager;
	
	private float[] magneticFieldValues={0,0,0};
	private float[] accelerometerValues={0,0,0};
	private float[] linearAccelerometerValues={0,0,0};
//	private float[] actual_orientation={0,0,0};
	private double displacementX=0.0;
	private double displacementY=0.0;
	private double displacementZ=0.0;
	private double worldAccelerationX=0.0;
	private double worldAccelerationY=0.0;
	private double worldAccelerationZ=0.0;
//	private double prevWorldAccelerationX=0.0;
//	private double prevWorldAccelerationY=0.0;
//	private double prevWorldAccelerationZ=0.0;
//	private double worldSpeedX=0.0;
//	private double worldSpeedY=0.0;
//	private double worldSpeedZ=0.0;
//	private double prevWorldSpeedX=0.0;
//	private double prevWorldSpeedY=0.0;
//	private double prevWorldSpeedZ=0.0;
//	private long prevTime = 0;
	protected OrientationFusion orientationFusion;
	private Timer logTimer;
	private float[] gyroscopeValues={0,0,0};
	
	
	public SensorInfo(){
		super();
		this.linearAccelerometerValues=new float[4];
		this.linearAccelerometerValues[3]=0;
		this.sensorManager = (SensorManager)MainActivity.getInstance().getSystemService(Context.SENSOR_SERVICE);
		this.orientationFusion=new OrientationFusion();
	}
	
	public void stopLogging() {
		if(this.logTimer!=null)
			this.logTimer.cancel();
	}
	
	class logOrientationTask extends TimerTask {
		public void run() {
			
		}
	}
		
    public void logData() {
    	float[] oFused = orientationFusion.getFusedOrientation();
    	float[] orgGyro = orientationFusion.getOriginalGyroscopeOrientation();
    	float[] oCompass = orientationFusion.getCompassOrientation();
    	float[] rm = orientationFusion.getRotationMatrix();
    	String line = oFused[0]+","+oFused[1]+","+oFused[2]+","
    			+ oCompass[0]+","+oCompass[1]+","+oCompass[2]+","
    			+ worldAccelerationX + "," + worldAccelerationY + "," + worldAccelerationZ + ","
    			+ magneticFieldValues[0] + "," + magneticFieldValues[1]+ ","+magneticFieldValues[2] + ","
    			+ MainActivity.getInstance().deadReckoning.getSteps() + ","
    			+ MainActivity.getInstance().deadReckoning.getDistance() + ","
    			+ MainActivity.getInstance().deadReckoning.getDistanceX() + ","
    			+ MainActivity.getInstance().deadReckoning.getDistanceY()+","
    			+gyroscopeValues[0]+","+gyroscopeValues[1]+","+gyroscopeValues[2]+","
    			+accelerometerValues[0]+","+accelerometerValues[1]+","+accelerometerValues[2]+","
    			+rm[0]+","+rm[1]+","+rm[2]+","
    			+rm[3]+","+rm[4]+","+rm[5]+","
    			+rm[6]+","+rm[7]+","+rm[8]+","
    			+orgGyro[0]+","+orgGyro[1]+","+orgGyro[2]
    					;
    	DataLogManager.addLine("datalog",line);
    }
//	}
        
	@Override
	void createUiMap(){
		ScrollView layout = (ScrollView)MainActivity.getInstance().findViewForPositionInPager(2);
		if(layout==null) {
			Log.d(TAG,"layout null");
			return;
		}
		uiMap.put("accelerometerXSensorValue", (TextView) layout.findViewById(R.id.accelerometerXSensorValue));
		uiMap.put("accelerometerYSensorValue", (TextView) layout.findViewById(R.id.accelerometerYSensorValue));
		uiMap.put("accelerometerZSensorValue", (TextView) layout.findViewById(R.id.accelerometerZSensorValue));
		uiMap.put("gravityXSensorValue", (TextView) layout.findViewById(R.id.gravityXSensorValue));
		uiMap.put("gravityYSensorValue", (TextView) layout.findViewById(R.id.gravityYSensorValue));
		uiMap.put("gravityZSensorValue", (TextView) layout.findViewById(R.id.gravityZSensorValue));
		uiMap.put("gyroscopeXSensorValue", (TextView) layout.findViewById(R.id.gyroscopeXSensorValue));
		uiMap.put("gyroscopeYSensorValue", (TextView) layout.findViewById(R.id.gyroscopeYSensorValue));
		uiMap.put("gyroscopeZSensorValue", (TextView) layout.findViewById(R.id.gyroscopeZSensorValue));
		uiMap.put("linearAccelerationXSensorValue", (TextView) layout.findViewById(R.id.linearAccelerationXSensorValue));
		uiMap.put("linearAccelerationYSensorValue", (TextView) layout.findViewById(R.id.linearAccelerationYSensorValue));
		uiMap.put("linearAccelerationZSensorValue", (TextView) layout.findViewById(R.id.linearAccelerationZSensorValue));
		uiMap.put("magneticFieldXSensorValue", (TextView) layout.findViewById(R.id.magneticFieldXSensorValue));
		uiMap.put("magneticFieldYSensorValue", (TextView) layout.findViewById(R.id.magneticFieldYSensorValue));
		uiMap.put("magneticFieldZSensorValue", (TextView) layout.findViewById(R.id.magneticFieldZSensorValue));
		uiMap.put("rotationVectorXSensorValue", (TextView) layout.findViewById(R.id.rotationVectorXSensorValue));
		uiMap.put("rotationVectorYSensorValue", (TextView) layout.findViewById(R.id.rotationVectorYSensorValue));
		uiMap.put("rotationVectorZSensorValue", (TextView) layout.findViewById(R.id.rotationVectorZSensorValue));
		uiMap.put("orientationXSensorValue", (TextView) layout.findViewById(R.id.orientationXSensorValue));
		uiMap.put("orientationYSensorValue", (TextView) layout.findViewById(R.id.orientationYSensorValue));
		uiMap.put("orientationZSensorValue", (TextView) layout.findViewById(R.id.orientationZSensorValue));
		uiMap.put("worldAccelerationXSensorValue", (TextView) layout.findViewById(R.id.worldAccelerationXSensorValue));
		uiMap.put("worldAccelerationYSensorValue", (TextView) layout.findViewById(R.id.worldAccelerationYSensorValue));
		uiMap.put("worldAccelerationZSensorValue", (TextView) layout.findViewById(R.id.worldAccelerationZSensorValue));
		uiMap.put("displacementXSensorValue", (TextView) layout.findViewById(R.id.displacementXSensorValue));
		uiMap.put("displacementYSensorValue", (TextView) layout.findViewById(R.id.displacementYSensorValue));
		uiMap.put("displacementZSensorValue", (TextView) layout.findViewById(R.id.displacementZSensorValue));
		uiMap.put("logInfo", (TextView) layout.findViewById(R.id.logInfo));
	}
	
	@Override
	void init() {
		registerSensors();
		DataLogManager.addLine("datalog", "% time | 3x orientation fused | 3x orientation compass | " +
				"3x accelerometer world | 3x magnetic field | steps | distance | x | y | " +
				"3x gyroscope raw | 3x acceleration | 9x rotation matrix | 3x gyroscope original",false);
		DataLogManager.addLine("datalog", "% orientation source: "+this.orientationFusion.getOrientationSource(),false);
		DataLogManager.addLine("datalog", "%%% K="+MainActivity.getInstance().deadReckoning.getK()+";",false);
		DataLogManager.addLine("datalog", "%%% orientationOffset="+MainActivity.getInstance().mapInfo.getCurMap().getOrientationOffsetRadians()+";",false);
		DataLogManager.addLine("datalog", "%%% filterCoefficient="+this.orientationFusion.getFilterCoefficient()+";",false);
	}
	
	@Override
	void update() {//update is also done asynchronously onSensorChanged()
		valuesMap.put("logInfo", DataLogManager.getInfo());
		float oFused = orientationFusion.getFusedZOrientation();
		float oGyro = orientationFusion.getGyroscopeZOrientation();
    	float oCompass = orientationFusion.getCompassZOrientation();
    	valuesMap.put("orientationXSensorValue",Misc.roundToDecimals(oCompass*180/3.14,2) + " / "+ Misc.roundToDecimals(oFused*180/3.14,2) + "/" + Misc.roundToDecimals(oGyro*180/3.14,2) + " / " + Misc.roundToDecimals(this.orientationFusion.getOrientationDiscrete()*180/3.14,2));
//    	valuesMap.put("orientationYSensorValue",oCompass[1] + " / "+ oFused[1]);
//		valuesMap.put("orientationZSensorValue",oCompass[2] + " / "+ oFused[2]);
		valuesMap.put("worldAccelerationXSensorValue", this.worldAccelerationX + "");
		valuesMap.put("worldAccelerationYSensorValue", this.worldAccelerationY + "");
		valuesMap.put("worldAccelerationZSensorValue", this.worldAccelerationZ + "");
		valuesMap.put("displacementXSensorValue",this.displacementX + "");
		valuesMap.put("displacementYSensorValue",this.displacementY + "");
		valuesMap.put("displacementZSensorValue",this.displacementZ + "");
	}
	

	
	private void updateWorldAcceleration() {
		float[] rotationMatrix = this.orientationFusion.getRotationMatrix();
		if(rotationMatrix!=null && this.linearAccelerometerValues!=null) {
			double[][] result = MatrixHelper.matrixMultiply(rotationMatrix, 3, 3, linearAccelerometerValues, 3, 1);
			
			this.worldAccelerationX=result[0][0];
			this.worldAccelerationY=result[1][0];
			this.worldAccelerationZ=result[2][0];
			
			this.logData();
		}
	}
	
//	private void updateDisplacement() {
//		Date date = new Date();
//		long currTime=date.getTime();
//		double dt = 0;
//		if(this.prevTime>0) {
//			dt = (currTime-prevTime)/1000.0;
//		}
//		this.prevTime=currTime;
//		
//		double xBias = 0;//- 0.000049690;
//		double yBias = 0;//+ 0.00050563;
//		double zBias = 0;//- 0.0091;//- 0.0192
//		double accelerometerFixedX =xBias+this.worldAccelerationX;
//		double accelerometerFixedY =yBias+this.worldAccelerationY;
//		double accelerometerFixedZ =zBias+this.worldAccelerationZ;
//		this.worldSpeedX+=(accelerometerFixedX+this.prevWorldAccelerationX)/2*dt;
//		this.worldSpeedY+=(accelerometerFixedY+this.prevWorldAccelerationY)/2*dt;
//		this.worldSpeedZ+=(accelerometerFixedZ+this.prevWorldAccelerationZ)/2*dt;
//		this.displacementX+=(this.worldSpeedX+this.prevWorldSpeedX)/2*dt*1000;//- 488.5955*dt;
//		this.displacementY+=(this.worldSpeedY+this.prevWorldSpeedY)/2*dt*1000;//+3178.8*dt;
//		this.displacementZ+=(this.worldSpeedZ+this.prevWorldSpeedZ)/2*dt*1000;//-138460*dt;
//		
//		this.prevWorldSpeedX=this.worldSpeedX;
//		this.prevWorldSpeedY=this.worldSpeedY;
//		this.prevWorldSpeedZ=this.worldSpeedZ;
//		this.prevWorldAccelerationX=accelerometerFixedX;
//		this.prevWorldAccelerationY=accelerometerFixedY;
//		this.prevWorldAccelerationZ=accelerometerFixedZ;
//	}
	
	public void registerSensors() {
    	Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	if(accelSensor!=null) {
    		sensorManager.registerListener(this,accelSensor,sensorDelay);
    	} else {
    		valuesMap.put("accelerometerXSensorValue","No accelerometer sensor available");
			valuesMap.put("accelerometerYSensorValue","-");
			valuesMap.put("accelerometerZSensorValue","-");
    	}
    	
//    	Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
//    	if(gravitySensor!=null) {
//    		sensorManager.registerListener(this,gravitySensor,sensorDelay);
//    	} else {
//    		valuesMap.put("gravityXSensorValue","No gravity sensor available");
//			valuesMap.put("gravityYSensorValue","-");
//			valuesMap.put("gravityZSensorValue","-");
//    	}
    	
    	Sensor gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    	if(gyroscopeSensor!=null) {
    		sensorManager.registerListener(this,gyroscopeSensor,sensorDelay);
    	} else {
    		valuesMap.put("gyroscopeXSensorValue","No gyroscope sensor available");
			valuesMap.put("gyroscopeYSensorValue","-");
			valuesMap.put("gyroscopeZSensorValue","-");
    	}
    	
    	Sensor linearAccelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    	if(linearAccelerationSensor!=null) {
    		sensorManager.registerListener(this,linearAccelerationSensor,sensorDelay);
    	} else {
    		valuesMap.put("linearAccelerationXSensorValue","No linear acceleration sensor available");
			valuesMap.put("linearAccelerationYSensorValue","-");
			valuesMap.put("linearAccelerationZSensorValue","-");
    	}
    	
    	Sensor magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    	if(magneticFieldSensor!=null) {
    		sensorManager.registerListener(this,magneticFieldSensor,sensorDelay);
    	} else {
    		valuesMap.put("magneticFieldXSensorValue","No magnetic field sensor available");
			valuesMap.put("magneticFieldYSensorValue","-");
			valuesMap.put("magneticFieldZSensorValue","-");
    	}
    	
//    	Sensor rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
//    	if(rotationVectorSensor!=null) {
//    		sensorManager.registerListener(this,rotationVectorSensor,sensorDelay);
//    	} else {
//    		valuesMap.put("rotationVectorXSensorValue","No rotation vector sensor available");
//			valuesMap.put("rotationVectorYSensorValue","-");
//			valuesMap.put("rotationVectorZSensorValue","-");
//    	}
    }
	
	public void onSensorChanged(SensorEvent event) {
		   int sensorType = event.sensor.getType();
//		   if(sensorType==Sensor.TYPE_GRAVITY){
//			   valuesMap.put("gravityXSensorValue",String.valueOf(event.values[0]));
//			   valuesMap.put("gravityYSensorValue",String.valueOf(event.values[1]));
//			   valuesMap.put("gravityZSensorValue",String.valueOf(event.values[2]));
//		   }
		   if(sensorType==Sensor.TYPE_ACCELEROMETER){
			   accelerometerValues=event.values.clone();
			   this.orientationFusion.setAccelerometer(accelerometerValues);
			   
			   valuesMap.put("accelerometerXSensorValue",String.valueOf(event.values[0]));
			   valuesMap.put("accelerometerYSensorValue",String.valueOf(event.values[1]));
			   valuesMap.put("accelerometerZSensorValue",String.valueOf(event.values[2]));
//			   this.updateDisplacement();
		   }
		   if(sensorType==Sensor.TYPE_GYROSCOPE){
//			   this.orientationFusion.integrateGyroscopeData(event.values);
			   this.orientationFusion.gyroFunction(event);
			   float[] temp = this.orientationFusion.getFusedOrientation();
			   gyroscopeValues=event.values.clone();
			   valuesMap.put("gyroscopeXSensorValue",Misc.roundToDecimals(event.values[0],4)+" / "+Misc.roundToDecimals(temp[0]*180/3.14,2));
			   valuesMap.put("gyroscopeYSensorValue",Misc.roundToDecimals(event.values[1],4)+" / "+Misc.roundToDecimals(temp[1]*180/3.14,2));
			   valuesMap.put("gyroscopeZSensorValue",Misc.roundToDecimals(event.values[2],4)+" / "+Misc.roundToDecimals(temp[2]*180/3.14,2));
		   }
		   if(sensorType==Sensor.TYPE_LINEAR_ACCELERATION){
			   
			   valuesMap.put("linearAccelerationXSensorValue",String.valueOf(event.values[0]));
			   valuesMap.put("linearAccelerationYSensorValue",String.valueOf(event.values[1]));
			   valuesMap.put("linearAccelerationZSensorValue",String.valueOf(event.values[2]));
			   float[] temp = event.values.clone();
			   this.linearAccelerometerValues[0]=temp[0];
			   this.linearAccelerometerValues[1]=temp[1];
			   this.linearAccelerometerValues[2]=temp[2];
//			   this.linearAccelerometerValues=event.values.clone();
			   this.updateWorldAcceleration();
		   }
		   if(sensorType==Sensor.TYPE_MAGNETIC_FIELD){
			   magneticFieldValues=event.values.clone();
			   this.orientationFusion.setMagneticField(magneticFieldValues);
			   
			   valuesMap.put("magneticFieldXSensorValue",String.valueOf(event.values[0]));
			   valuesMap.put("magneticFieldYSensorValue",String.valueOf(event.values[1]));
			   valuesMap.put("magneticFieldZSensorValue",String.valueOf(event.values[2]));
		   }
//		   if(sensorType==Sensor.TYPE_ROTATION_VECTOR){
//			   valuesMap.put("rotationVectorXSensorValue",String.valueOf(event.values[0]));
//			   valuesMap.put("rotationVectorYSensorValue",String.valueOf(event.values[1]));
//			   valuesMap.put("rotationVectorZSensorValue",String.valueOf(event.values[2]));
//		   }
		   
	   }
	
		public void onAccuracyChanged(Sensor arg0, int arg1) {
		    // TODO Auto-generated method stub
			    
		}
		
		public void triggerGyroscopeCalibration() {
			new AlertDialog.Builder(MainActivity.getInstance())
				.setTitle(R.string.gyroscopeCalibrationTitle)
				.setMessage(R.string.gyroscopeCalibrationMsg)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int whichButton) {
				    	orientationFusion.startGyroscopeCalibration();
				    }
			    }).setNegativeButton(android.R.string.cancel,  new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int whichButton) {
				    	
				    }
			    }).show();
		}
		
		public void reloadSettings(int sensorDelay, float gyroscopeXOffset, float gyroscopeYOffset, float gyroscopeZOffset, short orientationSource, float filterCoefficient) {
			this.sensorDelay=sensorDelay;
			this.orientationFusion.reloadSettings(gyroscopeXOffset,gyroscopeYOffset,gyroscopeZOffset,orientationSource,filterCoefficient);
		}
		
		public float getWorldAccelerationZ() {
			return (float)this.worldAccelerationZ;
		}
		
		public float getWorldAccelerationX() {
			return (float)this.worldAccelerationX;
		}
}
