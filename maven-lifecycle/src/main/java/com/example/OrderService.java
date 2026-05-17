package com.example;

public class OrderService {

    public Order createOrder(String product, int quantity, int unitPrice) {
        if (product == null || product.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다");
        }
        if (unitPrice <= 0) {
            throw new IllegalArgumentException("단가는 1 이상이어야 합니다");
        }
        return new Order(product, quantity, unitPrice);
    }

    public int calculateTotal(int quantity, int unitPrice) {
        return quantity * unitPrice;
    }

    public boolean isDiscountApplicable(int totalAmount) {
        return totalAmount >= 50000;
    }

    public int applyDiscount(int totalAmount) {
        if (isDiscountApplicable(totalAmount)) {
            return (int) (totalAmount * 0.9);
        }
        return totalAmount;
    }
}
