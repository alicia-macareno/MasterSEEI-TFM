package com.mseei.myhealthcare;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CreateParentAccountScreen extends AppCompatActivity {

    private EditText emailInput, firstNameInput, lastNameInput, secondLastNameInput, phoneNumberInput, passwordInput, confirmPasswordInput;
    private Spinner phonePrefixSpinner;
    private TextView passwordValidation1, passwordValidation2, passwordValidation3;
    private ImageView passwordValidationIcon1, passwordValidationIcon2, passwordValidationIcon3;
    private Button createAccountButton;
    private String resultMessage;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_parent_account_screen);

        // Inicializar las vistas
        emailInput = findViewById(R.id.emailCreateAccountTextInput);
        firstNameInput = findViewById(R.id.firstNameTextInput);
        lastNameInput = findViewById(R.id.lastNameTextInput);
        secondLastNameInput = findViewById(R.id.secondLastNameTextInput);
        phoneNumberInput = findViewById(R.id.phoneNumberTextInput);
        passwordInput = findViewById(R.id.passwordCreateAccountTextInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordTextInput);
        phonePrefixSpinner = findViewById(R.id.phonePrefixSpinner);
        passwordValidation1 = findViewById(R.id.passwordValidation1);
        passwordValidation2 = findViewById(R.id.passwordValidation2);
        passwordValidation3 = findViewById(R.id.passwordValidation3);
        passwordValidationIcon1 = findViewById(R.id.passwordValidationIcon1);
        passwordValidationIcon2 = findViewById(R.id.passwordValidationIcon2);
        passwordValidationIcon3 = findViewById(R.id.passwordValidationIcon3);
        createAccountButton = findViewById(R.id.createAccountButton);

        // Desactivar el botón de enviar por defecto
        createAccountButton.setEnabled(false);
        createAccountButton.setBackgroundColor(Color.GRAY);

        // Añadir listeners para la validación
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No es necesario implementar esto
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No es necesario implementar esto
            }
        };

        emailInput.addTextChangedListener(textWatcher);
        firstNameInput.addTextChangedListener(textWatcher);
        lastNameInput.addTextChangedListener(textWatcher);
        phoneNumberInput.addTextChangedListener(textWatcher);
        passwordInput.addTextChangedListener(textWatcher);
        confirmPasswordInput.addTextChangedListener(textWatcher);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí recoges la información del formulario
                String email = emailInput.getText().toString();
                String firstName = firstNameInput.getText().toString();
                String firstLastName = lastNameInput.getText().toString();
                String secondLastName = secondLastNameInput.getText().toString();
                String phoneNumber = phoneNumberInput.getText().toString();
                String phonePrefix = phonePrefixSpinner.getSelectedItem().toString();
                String password = passwordInput.getText().toString();


                // Crear nueva cuenta
                new CreateParentAccount().execute(email, firstName, firstLastName, phoneNumber, phonePrefix, password, secondLastName);

            }
        });
    }

    private class CreateParentAccount extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Muestra un mensaje indicando que se está realizando la tarea
            Toast.makeText(CreateParentAccountScreen.this, "Creando nueva cuenta...", Toast.LENGTH_SHORT).show();
        }
        @Override
        protected String doInBackground(String... strings) {
            String email = strings[0];
            String firstName = strings[1];
            String firstLastName = strings[2];
            String phoneNumber = strings[3];
            String phonePrefix = strings[4];
            String password = strings[5];
            String secondLastName =  strings[6];

            try {
                URL url = new URL("https://myhealthappmanagement.azurewebsites.net/api/ParentAccounts");
                HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
                myConnection.setRequestMethod("POST");
                myConnection.setDoInput(true);
                myConnection.setDoOutput(true);
                myConnection.setRequestProperty("Content-Type", "application/json");

                JSONObject jsonParams = new JSONObject();
                jsonParams.put("email", email);
                jsonParams.put("password", password);
                jsonParams.put("firstName", firstName);
                jsonParams.put("firstLastName", firstLastName);
                jsonParams.put("contactPhonePrefix", phonePrefix);
                jsonParams.put("contactPhone",phoneNumber);
                jsonParams.put("secondLastName",secondLastName);

                // Escribir el objeto JSON en el cuerpo de la solicitud
                OutputStream outputStream = myConnection.getOutputStream();
                outputStream.write(jsonParams.toString().getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                switch(myConnection.getResponseCode()) {
                    case HttpURLConnection.HTTP_CREATED:
                        resultMessage = getString(R.string.account_created);
                        Intent intent = new Intent(CreateParentAccountScreen.this, SuccessfulParentAccountCreation.class);
                        intent.putExtra("email", email);
                        intent.putExtra("password", password);
                        startActivity(intent);
                        break;
                    case HttpURLConnection.HTTP_CONFLICT:
                        resultMessage = "Ya existe una cuenta con ese correo electrónico. Inicie sesión o elija otra dirección.";
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
            Toast.makeText(CreateParentAccountScreen.this, resultMessage, Toast.LENGTH_SHORT).show();

        }
    }

    private void validateForm() {
        boolean emailValid = !emailInput.getText().toString().trim().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailInput.getText().toString().trim()).matches();
        boolean firstNameValid = !firstNameInput.getText().toString().trim().isEmpty();
        boolean lastNameValid = !lastNameInput.getText().toString().trim().isEmpty();
        boolean phoneNumberValid = !phoneNumberInput.getText().toString().trim().isEmpty();
        boolean passwordValid = !passwordInput.getText().toString().trim().isEmpty();
        boolean confirmPasswordValid = !confirmPasswordInput.getText().toString().trim().isEmpty();
        boolean passwordsMatch = passwordInput.getText().toString().equals(confirmPasswordInput.getText().toString());

        validatePassword(passwordInput.getText().toString());

        if (emailValid && firstNameValid && lastNameValid && phoneNumberValid && passwordValid && confirmPasswordValid && passwordsMatch) {
            createAccountButton.setEnabled(true);
            createAccountButton.setBackgroundColor(Color.parseColor("#52796F"));
        } else {
            createAccountButton.setEnabled(false);
            createAccountButton.setBackgroundColor(Color.GRAY);
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

}
