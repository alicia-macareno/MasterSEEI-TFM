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
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;

public class ChildAccountMainScreen extends AppCompatActivity implements SensorEventListener {
    private FusedLocationProviderClient fusedLocationClient; // Cliente para obtener la ubicación del dispositivo
    private LocationCallback locationCallback; // Callback para recibir actualizaciones de ubicación
    private String resultMessage; // Mensaje de resultado para mostrar al usuario
    String firstName;
    String firstLastName;
    String phoneNumber;
    int perimeter; // Perímetro definido por el usuario
    boolean realTimeMonitoring = false; // Bandera para monitoreo en tiempo real
    double baseLocationLatitude; // Latitud de la ubicación base
    double baseLocationLongitude; // Longitud de la ubicación base

    private SensorManager sensorManager; // Gestor de sensores
    private Sensor accelerometer; // Sensor de acelerómetro
    private Sensor gyroscope; // Sensor de giroscopio
    private boolean isFalling = false; // Bandera para detectar caídas

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
        if(email != null && password != null){
            new ChildAccountMainScreen.GetChildAccountInfo().execute(email, password);
        }

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ImageView imageView = findViewById(R.id.simpsongif);

        // Cargar imagen animada usando Glide
        Glide.with(this)
                .load(R.drawable.dancing_simpson)
                .into(imageView);

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
                                "¡Alerta! Te has alejado más allá del perímetro establecido.",
                                Toast.LENGTH_SHORT).show();
                        // Enviar SMS de alerta
                        String message = "¡Alerta! El usuario se ha alejado del perímetro establecido.";
                        sendFallAlertSms(phoneNumber, message);

                    }

                    // Mostrar nueva ubicación en un Toast
                    Toast.makeText(ChildAccountMainScreen.this,
                            "Nueva ubicación: " + location.getLatitude() + ", " + location.getLongitude(),
                            Toast.LENGTH_SHORT).show();

                    // Enviar ubicación del usuario al servidor
                    new ChildAccountMainScreen.SendUserLocation().execute(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), email);
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
            if (magnitude < 2) {  // Un valor de umbral bajo indica caída libre
                isFalling = true;
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Detectar cambio brusco en orientación
            float omegaX = event.values[0];
            float omegaY = event.values[1];
            float omegaZ = event.values[2];

            double rotationMagnitude = Math.sqrt(omegaX * omegaX + omegaY * omegaY + omegaZ * omegaZ);
            if (isFalling && rotationMagnitude > 3) {  // Si hubo caída y cambio brusco en orientación
                // Se detecta una posible caída
                Toast.makeText(this, "¡Posible caída detectada!", Toast.LENGTH_SHORT).show();
                isFalling = false;
                // Enviar SMS de alerta
                String message = "¡Alerta! Se ha detectado una posible caída.";
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
            Toast.makeText(ChildAccountMainScreen.this, "Obteniendo datos del usuario...", Toast.LENGTH_SHORT).show();
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

                JSONObject jsonParams = new JSONObject();
                jsonParams.put("email", email);
                jsonParams.put("password", password);

                // Escribir el objeto JSON en el cuerpo de la solicitud
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

                switch(myConnection.getResponseCode()) {
                    case HttpURLConnection.HTTP_OK:
                        resultMessage = "Data successfully retrieved!";
                        // Crear un objeto JSON a partir de la respuesta del servidor
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        // Aquí puedes acceder a los datos del objeto jsonResponse
                        firstName = jsonResponse.getString("FirstName");
                        firstLastName = jsonResponse.getString("FirstLastName");
                        perimeter = jsonResponse.getInt("Perimeter");
                        realTimeMonitoring = jsonResponse.getBoolean("RealTimeMonitoring");
                        baseLocationLatitude = jsonResponse.getDouble("BaseLocationLatitude");
                        baseLocationLongitude = jsonResponse.getDouble("BaseLocationLongitude");
                        phoneNumber = jsonResponse.getString("ContactPhone");
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        resultMessage = "Incorrect email or password";
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        resultMessage = "There is no user registered with that email";
                        break;
                    default:
                        resultMessage = "There was an error trying to log you in, please try again later.";
                        break;
                }

            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }

            return null;

        }
        @Override
        protected void onPostExecute(String message) {
            // Update UI with the message
            // Aquí puedes mostrar un Toast, cambiar un TextView o cualquier otra acción para notificar al usuario.
            Toast.makeText(ChildAccountMainScreen.this, resultMessage, Toast.LENGTH_SHORT).show();

        }
    }

    // Clase interna para enviar la ubicación del usuario al servidor
    private class SendUserLocation extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Muestra un mensaje indicando que se está realizando la tarea
            Toast.makeText(ChildAccountMainScreen.this, "Enviando datos de localización...", Toast.LENGTH_SHORT).show();
        }
        @Override
        protected String doInBackground(String... strings) {
            Double latitude = Double.parseDouble(strings[0]);
            Double longitude = Double.parseDouble(strings[1]);
            String email = strings[2];

            try {
                URL url = new URL("https://myhealthappmanagement.azurewebsites.net/api/UserLocation");
                HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
                myConnection.setRequestMethod("POST");
                myConnection.setDoInput(true);
                myConnection.setDoOutput(true);
                myConnection.setRequestProperty("Content-Type", "application/json");

                JSONObject jsonParams = new JSONObject();
                jsonParams.put("ChildAccountEmail", email);
                jsonParams.put("Latitude", latitude);
                jsonParams.put("Longitude", longitude);

                // Escribir el objeto JSON en el cuerpo de la solicitud
                OutputStream outputStream = myConnection.getOutputStream();
                outputStream.write(jsonParams.toString().getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                switch(myConnection.getResponseCode()) {
                    case HttpURLConnection.HTTP_CREATED:
                        resultMessage = "Location successfully registered!";
                        break;
                    case HttpURLConnection.HTTP_FORBIDDEN:
                        resultMessage = "Forbidden";
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        resultMessage = "There is no user registered with that email";
                        break;
                    default:
                        resultMessage = "There was an error trying to log you in, please try again later.";
                        break;
                }

            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }

            return null;

        }
        @Override
        protected void onPostExecute(String message) {
            // Update UI with the message
            // Aquí puedes mostrar un Toast, cambiar un TextView o cualquier otra acción para notificar al usuario.
            Toast.makeText(ChildAccountMainScreen.this, resultMessage, Toast.LENGTH_SHORT).show();

        }
    }

    // Método para iniciar actualizaciones de ubicación
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // Intervalo de actualización de ubicación en milisegundos
        locationRequest.setFastestInterval(5000); // Intervalo más rápido para actualizaciones de ubicación
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                null /* Looper */);
    }

    // Método para solicitar permisos de ubicación
    private void requestLocationPermissions(){
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                        result -> {
                            Boolean fineLocationGranted = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                fineLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_FINE_LOCATION, false);
                            }
                            Boolean coarseLocationGranted = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                coarseLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            }
                            Boolean backgroundLocationGranted = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                backgroundLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_BACKGROUND_LOCATION, false);
                            }

                            if (fineLocationGranted && coarseLocationGranted && backgroundLocationGranted) {
                                // Permisos concedidos
                            } else {
                                // Permisos no concedidos
                            }
                        });

        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        });
    }

    // Solicitar permisos de SMS en tiempo de ejecución
    private void requestSmsPermission() {
        ActivityResultLauncher<String[]> smsPermissionRequest =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                        result -> {
                            Boolean sendSmsGranted = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                sendSmsGranted = result.getOrDefault(
                                        Manifest.permission.SEND_SMS, false);
                            }

                            if (!sendSmsGranted) {
                                // Permiso no concedido, maneja la situación
                                Toast.makeText(this, "Permiso para enviar SMS no concedido", Toast.LENGTH_SHORT).show();
                            }
                        });

        smsPermissionRequest.launch(new String[]{Manifest.permission.SEND_SMS});
    }

    // Método para el envío de SMS
    private void sendFallAlertSms(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }
}
