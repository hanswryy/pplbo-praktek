package com.bujank.microservices.order_service.service;

import com.bujank.microservices.order_service.client.InventoryClient;
import com.bujank.microservices.order_service.dto.OrderRequest;
import com.bujank.microservices.order_service.model.Order;
import com.bujank.microservices.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public void placeOrder(OrderRequest orderRequest) {
        var isInProductInStock = inventoryClient.isInStock(orderRequest.skuCode(), orderRequest.quantity());

        if (isInProductInStock) {
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setPrice(orderRequest.price());
//      print orderRequest.price() to console
            System.out.println(orderRequest.price());
            order.setQuantity(orderRequest.quantity());
            order.setSkuCode(orderRequest.skuCode());

            orderRepository.save(order);
        } else {
            throw new RuntimeException("Product with SkuCode: " + orderRequest.skuCode() + " is out of stock");
        }
    }
}
