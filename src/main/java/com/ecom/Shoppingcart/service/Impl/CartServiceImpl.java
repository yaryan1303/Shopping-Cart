package com.ecom.Shoppingcart.service.Impl;

import  java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.stereotype.Service; 

import com.ecom.Shoppingcart.model.Cart;
import com.ecom.Shoppingcart.model.Product;
import com.ecom.Shoppingcart.model.UserDtls;
import com.ecom.Shoppingcart.repository.CartRepository;
import com.ecom.Shoppingcart.repository.ProductRepository;
import com.ecom.Shoppingcart.repository.userRepository;
import com.ecom.Shoppingcart.service.CartService;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private userRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    
    @Override
	public Cart saveCart(Integer productId, Integer userId) {

		UserDtls userDtls = userRepository.findById(userId).get();
		Product product = productRepository.findById(productId).get();

		Cart cartStatus = cartRepository.findByProductIdAndUserId(productId, userId);

		Cart cart = null;

		if (ObjectUtils.isEmpty(cartStatus)) {
			cart = new Cart();
			cart.setProduct(product);
			cart.setUser(userDtls);
			cart.setQuantity(1);
			cart.setTotalPrice(1 * product.getDiscountPrice());
		} else {
			cart = cartStatus;
			cart.setQuantity(cart.getQuantity() + 1);
			cart.setTotalPrice(cart.getQuantity() * cart.getProduct().getDiscountPrice());
		}
		Cart saveCart = cartRepository.save(cart);

		return saveCart;
	}

	@Override
	public List<Cart> getCartsByUser(Integer userId) {
		List<Cart> carts = cartRepository.findByUserId(userId);

		Double totalOrderPrice = 0.0;
		List<Cart> updateCarts = new ArrayList<>();
		for (Cart c : carts) {
			Double totalPrice = (c.getProduct().getDiscountPrice() * c.getQuantity());
			c.setTotalPrice(totalPrice);
			totalOrderPrice = totalOrderPrice + totalPrice;
			c.setTotalOrderPrice(totalOrderPrice);
			updateCarts.add(c);
		}

		return updateCarts;
	}

	@Override
	public Integer getCountCart(Integer userId) {
		Integer countByUserId = cartRepository.countByUserId(userId);
		return countByUserId;
	}

	@Override
public int updateQuantity(String sy, Integer cid) {
    
    // Get the cart by its ID
    Cart cart = cartRepository.findById(cid).get();
    
    // Get the product associated with the cart
    Product product = cart.getProduct();
    
    int updateQuantity;

    if (sy.equalsIgnoreCase("de")) {
        // Decrease the quantity
        updateQuantity = cart.getQuantity() - 1;

        // If quantity is zero or less, remove the cart item
        if (updateQuantity <= 0) {
            cartRepository.delete(cart);
        } else {
            // Update the quantity and save the cart
            cart.setQuantity(updateQuantity);
            cartRepository.save(cart);
        }

		return 1;

    } else {
        // Check available stock before increasing the quantity
        updateQuantity = cart.getQuantity() + 1;
        
        // Compare with product stock
        if (updateQuantity <= product.getStock()) {
            // If stock is available, update the cart quantity
            cart.setQuantity(updateQuantity);
            cartRepository.save(cart);
			return 1;
        } else {
			return -1;
            // Handle case when stock is not sufficient (e.g., throw an exception or return an error message)
            
        }
    }
}



}
