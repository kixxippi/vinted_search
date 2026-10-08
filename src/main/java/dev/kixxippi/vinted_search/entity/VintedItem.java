package dev.kixxippi.vinted_search.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VintedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vinted_id", unique = true, nullable = false)
    private Long vintedId;

    private String title;
    private String condition;
    private String price;
    private String totalPrice;

    @Column(length = 1000)
    private String url;

    @Column(length = 2000)
    private String imageUrl;
}