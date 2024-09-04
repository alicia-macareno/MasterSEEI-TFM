package com.mseei.myhealthcare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class ChildAccountLoginScreen extends AppCompatActivity {

    private EditText loginEmail;
    private EditText loginPassword;
    private String resultMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Leer el idioma guardado de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String language = prefs.getString("language", Locale.getDefault().getLanguage());

        // Actualizar la configuración de idioma
        Locale newLocale = new Locale(language);
        Locale.setDefault(newLocale);
        Configuration config = new Configuration();
        config.setLocale(newLocale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        setContentView(R.layout.child_account_login_screen);

        // Inicializar los campos de texto para el email y la contraseña
        loginEmail = findViewById(R.id.emailSignInTextInput);
        loginPassword = findViewById(R.id.passwordSignInTextInput);
    }

    // Método llamado cuando el usuario hace clic en el botón de inicio de sesión
    public void onClickSendSignIn(View view) {
        Editable email = loginEmail.getText();
        Editable password = loginPassword.getText();
        closeKeyboard(); // Cierra el teclado virtual
        new LoginUser().execute(email.toString(), password.toString()); // Ejecutar la tarea de inicio de sesión
    }

    // Navegar a la pantalla para crear una cuenta de administrador
    public void navigateToCreateParentAccount(View view) {
        closeKeyboard(); // Cierra el teclado virtual
        Intent intent = new Intent(ChildAccountLoginScreen.this, CreateParentAccountScreen.class);
        startActivity(intent);
    }

    // Clase para manejar el proceso de inicio de sesión en segundo plano
    private class LoginUser extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Mostrar un mensaje mientras se está procesando el inicio de sesión
            Toast.makeText(ChildAccountLoginScreen.this, R.string.login_in_progress, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String email = strings[0];
            String password = strings[1];

            try {
                URL url = new URL("https://myhealthappmanagement.azurewebsites.net/api/ChildAccountLogin");
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

                // Manejar la respuesta del servidor
                switch (myConnection.getResponseCode()) {
                    case HttpURLConnection.HTTP_OK:
                        resultMessage = getString(R.string.login_success);
                        // Iniciar la actividad principal del usuario
                        Intent intent = new Intent(ChildAccountLoginScreen.this, ChildAccountMainScreen.class);
                        intent.putExtra("email", email);
                        intent.putExtra("password", password);
                        startActivity(intent);
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        resultMessage = getString(R.string.incorrect_email_or_password);
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        resultMessage = getString(R.string.user_not_found);
                        break;
                    default:
                        resultMessage = getString(R.string.login_error);
                        break;
                }

            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            // Mostrar el mensaje de resultado después de la tarea de inicio de sesión
            Toast.makeText(ChildAccountLoginScreen.this, resultMessage, Toast.LENGTH_SHORT).show();
        }
    }

    // Método para cerrar el teclado virtual
    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Navegar a la pantalla de inicio de sesión de cuentas de administración
    public void navigateToParentAccountLogin(View view) {
        Intent intent = new Intent(ChildAccountLoginScreen.this, ParentAccountLoginScreen.class);
        startActivity(intent);
    }

    // Método para cambiar el idioma de la aplicación
    public void onLanguageChangeClick(View view) {
        // Obtener el idioma actual
        String currentLanguage = Locale.getDefault().getLanguage();

        // Determinar el nuevo idioma basado en el idioma actual
        Locale newLocale;
        if (currentLanguage.equals("es")) {
            newLocale = Locale.ENGLISH; // Cambiar a inglés
        } else {
            newLocale = new Locale("es"); // Cambiar a español
        }

        // Establecer el nuevo Locale
        Locale.setDefault(newLocale);
        Configuration config = new Configuration();
        config.setLocale(newLocale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Guardar el idioma en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("language", newLocale.getLanguage());
        editor.apply();

        // Reiniciar la actividad para aplicar el cambio de idioma
        Intent refresh = new Intent(this, ChildAccountLoginScreen.class);
        finish();
        startActivity(refresh);
    }
}
