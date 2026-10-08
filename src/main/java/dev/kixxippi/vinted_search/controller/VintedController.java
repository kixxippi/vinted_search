package dev.kixxippi.vinted_search.controller;

import dev.kixxippi.vinted_search.entity.VintedItem;
import dev.kixxippi.vinted_search.service.VintedService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class VintedController {

    private final VintedService vintedService;

    public VintedController(VintedService vintedService) {
        this.vintedService = vintedService;
    }

    @GetMapping("/vinted")
    public List<VintedItem> getItems(@RequestParam String url)
            throws Exception {

        return vintedService.getItems(url);
    }
}