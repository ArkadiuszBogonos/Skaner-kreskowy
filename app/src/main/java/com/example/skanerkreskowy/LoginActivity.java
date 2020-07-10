package com.example.skanerkreskowy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivityTAG";
    public static final String EXTRA_FIRST_NAME = "com.example.skanerkreskowy,EXTRA_FIRST_NAME";
    public static final String EXTRA_LAST_NAME = "com.example.skanerkreskowy,EXTRA_LAST_NAME";
    public static final String SHARED_PREFS = "dbCredentials";
    private static final String MS_SQL_SERVER_DRIVER = "net.sourceforge.jtds.jdbc.Driver";

    public static Connection connection = null;
    private static String databaseIp, databasePort, databaseName, databaseLogin, databasePassword, databaseUrl;
    private boolean isConnectionSetCorrectly;

    private EditText mLoginEditText, mPasswordEditText;
    private TextView mConfigureConnectionTextView;
    private Button mLoginButton;
    public ProgressBar mCircuralProgressBar;

    private String mLogin, mPassword;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        connectVariablesToGui();

        isConnectionSetCorrectly = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void loginButton(View view) {

        mLogin = mLoginEditText.getText().toString();
        mPassword = mPasswordEditText.getText().toString();

        if (!isConnectionSetCorrectly) {
            setConnection();
        }

        if (isConnectionSetCorrectly) {
            validateCredentials();
        } else {
            Toast.makeText(this, "Bład połączenia z bazą. Sprawdź konfigurację.", Toast.LENGTH_SHORT).show();
        }
    }

    public void configureDbConnectionButton(View view) {
        Intent intent = new Intent(this, ConfigureDbConnectionActivity.class);
        startActivity(intent);
    }

    private void setConnection() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        String dbInstance = "MYSQLSERVER";

        databaseName = sharedPreferences.getString("dbName", null);
        databaseLogin = sharedPreferences.getString("dbLogin", null);
        databasePassword = sharedPreferences.getString("dbPassword", null);
        databaseIp = sharedPreferences.getString("dbIp", null);
        databasePort = sharedPreferences.getString("dbPort", null);

        databaseUrl = "jdbc:jtds:sqlserver://" + databaseIp + ":" + databasePort + "/" + databaseName;
        /*databaseUrl = "jdbc:jtds:sqlserver://" + databaseIp + ":" + databasePort + "/" + databaseName
        +";instance="+ dbInstance;*/


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            Class.forName(MS_SQL_SERVER_DRIVER);
            connection = DriverManager.getConnection(databaseUrl, databaseLogin, databasePassword);
            Log.d(TAG, "onCreate: connected to the database");
            isConnectionSetCorrectly = true;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "setConnection: class not found", e);
            isConnectionSetCorrectly = false;

        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "setConnection: sql exception", e);
            isConnectionSetCorrectly = false;

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "setConnection: unknown exception", e);
            isConnectionSetCorrectly = false;
        }
    }

    private void validateCredentials() {
        String firstName, lastName;

        mLoginEditText.setBackground(this.getDrawable(R.drawable.round_border));
        mPasswordEditText.setBackground(this.getDrawable(R.drawable.round_border));

        try {
            if (mLogin.contains(".") && mLogin.substring(mLogin.indexOf('.')).length() > 1) {
                firstName = mLogin.substring(0, mLogin.indexOf('.'));
                firstName.toLowerCase();
                lastName = mLogin.substring(mLogin.indexOf('.') + 1);
                lastName.toLowerCase();

                Log.d(TAG, "first name: " + firstName + " last name: " + lastName);

                if (isCorrect(firstName, lastName, mPassword)) {
                    openMainActivity(firstName, lastName);
                } else {
                    if (mPasswordEditText.getText().toString().length() > 0) {
                        mLoginEditText.setText("");
                        mLoginEditText.setHint(R.string.wrong_credentials);
                        mLoginEditText.setBackground(this.getDrawable(R.drawable.round_border_error_red));
                    } else {
                        mPasswordEditText.setHint(R.string.empty_password);
                        mPasswordEditText.setBackground(this.getDrawable(R.drawable.round_border_error_red));
                    }
                }
            } else {
                if (mLoginEditText.getText().toString().length() > 0) {
                    mLoginEditText.setText("");
                    mLoginEditText.setHint(R.string.wrong_login_format);
                    mLoginEditText.setBackground(this.getDrawable(R.drawable.round_border_error_red));
                } else {
                    mLoginEditText.setText("");
                    mLoginEditText.setHint(R.string.empty_login);
                    mLoginEditText.setBackground(this.getDrawable(R.drawable.round_border_error_red));
                }

            }
        } catch (Exception e) {
            Log.d(TAG, "loginButton: Login fail");
            e.printStackTrace();
        }
    }

    private void openMainActivity(String firstName, String lastName) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_FIRST_NAME, firstName);
        intent.putExtra(EXTRA_LAST_NAME, lastName);
        startActivity(intent);
        finish();
    }


    private boolean isCorrect(String firstName, String lastName, String password) {

        Statement statement = null;

        try {
            statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT * FROM uz__uzytkownik " +
                    "WHERE uz_Imie='" + firstName + "' AND uz_Nazwisko='" + lastName + "' AND uz_Haslo='"
                    + password + "';");

            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void connectVariablesToGui() {
        mLoginEditText = findViewById(R.id.activity_login_et__login);
        mPasswordEditText = findViewById(R.id.activity_login_et_password);
        mCircuralProgressBar = findViewById(R.id.circuralProgressBar);
        mLoginButton = findViewById(R.id.activity_login_btn_login);
        mConfigureConnectionTextView = findViewById(R.id.activity_login_tv_clicable_conf_db_conn);
    }

}

