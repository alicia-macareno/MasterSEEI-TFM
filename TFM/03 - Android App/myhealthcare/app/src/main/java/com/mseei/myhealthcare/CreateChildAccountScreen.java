package com.mseei.myhealthcare;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CreateChildAccountScreen extends AppCompatActivity {

    private static final int REQUEST_CODE_MAP = 1;
    private static final String TAG = "CreateChildAccount";

    private EditText loginEmailEditText, firstNameEditText, firstLastNameEditText, secondLastNameEditText, perimeterEditText, passwordEditText, confirmPasswordEditText;
    private Button selectLocationButton, sendButton;
    private double selectedLatitude, selectedLongitude;
    private String parentEmail;
    private Switch realTimeMonitoringSwitch;

    private TextView passwordValidation1, passwordValidation2, passwordValidation3;
    private ImageView passwordValidationIcon1, passwordValidationIcon2, passwordValidationIcon3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_child_account_screen);

        // Inicializa los campos
        loginEmailEditText = findViewById(R.id.loginEmailEditText);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        firstLastNameEditText = findViewById(R.id.firstLastNameEditText);
        secondLastNameEditText = findViewById(R.id.secondLastNameEditText);
        perimeterEditText = findViewById(R.id.perimeterEditText);
        passwordEditText = findViewById(R.id.passwordCreateAccountTextInput);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordTextInput);
        selectLocationButton = findViewById(R.id.selectLocationButton);
        sendButton = findViewById(R.id.sendButton);
        realTimeMonitoringSwitch = findViewById(R.id.realTimeMonitoringSwitch);

        passwordValidation1 = findViewById(R.id.passwordValidation1);
        passwordValidation2 = findViewById(R.id.passwordValidation2);
        passwordValidation3 = findViewById(R.id.passwordValidation3);
        passwordValidationIcon1 = findViewById(R.id.passwordValidationIcon1);
        passwordValidationIcon2 = findViewById(R.id.passwordValidationIcon2);
        passwordValidationIcon3 = findViewById(R.id.passwordValidationIcon3);

        // Desactivar el botón de enviar por defecto
        sendButton.setEnabled(false);
        sendButton.setBackgroundColor(Color.GRAY);

        // Obtén el email del padre desde los extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            parentEmail = extras.getString("parentEmail");
            Log.d(TAG, "Parent email recibido: " + parentEmail);
        } else {
            Log.e(TAG, "No se recibió el email del padre");
        }

        // Configurar el botón para seleccionar la ubicación
        selectLocationButton.setOnClickListener(v -> {
            Log.d(TAG, "Abriendo el mapa para seleccionar la ubicación");
            Intent intent = new Intent(CreateChildAccountScreen.this, MapFragment.class);
            startActivityForResult(intent, REQUEST_CODE_MAP);
        });

        // Configurar el botón para enviar los datos
        sendButton.setOnClickListener(v -> {
            String email = loginEmailEditText.getText().toString().trim();
            String firstName = firstNameEditText.getText().toString().trim();
            String firstLastName = firstLastNameEditText.getText().toString().trim();
            String secondLastName = secondLastNameEditText.getText().toString().trim();
            String perimeterStr = perimeterEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();
            boolean realTimeMonitoring = realTimeMonitoringSwitch.isChecked();

            Log.d(TAG, "Datos ingresados: Email: " + email + ", Nombre: " + firstName + ", Apellido 1: " + firstLastName + ", Apellido 2: " + secondLastName + ", Latitud: " + selectedLatitude + ", Longitud: " + selectedLongitude + ", Perímetro: " + perimeterStr + ", Monitoreo en tiempo real: " + realTimeMonitoring);

            // Validar que todos los campos requeridos estén completos y las contraseñas sean válidas
            if (email.isEmpty() || firstName.isEmpty() || firstLastName.isEmpty() || confirmPassword.isEmpty() || password.isEmpty() || parentEmail.isEmpty() || !password.equals(confirmPassword)) {
                Toast.makeText(CreateChildAccountScreen.this, "Por favor, complete todos los campos requeridos y asegúrese de que las contraseñas coincidan", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Campos faltantes o contraseñas no coinciden. No se puede proceder con la creación de la cuenta.");
                return;
            }

            // Ejecutar la tarea en segundo plano para enviar los datos
            new CreateChildAccountTask().execute(email, firstName, firstLastName, secondLastName, parentEmail, String.valueOf(selectedLatitude), String.valueOf(selectedLongitude), perimeterStr, password, String.valueOf(realTimeMonitoring));
        });

        // Añadir TextWatchers para la validación de contraseñas
        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(passwordEditText.getText().toString().trim());
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };

        passwordEditText.addTextChangedListener(passwordWatcher);
        confirmPasswordEditText.addTextChangedListener(passwordWatcher);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MAP && resultCode == RESULT_OK) {
            selectedLatitude = data.getDoubleExtra("selected_latitude", 0);
            selectedLongitude = data.getDoubleExtra("selected_longitude", 0);
            Log.d(TAG, "Ubicación seleccionada: Lat: " + selectedLatitude + ", Lng: " + selectedLongitude);
        } else {
            Log.e(TAG, "No se seleccionó ninguna ubicación o hubo un error.");
        }
    }

    private class CreateChildAccountTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "Iniciando la tarea de creación de cuenta.");
        }

        @Override
        protected String doInBackground(String... params) {
            String email = params[0];
            String firstName = params[1];
            String firstLastName = params[2];
            String secondLastName = params[3];
            String parentAccountEmail = params[4];
            String password = params[8];
            double latitude = Double.parseDouble(params[5]);
            double longitude = Double.parseDouble(params[6]);
            int perimeter = params[7].isEmpty() ? 0 : Integer.parseInt(params[7]);
            boolean realTimeMonitoring = Boolean.parseBoolean(params[9]);

            HttpURLConnection myConnection = null;
            try {
                URL url = new URL("https://myhealthappmanagement.azurewebsites.net/api/ChildAccounts");
                myConnection = (HttpURLConnection) url.openConnection();
                myConnection.setRequestMethod("POST");
                myConnection.setDoInput(true);
                myConnection.setDoOutput(true);
                myConnection.setRequestProperty("Content-Type", "application/json");

                JSONObject jsonParams = new JSONObject();
                jsonParams.put("email", email);
                jsonParams.put("firstName", firstName);
                jsonParams.put("firstLastName", firstLastName);
                jsonParams.put("secondLastName", secondLastName);
                jsonParams.put("parentAccountEmail", parentAccountEmail);
                jsonParams.put("latitude", latitude);
                jsonParams.put("longitude", longitude);
                jsonParams.put("password", password);
                jsonParams.put("perimeter", perimeter);
                jsonParams.put("realTimeMonitoring", realTimeMonitoring);

                OutputStream outputStream = myConnection.getOutputStream();
                outputStream.write(jsonParams.toString().getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseCode = myConnection.getResponseCode();
                Log.d(TAG, "Respuesta del servidor: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    Log.d(TAG, "Cuenta creada con éxito.");
                    return "Cuenta creada con éxito.";
                } else {
                    Log.e(TAG, "Error al crear la cuenta: " + responseCode);
                    return "Error al crear la cuenta.";
                }

            } catch (Exception e) {
                Log.e(TAG, "Error en la conexión o en la creación de la cuenta", e);
                return "Error en la conexión o en la creación de la cuenta";
            } finally {
                if (myConnection != null) {
                    myConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "Resultado de la tarea: " + result);

            if (result.equals("Cuenta creada con éxito.")) {
                Log.d(TAG, "Navegando de vuelta a la pantalla principal.");
                Intent intent = new Intent(CreateChildAccountScreen.this, ParentAccountMainScreen.class);
                intent.putExtra("email", parentEmail);
                startActivity(intent);
            }
        }
    }


    private void validatePassword(String password) {
        boolean hasEightCharacters = password.length() >= 8; // Cambio de > a >= para mejor precisión
        boolean hasLetterAndNumber = password.matches("^(?=.*[A-Za-z])(?=.*\\d).+$");
        boolean hasSpecialCharacter = password.matches("^(?=.*[@#$%^&+=!]).+$");

        int darkGreen = Color.parseColor("#004d00"); // Verde más oscuro

        // Validación de ocho caracteres
        if (hasEightCharacters) {
            passwordValidation1.setTextColor(darkGreen);
            passwordValidationIcon1.setColorFilter(darkGreen);
            passwordValidationIcon1.setImageResource(R.drawable.ic_check); // Cambia el ícono a check
        } else {
            passwordValidation1.setTextColor(Color.GRAY);
            passwordValidationIcon1.setColorFilter(Color.GRAY);
            passwordValidationIcon1.setImageResource(R.drawable.sharp_cancel); // Cambia el ícono a cruz
        }

        // Validación de letras y números
        if (hasLetterAndNumber) {
            passwordValidation2.setTextColor(darkGreen);
            passwordValidationIcon2.setColorFilter(darkGreen);
            passwordValidationIcon2.setImageResource(R.drawable.ic_check); // Cambia el ícono a check
        } else {
            passwordValidation2.setTextColor(Color.GRAY);
            passwordValidationIcon2.setColorFilter(Color.GRAY);
            passwordValidationIcon2.setImageResource(R.drawable.sharp_cancel); // Cambia el ícono a cruz
        }

        // Validación de carácter especial
        if (hasSpecialCharacter) {
            passwordValidation3.setTextColor(darkGreen);
            passwordValidationIcon3.setColorFilter(darkGreen);
            passwordValidationIcon3.setImageResource(R.drawable.ic_check); // Cambia el ícono a check
        } else {
            passwordValidation3.setTextColor(Color.GRAY);
            passwordValidationIcon3.setColorFilter(Color.GRAY);
            passwordValidationIcon3.setImageResource(R.drawable.sharp_cancel); // Cambia el ícono a cruz
        }
    }

    private void validateForm() {
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        boolean isPasswordValid = !password.isEmpty() && password.equals(confirmPassword);
        boolean isAllFieldsFilled = !loginEmailEditText.getText().toString().trim().isEmpty()
                && !firstNameEditText.getText().toString().trim().isEmpty()
                && !firstLastNameEditText.getText().toString().trim().isEmpty()
                && !confirmPassword.isEmpty()
                && !password.isEmpty()
                && !parentEmail.isEmpty();

        sendButton.setEnabled(isPasswordValid && isAllFieldsFilled);
        sendButton.setBackgroundColor(isPasswordValid && isAllFieldsFilled ? Color.parseColor("#52796F") : Color.GRAY);
    }
}
