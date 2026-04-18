package com.chtrembl.petstore.order.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class ServiceBusConfig {

    @Bean(destroyMethod = "close")
    public ServiceBusSenderClient serviceBusSenderClient(
            @Value("${azure.servicebus.connection-string}") String connectionString,
            @Value("${azure.servicebus.queue-name}") String queueName
    ) {
        if (!StringUtils.hasText(connectionString)) {
            throw new IllegalStateException("AZURE_SERVICEBUS_CONNECTION_STRING is not configured");
        }
        if (!StringUtils.hasText(queueName)) {
            throw new IllegalStateException("AZURE_SERVICEBUS_QUEUE_NAME is not configured");
        }

        return new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName(queueName)
                .buildClient();
    }
}