package com.bujank.microservices.order_service.service;

import com.bujank.microservices.order_service.client.InventoryClient;
import com.bujank.microservices.order_service.dto.OrderRequest;
import com.bujank.microservices.order_service.event.OrderPlacedEvent;
import com.bujank.microservices.order_service.model.Order;
import com.bujank.microservices.order_service.repository.OrderRepository;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
//    log
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public void placeOrder(OrderRequest orderRequest) {
        var isInProductInStock = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());
        System.out.println(orderRequest);
        System.out.println(orderRequest.userDetails());
        if (isInProductInStock) {
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setPrice(orderRequest.price());
//      print orderRequest.price() to console
            System.out.println(orderRequest.price());
            order.setQuantity(orderRequest.quantity());
            order.setSkuCode(orderRequest.skuCode());

            orderRepository.save(order);

//            Send the message to Kafka Topic (orderNumber, email)
            OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent();
            orderPlacedEvent.setOrderNumber(order.getOrderNumber());
            orderPlacedEvent.setEmail(orderRequest.userDetails().email());
            orderPlacedEvent.setFirstName(orderRequest.userDetails().firstName());
            orderPlacedEvent.setLastName(orderRequest.userDetails().lastName());
//            try log the first name and last name
            System.out.println(orderRequest.userDetails().firstName());
            System.out.println(orderRequest.userDetails().lastName());
            log.info("Start - Sending OrderPlacedEvent {} to Kafka Topic order-placed", orderPlacedEvent);
            kafkaTemplate.send("order-placed", orderPlacedEvent);
            log.info("End - Sending OrderPlacedEvent {} to Kafka Topic order-placed", orderPlacedEvent);

        } else {
            throw new RuntimeException("Product with SkuCode: " + orderRequest.skuCode() + " is out of stock");
        }
    }
}
