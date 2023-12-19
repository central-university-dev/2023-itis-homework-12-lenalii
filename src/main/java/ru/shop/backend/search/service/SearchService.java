package ru.shop.backend.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.shop.backend.search.dto.*;
import ru.shop.backend.search.dto.enums.TypeOfQuery;
import ru.shop.backend.search.model.*;
import ru.shop.backend.search.repository.ItemSkuRepository;
import ru.shop.backend.search.repository.ItemElasticRepository;
import ru.shop.backend.search.repository.ItemRepository;
import ru.shop.backend.search.util.StringUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static ru.shop.backend.search.util.StringUtil.convert;

@Service
@RequiredArgsConstructor
public class SearchService {
    private static final Pageable PAGEABLE = PageRequest.of(0, 150);
    private static final Pageable PAGEABLE_SMALL = PageRequest.of(0, 10);
    public static final String CAT_URL = "/cat/";

    private final ItemElasticRepository itemElasticRepository;
    private final ItemSkuRepository itemSkuRepository;
    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public SearchResultElastic findByText(String text) {
        if (StringUtil.isNumeric(text)) {
            Optional<Integer> itemId = itemSkuRepository.findFirstBySku(text);
            if (itemId.isEmpty()) {
                List<CatalogueElastic> catalogue = getByName(text);
                if (catalogue.size() > 0) {
                    return new SearchResultElastic(catalogue);
                }
                return new SearchResultElastic(getAllFull(text));
            } else {
                return new SearchResultElastic(getByItemId(itemId.get().toString()));
            }
        }
        return new SearchResultElastic(getAllFull(text));
    }

    @Transactional(readOnly = true)
    public SearchResult getSearchResult(Integer regionId, String text) {
        List<CatalogueElastic> result = null;
        if (StringUtil.isNumeric(text)) {
            result = getCatalogueElastics(text);
        }
        if (result == null || result.isEmpty()) {
            result = getAll(text, PAGEABLE_SMALL);
        }

        String brand = "";
        if (!result.isEmpty())
            brand = result.get(0).getBrand().toLowerCase(Locale.ROOT);


        List<Long> collect = result.stream()
                .flatMap(category -> category.getItems().stream())
                .map(ItemElastic::getItemId)
                .collect(Collectors.toList());

        List<Item> items = itemRepository.findByIdsAndRegionId(regionId, collect);
        Set<String> catUrls = new HashSet();

        List<Integer> collect1 = items.stream()
                .map(Item::getItemId)
                .collect(Collectors.toList());
        String finalBrand = brand.isEmpty() ? "" : "/brands/" + brand;
        List<Category> categories = itemRepository.findCatsByIds(collect1)
                .stream()
                .map(category -> {
                    if (catUrls.contains(category.getUrl()))
                        return null;
                    catUrls.add(category.getUrl());
                    return
                            new Category(category.getName(),
                                    category.getParentName(),
                                    CAT_URL + category.getUrl() + finalBrand,
                                    CAT_URL + category.getParentUrl(),
                                    category.getImage());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        String type = result.get(0).getItems().get(0).getType();
        String brand1 = result.get(0).getBrand();
        String typeHelpText = ((type != null ? type : "") +
                " " + (brand1 != null ? brand1 : "")).trim();
        List<TypeHelpText> typeHelpTexts = List.of(new TypeHelpText(TypeOfQuery.SEE_ALSO, typeHelpText));
        List<TypeHelpText> typeQueries = result.size() > 0 ? (typeHelpTexts) : new ArrayList<>();

        return new SearchResult(
                items,
                categories,
                typeQueries
        );
    }

    private List<CatalogueElastic> getCatalogueElastics(String text) {
        List<CatalogueElastic> result = null;
        Optional<Integer> itemId = itemSkuRepository.findFirstBySku(text);

        if (itemId.isEmpty()) {
            var catalogue = getByName(text);
            if (catalogue.size() > 0) {
                result = catalogue;
            }
        } else {
            result = getByItemId(itemId.get().toString());
        }
        return result;
    }

    private List<CatalogueElastic> getAll(String text, Pageable pageable) {
        String type = "";
        List<ItemElastic> list = new ArrayList<>();
        String brand = "", text2 = text;
        Long catalogueId = null;
        boolean needConvert = true;

        if (StringUtil.isContainErrorChar(text)) {
            text = convert(text);
            needConvert = false;
        }

        String convertedText = convert(text);
        if (needConvert && StringUtil.isContainErrorChar(convertedText)) {
            needConvert = false;
        }

        if (text.contains(" ")) {

            for (String queryWord : text.split("\\s")) {
                list = itemElasticRepository.findAllByBrand(queryWord, pageable);

                if (list.isEmpty() && needConvert) {
                    list = itemElasticRepository.findAllByBrand(convert(text), pageable);
                }
                if (!list.isEmpty()) {
                    text = text.replace(queryWord, "").trim().replace("  ", " ");
                    brand = list.get(0).getBrand();
                    break;
                }
            }
        }

        list = itemElasticRepository.findAllByType(text, pageable);
        if (list.isEmpty() && needConvert) {
            list = itemElasticRepository.findAllByType(convert(text), pageable);
        }
        if (!list.isEmpty()) {
            type = (list.stream().map(ItemElastic::getType).min(Comparator.comparingInt(String::length)).get());
        } else {
            for (String queryWord : text.split("\\s")) {
                list = itemElasticRepository.findAllByType(queryWord, pageable);
                if (list.isEmpty() && needConvert) {
                    list = itemElasticRepository.findAllByType(convert(text), pageable);
                }
                if (!list.isEmpty()) {
                    text = text.replace(queryWord, "");
                    type = (list.stream().map(ItemElastic::getType).min(Comparator.comparingInt(String::length)).get());
                }
            }
        }
        if (brand.isEmpty()) {
            list = itemElasticRepository.findByCatalogue(text, pageable);
            if (list.isEmpty() && needConvert) {
                list = itemElasticRepository.findByCatalogue(convert(text), pageable);
            }
            if (!list.isEmpty()) {
                catalogueId = list.get(0).getCatalogueId();
            }
        }
        text = text.trim();
        if (text.isEmpty() && !brand.isEmpty())
            return Collections.singletonList(new CatalogueElastic(list.get(0).getCatalogue(), list.get(0).getCatalogueId(), null, brand));
        text += "?";
        if (brand.isEmpty()) {
            type += "?";
            if (catalogueId == null) {
                list = itemElasticRepository.findAllByType(text, type, pageable);
                if (list.isEmpty()) {
                    list = itemElasticRepository.findAllByType(convert(text), type, pageable);
                }
            } else {
                list = itemElasticRepository.find(text, catalogueId, pageable);
                if (list.isEmpty()) {
                    list = itemElasticRepository.find(convert(text), catalogueId, pageable);
                }
            }

        } else {
            if (type.isEmpty()) {
                list = itemElasticRepository.findAllByBrand(text, brand, pageable);
                if (list.isEmpty()) {
                    list = itemElasticRepository.findAllByBrand(convert(text), brand, pageable);
                }
            } else {
                type += "?";
                list = itemElasticRepository.findAllByTypeAndBrand(text, brand, type, pageable);
                if (list.isEmpty()) {
                    list = itemElasticRepository.findAllByTypeAndBrand(convert(text), brand, type, pageable);
                }
            }
        }

        if (list.isEmpty()) {
            if (text2.contains(" "))
                text = String.join(" ", text.split("\\s"));
            text2 += "?";
            list = itemElasticRepository.findAllNotStrong(text2, pageable);
            if (list.isEmpty() && needConvert) {
                list = itemElasticRepository.findAllByTypeAndBrand(convert(text2), brand, type, pageable);
            }
        }
        return get(list, text, brand);
    }

    private List<CatalogueElastic> get(List<ItemElastic> list, String name, String brand) {
        Map<String, List<ItemElastic>> map = new HashMap<>();
        AtomicReference<ItemElastic> searchedItem = new AtomicReference<>();
        list.forEach(
                i -> {
                    if (name.replace("?", "").equals(i.getName())) {
                        searchedItem.set(i);
                    }
                    if (name.replace("?", "").endsWith(i.getName()) && name.replace("?", "").startsWith(i.getType())) {
                        searchedItem.set(i);
                    }
                    if (!map.containsKey(i.getCatalogue())) {
                        map.put(i.getCatalogue(), new ArrayList<>());
                    }
                    map.get(i.getCatalogue()).add(i);
                }
        );
        if (brand.isEmpty())
            brand = null;
        if (searchedItem.get() != null) {
            ItemElastic i = searchedItem.get();
            return Collections.singletonList(new CatalogueElastic(i.getCatalogue(), i.getCatalogueId(), Collections.singletonList(i), brand));
        }
        String finalBrand = brand;
        return map.keySet().stream().map(c ->
                new CatalogueElastic(c, map.get(c).get(0).getCatalogueId(), map.get(c), finalBrand)).collect(Collectors.toList());
    }

    private List<CatalogueElastic> getByName(String num) {
        List<ItemElastic> list = new ArrayList<>();
        list = itemElasticRepository.findAllByName(".*" + num + ".*", PAGEABLE);
        return get(list, num, "");
    }

    private List<CatalogueElastic> getByItemId(String itemId) {
        var list = itemElasticRepository.findByItemId(itemId, PageRequest.of(0, 1));
        return Collections.singletonList(new CatalogueElastic(list.get(0).getCatalogue(), list.get(0).getCatalogueId(), list, list.get(0).getBrand()));
    }

    private List<CatalogueElastic> getAllFull(String text) {
        return getAll(text, PAGEABLE);
    }
}
