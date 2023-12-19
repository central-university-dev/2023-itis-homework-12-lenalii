package ru.shop.backend.search.config;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

@SpringBootTest
public class ApplicationTestContainers {
    public static final PostgreSQLContainer<?> postgreSQLContainer;
    public static final ElasticsearchContainer elasticContainer;

    static {
        postgreSQLContainer = new PostgreSQLContainer<>("postgres:12-alpine")
                .withDatabaseName("sudis_rdoc_write_uat")
                .withUsername("sudis")
                .withPassword("postgres");

        postgreSQLContainer.start();
        elasticContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.17.4").withExposedPorts(9200)
                .withEnv("xpack.security.enabled", "false")
                .withEnv("discovery.type", "single-node");
        elasticContainer.start();

    }

    @DynamicPropertySource
    static void datasourceConfig(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.elasticsearch.uris", elasticContainer::getHttpHostAddress);
    }
}
