package com.example.skanerkreskowy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ConfigureDbConnectionActivity extends AppCompatActivity {

    private EditText mDbNameEditText, mLoginEditText, mPasswordEditText, mIpEditText, mPortEditText;

    public static final String SHARED_PREF_DB_CREDENTIALS = "dbCredentials";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_db_connection);
        connectVariablesToGui();
    }

    public void cancelButton(View view) {
        finish();
    }

    public void saveButton(View view) {
        if (validateData()) {
            saveData();
            finish();
        }
    }

    //ToDo: make edittext data validation

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_DB_CREDENTIALS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("dbName", mDbNameEditText.getText().toString());
        editor.putString("dbLogin", mLoginEditText.getText().toString());
        editor.putString("dbPassword", mPasswordEditText.getText().toString());
        editor.putString("dbIp", mIpEditText.getText().toString());
        editor.putString("dbPort", mPortEditText.getText().toString());

        editor.apply();
        Toast.makeText(this, "Zapisano konfigurację połączenia", Toast.LENGTH_SHORT).show();
    }

    private boolean validateData() {
        boolean isValid = true;

        // DB name
        if (mDbNameEditText.getText().toString().isEmpty()){
            mDbNameEditText.setBackground(this.getDrawable(R.drawable.round_border_error_red));
            isValid = false;
        }
        else
            mDbNameEditText.setBackground(this.getDrawable(R.drawable.round_border));

        // Login
        if (mLoginEditText.getText().toString().isEmpty()){
            mLoginEditText.setBackground(this.getDrawable(R.drawable.round_border_error_red));
            isValid = false;
        }
        else
            mLoginEditText.setBackground(this.getDrawable(R.drawable.round_border));

        // Password
        if (mPasswordEditText.getText().toString().isEmpty()){
            mPasswordEditText.setBackground(this.getDrawable(R.drawable.round_border_error_red));
            isValid = false;
        }
        else
            mPasswordEditText.setBackground(this.getDrawable(R.drawable.round_border));

        // IP
        if (mIpEditText.getText().toString().isEmpty()){
            mIpEditText.setBackground(this.getDrawable(R.drawable.round_border_error_red));
            isValid = false;
        }
        else
            mIpEditText.setBackground(this.getDrawable(R.drawable.round_border));

        // Port
        if (mPortEditText.getText().toString().isEmpty()){
            mPortEditText.setBackground(this.getDrawable(R.drawable.round_border_error_red));
            isValid = false;
        }
        else
            mPortEditText.setBackground(this.getDrawable(R.drawable.round_border));

        return isValid;
    }

    private void connectVariablesToGui() {
        mDbNameEditText = findViewById(R.id.activity_set_db_connection_et_dbname);
        mLoginEditText = findViewById(R.id.activity_set_db_connection_et_login);
        mPasswordEditText = findViewById(R.id.activity_set_db_connection_et_password);
        mIpEditText = findViewById(R.id.activity_set_db_connection_et_ip);
        mPortEditText = findViewById(R.id.activity_set_db_connection_et_port);
    }
}
