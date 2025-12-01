package com.aurora.climatesync.util;

import javax.swing.*;
import java.net.URL;

public class WeatherIconLoader {

    public static ImageIcon load(String fileName) {
        String fullpath = "/assets/weather-icons/" + fileName;
        URL url = WeatherIconLoader.class.getResource(fullpath);

        if (url == null) {
            System.out.println("Couldn't find file: " + fileName);
            return null;
        }
        return new ImageIcon(url);
    }
}
