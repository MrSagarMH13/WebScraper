package com.webscraper;

import java.util.HashMap;
import java.util.Map;

public class MappingUtil {

    private static Map<String, Map<String, String>> dataMap = null;

    public static Map<String, String> getContentMapper(String url) {
        if (dataMap == null)
            dataMap = populateDataMap();
        return dataMap.get(url);
    }

    private static Map<String, Map<String, String>> populateDataMap() {
        dataMap = new HashMap<>();
        dataMap.put("https://www.computerworld.com/article/3313417/tech-event-calendar-2020-upcoming-shows-conferences-and-it-expos.html",
            getComputerworldMapper());
        dataMap.put("https://www.techmeme.com/events", getTechmemeMapper());
        return dataMap;
    }

    private static Map<String, String> getTechmemeMapper() {
        Map<String, String> techmemeMap = new HashMap<>();
        techmemeMap.put("elements", "events.rhov a");
        techmemeMap.put("eventName", "th");
        techmemeMap.put("eventDetails", "td");
        return techmemeMap;
    }


    private static Map<String, String> getComputerworldMapper() {
        Map<String, String> computerWorldMap = new HashMap<>();
        computerWorldMap.put("elements", "table.tablesorter tbody tr");
        computerWorldMap.put("eventName", "th");
        computerWorldMap.put("eventDetails", "td");
        computerWorldMap.put("startDate", "1");
        computerWorldMap.put("location", "3");
        return computerWorldMap;
    }
}
