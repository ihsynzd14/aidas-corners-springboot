package com.aidascorner.featureOrder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.aidascorner.featureBranch.service.BranchService;
import com.aidascorner.featureOrder.model.Order;
import com.aidascorner.featureOrder.model.OrderItem;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;

@Service
public class OrderService {

    private final Firestore firestore;
    private final BranchService branchService;

    public OrderService(Firestore firestore, BranchService branchService) {
        this.firestore = firestore;
        this.branchService = branchService;
    }

    /**
     * Get orders for a specific date
     */
    public List<Order> getOrdersByDate(LocalDate date) throws ExecutionException, InterruptedException {
        String dateStr = DateUtil.formatDate(date);
        return getOrdersByDateString(dateStr);
    }

    private List<Order> getOrdersByDateString(String dateStr) throws ExecutionException, InterruptedException {
        List<Order> orders = new ArrayList<>();
        
        // Get reference to the "branches" collection for this date
        CollectionReference branchesRef = firestore.collection("orders").document(dateStr).collection("branches");
        
        // Get all branch documents
        ApiFuture<QuerySnapshot> future = branchesRef.get();

        List<QueryDocumentSnapshot> branchDocs = future.get().getDocuments();
 
        for (QueryDocumentSnapshot branchDoc : branchDocs) {
            String branchId = branchDoc.getId();
            Map<String, Object> branchData = branchDoc.getData();

            Map<String, String> products = new HashMap<>();
            for (Map.Entry<String, Object> entry : branchData.entrySet()) {
                products.put(entry.getKey(), entry.getValue().toString());
            }
            // Create order
            Order order = new Order(branchId, branchId, dateStr, products);
            orders.add(order);
        }

        return orders;
    }

    /**
     * Get orders for a date range and merge by branch
     */
    public Map<String, List<Order>> getOrdersForDateRange(LocalDate startDate, LocalDate endDate) throws ExecutionException, InterruptedException {
        Map<String, List<Order>> allOrders = new HashMap<>();
        Map<String, Order> mergedOrders = new HashMap<>();
        Map<String, Double> totalProducts = new HashMap<>();

        List<String> dateStrings = DateUtil.getDateStringsInRange(startDate, endDate);
        String dateRangeKey = DateUtil.formatDate(startDate) + " - " + DateUtil.formatDate(endDate);

        for(String dateStr : dateStrings) {
            List<Order> ordersForDate = getOrdersByDateString(dateStr);
            for(Order order : ordersForDate) {
                String branchId = order.getBranchId();
                
                // Update total products across all branches
                Map<String, String> products = order.getProducts();
                for(Map.Entry<String, String> entry : products.entrySet()) {
                    String product = entry.getKey();
                    double quantity = Double.parseDouble(entry.getValue());
                    totalProducts.merge(product, quantity, Double::sum);
                }
                
                if(mergedOrders.containsKey(branchId)) {
                    // Merge products for existing branch
                    Order existingOrder = mergedOrders.get(branchId);
                    Map<String, String> existingProducts = existingOrder.getProducts();
                    Map<String, String> newProducts = order.getProducts();
                    
                    for(Map.Entry<String, String> entry : newProducts.entrySet()) {
                        String product = entry.getKey();
                        double newQuantity = Double.parseDouble(entry.getValue());
                        
                        if(existingProducts.containsKey(product)) {
                            double existingQuantity = Double.parseDouble(existingProducts.get(product));
                            existingProducts.put(product, String.valueOf(existingQuantity + newQuantity));
                        } else {
                            existingProducts.put(product, entry.getValue());
                        }
                    }
                } else {
                    // Create new merged order for this branch
                    Order mergedOrder = new Order(
                        order.getBranchId(),
                        order.getBranchName(),
                        dateRangeKey,
                        new HashMap<>(order.getProducts())
                    );
                    mergedOrders.put(branchId, mergedOrder);
                }
            }
        }
        
        // Add total products as a special "branch"
        Map<String, String> totalProductsStr = new HashMap<>();
        for (Map.Entry<String, Double> entry : totalProducts.entrySet()) {
            totalProductsStr.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        
        Order totalOrder = new Order(
            "total",
            "Total Across All Branches",
            dateRangeKey,
            totalProductsStr
        );
        mergedOrders.put("total", totalOrder);
        
        if (!mergedOrders.isEmpty()) {
            allOrders.put(dateRangeKey, new ArrayList<>(mergedOrders.values()));
        }
        
        return allOrders;
    }

    /**
     * Add a new order
     */
    public void addOrder(String dateStr, OrderItem orderItem) throws ExecutionException, InterruptedException {
        // Get reference to the date document
        DocumentReference dateDocRef = firestore.collection("orders").document(dateStr);
        
        // Get reference to the branch document
        DocumentReference branchDocRef = dateDocRef.collection("branches").document(orderItem.getBranch());
        
        // Create a batch write
        WriteBatch batch = firestore.batch();
        
        // Create or update the branch document with the product
        Map<String, Object> updates = new HashMap<>();
        updates.put(orderItem.getProduct(), orderItem.getQuantity());
        
        batch.set(branchDocRef, updates, com.google.cloud.firestore.SetOptions.merge());
        
        // Commit the batch
        batch.commit().get();
    }
}
