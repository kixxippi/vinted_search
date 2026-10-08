package dev.kixxippi.vinted_search.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "searches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Search {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2000, nullable = false)
    private String url;

    @Column(nullable = false)
    private boolean active;
}