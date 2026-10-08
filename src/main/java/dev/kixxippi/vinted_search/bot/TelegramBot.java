package dev.kixxippi.vinted_search.bot;

import dev.kixxippi.vinted_search.entity.VintedItem;
import dev.kixxippi.vinted_search.entity.Search;
import dev.kixxippi.vinted_search.service.SearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class TelegramBot implements SpringLongPollingBot, LongPollingUpdateConsumer {

    private final String token;
    private final TelegramClient telegramClient;
    private final SearchService searchService;
    private final long chatId = 794550659L;

    private static final Logger log =
            LoggerFactory.getLogger(TelegramBot.class);

    public TelegramBot(
            @Value("${telegram.bot.token}") String token,
            SearchService searchService
    ) {
        this.token = token;
        this.telegramClient = new OkHttpTelegramClient(token);
        this.searchService = searchService;
    }

    public void sendNewItem(VintedItem item) {

        String text = """
            🆕 Новый товар!

            📦 %s
            💰 Цена: %s
            💵 Всего: %s

            🔗 %s
            """.formatted(
                item.getTitle(),
                item.getPrice(),
                item.getTotalPrice(),
                item.getUrl()
        );

        try {

            if (item.getImageUrl() != null && !item.getImageUrl().isBlank()) {

                SendPhoto photo = new SendPhoto(
                        String.valueOf(chatId),
                        new InputFile(item.getImageUrl())
                );

                photo.setCaption(text);

                telegramClient.execute(photo);

            } else {

                sendMessage(chatId, text);
            }

        } catch (Exception e) {

            System.err.println(
                    "Telegram error while sending item " +
                            item.getVintedId() +
                            ": " +
                            e.getMessage()
            );
        }
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(List<Update> updates) {

        for (Update update : updates) {

            if (!update.hasMessage() || !update.getMessage().hasText()) {
                continue;
            }

            String text = update.getMessage().getText().trim();
            long chatId = update.getMessage().getChatId();

            if (chatId != this.chatId) {
                log.warn("Ignoring message from unauthorized chat: {}", chatId);
                continue;
            }

            log.info("Received command: {}", text);

            // /start
            if (text.equals("/start")) {

                sendMessage(
                        chatId,
                        """
                        Привет! Бот работает.

                        Доступные команды:

                        /add <URL> — добавить поиск
                        /list — список поисков
                        /delete <id> — удалить поиск
                        /pause <id> — поставить поиск на паузу
                        /resume <id> — возобновить поиск
                        """
                );
            }

            // /add URL
            else if (text.startsWith("/add ")) {

                String url = text.substring(5).trim();

                if (url.isEmpty()) {
                    sendMessage(chatId, "Укажи URL после /add");
                    continue;
                }

                searchService.addSearch(url);

                sendMessage(chatId, "Поиск добавлен.");
            }

            // /list
            else if (text.equals("/list")) {

                List<Search> searches = searchService.getAllSearches();

                if (searches.isEmpty()) {
                    sendMessage(chatId, "У тебя пока нет поисков.");
                    continue;
                }

                StringBuilder response = new StringBuilder();
                response.append("Твои поиски:\n\n");

                for (Search search : searches) {

                    String status = search.isActive()
                            ? "🟢 активен"
                            : "⏸ на паузе";

                    response.append("ID: ")
                            .append(search.getId())
                            .append("\n");

                    response.append(status)
                            .append("\n");

                    response.append(search.getUrl())
                            .append("\n\n");
                }

                sendMessage(chatId, response.toString());
            }

            // /delete ID
            else if (text.startsWith("/delete ")) {

                Long id = parseId(text);

                if (id == null) {
                    sendMessage(chatId, "Использование: /delete <id>");
                    continue;
                }

                try {
                    searchService.deleteSearch(id);
                    sendMessage(chatId, "Поиск " + id + " удалён.");
                } catch (Exception e) {
                    sendMessage(chatId, "Не удалось удалить поиск " + id);
                }
            }

            // /pause ID
            else if (text.startsWith("/pause ")) {

                Long id = parseId(text);

                if (id == null) {
                    sendMessage(chatId, "Использование: /pause <id>");
                    continue;
                }

                try {
                    searchService.setActive(id, false);
                    sendMessage(chatId, "Поиск " + id + " поставлен на паузу.");
                } catch (Exception e) {
                    sendMessage(chatId, "Поиск " + id + " не найден.");
                }
            }

            // /resume ID
            else if (text.startsWith("/resume ")) {

                Long id = parseId(text);

                if (id == null) {
                    sendMessage(chatId, "Использование: /resume <id>");
                    continue;
                }

                try {
                    searchService.setActive(id, true);
                    sendMessage(chatId, "Поиск " + id + " возобновлён.");
                } catch (Exception e) {
                    sendMessage(chatId, "Поиск " + id + " не найден.");
                }
            }

            // неизвестная команда
            else {
                sendMessage(
                        chatId,
                        "Неизвестная команда. Используй /start для списка команд."
                );
            }
        }
    }

    private Long parseId(String text) {

        try {
            String[] parts = text.split("\\s+");

            if (parts.length != 2) {
                return null;
            }

            return Long.parseLong(parts[1]);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void sendMessage(long chatId, String text) {

        try {

            SendMessage message = new SendMessage(
                    String.valueOf(chatId),
                    text
            );

            telegramClient.execute(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}