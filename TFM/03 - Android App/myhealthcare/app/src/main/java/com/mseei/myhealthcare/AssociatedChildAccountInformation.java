package com.mseei.myhealthcare;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AssociatedChildAccountInformation extends AppCompatActivity {

    private static final String TAG = "AssociatedChildAccountInfo";

    // Declaración de vistas
    private EditText loginEmailEditText, firstNameEditText, firstLastNameEditText, secondLastNameEditText, perimeterEditText;
    private Switch statusSwitch, blockedSwitch, realTimeMonitoringSwitch;
    private Button updateButton;
    private String originalLoginEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_associated_child_account_information);

        // Inicialización de vistas
        loginEmailEditText = findViewById(R.id.loginEmailEditText);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        firstLastNameEditText = findViewById(R.id.firstLastNameEditText);
        secondLastNameEditText = findViewById(R.id.secondLastNameEditText);
        perimeterEditText = findViewById(R.id.perimeterEditText);
        statusSwitch = findViewById(R.id.statusSwitch);
        blockedSwitch = findViewById(R.id.blockedSwitch);
        realTimeMonitoringSwitch = findViewById(R.id.realTimeMonitoringSwitch);
        updateButton = findViewById(R.id.updateButton);

        // Obtener datos del Intent
        Intent intent = getIntent();
        originalLoginEmail = intent.getStringExtra("loginEmail");
        loginEmailEditText.setText(originalLoginEmail);
        firstNameEditText.setText(intent.getStringExtra("firstName"));
        firstLastNameEditText.setText(intent.getStringExtra("firstLastName"));
        secondLastNameEditText.setText(intent.getStringExtra("secondLastName"));
        statusSwitch.setChecked(intent.getBooleanExtra("status", false));
        blockedSwitch.setChecked(intent.getBooleanExtra("blocked", false));
        realTimeMonitoringSwitch.setChecked(intent.getBooleanExtra("realTimeMonitoring", false));
        perimeterEditText.setText(String.valueOf(intent.getIntExtra("perimeter", 0)));

        Log.d(TAG, getString(R.string.log_received_data, originalLoginEmail));

        // Configurar el botón de actualización
        updateButton.setOnClickListener(v -> {
            if (validateForm()) {
                updateData();
            } else {
                Toast.makeText(AssociatedChildAccountInformation.this, R.string.toast_complete_fields, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para validar el formulario
    private boolean validateForm() {
        return !loginEmailEditText.getText().toString().trim().isEmpty()
                && !firstNameEditText.getText().toString().trim().isEmpty()
                && !firstLastNameEditText.getText().toString().trim().isEmpty();
    }

    // Método para actualizar los datos
    private void updateData() {
        String email = loginEmailEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String firstLastName = firstLastNameEditText.getText().toString().trim();
        String secondLastName = secondLastNameEditText.getText().toString().trim();
        int perimeter = Integer.parseInt(perimeterEditText.getText().toString().trim());
        boolean status = statusSwitch.isChecked();
        boolean blocked = blockedSwitch.isChecked();
        boolean realTimeMonitoring = realTimeMonitoringSwitch.isChecked();

        Log.d(TAG, String.format(getString(R.string.log_updating_data), email, firstName, firstLastName, secondLastName, perimeter, status, blocked, realTimeMonitoring));

        new UpdateChildAccountTask().execute(
                email,
                firstName,
                firstLastName,
                secondLastName,
                String.valueOf(perimeter),
                String.valueOf(status),
                String.valueOf(blocked),
                String.valueOf(realTimeMonitoring)
        );
    }

    // Tarea asíncrona para actualizar la cuenta de usuario final
    private class UpdateChildAccountTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, getString(R.string.log_starting_task));
        }

        @Override
        protected String doInBackground(String... params) {
            String email = params[0];
            String firstName = params[1];
            String firstLastName = params[2];
            String secondLastName = params[3];
            int perimeter = Integer.parseInt(params[4]);
            boolean status = Boolean.parseBoolean(params[5]);
            boolean blocked = Boolean.parseBoolean(params[6]);
            boolean realTimeMonitoring = Boolean.parseBoolean(params[7]);

            HttpURLConnection myConnection = null;
            try {
                // URL de la API de actualización de datos de usuarios finales
                URL url = new URL("https://myhealthappmanagement.azurewebsites.net/api/UpdateChildAccountData?code=JCo0YNgH6J6QNNrBtSLLdwr4c9KM2Yr7IqEv6wJYaIFWAzFu5hj2IA%3D%3D");
                myConnection = (HttpURLConnection) url.openConnection();
                myConnection.setRequestMethod("POST");
                myConnection.setDoInput(true);
                myConnection.setDoOutput(true);
                myConnection.setRequestProperty("Content-Type", "application/json");

                JSONObject jsonParams = new JSONObject();
                jsonParams.put("loginEmail", email);
                jsonParams.put("newFirstName", firstName);
                jsonParams.put("newFirstLastName", firstLastName);
                jsonParams.put("newSecondLastName", secondLastName);
                jsonParams.put("newPerimeter", perimeter);
                jsonParams.put("newStatus", status);
                jsonParams.put("newBlocked", blocked);
                jsonParams.put("newRealTimeMonitoring", realTimeMonitoring);

                OutputStream outputStream = myConnection.getOutputStream();
                outputStream.write(jsonParams.toString().getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseCode = myConnection.getResponseCode();
                Log.d(TAG, String.format(getString(R.string.log_server_response), responseCode));

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, getString(R.string.log_update_success));
                    return getString(R.string.toast_update_success);
                } else {
                    Log.e(TAG, String.format(getString(R.string.log_update_error), responseCode));
                    return getString(R.string.toast_update_error);
                }

            } catch (Exception e) {
                Log.e(TAG, getString(R.string.log_connection_error), e);
                return getString(R.string.toast_connection_error);
            } finally {
                if (myConnection != null) {
                    myConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, getString(R.string.log_task_result) + result);
            Toast.makeText(AssociatedChildAccountInformation.this, result, Toast.LENGTH_SHORT).show();

            if (result.equals(getString(R.string.toast_update_success))) {
                finish(); // Regreso a la pantalla anterior
            }
        }
    }
}
