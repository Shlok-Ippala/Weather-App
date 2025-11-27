package com.aurora.climatesync.service;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {
    @Autowired
    private WeatherService weatherService;

    /**
     * Searches for locations matching the partial query (e.g., "New Y" for New York suggestions).
     * Ideal for autocomplete dropdowns. Returns up to maxResults matches.
     * @param query The search term (min 2 chars recommended for API).
     * @param maxResults Max number of results (1-100; default 10 if 0).
     * @return List of matching Locations, sorted by relevance (API default).
     */

    public List<Location> searchLocations(String query, int maxResults){
        if(query == null || query.trim().isEmpty()){
            return new ArrayList<>();
        }

        String q= query.trim().toLowerCase();

        if (q.contains("virtual") || q.contains("online")) {
            return new ArrayList<>();
        }

        int count = (maxResults > 0 && maxResults <=100) ? maxResults : 100;

        try{
            String urlString = "https://geocoding-api.open-meteo.com/v1/search?q=" +
                    URLEncoder.encode(q, StandardCharsets.UTF_8) +
                    "&count=" + count +
                    "&language=en&format=json";

            JSONObject json = makeHttpRequest(urlString);

            if(!json.has("results")) {
                return new ArrayList<>();
            }

            JSONArray results = json.getJSONArray("results");
            List<Location> locations = new ArrayList<>();

            for(int i = 0; i < results.length(); i++){
                JSONObject obj = results.getJSONObject(i);

                String cityName = obj.optString("name", "Unknown City");
                String country = obj.optString("country", "Unknown Country");
                double latitude = obj.optDouble("latitude", 0.0);
                double longitude = obj.optDouble("longitude", 0.0);

                if (latitude == 0.0 && longitude == 0.0) {
                    locations.add(Location.unknown());
                } else {
                    locations.add(new Location(cityName, country, latitude, longitude));
                }
            }
            return locations;

        }catch(Exception e) {
            return new ArrayList<>();
        }

    }

    /**
     * Convenience: get the top result only (e.g. when user presses Enter)
     */

    public Location getTopLocation(String query){
        List<Location> results = searchLocations(query, 1);
        return results.isEmpty() ? Location.unknown() : results.get(0);
    }

    public List<WeatherForecast> searchAndGetWeeklyForecast(String query){
        Location location = getTopLocation(query);
        if(location.isUnknown()) {
            return new ArrayList<>();
        }

        return weatherService.getWeeklyForecast(location);
    }

    private JSONObject makeHttpRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),
                StandardCharsets.UTF_8));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return new JSONObject(response.toString());
    }
}