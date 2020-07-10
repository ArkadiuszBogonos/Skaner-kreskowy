package com.example.skanerkreskowy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener,
        DatePickerDialogFragment.OnDataPass, HowManyLabelsDialogFragment.HowManyLabelsDialogListener {

    private static final String TAG = "MainActivityTAG";
    private static final String SHARED_PREF_DB_CREDENTIALS = "dbCredentials";
    private static final String MS_SQL_SERVER_DRIVER = "net.sourceforge.jtds.jdbc.Driver";

    public static Connection connection = null;

    private static String databaseIp, databasePort, databaseName, databaseLogin, databasePassword, databaseUrl;

    private TextView mBarcodeTextView, mItemNameTextView, mItemPriceTextView, mItemConverterTextView,
            mUserNameTextView;
    private Button mDatePickerBtn, mAislePickerBtn, mGoNextBtn;
    private CheckBox mPriceCheckbox, mLabelsCheckbox;

    private String userFirstName, userLastName;

    private String barcode;
    private int numberOfLabels;
    boolean isItemInDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectVariablesToGui();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PackageManager.PERMISSION_GRANTED);
        setConnection();

        setUserName();
    }

    /* Button handling */
    public void aislePickerDialogButton(View view) {
        AislePickerDialogFragment newFragment = new AislePickerDialogFragment();
        newFragment.setValueChangeListener(this);
        newFragment.show(getSupportFragmentManager(), "aislePicker");
    }

    public void datePickerDialogButton(View view) {
        DialogFragment newFragment = new DatePickerDialogFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void goNextButton(View view) {

        if (mLabelsCheckbox.isChecked()) {
            openLabelPickerDialog();
        } else {
            if (!mDatePickerBtn.getText().toString().equals(getApplicationContext().getString(R.string.set_expiration_date))) {
                insertIntoDatabase();
            }

            IntentIntegrator intentIntegrator = new IntentIntegrator(this);
            intentIntegrator.setOrientationLocked(false);
            intentIntegrator.initiateScan();

        }
    }
    /*#################*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (intentResult != null && connection != null) {
            if (intentResult.getContents() == null) {
                Log.d(TAG, "onActivityResult: BARCODE OR CONNECTION ERROR");
            } else {        //If scanner found barcode then do the following

                barcode = intentResult.getContents();
                getItemDetailsFromDatabase(barcode);

                // Set GUI
                mBarcodeTextView.setText("Kod kreskowy: " + barcode);
                if (isItemInDatabase) {
                    mDatePickerBtn.setEnabled(true);
                }
                mDatePickerBtn.setText(R.string.set_expiration_date);
                disableCheckboxes();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        mAislePickerBtn.setText(Integer.toString(numberPicker.getValue()));
        mGoNextBtn.setEnabled(true);
    }

    @Override
    public void onCalendarDataPass(String data) {
        mDatePickerBtn.setText(data);   //Show chosen date on button

        mPriceCheckbox.setEnabled(true);    //Enable checkboxes
        mLabelsCheckbox.setEnabled(true);
    }

    @Override
    public void applyNumberOfLabels(int numberOfLabels) {
        //Get the number of lables to print here
        this.numberOfLabels = numberOfLabels;

        insertIntoDatabase();
        clearGui();
    }

    public void openLabelPickerDialog() {
        HowManyLabelsDialogFragment howManyLabelsDialogFragment = new HowManyLabelsDialogFragment();
        howManyLabelsDialogFragment.show(getSupportFragmentManager(), "Pick how many labels dialog");
    }

    public void clearGui() {
        mDatePickerBtn.setText(R.string.set_expiration_date);
        mDatePickerBtn.setEnabled(false);

        mItemNameTextView.setText(R.string.scan_next);
        mItemPriceTextView.setText("");
        mBarcodeTextView.setText("");
        disableCheckboxes();
    }

    private void disableCheckboxes() {
        mPriceCheckbox.setEnabled(false);
        mLabelsCheckbox.setEnabled(false);
        mPriceCheckbox.setChecked(false);
        mLabelsCheckbox.setChecked(false);

    }

    private void getItemDetailsFromDatabase(String barcode) {
        isItemInDatabase = false;
        Statement statement = null;

        try {
            statement = connection.createStatement();

            // Get item name
            ResultSet resultSet = statement.executeQuery("SELECT tw_nazwa FROM tw__towar "
                    + "WHERE tw_ean=" + barcode);
            if (resultSet.isBeforeFirst()) {
                while (resultSet.next()) {
                    mItemNameTextView.setText(resultSet.getString("tw_nazwa"));
                    isItemInDatabase = true;
                }
            } else {
                clearGui();
                mItemNameTextView.setText(R.string.no_item_in_database);
            }


            // Get item price
            resultSet = statement.executeQuery("SELECT tw_cena_sprz FROM tw__towar " +
                    "WHERE tw_ean=" + barcode);


            try {
                if (resultSet.isBeforeFirst()) {
                    while (resultSet.next()) {

                        if (resultSet.getString(1) != null) {
                            if (resultSet.getString(1).length() > 2) {
                                mItemPriceTextView.setText("Cena: " + resultSet.getString("tw_cena_sprz")
                                        .substring(0, resultSet.getString(1).length() - 2) + " zł");
                            } else {
                                mItemPriceTextView.setText("CUTTING PRICE STRING ERROR");
                            }
                        } else {
                            mItemPriceTextView.setText(R.string.item_has_no_price);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        } catch (SQLException e) {
            Log.e(TAG, "AFTER SCAN:" + e.getMessage());
        }
    }

    private void connectVariablesToGui() {
        mBarcodeTextView = findViewById(R.id.activity_main_tv_barcode);
        mItemNameTextView = findViewById(R.id.activity_main_tv_name);
        mItemPriceTextView = findViewById(R.id.activity_main_tv_price);
        mItemConverterTextView = findViewById(R.id.activity_main_tv_converter);
        mDatePickerBtn = findViewById(R.id.activity_main_btn_set_exp_date);
        mAislePickerBtn = findViewById(R.id.activity_main_btn_set_aisle);
        mGoNextBtn = findViewById(R.id.activity_main_btn_scan);
        mPriceCheckbox = findViewById(R.id.activity_main_checkBox_price);
        mLabelsCheckbox = findViewById(R.id.activity_main_checkBox_labels);
        mUserNameTextView = findViewById(R.id.activity_main_tv_username);

    }

    private void setConnection() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_DB_CREDENTIALS, MODE_PRIVATE);

        databaseName = sharedPreferences.getString("dbName", null);
        databaseLogin = sharedPreferences.getString("dbPassword", null);
        databasePassword = sharedPreferences.getString("dbPassword", null);
        databaseIp = sharedPreferences.getString("dbIp", null);
        databasePort = sharedPreferences.getString("dbPort", null);
        databaseUrl = "jdbc:jtds:sqlserver://" + databaseIp + ":" + databasePort + "/" + databaseName;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            Class.forName(MS_SQL_SERVER_DRIVER);
            connection = DriverManager.getConnection(databaseUrl, databaseLogin, databasePassword);
            Log.d(TAG, "onCreate: connected to the database");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "onCreate: class not found", e);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "onCreate: sql exception", e);
        }
    }

    private void setUserName() {
        Intent intent = getIntent();
        userFirstName = intent.getStringExtra(LoginActivity.EXTRA_FIRST_NAME);
        userLastName = intent.getStringExtra(LoginActivity.EXTRA_LAST_NAME);

        userFirstName = userFirstName.substring(0, 1).toUpperCase() + userFirstName.substring(1).toLowerCase();
        userLastName = userLastName.substring(0, 1).toUpperCase() + userLastName.substring(1).toLowerCase();

        if (isAdmin(userFirstName, userLastName)) {
            mUserNameTextView.setText(userFirstName + " " + userLastName + " " + "(admin)");
        } else {
            mUserNameTextView.setText(userFirstName + " " + userLastName);
        }
    }

    private void insertIntoDatabase() {
        String expDate = mDatePickerBtn.getText().toString();
        Date date = null;

        String aisle = mAislePickerBtn.getText().toString();
        String tt_twid = null, tt_uzid = null;

        int isPriceIncorrect;
        if (mPriceCheckbox.isChecked()) {
            isPriceIncorrect = 1;
        } else {
            isPriceIncorrect = 0;
        }

        // get correct date format
        SimpleDateFormat buforFormat = new SimpleDateFormat("dd.MM.yyyy");
        try {
            date = buforFormat.parse(expDate);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            format.format(date);

            expDate = format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // get timestamp
        Date currentDate = new Date();
        long time = currentDate.getTime();
        Timestamp timestamp = new Timestamp(time);


        Statement statement = null;

        // get item id from barcode
        try {
            statement = connection.createStatement();

            ResultSet resultSet1 = statement.executeQuery("SELECT tw_id FROM tw__towar " +
                    "WHERE tw_ean = '" + barcode + "';");

            if (resultSet1.isBeforeFirst()) {
                while (resultSet1.next()) {
                    tt_twid = resultSet1.getString(1);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "sendToDatabase() - get item id from barcode ERROR: " + e.getMessage());
        }


        // get user id
        try {
            ResultSet resultSet2 = statement.executeQuery("SELECT uz_Id FROM uz__uzytkownik " +
                    "WHERE uz_Imie = '" + userFirstName + "'AND uz_Nazwisko = '" + userLastName + "';");

            if (resultSet2.isBeforeFirst()) {
                while (resultSet2.next()) {
                    tt_uzid = resultSet2.getString(1);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "sendToDatabase() - get user ID ERROR: " + e.getMessage());
        }


        // check if there is record already
        try {
            ResultSet resultSet3 = statement.executeQuery("SELECT * FROM tw_termin WHERE tt_twid = " + tt_twid + ";");

            if (resultSet3.isBeforeFirst()) { // if so, update existing record
                try {
                    statement.executeUpdate("UPDATE tw_termin SET tt_uzid =" + tt_uzid +
                            ", tt_data ='" + expDate + "', tt_miejsce='" + aisle + "', tt_ile_etykiet="
                            + numberOfLabels + ", tt_bledna_cena=" + isPriceIncorrect + ", tt_timestamp='" +
                            timestamp + "' WHERE tt_twid =" + tt_twid + ";");

                } catch (SQLException e) {
                    Log.e(TAG, "sendToDatabase() - UPDATE query ERROR: " + e.getMessage());
                }
            } else { // else send new record to database
                try {
                    statement.executeUpdate("INSERT INTO tw_termin " +
                            "(tt_twid, tt_uzid, tt_data, tt_miejsce, tt_ile_etykiet, tt_bledna_cena, tt_timestamp) " +
                            "VALUES (" + tt_twid + ", " + tt_uzid + ", '" + expDate + "','" + aisle + "', " + numberOfLabels +
                            ", " + isPriceIncorrect + ", '" + timestamp + "');");

                } catch (SQLException e) {
                    Log.e(TAG, "sendToDatabase() - INSERT INTO query ERROR: " + e.getMessage());
                }

            }
        } catch (SQLException e) {
            Log.e(TAG, "sendToDatabase() - check if there is record with given item ID ERROR: " + e.getMessage());
        }

        barcode = null;
        numberOfLabels = 0;
    }

    private boolean isAdmin(String userFirstName, String userLastName) {
        String firstName = userFirstName.toLowerCase();
        String lastName = userLastName.toLowerCase();

        Statement statement = null;

        try {

            statement = connection.createStatement();

            Log.d(TAG, "isAdmin query: SELECT * FROM uz__uzytkownik " +
                    "WHERE uz_Imie='" + firstName + "' AND uz_Nazwisko='" + lastName
                    + "' AND uz_Administrator=1;");

            ResultSet resultSet = statement.executeQuery("SELECT * FROM uz__uzytkownik " +
                    "WHERE uz_Imie='" + firstName + "' AND uz_Nazwisko='" + lastName
                    + "' AND uz_Administrator=1;");


            if (resultSet.isBeforeFirst()) {
                return true;
            }


        } catch (SQLException e) {
            Log.d(TAG, "isAdmin: error message: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

}