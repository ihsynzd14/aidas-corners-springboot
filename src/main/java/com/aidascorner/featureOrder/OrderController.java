package com.aidascorner.featureOrder;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.aidascorner.featureOrder.model.Order;
import com.aidascorner.featureOrder.model.OrderItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
        logger.info("OrderController initialized");
    }

    /**
     * GET /api/orders/date/{date} - Get orders for a specific date
     * Date format: yyyy-MM-dd
     */
    @GetMapping("/api/orders/date/{date}")
    public ResponseEntity<List<Order>> getOrdersByDate(
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        logger.info("Received request for orders on date: {}", date);
        try {
            List<Order> orders = orderService.getOrdersByDate(date);
            logger.info("Returning {} orders for date {}", orders.size(), date);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error getting orders for date: " + date, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/orders - Get orders for a date range
     * Date format: yyyy-MM-dd
     */
    @GetMapping("/api/orders")
    public ResponseEntity<Map<String, List<Order>>> getOrdersForDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.info("Received request for orders between {} and {}", startDate, endDate);
        try {
            Map<String, List<Order>> orders = orderService.getOrdersForDateRange(startDate, endDate);
            logger.info("Returning orders for {} dates", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error getting orders for date range", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * POST /api/orders - Add a new order
     */
    @PostMapping("/api/orders")
    public ResponseEntity<String> addOrder(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody OrderItem orderItem) {
        logger.info("Received request to add order for date: {} branch: {} product: {}", 
                   date, orderItem.getBranch(), orderItem.getProduct());
        try {
            String dateStr = DateUtil.formatDate(date);
            orderService.addOrder(dateStr, orderItem);
            return ResponseEntity.ok("Order added successfully");
        } catch (Exception e) {
            logger.error("Error adding order", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}