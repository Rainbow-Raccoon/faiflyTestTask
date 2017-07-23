package vlad.zamashka.com.faiflytesttask;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import vlad.zamashka.com.faiflytesttask.database.CountryDbHelper;

import static vlad.zamashka.com.faiflytesttask.R.layout.progress_bar_layout;

public class ProgressBarActivity extends AppCompatActivity {
    private SharedPreferences settings = null;
    private boolean firstRun, connected;
    private Intent intentCountriesSpinner;
    private CountryDbHelper countryDbHelper = new CountryDbHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(progress_bar_layout);

        settings = getSharedPreferences("vlad.zamashka.com.faiflytesttask", 0);
        firstRun = settings.getBoolean("firstRun", true);
        intentCountriesSpinner = new Intent(this, CountriesSpinnerActivity.class);

        if (isConnected()) {
            if (firstRun) {

                SharedPreferences.Editor settingsEditor = settings.edit();
                settingsEditor.putBoolean("firstRun", false);
                settingsEditor.commit();

                new GetDataToDatabase().execute("https://raw.githubusercontent.com/David-Haim/" +
                        "CountriesToCitiesJSON/master/countriesToCities.json");

            } else {
                startActivity(intentCountriesSpinner);
                finish();
            }
        } else {
            AlertDialog.Builder alertBuilder;
            alertBuilder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault);
            alertBuilder.setTitle("No internet connection.")
                    .setMessage("Enable internet connection, please.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }

    }

    private class GetDataToDatabase extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... path) {
            try {
                getContent(path[0]);
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        private void getContent(String path) throws IOException {
            BufferedReader reader = null;
            SQLiteDatabase sqLiteDatabase = countryDbHelper.getWritableDatabase();

            try {

                URL url = new URL(path);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.connect();

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

                JsonReader jsonReader = new JsonReader(reader);
                jsonReader.beginObject();

                ContentValues contentValuesCountries = new ContentValues();
                ContentValues contentValuesCities = new ContentValues();

                String countryName, cityName;
                sqLiteDatabase.beginTransaction();
                while (jsonReader.hasNext()) {
                    countryName = jsonReader.nextName();

                    if (!countryName.equals("")) {
                        contentValuesCountries.put(countryDbHelper.getKeyCountryName(), countryName);
                        sqLiteDatabase.insert(countryDbHelper.getTableCountries(), null, contentValuesCountries);
                        contentValuesCountries.clear();
                    }

                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        cityName = jsonReader.nextString();

                        if (!cityName.equals("")) {
                            contentValuesCities.put(countryDbHelper.getKeyCityName(), cityName);
                            contentValuesCities.put(countryDbHelper.getKeyCountryName(), countryName);
                            sqLiteDatabase.insert(countryDbHelper.getTableCities(), null, contentValuesCities);
                            contentValuesCities.clear();
                        }
                    }
                    jsonReader.endArray();
                }
                sqLiteDatabase.setTransactionSuccessful();
            } finally {
                if (reader != null) {
                    reader.close();
                }
                sqLiteDatabase.endTransaction();
                sqLiteDatabase.close();
            }
            startActivity(intentCountriesSpinner);
            finish();
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
