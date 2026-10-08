package dev.kixxippi.vinted_search.scheduler;

import dev.kixxippi.vinted_search.bot.TelegramBot;
import dev.kixxippi.vinted_search.entity.Search;
import dev.kixxippi.vinted_search.entity.VintedItem;
import dev.kixxippi.vinted_search.repository.SearchRepository;
import dev.kixxippi.vinted_search.service.VintedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VintedScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(VintedScheduler.class);

    private final SearchRepository searchRepository;
    private final VintedService vintedService;
    private final TelegramBot telegramBot;

    public VintedScheduler(
            SearchRepository searchRepository,
            VintedService vintedService,
            TelegramBot telegramBot
    ) {
        this.searchRepository = searchRepository;
        this.vintedService = vintedService;
        this.telegramBot = telegramBot;
    }

    @Scheduled(fixedRate = 15_000)
    public void checkVinted() {

        List<Search> searches = searchRepository.findByActiveTrue();

        log.info("Starting Vinted check. Active searches: {}", searches.size());

        for (Search search : searches) {

            try {
                log.info("Checking search {}: {}", search.getId(), search.getUrl());

                List<VintedItem> newItems =
                        vintedService.getItems(search.getUrl());

                log.info(
                        "Search {} finished. New items: {}",
                        search.getId(),
                        newItems.size()
                );

                for (VintedItem item : newItems) {

                    log.info(
                            "New item: {} | {} | {}",
                            item.getTitle(),
                            item.getPrice(),
                            item.getUrl()
                    );

                    telegramBot.sendNewItem(item);
                }

            } catch (Exception e) {

                log.error(
                        "Error checking search {}: {}",
                        search.getId(),
                        search.getUrl(),
                        e
                );
            }
        }

        log.info("Vinted check finished.");
    }
}