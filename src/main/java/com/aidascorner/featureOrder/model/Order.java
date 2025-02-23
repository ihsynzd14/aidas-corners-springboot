package com.aidascorner.featureOrder.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String branchId;
    private String branchName;
    private String date;
    private Map<String, String> products; // product name -> quantity
}
