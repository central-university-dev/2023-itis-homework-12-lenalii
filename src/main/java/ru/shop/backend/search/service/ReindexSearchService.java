package ru.shop.backend.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.shop.backend.search.model.ItemElastic;
import ru.shop.backend.search.repository.ItemSkuRepository;
import ru.shop.backend.search.repository.ItemElasticRepository;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReindexSearchService {
    private final ItemSkuRepository dbRepository;
    private final ItemElasticRepository searchRepository;

    @Scheduled(fixedDelay = 43200000)
    @Transactional
    public void reindex() {
        log.info("генерация индексов по товарам запущена");

        dbRepository.findAll().stream().parallel()
                .map(ItemElastic::new)
                .forEach(searchRepository::save);

        log.info("генерация индексов по товарам закончилась");
    }
}
