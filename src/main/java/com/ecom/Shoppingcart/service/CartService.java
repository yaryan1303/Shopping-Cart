package com.ecom.Shoppingcart.service;

import java.util.*;

import com.ecom.Shoppingcart.model.Cart;

public interface CartService {
    public Cart saveCart(Integer productId, Integer userId);

	public List<Cart> getCartsByUser(Integer userId);
	
	public Integer getCountCart(Integer userId);

	public int updateQuantity(String sy, Integer cid);

}
