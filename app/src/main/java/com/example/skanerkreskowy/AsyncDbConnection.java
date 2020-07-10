package com.example.skanerkreskowy;

import android.os.AsyncTask;
import android.view.View;

import java.lang.ref.WeakReference;

public class AsyncDbConnection extends AsyncTask<String, Boolean, Boolean> {

    private static final String MS_SQL_SERVER_DRIVER = "net.sourceforge.jtds.jdbc.Driver";


    private WeakReference<LoginActivity> activityWeakReference;
    AsyncDbConnection(LoginActivity activity) {
        activityWeakReference = new WeakReference<LoginActivity>(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... strings) {

        LoginActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return false;
        }

        return true;
    }

    @Override
    protected void onProgressUpdate(Boolean... values) {
        super.onProgressUpdate(values);
    }


    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        LoginActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        activity.mCircuralProgressBar.setVisibility(View.INVISIBLE);
    }

}