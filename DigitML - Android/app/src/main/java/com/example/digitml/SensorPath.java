package com.example.digitml;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.graphics.Path;
import android.util.Log;
import android.view.View;

public class SensorPath implements SensorEventListener {
    private Context context;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isDrawing;
    private float lastX, lastY;
    private Path path;

    private int sensorType = Sensor.TYPE_ACCELEROMETER;
    private View view;

    private boolean sensorSupported;

    private float lastTimestamp =0;
    private final float scaleFactor = 280f/0.7f; // Scala dp/m

    float i=0;
    private float previousVelocityX;
    private float previousVelocityY;
    private boolean iscalibrated;
    private boolean iscalibrating;
    private float limitX;
    private float limitY;


    public SensorPath(Context context, View view) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager==null){
            sensorSupported = false;
        }else {
            sensorSupported = true;
        }
        this.path = ((DrawingView)view).getPath();
        this.view = view;
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(sensorType);
            Log.e("disegno", accelerometer==null? "null" : "c'è");
        }
        previousVelocityX=0f;
        previousVelocityY=0f;
        isDrawing = false;

        iscalibrated = false;

        iscalibrating=false;
    }

    public void start(float startX, float startY) {
        if (accelerometer != null) {
            path.reset();
            moveTo(startX, startY); // Inizia dal punto specificato
        }
    }

    //moveTo implica la volonta di voler conituare a disegnare
    public void moveTo(float x, float y){
        path.moveTo(x, y);
        isDrawing = true;
        lastX = x;
        lastY = y;
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    public Path stop() {
        if (accelerometer != null) {
            isDrawing = false;
            sensorManager.unregisterListener(this);
        }
        return path;
    }

    public boolean isDrawing(){
        return isDrawing;
    }

    public boolean isIscalibrated() {
        return iscalibrated;
    }

    public void setIscalibrated(boolean iscalibrated) {
        this.iscalibrated = iscalibrated;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.e("disegno", event.timestamp+"<="+lastTimestamp+0.0002f);
        if (isDrawing && event.sensor.getType() == sensorType) {
            /*
            long currentTimestamp = event.timestamp;
            if (currentTimestamp - lastTimestamp != 0 ) {
                float deltaTime = (currentTimestamp - lastTimestamp) / 1000000000.0f;
                float accelerationX = event.values[0]; // Accelerazione lungo X
                float accelerationY = event.values[1]; //- normalY; // Accelerazione lungo Y
                Log.e("yacc", accelerationY+"");
                if(!iscalibrated.get()){
                    Log.w("calibration", "is: " + iscalibrated);
                    if(!iscalibrating){
                        iscalibrating=true;
                        MyThread t = new MyThread(iscalibrated, 5000, true, context);
                        t.start();
                    }
                    if(limitX <Math.abs(accelerationX))
                        limitX =Math.abs(accelerationX);

                    if(limitY <Math.abs(accelerationY))
                        limitY =Math.abs(accelerationY);

                }else {
                    if (Math.abs(accelerationX) > limitX || Math.abs(accelerationY)>limitY) {
                        // Calcola nuova velocità

                        float velocityX = previousVelocityX + accelerationX * deltaTime;
                        float velocityY = previousVelocityY + accelerationY * deltaTime;

                        // Calcola nuova posizione
                        float deltaX = velocityX * deltaTime +0.5f * accelerationX * deltaTime * deltaTime * scaleFactor;
                        float deltaY = velocityY * deltaTime +0.5f * accelerationY * deltaTime * deltaTime * scaleFactor;

                        lastX += (deltaX );
                        lastY -= (deltaY );

                        // Aggiorna la velocità precedente
                        previousVelocityX = velocityX;
                        previousVelocityY = velocityY;

                        //if(Math.abs(deltaX)>4f || Math.abs(deltaY)>4f)
                        {
                            Log.w("valore", "delta:" + deltaX);
                            Log.i("valore", "velocit: " + previousVelocityX);
                            Log.i("valore", "acc: " + accelerationX);
                        }

                        path.lineTo(lastX, lastY); // Traccia la linea


                        ((DrawingView) view).refresh(path);
                    }
                }

            }
            lastTimestamp = currentTimestamp;


             */

            float rotationX = event.values[0]; // Accelerazione lungo X
            float rotationY = event.values[1]; //- normalY; // Accelerazione lungo Y

            Log.e("valore", rotationX+"");

            if(Math.abs(rotationX)-1>0 || Math.abs(rotationY)-1>0) {
                lastX -= rotationX;
                lastY += rotationY;


                path.lineTo(lastX, lastY);
                ((DrawingView) view).refresh();
            }
        }


    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void clear(){
        path.reset();
        lastX=0;
        lastY=0;
        isDrawing = false;
    }
}
