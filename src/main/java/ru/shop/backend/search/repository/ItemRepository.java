package ru.shop.backend.search.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.shop.backend.search.dto.Category;
import ru.shop.backend.search.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    @Query(value = "select i.item_id, i.name, r.price, i.itemurl as url, i as image , " +
            "i.type as cat from item as i " +
            "join remain as r on r.item_id = i.item_id and r.region_id = :regionId " +
            "where i.item_id in  :ids", nativeQuery = true)
    List<Item> findByIdsAndRegionId(@Param("regionId") Integer regionId, @Param("ids") List<Long> ids);

    @Query(value = "select distinct c.name, cp.name as parent_name, c.realcatname as url, " +
            "cp.realcatname as parent_url, " +
            "c.image from item as i " +
            "join catalogue as c using(catalogue_id) " +
            "join catalogue cp on cp.catalogue_id  = c.parent_id  where i.item_id  in :ids ", nativeQuery = true)
    List<Category> findCatsByIds(@Param("ids") List<Integer> ids);
}
