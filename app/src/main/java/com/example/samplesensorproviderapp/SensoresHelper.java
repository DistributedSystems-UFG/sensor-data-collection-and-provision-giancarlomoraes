// SensoresHelper.java
package com.example.samplesensorproviderapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensoresHelper {
    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private Sensor accelerometerSensor;
    private SensorEventListener gyroscopeListener;
    private SensorEventListener accelerometerListener;

    private float[] gyroscopeValues = new float[3];
    private float[] accelerometerValues = new float[3];

    public SensoresHelper(Context context, SensorEventListener gyroscopeListener, SensorEventListener accelerometerListener) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        // Obter sensores
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        this.gyroscopeListener = gyroscopeListener;
        this.accelerometerListener = accelerometerListener;
    }

    public void registerListeners() {
        // Registrar ouvinte para o giroscópio
        sensorManager.registerListener(gyroscopeListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);

        // Registrar ouvinte para o acelerômetro
        sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterListeners() {
        // Desregistrar ouvintes
        sensorManager.unregisterListener(gyroscopeListener);
        sensorManager.unregisterListener(accelerometerListener);
    }

    public float[] getGyroscopeValues() {
        return gyroscopeValues;
    }

    public float[] getAccelerometerValues() {
        return accelerometerValues;
    }
}
