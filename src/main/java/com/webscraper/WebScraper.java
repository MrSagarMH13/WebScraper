package com.webscraper;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.Doc;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class WebScraper {

    private static final List<String> USER_AGENT_STRINGS = new ArrayList<>();
    private static final List<String> URLS = new ArrayList<>();

    static {
        USER_AGENT_STRINGS.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36");
        USER_AGENT_STRINGS.add("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; pl-PL; rv:1.0.1) Gecko/20021111 Chimera/0.6");
        USER_AGENT_STRINGS.add("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/419 (KHTML, like Gecko, Safari/419.3) Cheshire/1.0.ALPHA");
        USER_AGENT_STRINGS.add("Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/532.0 (KHTML, like Gecko) Chrome/4.0.204.0 Safari/532.0");
        USER_AGENT_STRINGS.add("Mozilla/5.0 (X11; Linux i686; rv:64.0) Gecko/20100101 Firefox/64.0");
        USER_AGENT_STRINGS.add("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:2.0a1pre) Gecko/2008060602 Minefield/4.0a1pre");

        URLS.add("https://www.computerworld.com/article/3313417/tech-event-calendar-2020-upcoming-shows-conferences-and-it-expos.html");
        //URLS.add("https://www.techmeme.com/events");
    }


    public static void main(String[] args) throws IOException {
        /*Create an ExecutorService using a newFixedThreadPool*/
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

        tasks.forEach((future, url) -> {
            try {
                Document document = (Document) future.get(120, TimeUnit.SECONDS);
                parseContent(url, document);
            } catch (InterruptedException | ExecutionException
                | TimeoutException e) {
                e.printStackTrace();
                parseContent(url, null);
            }
        });
        executorService.shutdown();
    }

    private static void parseContent(String url, Document page) {
        Map<String, String> contentMap = MappingUtil.getContentMapper(url);
        final Elements elements = page.select(contentMap.get("elements"));
        for (Element element : elements) {
            System.out.println("Event Name :: " + element.select(contentMap.get("eventName")).text());
            Elements dataElements = element.select(contentMap.get("eventDetails"));
            System.out.println("Event Date :: " + dataElements.get(Integer.parseInt(contentMap.get("startDate"))).text());
            System.out.println("Event Location :: " + dataElements.get(Integer.parseInt(contentMap.get("location"))).text());
            System.out.println("===================================================");
        }
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
