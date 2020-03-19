package com.coronavirus.tracker.services;

import com.coronavirus.tracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;


@Service
public class CoronaVirusDataService {

    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv";

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    private List<LocationStats> allStats = new ArrayList<>();
    private List<LocationStats> newStats = new ArrayList<>();

    @PostConstruct
    @Scheduled(cron = "* 1 * * * *")
    /***
     * The pattern of cron should be:
     * second, minute, hour, day of month, month, day of week
     */
    public void fetchVirusData() throws IOException, InterruptedException {
        // Using Java 11
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader = new StringReader(httpResponse.body());

        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records){
            LocationStats locationStat = new LocationStats();
            locationStat.setState(record.get("Province/State"));
            locationStat.setCountry(record.get("Country/Region"));

            locationStat.setLatestTotalCases(record.get(record.size() - 1).equals("") ? 0 : Integer.parseInt(record.get(record.size() - 1)));

            newStats.add(locationStat);
        }

        this.allStats = newStats;
    }

    /* Using Java 8
    @PostConstruct
    public void fetch() throws IOException {

        URL url = new URL(VIRUS_DATA_URL);

        InputStream inputStream = url.openStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        List<String> line = bufferedReader.lines().collect(Collectors.toList());
        line.forEach(System.out::println);
    }
    */
}
