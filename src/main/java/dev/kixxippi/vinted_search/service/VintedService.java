package dev.kixxippi.vinted_search.service;

import dev.kixxippi.vinted_search.repository.SearchRepository;
import dev.kixxippi.vinted_search.entity.VintedItem;
import dev.kixxippi.vinted_search.repository.VintedItemRepository;
import dev.kixxippi.vinted_search.parser.VintedParser;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class VintedService {

    private final HttpClient client;
    private final VintedParser parser;
    private final VintedItemRepository itemRepository;
    private final SearchRepository searchRepository;

    public VintedService(
            VintedItemRepository itemRepository,
            SearchRepository searchRepository
    ) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        this.parser = new VintedParser();
        this.itemRepository = itemRepository;
        this.searchRepository = searchRepository;
    }

    public List<VintedItem> getItems(String url) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent",
                        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:157.0) Gecko/20100101 Firefox/157.0")
                .header("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .GET()
                .build();


        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 429) {
            throw new RuntimeException("Vinted returned 429 Too Many Requests");
        }

        if (response.statusCode() == 403) {
            throw new RuntimeException(
                    "Vinted returned 403 Forbidden. Body: " + response.body()
            );
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException(
                    "Vinted returned HTTP " + response.statusCode()
            );
        }

        List<VintedItem> items = parser.parse(response.body());

        List<VintedItem> newItems = new ArrayList<>();

        for (VintedItem item : items) {
            if (!itemRepository.existsByVintedId(item.getVintedId())) {
                itemRepository.save(item);
                newItems.add(item);
            }
        }

        return newItems;
    }
}