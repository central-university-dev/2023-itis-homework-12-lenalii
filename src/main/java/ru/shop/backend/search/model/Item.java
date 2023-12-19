package ru.shop.backend.search.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Entity
public class Item {
    @Id
    private Integer itemId;

    private Integer price;
    private String name;
    private String url;
    private String image;
    private String cat;
}
