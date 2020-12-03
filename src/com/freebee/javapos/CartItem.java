package com.freebee.javapos;

//BASE CARTITEM CLASS, CONTAINS PRICE & PRODUCTNAME
public class CartItem {
    public CartItem(float _price,  String _productname) {price = _price; productname = _productname;}
    public float price;
    public String productname;

    @Override
    public String toString() {
        return "CartItem{" +
                "price=" + price +
                ", productname='" + productname + '\'' +
                '}';
    }
}
