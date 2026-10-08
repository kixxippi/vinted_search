package dev.kixxippi.vinted_search.parser;

import dev.kixxippi.vinted_search.entity.VintedItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VintedParser {

    private static final Pattern ID_PATTERN =
            Pattern.compile("product-item-id-(\\d+)--summary");

    public List<VintedItem> parse(String html) {

        Document document = Jsoup.parse(html);

        List<VintedItem> items = new ArrayList<>();

        for (Element product : document.select(
                "[data-testid^=product-item-id-][data-testid$=--summary]"
        )) {

            String testId = product.attr("data-testid");

            Matcher matcher = ID_PATTERN.matcher(testId);

            if (!matcher.find()) {
                continue;
            }

            long id = Long.parseLong(matcher.group(1));

            Element titleElement = product.selectFirst(
                    "[data-testid$=--description-title]"
            );

            Element conditionElement = product.selectFirst(
                    "[data-testid$=--description-subtitle]"
            );

            Element priceElement = product.selectFirst(
                    "[data-testid$=--price-text]"
            );

            Element totalPriceElement = product.selectFirst(
                    "[data-testid=total-combined-price]"
            );

            Element card = product.parent();

            while (card != null && card.selectFirst("img") == null) {
                card = card.parent();
            }

            Element imageElement = card != null
                    ? card.selectFirst("img")
                    : null;


            String title = getText(titleElement);
            String condition = getText(conditionElement);
            String price = getText(priceElement);
            String totalPrice = getText(totalPriceElement);

            String url = "https://www.vinted.sk/items/" + id;

            String imageUrl = null;

            if (imageElement != null) {
                imageUrl = imageElement.hasAttr("src")
                        ? imageElement.attr("src")
                        : imageElement.attr("data-src");
            }

            items.add(new VintedItem(
                    null,
                    id,
                    title,
                    condition,
                    price,
                    totalPrice,
                    url,
                    imageUrl
            ));
        }

        return items;
    }

    private String getText(Element element) {
        if (element == null) {
            return null;
        }

        return element.text().trim();
    }
}
