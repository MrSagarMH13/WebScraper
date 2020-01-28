package com.webscraper;


import com.webscraper.dto.EventPOJO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.Doc;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class WebScraper {

    private static final List<String> USER_AGENT_STRINGS = new ArrayList<>();
    private static final List<String> URLS = new ArrayList<>();

    /* This is added basically to scrape the site from differnt clients for each time to avoid blocking */
    static {
        USER_AGENT_STRINGS.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");
        USER_AGENT_STRINGS.add("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; pl-PL; rv:1.0.1) Gecko/20021111 Chimera/0.6");
        USER_AGENT_STRINGS.add("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/419 (KHTML, like Gecko, Safari/419.3) Cheshire/1.0.ALPHA");
        USER_AGENT_STRINGS.add("Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/532.0 (KHTML, like Gecko) Chrome/4.0.204.0 Safari/532.0");
        USER_AGENT_STRINGS.add("Mozilla/5.0 (X11; Linux i686; rv:64.0) Gecko/20100101 Firefox/64.0");
        USER_AGENT_STRINGS.add("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:2.0a1pre) Gecko/2008060602 Minefield/4.0a1pre");

        URLS.add("https://www.computerworld.com/article/3313417/tech-event-calendar-2020-upcoming-shows-conferences-and-it-expos.html");
        URLS.add("https://www.techmeme.com/events");
    }


    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        /*Create an ExecutorService using a new fixed thread pool*/
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        /* Create a map of Future and URLs*/
        Map<Future, String> tasks = new LinkedHashMap<>();
        // Iterator through all URLs for scraping the web
        URLS.forEach(url -> {
            /* Create a callable instance which calls the function that invokes the scraping for each URL
             and get the content */
            Callable callable = new Callable() {
                public Document call() throws Exception {
                    return scrapeURL(url);
                }
            };

            /* Submit the task to executorService; At this point the scraping starts*/

            Future future = executorService.submit(callable);
            tasks.put(future, url);
        });

        /* For each task, iterate and get the content; Write the content to a file*/

        List<EventPOJO> events = new ArrayList<>();
        tasks.forEach((future, url) -> {
            try {
                Document document = (Document) future.get(120, TimeUnit.SECONDS);
                events.addAll(parseContent(url, document));
            } catch (InterruptedException | ExecutionException
                | TimeoutException e) {
                e.printStackTrace();
                // events.addAll(parseContent(url, null));
            }
        });
        executorService.shutdown();

        MySQLConnection mySQLConnection = new MySQLConnection("jdbc:mysql://localhost:3306/demo", "root", "root");
        mySQLConnection.saveData(events);
    }

    private static List<EventPOJO> parseContent(String url, Document page) {
        if (url.equals(URLS.get(0))) {
            return parseComputerworldData(url, page);
        } else if (url.equals(URLS.get(1))) {
            return parseTechmemeData(url, page);
        }
        return new ArrayList<>();
    }

    private static List<EventPOJO> parseTechmemeData(String url, Document page) {
        List<EventPOJO> events = new ArrayList<>();
        //System.out.println(page.outerHtml());
        Element element = page.getElementById("events");
        // System.out.println(element.outerHtml());
        Elements elements = element.select("div.rhov a");
        elements.forEach(selectedElement -> {
            EventPOJO event = new EventPOJO();
            System.out.println("Event Name :: " + selectedElement.select("div").get(1).text());

            event.setName(selectedElement.select("div").get(1).text());

            System.out.println("Event Date :: " + selectedElement.select("div").get(0).text());

            event.setEventDate(selectedElement.select("div").get(0).text());

            System.out.println("Event Location :: " + selectedElement.select("div").get(2).text());

            event.setLocation(selectedElement.select("div").get(2).text());

            System.out.println("===================================================");

            events.add(event);
        });
        return events;
    }

    public static List<EventPOJO> parseComputerworldData(String url, Document page) {
        List<EventPOJO> events = new ArrayList<>();
//        Map<String, String> contentMap = MappingUtil.getContentMapper(url);
        Elements elements = page.select("table.tablesorter tbody tr");
        for (Element element : elements) {
            EventPOJO event = new EventPOJO();
            /*System.out.println("Event Name :: " + element.select(contentMap.get("eventName")).text());
            Elements dataElements = element.select(contentMap.get("eventDetails"));
            System.out.println("Event Date :: " + dataElements.get(Integer.parseInt(contentMap.get("startDate"))).text());
            System.out.println("Event Location :: " + dataElements.get(Integer.parseInt(contentMap.get("location"))).text());
            System.out.println("===================================================");*/
            System.out.println("Event Name :: " + element.select("th").text());

            event.setName(element.select("th").text());

            Elements dataElements = element.select("td");

            System.out.println("Event Date :: " + dataElements.get(1).text());

            event.setEventDate(dataElements.get(1).text());

            System.out.println("Event Location :: " + dataElements.get(3).text());

            event.setLocation(dataElements.get(3).text());

            System.out.println("===================================================");

            events.add(event);
        }
        return events;
    }


    private static Document scrapeURL(String url) {
        try {
            final Document page = Jsoup
                .connect(url)
                .userAgent(getRandomUserAgent()).get();
            return page;
        } catch (IOException e) {

        }
        return null;
    }

    private static String getRandomUserAgent() {
        final int randomNo = ThreadLocalRandom.current().nextInt(0, USER_AGENT_STRINGS.size());
        return USER_AGENT_STRINGS.get(randomNo);
    }
}
