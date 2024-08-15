package com.mseei.myhealthcare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
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


public class ChildAccountLoginScreen extends AppCompatActivity {

    private EditText loginEmail;
    private EditText loginPassword;
    private String resultMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_account_login_screen);
        loginEmail = findViewById(R.id.emailSignInTextInput);
        loginPassword = findViewById(R.id.passwordSignInTextInput);
    }

    public void onClickSendSignIn(View view) {
        Editable email = loginEmail.getText();
        Editable password = loginPassword.getText();
        closeKeyboard();
        new LoginUser().execute(email.toString(), password.toString());

    }

    public void navigateToCreateParentAccount(View view)
    {
        closeKeyboard();
        Intent intent = new Intent(ChildAccountLoginScreen.this, CreateParentAccountScreen.class);
        startActivity(intent);
    }

    private class LoginUser extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Muestra un mensaje indicando que se está realizando la tarea
            Toast.makeText(ChildAccountLoginScreen.this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();
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
                        resultMessage = "Successful login!";
                        Intent intent = new Intent(ChildAccountLoginScreen.this, ChildAccountMainScreen.class);
                        intent.putExtra("email", email);
                        intent.putExtra("password", password);
                        startActivity(intent);
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
            Toast.makeText(ChildAccountLoginScreen.this, resultMessage, Toast.LENGTH_SHORT).show();

        }
    }

    private void closeKeyboard()
    {
        // this will give us the view
        // which is currently focus
        // in this layout
        View view = this.getCurrentFocus();

        // if nothing is currently
        // focus then this will protect
        // the app from crash
        if (view != null) {

            // now assign the system
            // service to InputMethodManager
            InputMethodManager manager
                    = (InputMethodManager)
                    getSystemService(
                            Context.INPUT_METHOD_SERVICE);
            manager
                    .hideSoftInputFromWindow(
                            view.getWindowToken(), 0);
        }
    }

    public void navigateToParentAccountLogin(View view)
    {
        Intent intent = new Intent(ChildAccountLoginScreen.this, ParentAccountLoginScreen.class);
        startActivity(intent);
    }


}