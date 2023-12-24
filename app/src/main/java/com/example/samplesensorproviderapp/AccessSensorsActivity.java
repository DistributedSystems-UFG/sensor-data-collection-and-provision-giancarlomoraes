package com.example.samplesensorproviderapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public class AccessSensorsActivity extends AppCompatActivity {

    private boolean cima = false;
    private boolean baixo = false;

    Activity thisActivity;

    private SensoresHelper sensoresHelper;

    public static final String brokerURI = "3.216.219.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_sensors);

        TextView textView = findViewById(R.id.textViewLuminosity);


        SensorEventListener gyroscopeListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // Atualizar os valores do giroscópio
                System.arraycopy(event.values, 0, sensoresHelper.getGyroscopeValues(), 0, event.values.length);

                // Processar dados do giroscópio
                // Atualizar a orientação conforme necessário
                float[] rotationMatrix = new float[9];
                float[] orientationValues = new float[3];

                SensorManager.getRotationMatrix(rotationMatrix, null, sensoresHelper.getAccelerometerValues(), sensoresHelper.getGyroscopeValues());
                SensorManager.getOrientation(rotationMatrix, orientationValues);

                double pitch = Math.toDegrees(orientationValues[1]);

                // Exemplo de critérios simples (ajuste conforme necessário)
                boolean viradoParaCima = pitch > 45 && pitch < 135;
                boolean viradoParaBaixo = pitch < -45 && pitch > -135;

                if (viradoParaCima && !cima) {
                    cima = true;
                    baixo = false;

                    Mqtt5BlockingClient client = Mqtt5Client.builder()
                            .identifier(UUID.randomUUID().toString())
                            .serverHost(brokerURI)
                            .buildBlocking();

                    client.connect();
                    client.publishWith()
                            .topic("orientation")
                            .qos(MqttQos.AT_LEAST_ONCE)
                            .payload("cima".getBytes())
                            .send();
                    client.disconnect();
                } else if (viradoParaBaixo && !baixo) {
                    cima = false;
                    baixo = true;
                    Mqtt5BlockingClient client = Mqtt5Client.builder()
                            .identifier(UUID.randomUUID().toString())
                            .serverHost(brokerURI)
                            .buildBlocking();

                    client.connect();
                    client.publishWith()
                            .topic("orientation")
                            .qos(MqttQos.AT_LEAST_ONCE)
                            .payload("baixo".getBytes())
                            .send();
                    client.disconnect();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Se necessário, lidar com mudanças na precisão do giroscópio
            }


        };

        Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(brokerURI)
                .buildBlocking();

        String topicName = "orientation";

        client.toAsync().subscribeWith()
                .topicFilter(topicName.toString())
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(msg -> {
                    thisActivity.runOnUiThread(() -> {
                        showToast(Arrays.toString(msg.getPayloadAsBytes()));
                    });
                })
                .send();

        SensorEventListener accelerometerListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // Atualizar os valores do acelerômetro
                System.arraycopy(event.values, 0, sensoresHelper.getAccelerometerValues(), 0, event.values.length);

                // Processar dados do acelerômetro
                // Atualizar a orientação conforme necessário
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Se necessário, lidar com mudanças na precisão do acelerômetro
            }
        };

        sensoresHelper = new SensoresHelper(this, gyroscopeListener, accelerometerListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensoresHelper.registerListeners();
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensoresHelper.unregisterListeners();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
