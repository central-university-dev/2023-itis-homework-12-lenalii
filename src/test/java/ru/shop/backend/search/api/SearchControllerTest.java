package ru.shop.backend.search.api;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.shop.backend.search.config.AbstractControllerTest;
import ru.shop.backend.search.dto.SearchResult;
import ru.shop.backend.search.dto.SearchResultElastic;

import javax.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SearchControllerTest extends AbstractControllerTest {

    @Test
    void findByTextAndRegionIdSuccessfully() throws Exception {
        Cookie cookie = new Cookie("regionId", "2");
        SearchResult result = objectMapper.readValue(
                mockMvc.perform(get("/api/search")
                                .param("text", "some text")
                                .cookie(cookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8"))
                        .andExpect(status().is2xxSuccessful())
                        .andDo(print())
                        .andReturn().getResponse().getContentAsString(), new TypeReference<>() {
                }
        );
    }

    @Test
    void findByTextSuccessfully() throws Exception {

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("text", "some text");

        Cookie cookie = new Cookie("regionId", "2");
        ResponseEntity<SearchResultElastic> result = objectMapper.readValue(
                mockMvc.perform(get("/api/search/by")
                                .param("text", "some text")
                                .cookie(cookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("utf-8"))
                        .andExpect(status().is2xxSuccessful())
                        .andDo(print())
                        .andReturn().getResponse().getContentAsString(), new TypeReference<>() {
                }
        );
    }
}
