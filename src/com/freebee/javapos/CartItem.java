package com.freebee.javapos;

public class CartItem {
    public CartItem(float _price, int _quantity, String _productname) {quantity = _quantity; price = _price; productname = _productname;}
    public int quantity;
    public float price;
    public String productname;

    public void AddQuantity() {quantity++;}
    public void DecreaseQuantity() {quantity--;}
}
