package com.chtrembl.petstore.order.service;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.chtrembl.petstore.order.model.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderItemsReserverService {

    private final ServiceBusSenderClient serviceBusSenderClient;
    private final ObjectMapper objectMapper;

    public void reserve(Order updatedOrder) {
        try {
            log.info("Sending order {} to Service Bus for item reservation.", updatedOrder.getId());

            String orderJson = objectMapper.writeValueAsString(updatedOrder);

            ServiceBusMessage message = new ServiceBusMessage(orderJson);
            message.setContentType("application/json");
            message.setSessionId(updatedOrder.getId());
            message.setCorrelationId(updatedOrder.getId());
            message.setSubject("OrderUpdate");
            message.setMessageId(updatedOrder.getId() + "_" + System.currentTimeMillis());
            serviceBusSenderClient.sendMessage(message);

            log.info("Successfully sent order {} to Service Bus.", updatedOrder.getId());
        } catch (JsonProcessingException e) {
            log.error("Error serializing order {} to JSON: {}", updatedOrder.getId(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error sending order {} to Service Bus: {}", updatedOrder.getId(), e.getMessage(), e);
        }
    }
}