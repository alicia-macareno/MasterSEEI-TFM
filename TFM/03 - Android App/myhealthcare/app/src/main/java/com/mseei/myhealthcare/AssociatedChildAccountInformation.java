package com.mseei.myhealthcare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

public class AssociatedChildAccountInformation extends AppCompatActivity {

    private EditText loginEmailEditText;
    private EditText firstNameEditText;
    private EditText firstLastNameEditText;
    private EditText secondLastNameEditText;
    private EditText perimeterEditText;
    private Switch statusSwitch;
    private Switch blockedSwitch;
    private Switch realTimeMonitoringSwitch;

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

        Button updateButton = findViewById(R.id.updateButton);

        // Obtener datos del Intent
        Intent intent = getIntent();
        loginEmailEditText.setText(intent.getStringExtra("LoginEmail"));
        firstNameEditText.setText(intent.getStringExtra("FirstName"));
        firstLastNameEditText.setText(intent.getStringExtra("FirstLastName"));
        secondLastNameEditText.setText(intent.getStringExtra("SecondLastName"));
        statusSwitch.setChecked(intent.getBooleanExtra("Status", false));
        blockedSwitch.setChecked(intent.getBooleanExtra("Blocked", false));
        realTimeMonitoringSwitch.setChecked(intent.getBooleanExtra("RealTimeMonitoring", false));
        perimeterEditText.setText(String.valueOf(intent.getIntExtra("Perimeter", 0)));

        Log.d("AssociatedChildAccountInfo", "Received loginEmailEditText: " + loginEmailEditText);

        // Configurar el botón de actualización
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
            }
        });
    }

    private void updateData() {
        String url = "https://api.example.com/update"; // Reemplaza con tu URL de API
    }
}
