package vlad.zamashka.com.faiflytesttask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class InformationAboutCityActivity extends Activity {
    private TextView infoAboutCityTextView;
    private ImageView cityImage;
    private Button backButton;
    private Intent backIntent;
    private List<String> infoAboutCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_about_city);

        infoAboutCityTextView = (TextView) findViewById(R.id.info_about_city_text_view);
        cityImage = (ImageView) findViewById(R.id.city_image);
        backButton = (Button) findViewById(R.id.back_button);
        backIntent = new Intent(this, CountriesSpinnerActivity.class);
        infoAboutCity = new ArrayList<>();

        GetCityInfo getCityInfo = new GetCityInfo();
        getCityInfo.execute(getIntent().getStringExtra("city"));

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(backIntent);
            }
        });

    }

    private class GetCityInfo extends AsyncTask<String, Void, Boolean> {
        private Drawable image;

        @Override
        protected Boolean doInBackground(String... names) {
            try {
                getContent(names[0]);
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            addInfoAboutCity(infoAboutCity);

            cityImage.setImageDrawable(image);
        }

        private void getContent(String name) throws IOException {

            BufferedReader reader = null;
            infoAboutCity.add(name);

            //Если я не ошибаюсь, то в исходнике сломана кодировка.
            // Потратил кучу времени, смог только подобрать ту, которая ниже.
            //С ней некоторые файлыгорода работают.
            try {
                String urlAdress = "http://api.geonames.org/wikipediaSearchJSON?q="
                        + URLEncoder.encode(name, "ISO-8859-15") + "&maxRows=1&username=vlad_zamashka";
                URL url = new URL(urlAdress);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                JsonReader jsonReader = new JsonReader(reader);
                jsonReader.beginObject();

                String key;
                while (jsonReader.hasNext()) {
                    jsonReader.nextName();
                    jsonReader.beginArray();
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        if ((key = jsonReader.nextName()).equals("summary") ||
                                key.equals("countryCode") || key.equals("thumbnailImg")) {
                            infoAboutCity.add(jsonReader.nextString());
                        } else {
                            jsonReader.nextString();
                        }
                    }
                    jsonReader.endObject();
                    jsonReader.endArray();
                }
                if (infoAboutCity.size() == 4) {
                    InputStream imageInputStream = (InputStream) new URL(infoAboutCity.get(3))
                            .getContent();
                    image = Drawable.createFromStream(imageInputStream, "city image");
                } else {
                    InputStream imageInputStream = (InputStream) new URL("http://" +
                            "www.freeiconspng.com/uploads/no-image-icon-23.jpg").getContent();
                    image = Drawable.createFromStream(imageInputStream, "city image");
                }

            } catch (IllegalStateException e) {
                infoAboutCity.clear();
                infoAboutCity.add("Oooops!");
                infoAboutCity.add("Trouble with encoding. \n Sorry.");
                infoAboutCity.add("None");
            } finally

            {
                if (reader != null) {
                    reader.close();
                }
            }
        }

        private void addInfoAboutCity(List<String> infoAboutCity) {
            infoAboutCityTextView.setText(infoAboutCity.get(0) + "\n"
                    + infoAboutCity.get(1) + '\n'
                    + "CountryCode: " + infoAboutCity.get(2) + '\n');
        }
    }

}
