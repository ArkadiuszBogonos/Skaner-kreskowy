package com.example.skanerkreskowy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String TAG = "DatabaseConnectionTAG";
    private static final String SHARED_PREF_DB_CREDENTIALS = "dbCredentials";
    private static final String MS_SQL_SERVER_DRIVER = "net.sourceforge.jtds.jdbc.Driver";

    private static Connection connection = null;
    private static String databaseIp, databasePort, databaseName, databaseLogin, databasePassword,
            databaseUrl, databaseInstance;
    public static boolean isConnectionSetCorrectly = false;

    public static void setConnection(Context context) {
        context.getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_DB_CREDENTIALS, context.MODE_PRIVATE);

        databaseName = sharedPreferences.getString("dbName", null);
        databaseInstance = sharedPreferences.getString("dbInstance", null);
        databaseLogin = sharedPreferences.getString("dbLogin", null);
        databasePassword = sharedPreferences.getString("dbPassword", null);
        databaseIp = sharedPreferences.getString("dbIp", null);
        databasePort = sharedPreferences.getString("dbPort", null);

        databaseUrl = "jdbc:jtds:sqlserver://" + databaseIp + ":" + databasePort + "/" + databaseName;
       /* databaseUrl = "jdbc:jtds:sqlserver://" + databaseIp + ":" + databasePort + "/" + databaseName
                +";instance="+ databaseInstance;*/

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            Class.forName(MS_SQL_SERVER_DRIVER);
            long startTime = System.nanoTime();
            connection = DriverManager.getConnection(databaseUrl, databaseLogin, databasePassword);
            long endTime = System.nanoTime();
            long timeElapsed = endTime - startTime;
            Log.d(TAG, "timeElapsed: " + (timeElapsed / 1000000));
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

    public static Connection getConnection() {
        return connection;
    }
}
