package ru.shop.backend.search.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.shop.backend.search.dto.SearchResult;
import ru.shop.backend.search.dto.SearchResultElastic;
import io.swagger.v3.oas.annotations.tags.Tag;
import ru.shop.backend.search.service.SearchService;

@Tag(name = "Поиск", description = "Методы поиска")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService service;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Возвращает результаты поиска для всплывающего окна",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SearchResult.class))}),
            @ApiResponse(responseCode = "400", description = "Ошибка обработки",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Регион не найден",
                    content = @Content)})
    @GetMapping
    public SearchResult findByTextAndRegionId(@Parameter(name = "text", description = "Поисковый запрос")
                                              @RequestParam String text,
                                              @CookieValue(name = "regionId", defaultValue = "1") int regionId) {
        return service.getSearchResult(regionId, text);
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Возвращает результаты поиска",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = SearchResultElastic.class))}),
            @ApiResponse(responseCode = "400", description = "Ошибка обработки",
                    content = @Content)})
    @GetMapping("/by")
    public ResponseEntity<SearchResultElastic> findByText(@Parameter(name = "text", description = "Поисковый запрос")
                                                          @RequestParam String text) {

        return ResponseEntity.ok(service.findByText(text));
    }
}
