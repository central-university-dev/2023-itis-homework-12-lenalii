package ru.shop.backend.search.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class ItemScuEntity {

    @Id
    private long itemId;

    private String name;
    private String brand;
    private String catalogue;
    private String type;
    private String description;
    private long brandId;
    private long catalogueId;
}