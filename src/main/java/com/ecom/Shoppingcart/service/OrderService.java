package com.ecom.Shoppingcart.service;


import com.ecom.Shoppingcart.model.OrderRequest;
import com.ecom.Shoppingcart.model.Product;
import com.ecom.Shoppingcart.model.ProductOrder;

import java.util.*;

public interface OrderService {
    public void saveOrder(Integer userid, OrderRequest orderRequest) throws Exception;

	public List<ProductOrder> getOrdersByUser(Integer userId);

	public ProductOrder updateOrderStatus(Integer id, String status);

	public List<ProductOrder> getAllOrders();

	public ProductOrder getOrdersByOrderId(String orderId);

	public void deleteOrder(Integer id);
	
	//public Page<ProductOrder> getAllOrdersPagination(Integer pageNo,Integer pageSize);

}
