package dev.kixxippi.vinted_search.service;

import dev.kixxippi.vinted_search.entity.Search;
import dev.kixxippi.vinted_search.repository.SearchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final SearchRepository searchRepository;

    public SearchService(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    public Search addSearch(String url) {
        Search search = new Search();
        search.setUrl(url);
        search.setActive(true);

        return searchRepository.save(search);
    }

    public List<Search> getAllSearches() {
        return searchRepository.findAll();
    }

    public List<Search> getActiveSearches() {
        return searchRepository.findByActiveTrue();
    }

    public void deleteSearch(Long id) {
        searchRepository.deleteById(id);
    }

    public Search setActive(Long id, boolean active) {
        Search search = searchRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Search not found: " + id));

        search.setActive(active);

        return searchRepository.save(search);
    }
}