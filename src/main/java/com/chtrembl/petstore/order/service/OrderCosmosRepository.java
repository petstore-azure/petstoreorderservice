package com.chtrembl.petstore.order.service;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.chtrembl.petstore.order.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Iterator;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class OrderCosmosRepository {

    private final CosmosContainer ordersContainer;

    public Optional<Order> findById(String orderId) {
        try {
            Order order = ordersContainer.readItem(orderId, new PartitionKey(orderId), Order.class).getItem();
            return Optional.ofNullable(order);
        } catch (CosmosException exception) {
            if (exception.getStatusCode() == 404) {
                return Optional.empty();
            }
            log.error("Error reading order {} from Cosmos DB", orderId, exception);
            throw exception;
        }
    }

    public Order upsert(Order order) {
        try {
            Order upsertedItem = ordersContainer.upsertItem(order, new PartitionKey(order.getId()), null).getItem();

            // If getItem() returns null, return the input order since it was successfully persisted
            if (upsertedItem != null) {
                return upsertedItem;
            }

            log.warn("Cosmos DB upsert response item was null for order {}, returning input order", order.getId());
            return order;
        } catch (CosmosException exception) {
            log.error("Error upserting order {} in Cosmos DB", order.getId(), exception);
            throw exception;
        }
    }

    public long countOrders() {
        try {
            Iterator<Number> results = ordersContainer.queryItems(
                    "SELECT VALUE COUNT(1) FROM c",
                    new CosmosQueryRequestOptions(),
                    Number.class
            ).iterator();

            return results.hasNext() ? results.next().longValue() : 0L;
        } catch (CosmosException exception) {
            log.error("Error counting orders in Cosmos DB", exception);
            throw exception;
        }
    }
}

