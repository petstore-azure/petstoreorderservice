package com.chtrembl.petstore.order.config;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CosmosConfig {

    @Bean(destroyMethod = "close")
    public CosmosClient cosmosClient(
            @Value("${azure.cosmos.connection-string:}") String connectionString) {
        if (!StringUtils.hasText(connectionString)) {
            throw new IllegalStateException("AZURE_COSMOS_CONNECTION_STRING is not configured");
        }

        // Singleton CosmosClient bean - reused across all requests.
        Map<String, String> parsedConnectionString = parseConnectionString(connectionString);
        String endpoint = parsedConnectionString.get("AccountEndpoint");
        String key = parsedConnectionString.get("AccountKey");

        if (!StringUtils.hasText(endpoint) || !StringUtils.hasText(key)) {
            throw new IllegalStateException("Invalid AZURE_COSMOS_CONNECTION_STRING. Expected AccountEndpoint and AccountKey");
        }

        return new CosmosClientBuilder()
                .endpoint(endpoint)
                .key(key)
                .buildClient();
    }

    @Bean
    public CosmosContainer ordersContainer(
            CosmosClient cosmosClient,
            @Value("${azure.cosmos.database}") String databaseName,
            @Value("${azure.cosmos.container}") String containerName) {
        CosmosDatabase database = cosmosClient.getDatabase(databaseName);
        try {
            database.read();
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Cosmos database '%s' does not exist or cannot be read".formatted(databaseName),
                    exception
            );
        }

        CosmosContainer container = database.getContainer(containerName);
        try {
            container.read();
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Cosmos container '%s' does not exist in database '%s'"
                            .formatted(containerName, databaseName),
                    exception
            );
        }

        return container;
    }

    private Map<String, String> parseConnectionString(String connectionString) {
        Map<String, String> values = new HashMap<>();
        for (String segment : connectionString.split(";")) {
            if (!StringUtils.hasText(segment) || !segment.contains("=")) {
                continue;
            }

            String[] pair = segment.split("=", 2);
            values.put(pair[0].trim(), pair[1].trim());
        }
        return values;
    }
}

