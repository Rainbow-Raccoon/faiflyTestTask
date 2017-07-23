package vlad.zamashka.com.faiflytesttask.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CountryDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "countries.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_COUNTRIES = "countries";
    private static final String TABLE_CITIES = "cities";

    private static final String KEY_ID_COUNTRY = "country_id";
    private static final String KEY_ID_CITY = "city_id";
    private static final String KEY_COUNTRY_NAME = "country_name";
    private static final String KEY_CITY_NAME = "city_name";

    public CountryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_COUNTRIES + " (" + KEY_ID_COUNTRY
                + " INTEGER PRIMARY KEY, " + KEY_COUNTRY_NAME + " TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_CITIES + " (" + KEY_ID_CITY
                + " INTEGER PRIMARY KEY, " + KEY_CITY_NAME + " TEXT, "
                + KEY_COUNTRY_NAME + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COUNTRIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CITIES);
    }

    public String getKeyCountryName() {
        return KEY_COUNTRY_NAME;
    }

    public String getKeyCityName() {
        return KEY_CITY_NAME;
    }

    public String getTableCountries() {
        return TABLE_COUNTRIES;
    }

    public String getTableCities() {
        return TABLE_CITIES;
    }

}
