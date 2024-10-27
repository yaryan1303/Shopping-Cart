package com.ecom.Shoppingcart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.Shoppingcart.model.Cart;

public interface CartRepository  extends JpaRepository<Cart,Integer>{

    Cart findByProductIdAndUserId(Integer productId, Integer userId);

    List<Cart> findByUserId(Integer userId);

    Integer countByUserId(Integer userId);

}
