package dev.kixxippi.vinted_search.repository;

import dev.kixxippi.vinted_search.entity.VintedItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VintedItemRepository extends JpaRepository<VintedItem, Long> {

    Optional<VintedItem> findByVintedId(Long vintedId);

    boolean existsByVintedId(Long vintedId);
}
