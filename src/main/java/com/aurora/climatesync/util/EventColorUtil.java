package com.aurora.climatesync.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class EventColorUtil {
    private static final Map<String, Color> EVENT_COLORS = new HashMap<>();
    
    public static final String[] COLOR_NAMES = {
        "Lavender", "Sage", "Grape", "Flamingo", "Banana", "Tangerine", 
        "Peacock", "Graphite", "Blueberry", "Basil", "Tomato"
    };
    
    public static final String[] COLOR_IDS = {
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"
    };

    static {
        EVENT_COLORS.put("1", new Color(121, 134, 203)); // Lavender
        EVENT_COLORS.put("2", new Color(51, 182, 121));  // Sage
        EVENT_COLORS.put("3", new Color(142, 36, 170));  // Grape
        EVENT_COLORS.put("4", new Color(230, 124, 115)); // Flamingo
        EVENT_COLORS.put("5", new Color(246, 191, 38));  // Banana
        EVENT_COLORS.put("6", new Color(244, 81, 30));   // Tangerine
        EVENT_COLORS.put("7", new Color(3, 155, 229));   // Peacock
        EVENT_COLORS.put("8", new Color(97, 97, 97));    // Graphite
        EVENT_COLORS.put("9", new Color(63, 81, 181));   // Blueberry
        EVENT_COLORS.put("10", new Color(11, 128, 67));  // Basil
        EVENT_COLORS.put("11", new Color(213, 0, 0));    // Tomato
    }

    public static Color getEventColor(String colorId) {
        if (colorId == null) return new Color(3, 155, 229); // Default to Peacock (Blue)
        return EVENT_COLORS.getOrDefault(colorId, new Color(3, 155, 229));
    }
    
    public static int getColorIndex(String colorId) {
        if (colorId == null) return 6; // Default Peacock
        for (int i = 0; i < COLOR_IDS.length; i++) {
            if (COLOR_IDS[i].equals(colorId)) {
                return i;
            }
        }
        return 6;
    }
}
