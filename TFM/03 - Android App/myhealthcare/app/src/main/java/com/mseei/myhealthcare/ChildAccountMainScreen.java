package com.mseei.myhealthcare;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.telephony.SmsManager;

public class ChildAccountMainScreen extends AppCompatActivity implements SensorEventListener {

    private FusedLocationProviderClient fusedLocationClient; // Cliente para obtener la ubicación del dispositivo
    private LocationCallback locationCallback; // Callback para recibir actualizaciones de ubicación
    private String resultMessage; // Mensaje de resultado para mostrar al usuario
    private String firstName; // Primer nombre del usuario
    private String firstLastName; // Apellido del usuario
    private String phoneNumber; // Número de teléfono para enviar alertas
    private int perimeter; // Perímetro definido por el usuario
    private boolean realTimeMonitoring = false; // Bandera para monitoreo en tiempo real
    private double baseLocationLatitude; // Latitud de la ubicación base
    private double baseLocationLongitude; // Longitud de la ubicación base

    private SensorManager sensorManager; // Gestor de sensores
    private Sensor accelerometer; // Sensor de acelerómetro
    private Sensor gyroscope; // Sensor de giroscopio
    private boolean isFalling = false; // Bandera para detectar caídas

    private Handler smsHandler; // Handler para el envío de SMS
    private Runnable smsRunnable; // Runnable para enviar SMS periódicamente
    private long smsInterval = 10 * 60 * 1000; // Intervalo de 10 minutos en milisegundos
    private long lastSmsTime = 0; // Último momento en que se envió un SMS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_account_main_screen);

        // Solicitar permisos SMS
        requestSmsPermission();

        // Obtener datos del intent
        Bundle extras = getIntent().getExtras();
        String email = extras.getString("email");
        String password = extras.getString("password");

        // Si email y password no son nulos, obtener información de la cuenta
        if (email != null && password != null) {
            new GetChildAccountInfo().execute(email, password);
        }

        // Inicializar cliente de ubicación fusionada
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Solicitar permisos de ubicación
        requestLocationPermissions();

        // Configurar callback de ubicación
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || !realTimeMonitoring) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Calcular la distancia desde la ubicación base
                    float[] distance = new float[1];
                    Location.distanceBetween(baseLocationLatitude, baseLocationLongitude, location.getLatitude(), location.getLongitude(), distance);
                    float distanceInMeters = distance[0];

                    // Verificar si la distancia excede el perímetro
                    if (distanceInMeters > perimeter) {
                        // Mostrar mensaje de alerta
                        Toast.makeText(ChildAccountMainScreen.this,
                                R.string.alert_out_of_perimeter,
                                Toast.LENGTH_SHORT).show();
                        // Verificar el intervalo de tiempo para el envío de SMS
                        if (System.currentTimeMillis() - lastSmsTime >= smsInterval) {
                            lastSmsTime = System.currentTimeMillis();
                            String message = getString(R.string.alert_message, firstName, firstLastName);
                            sendFallAlertSms(phoneNumber, message);
                        }
                    }

                    // Mostrar nueva ubicación en un Toast
                    Toast.makeText(ChildAccountMainScreen.this,
                            getString(R.string.new_location, location.getLatitude(), location.getLongitude()),
                            Toast.LENGTH_SHORT).show();

                    // Enviar ubicación del usuario al servidor
                    new SendUserLocation().execute(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), email);
                }
            }
        };

        // Iniciar actualizaciones de ubicación
        startLocationUpdates();

        // Inicializar el sensor de acelerómetro y giroscopio
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

        // Registrar los listeners de los sensores
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Detectar caída basada en aceleración
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double magnitude = Math.sqrt(x * x + y * y + z * z);
            if (magnitude < 2) { // Un valor de umbral bajo indica caída libre
                isFalling = true;
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Detectar cambio brusco en orientación
            float omegaX = event.values[0];
            float omegaY = event.values[1];
            float omegaZ = event.values[2];

            double rotationMagnitude = Math.sqrt(omegaX * omegaX + omegaY * omegaY + omegaZ * omegaZ);
            if (isFalling && rotationMagnitude > 3) { // Si hubo caída y cambio brusco en orientación
                // Se detecta una posible caída
                Toast.makeText(this, R.string.possible_fall_detected, Toast.LENGTH_SHORT).show();
                isFalling = false;

                String message = getString(R.string.fall_alert_message, firstName, firstLastName);
                sendFallAlertSms(phoneNumber, message);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Desregistrar los listeners de los sensores
        sensorManager.unregisterListener(this);
    }

    // Clase interna para obtener la información de la cuenta
    private class GetChildAccountInfo extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Muestra un mensaje indicando que se está realizando la tarea
            Toast.makeText(ChildAccountMainScreen.this, R.string.getting_user_data, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String email = strings[0];
            String password = strings[1];

            try {
                URL url = new URL("https://myhealthappmanagement.azurewebsites.net/api/ChildAccountInfo");
                HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
                myConnection.setRequestMethod("POST");
                myConnection.setDoInput(true);
                myConnection.setDoOutput(true);
                myConnection.setRequestProperty("Content-Type", "application/json");

                // Preparar los parámetros para enviar en la solicitud
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("email", email);
                jsonParams.put("password", password);

                OutputStream outputStream = myConnection.getOutputStream();
                outputStream.write(jsonParams.toString().getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                // Leer la respuesta del servidor
                StringBuilder response = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                switch (myConnection.getResponseCode()) {
                    case HttpURLConnection.HTTP_OK:
                        return response.toString();
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        return "400 Bad Request";
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        return "401 Unauthorized";
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        return "404 Not Found";
                    default:
                        return "Error desconocido";
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return "Error en la solicitud";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals("Error en la solicitud")) {
                Toast.makeText(ChildAccountMainScreen.this, R.string.request_error, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject jsonResponse = new JSONObject(result);
                firstName = jsonResponse.getString("FirstName");
                firstLastName = jsonResponse.getString("FirstLastName");
                phoneNumber = jsonResponse.getString("ContactPhone");
                perimeter = jsonResponse.getInt("Perimeter");
                baseLocationLatitude = jsonResponse.getDouble("BaseLocationLatitude");
                baseLocationLongitude = jsonResponse.getDouble("BaseLocationLongitude");
                realTimeMonitoring = jsonResponse.getBoolean("RealTimeMonitoring");

                resultMessage = getString(R.string.user_data_received, firstName);
                Toast.makeText(ChildAccountMainScreen.this, resultMessage, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(ChildAccountMainScreen.this, R.string.json_parsing_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Clase interna para enviar la ubicación del usuario al servidor
    private class SendUserLocation extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(ChildAccountMainScreen.this, R.string.sending_location, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... params) {
            String latitude = params[0];
            String longitude = params[1];
            String email = params[2];

            try {
                URL url = new URL("https://myhealthappmanagement.azurewebsites.net/api/UserLocation");
                HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
                myConnection.setRequestMethod("POST");
                myConnection.setDoInput(true);
                myConnection.setDoOutput(true);
                myConnection.setRequestProperty("Content-Type", "application/json");

                JSONObject jsonParams = new JSONObject();
                jsonParams.put("latitude", latitude);
                jsonParams.put("longitude", longitude);
                jsonParams.put("email", email);

                OutputStream outputStream = myConnection.getOutputStream();
                outputStream.write(jsonParams.toString().getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                StringBuilder response = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(myConnection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                switch (myConnection.getResponseCode()) {
                    case HttpURLConnection.HTTP_CREATED:
                        return response.toString();
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        return "400 Bad Request";
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        return "401 Unauthorized";
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        return "404 Not Found";
                    default:
                        return "Error desconocido";
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return "Error en la solicitud";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.equals("Error en la solicitud")) {
                //Toast.makeText(ChildAccountMainScreen.this, R.string.request_error, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ChildAccountMainScreen.this, R.string.location_sent, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para enviar SMS
    private void sendFallAlertSms(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Toast.makeText(this, R.string.sms_sent, Toast.LENGTH_SHORT).show();
    }

    // Solicitar permisos de SMS
    private void requestSmsPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
        }
    }

    // Solicitar permisos de ubicación
    private void requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    // Iniciar actualizaciones de ubicación
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // Intervalo de actualización de ubicación en milisegundos
        locationRequest.setFastestInterval(5000); // Intervalo más rápido de actualización en milisegundos
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }
}
