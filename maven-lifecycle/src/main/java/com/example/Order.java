package com.example;

// 최대한 간단한 로직
public class Order {

    private String product;
    private int quantity;
    private int unitPrice;
    private int totalAmount;

    public Order(String product, int quantity, int unitPrice) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = quantity * unitPrice;
    }

    public String getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public int getUnitPrice() { return unitPrice; }
    public int getTotalAmount() { return totalAmount; }
}
