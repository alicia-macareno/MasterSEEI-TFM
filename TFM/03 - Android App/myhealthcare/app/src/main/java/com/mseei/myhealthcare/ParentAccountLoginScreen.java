package com.mseei.myhealthcare;

import androidx.appcompat.app.AppCompatActivity;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class ParentAccountLoginScreen extends AppCompatActivity {

    private EditText loginEmail;
    private EditText loginPassword;
    private String resultMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Leer el idioma guardado de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String language = prefs.getString("language", Locale.getDefault().getLanguage());

        // Establecer el nuevo Locale para la aplicación
        Locale newLocale = new Locale(language);
        Locale.setDefault(newLocale);
        Configuration config = new Configuration();
        config.setLocale(newLocale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        setContentView(R.layout.activity_parent_account_login);
        loginEmail = findViewById(R.id.emailSignInTextInput);
        loginPassword = findViewById(R.id.passwordSignInTextInput);
    }

    public void onClickSendSignIn(View view) {
        Editable email = loginEmail.getText();
        Editable password = loginPassword.getText();
        closeKeyboard();
        new LoginUser().execute(email.toString(), password.toString());
    }

    public void navigateToCreateParentAccount(View view) {
        closeKeyboard();
        Intent intent = new Intent(ParentAccountLoginScreen.this, CreateParentAccountScreen.class);
        startActivity(intent);
    }

    private class LoginUser extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Mostrar mensaje indicando que se está realizando la tarea
            Toast.makeText(ParentAccountLoginScreen.this, R.string.logging_in_message, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String email = strings[0];
            String password = strings[1];

            try {
                URL url = new URL("https://myhealthappmanagement.azurewebsites.net/api/ParentAccountLogin");
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

                switch(myConnection.getResponseCode()) {
                    case HttpURLConnection.HTTP_OK:
                        resultMessage = getString(R.string.login_success_message);
                        Intent intent = new Intent(ParentAccountLoginScreen.this, ParentAccountMainScreen.class);
                        intent.putExtra("email", email);
                        intent.putExtra("password", password);
                        startActivity(intent);
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:
                        resultMessage = getString(R.string.incorrect_credentials_message);
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        resultMessage = getString(R.string.user_not_found_message);
                        break;
                    default:
                        resultMessage = getString(R.string.login_error_message);
                        break;
                }

            } catch (JSONException | IOException e) {
                resultMessage = getString(R.string.login_error_message);
                e.printStackTrace(); // Imprimir el stack trace para depuración
            }

            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            // Mostrar mensaje al usuario
            Toast.makeText(ParentAccountLoginScreen.this, resultMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();

        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void onLanguageChangeClick(View view) {
        // Obtener el idioma actual
        String currentLanguage = Locale.getDefault().getLanguage();

        // Determinar el nuevo idioma
        Locale newLocale;
        if (currentLanguage.equals("es")) {
            newLocale = Locale.ENGLISH; // Cambiar a inglés
        } else {
            newLocale = new Locale("es"); // Cambiar a español
        }

        // Establecer el nuevo Locale para la aplicación
        Locale.setDefault(newLocale);
        Configuration config = new Configuration();
        config.setLocale(newLocale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Guardar el idioma en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("language", newLocale.getLanguage());
        editor.apply();

        // Reiniciar la actividad para aplicar el cambio
        Intent refresh = new Intent(this, ParentAccountLoginScreen.class);
        finish();
        startActivity(refresh);
    }
}
