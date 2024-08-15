package com.mseei.myhealthcare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SuccessfulParentAccountCreation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_successful_parent_account_creation);
    }

    public void navigateToParentAccountLogin(View view)
    {
        Intent intent = new Intent(SuccessfulParentAccountCreation.this, ParentAccountLoginScreen.class);
        startActivity(intent);
    }
}