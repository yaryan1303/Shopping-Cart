package com.ecom.Shoppingcart.service.Impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.Shoppingcart.model.Cart;
import com.ecom.Shoppingcart.model.OrderAddress;
import com.ecom.Shoppingcart.model.OrderRequest;
import com.ecom.Shoppingcart.model.ProductOrder;
import com.ecom.Shoppingcart.repository.CartRepository;
import com.ecom.Shoppingcart.repository.ProductOrderRepository;
import com.ecom.Shoppingcart.service.OrderService;
import com.ecom.Shoppingcart.util.CommonUtil;
import com.ecom.Shoppingcart.util.OrderStatus;

@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private ProductOrderRepository OrderRepository;

    @Autowired
    private CartRepository cartRepository;

	@Autowired
	private CommonUtil commonUtil;

    @Override
    public void saveOrder(Integer userid, OrderRequest orderRequest) throws Exception {
        
		List<Cart> carts = cartRepository.findByUserId(userid);

		for (Cart cart : carts) {

			ProductOrder order = new ProductOrder();

			order.setOrderId(UUID.randomUUID().toString());
			order.setOrderDate(LocalDate.now());

			order.setProduct(cart.getProduct());
			order.setPrice(cart.getProduct().getDiscountPrice());

			order.setQuanity(cart.getQuantity());
			order.setUser(cart.getUser());

			order.setStatus(OrderStatus.IN_PROGRESS.getName());
			order.setPaymentType(orderRequest.getPaymentType());

			OrderAddress address = new OrderAddress();
			address.setFirstName(orderRequest.getFirstName());
			address.setLastName(orderRequest.getLastName());
			address.setEmail(orderRequest.getEmail());
			address.setMobileno(orderRequest.getMobileNo());
			address.setAddress(orderRequest.getAddress());
			address.setCity(orderRequest.getCity());
			address.setState(orderRequest.getState());
			address.setPincode(orderRequest.getPincode());

			order.setOrderAddress(address);

			ProductOrder saveOrder = OrderRepository.save(order);
		     commonUtil.sendMailForProductOrder(saveOrder, "success");
		}
    }

    @Override
    public List<ProductOrder> getOrdersByUser(Integer userId) {
        List<ProductOrder> orders = OrderRepository.findByUserId(userId);
		return orders;
    }

    @Override
    public ProductOrder updateOrderStatus(Integer id, String status) {
        Optional<ProductOrder> findById = OrderRepository.findById(id);
		if (findById.isPresent()) {
			ProductOrder productOrder = findById.get();
			productOrder.setStatus(status);
			ProductOrder updateOrder = OrderRepository.save(productOrder);
			return updateOrder;
		}
		return null;
    }

    @Override
    public List<ProductOrder> getAllOrders() {
        return OrderRepository.findAll();
    }

    @Override
    public ProductOrder getOrdersByOrderId(String orderId) {
        ProductOrder allById = OrderRepository.findByOrderId(orderId);
		return allById;
    }

    @Override
    public void deleteOrder(Integer id) {
        OrderRepository.deleteById(id);
    }

}
