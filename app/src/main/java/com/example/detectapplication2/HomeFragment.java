package com.example.detectapplication2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HomeFragment extends Fragment {

    private ImageView image1, image2;
    private TextView temperatureText, humidityText, conditionText;
    private EditText cityInput;
    private Button searchButton;
    private final String API_KEY = "90ec80953b809a18c31b89f696cf4b76";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        image1 = view.findViewById(R.id.pothehole);
        image2 = view.findViewById(R.id.distance);
        temperatureText = view.findViewById(R.id.temperature_text);
        humidityText = view.findViewById(R.id.humidity_text);
        conditionText = view.findViewById(R.id.condition_text);
        cityInput = view.findViewById(R.id.city_input);
        searchButton = view.findViewById(R.id.search_button);

        // Set click listeners for images
        image1.setOnClickListener(v -> startActivity(new Intent(getActivity(), PothethonListActivity.class)));
        image2.setOnClickListener(v -> startActivity(new Intent(getActivity(), distance.class)));

        // Set up search button click listener
        searchButton.setOnClickListener(v -> {
            String city = cityInput.getText().toString().trim();
            if (!city.isEmpty()) {
                new GetWeatherTask(city).execute();
            }
        });

        // Fetch weather for Saigon on startup
        new GetWeatherTask("Saigon").execute();

        return view;
    }

    private class GetWeatherTask extends AsyncTask<Void, Void, String> {
        private String city;

        public GetWeatherTask(String city) {
            this.city = city;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String response = "";
            try {
                URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY + "&units=metric");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response += line;
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject main = jsonObject.getJSONObject("main");
                double temp = main.getDouble("temp");
                int humidity = main.getInt("humidity");

                String weatherCondition = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");

                temperatureText.setText("Temperature: " + temp + " °C");
                humidityText.setText("Humidity: " + humidity + "%");
                conditionText.setText("Condition: " + weatherCondition);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}