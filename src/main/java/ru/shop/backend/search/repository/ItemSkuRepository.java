package ru.shop.backend.search.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.shop.backend.search.model.ItemScuEntity;

import java.util.Optional;

public interface ItemSkuRepository extends JpaRepository<ItemScuEntity, Long> {

    @Query(value = "select item_id from item_sku where sku = :parseInt limit 1", nativeQuery = true)
    Optional<Integer> findFirstBySku(String parseInt);
}
