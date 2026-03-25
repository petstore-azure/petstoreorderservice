package com.chtrembl.petstore.order.service;

import com.chtrembl.petstore.order.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderItemsReserverService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String RESERVE_ENDPOINT = "/api/OrderItemsReserver";

    @Value("${petstore.service.order-items-reserver.url:http://localhost:62301}")
    private String orderItemsReserverUrl;
    @Value("${petstore.service.order-items-reserver.x-functions-key}")
    private String orderReserverFunctionKey;

    public void reserve(Order updatedOrder) {
        try {
            log.info("Reserving order items for order {} by calling Order Items Reserver function.", updatedOrder.getId());
            String url = orderItemsReserverUrl + RESERVE_ENDPOINT;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-functions-key", orderReserverFunctionKey);

            HttpEntity<Order> entity = new HttpEntity<>(updatedOrder, headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Void.class
            );
            System.out.println(response);
            log.info("Successfully reserved order items for order {}. Response status: {}", updatedOrder.getId(),
                    response.getStatusCode());
        } catch (Exception e) {
            log.error("Error reserving order items for order {}: {}", updatedOrder.getId(), e.getMessage(), e);
        }
    }
}
