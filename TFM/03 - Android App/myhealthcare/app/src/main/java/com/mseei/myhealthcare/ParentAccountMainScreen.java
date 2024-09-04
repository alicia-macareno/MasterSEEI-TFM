// ParentAccountMainScreen.java
package com.mseei.myhealthcare;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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

        Bundle extras = getIntent().getExtras();
        parentEmail = extras.getString("email");

        // Inicializa el RecyclerView y el Adapter
        childAccountsRecyclerView = findViewById(R.id.recyclerView);
        childAccountAdapter = new ChildAccountAdapter(this, childAccountList);
        childAccountsRecyclerView.setAdapter(childAccountAdapter);
        childAccountsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch child accounts
        new FetchChildAccountsTask().execute(parentEmail);
    }

    private class FetchChildAccountsTask extends AsyncTask<String, Void, String> {
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
                }
            } else {
                Log.e("ParentAccountMainScreen", "Failed to fetch child accounts");
            }
        }


    }

    public void navigateToCreateChildAccountScreen(View view)
    {
        Intent intent = new Intent(ParentAccountMainScreen.this, CreateChildAccountScreen.class);
        intent.putExtra("parentEmail", parentEmail);
        startActivity(intent);
    }
}
