package dev.kixxippi.vinted_search.repository;

import dev.kixxippi.vinted_search.entity.Search;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchRepository extends JpaRepository<Search, Long> {

    List<Search> findByActiveTrue();
}
