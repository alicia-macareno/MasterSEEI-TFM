// ParentAccountMainScreen.java
package com.mseei.myhealthcare;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ParentAccountMainScreen extends AppCompatActivity {

    private RecyclerView childAccountsRecyclerView;
    private ChildAccountAdapter childAccountAdapter;
    private List<ChildAccount> childAccountList = new ArrayList<>();

    private String parentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_account_main_screen);

        parentEmail = "aaperezmac@hotmail.com";

        // Configure child accounts RecyclerView
        childAccountsRecyclerView = findViewById(R.id.recyclerView);
        childAccountAdapter = new ChildAccountAdapter(this, childAccountList);
        childAccountsRecyclerView.setAdapter(childAccountAdapter);
        childAccountsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch child accounts
        new FetchChildAccountsTask().execute(parentEmail);
    }

    private class FetchChildAccountsTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           // Toast.makeText(ParentAccountMainScreen.this, "Fetching child accounts...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... params) {
            String email = params[0];
            try {
                URL url = new URL("https://myhealthappmanagement.azurewebsites.net/api/GetAssociatedChildAccounts");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);

                // Write the email to the request body
                String jsonInputString = "{\"email\":\"" + email + "\"}";
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Read the response
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    childAccountList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        ChildAccount childAccount = new ChildAccount();
                        childAccount.setChildAccountID(jsonObject.getInt("ChildAccountID"));
                        childAccount.setLoginEmail(jsonObject.getString("LoginEmail"));
                        childAccount.setFirstName(jsonObject.getString("FirstName"));
                        childAccount.setFirstLastName(jsonObject.getString("FirstLastName"));
                        childAccount.setSecondLastName(jsonObject.getString("SecondLastName"));
                        childAccount.setStatus(jsonObject.getBoolean("Status"));
                        childAccount.setBlocked(jsonObject.getBoolean("Blocked"));
                        childAccount.setFailedLoginAttempts(jsonObject.getInt("FailedLoginAttempts"));
                        childAccount.setCreatedOn(jsonObject.getString("CreatedOn"));
                        childAccount.setRealTimeMonitoring(jsonObject.getBoolean("RealTimeMonitoring"));
                        childAccount.setPerimeter(jsonObject.getInt("Perimeter"));
                        childAccount.setPendingLocationConfig(jsonObject.getBoolean("PendingLocationConfig"));
                        childAccountList.add(childAccount);
                    }
                    childAccountAdapter.notifyDataSetChanged();
                    Log.d("ParentAccountMainScreen", "Child accounts fetched: " + childAccountList.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ParentAccountMainScreen.this, "Failed to parse response", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ParentAccountMainScreen.this, "Failed to fetch child accounts", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void navigateToAssociatedChildAccountInformationScreen(int childAccountID, String loginEmail, String firstName,
                                                                  String firstLastName, String secondLastName, boolean status,
                                                                  boolean blocked, int failedLoginAttempts, String createdOn,
                                                                  boolean realTimeMonitoring, int perimeter, boolean pendingLocationConfig)
    {
        Intent intent = new Intent(ParentAccountMainScreen.this, AssociatedChildAccountInformation.class);

        // Pasar los datos a la actividad de destino
        intent.putExtra("childAccountID", childAccountID);
        intent.putExtra("loginEmail", loginEmail);
        intent.putExtra("firstName", firstName);
        intent.putExtra("firstLastName", firstLastName);
        intent.putExtra("secondLastName", secondLastName);
        intent.putExtra("status", status);
        intent.putExtra("blocked", blocked);
        intent.putExtra("failedLoginAttempts", failedLoginAttempts);
        intent.putExtra("createdOn", createdOn);
        intent.putExtra("realTimeMonitoring", realTimeMonitoring);
        intent.putExtra("perimeter", perimeter);
        intent.putExtra("pendingLocationConfig", pendingLocationConfig);

        startActivity(intent);
    }

}
