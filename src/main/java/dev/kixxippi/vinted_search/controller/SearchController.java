package dev.kixxippi.vinted_search.controller;

import dev.kixxippi.vinted_search.entity.Search;
import dev.kixxippi.vinted_search.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/searches")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    public Search add(@RequestParam String url) {
        return searchService.addSearch(url);
    }

    @GetMapping
    public List<Search> getAll() {
        return searchService.getAllSearches();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        searchService.deleteSearch(id);
    }
}
