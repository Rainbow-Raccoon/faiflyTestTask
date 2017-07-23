package vlad.zamashka.com.faiflytesttask;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vlad.zamashka.com.faiflytesttask.database.CountryDbHelper;

public class CountriesSpinnerActivity extends Activity {
    private CountryDbHelper countryDbHelper;
    private Spinner countriesSpinner;
    private ListView listOfCities;
    private static Intent infoAboutCityIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_country);

        countryDbHelper = new CountryDbHelper(this);
        countriesSpinner = (Spinner) findViewById(R.id.spinner_countries);
        listOfCities = (ListView) findViewById(R.id.list_of_cities);
        infoAboutCityIntent = new Intent(this, InformationAboutCityActivity.class);

        addDataToSpinner();

        countriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                addDataToListOfCities(countriesSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing.
            }
        });

        listOfCities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                infoAboutCityIntent.putExtra("city", listOfCities.getItemAtPosition(position).toString());
                startActivity(infoAboutCityIntent);
            }
        });
    }

    private void addDataToSpinner() {

        SQLiteDatabase database = countryDbHelper.getReadableDatabase();
        Cursor cursor = database.query(countryDbHelper.getTableCountries(),
                new String[]{countryDbHelper.getKeyCountryName()}, null, null, null, null, null);
        cursor.moveToFirst();

        List<String> countryNames = new ArrayList<>();

        while (!cursor.isLast()) {
            countryNames.add(cursor.getString(cursor.getColumnIndexOrThrow("country_name")));
            cursor.moveToNext();
        }

        Collections.sort(countryNames);

        ArrayAdapter<String> countryNamesArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, countryNames);
        countryNamesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        countriesSpinner = (Spinner) findViewById(R.id.spinner_countries);
        countriesSpinner.setAdapter(countryNamesArrayAdapter);

    }

    private void addDataToListOfCities(String countryName) {
        List<String> citiesNames = new ArrayList<>();

        SQLiteDatabase database = countryDbHelper.getReadableDatabase();
        String query = "SELECT " + countryDbHelper.getKeyCityName() + " FROM " +
                countryDbHelper.getTableCities() + " WHERE " + countryDbHelper.getKeyCountryName() + " =?;";
        Cursor cursor = database.rawQuery(query, new String[] {countryName.replaceAll("'", "\'")});
        cursor.moveToFirst();

        while (!cursor.isLast()) {
            citiesNames.add(cursor.getString(cursor.getColumnIndex("city_name")));
            cursor.moveToNext();
        }

        Collections.sort(citiesNames);

        ArrayAdapter<String> nameOfCitiesArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, citiesNames);
        listOfCities.setAdapter(nameOfCitiesArrayAdapter);
    }
}
