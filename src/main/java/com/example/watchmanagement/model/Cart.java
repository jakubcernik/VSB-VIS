package com.example.watchmanagement.model;

import java.util.HashMap;
import java.util.Map;

public class Cart {
    private Map<Watch, Integer> items = new HashMap<>();

    public Map<Watch, Integer> getItems() {
        return items;
    }

    public void addItem(Watch watch) {
        items.put(watch, items.getOrDefault(watch, 0) + 1);
    }

    public void removeItem(Watch watch) {
        if (items.containsKey(watch)) {
            int count = items.get(watch);
            if (count > 1) {
                items.put(watch, count - 1);
            } else {
                items.remove(watch);
            }
        }
    }

    public double getTotalPrice() {
        return items.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrice() * entry.getValue())
                .sum();
    }
}
