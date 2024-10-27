package com.ecom.Shoppingcart.controller;

import java.security.Principal;
import java.security.PrivateKey;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.Shoppingcart.model.Cart;
import com.ecom.Shoppingcart.model.Category;
import com.ecom.Shoppingcart.model.OrderRequest;
import com.ecom.Shoppingcart.model.ProductOrder;
import com.ecom.Shoppingcart.model.UserDtls;
import com.ecom.Shoppingcart.service.CartService;
import com.ecom.Shoppingcart.service.CategoryService;
import com.ecom.Shoppingcart.service.OrderService;
import com.ecom.Shoppingcart.service.userService;
import com.ecom.Shoppingcart.util.CommonUtil;
import com.ecom.Shoppingcart.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private userService uService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommonUtil commonUtil;

    @GetMapping("/")
    public String home()
    {
        return "user/home";

    }

     @ModelAttribute
    public void getUserDetails(Principal p,Model m)
    {
        if(p!=null)
        {
            String email=p.getName();
            UserDtls userDtls=uService.getUserByEmail(email);
            m.addAttribute("user",userDtls);

        }
        List<Category> allActiveCategory = categoryService.getAllActiveCategory();
        m.addAttribute("categorys", allActiveCategory);

    }

    @GetMapping("/addCart")
    public String addToCart(@RequestParam Integer pid,@RequestParam Integer uid,HttpSession session)
    {
        Cart savecart=cartService.saveCart(pid, uid);
        if(ObjectUtils.isEmpty(savecart))
        {
            session.setAttribute("errorMsg", "Product add to cart failed");
        }
        else{
            session.setAttribute("succMsg", "Product added to cart");
        }
        return "redirect:/product/"+pid;
    }

    @GetMapping("/cart")
    public String loadCartPage(Principal p,Model m)
    {
        String email = p.getName();

        UserDtls userDtls = uService.getUserByEmail(email);

        List<Cart> carts = cartService.getCartsByUser(userDtls.getId());

        if(carts.size()>0)
        {

        m.addAttribute("carts", carts);
        m.addAttribute("totalOrderPrice", carts.get(carts.size()-1).getTotalOrderPrice());
        }

        return "user/cart";
    }

    @GetMapping("/cartQuantityUpdate")
    public String updateCartQuantity(@RequestParam String sy,@RequestParam Integer cid,HttpSession session)
    {
        int updateQuantity = cartService.updateQuantity(sy, cid);
        if(updateQuantity ==1)
        {
            session.setAttribute("succMsg", "Product updated successfully");
        }
        else if(updateQuantity==-1)
        {
            session.setAttribute("errorMsg", "Product Out of stock");
        }
        return "redirect:/user/cart";
    }
    

    @GetMapping("/orders")
    public String OrderPage(Principal p,Model m)
    {
        String email = p.getName();

        UserDtls userDtls = uService.getUserByEmail(email);

        List<Cart> carts = cartService.getCartsByUser(userDtls.getId());

        if(carts.size()>0)
        {

            Double orderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
			Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice() + 250 + 100;
			m.addAttribute("orderPrice", orderPrice);
			m.addAttribute("totalOrderPrice", totalOrderPrice);
        }

       
        return "user/Order";
    }

    @PostMapping("/save-order")
    public String SaveOrder(@ModelAttribute OrderRequest request,Principal p) throws Exception
    {
        String email = p.getName();

        UserDtls userDtls = uService.getUserByEmail(email);
       
        orderService.saveOrder(userDtls.getId(), request);
        return "redirect:/user/success";
    }

    @GetMapping("/success")
    public String LoadSuccesspage()
    {
        return "user/success";
    }

    @GetMapping("/user-orders")
	public String myOrder(Model m, Principal p) {
        String email = p.getName();

        UserDtls userDtls = uService.getUserByEmail(email);
		List<ProductOrder> orders = orderService.getOrdersByUser(userDtls.getId());
		m.addAttribute("orders", orders);
		return "/user/my_order";
	}

    @GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) 
    {

		OrderStatus[] values = OrderStatus.values();
		String status = null;

		for (OrderStatus orderSt : values) {
			if (orderSt.getId().equals(st)) {
                System.out.println(orderSt.getId()+"====================================================================================================");
				status = orderSt.getName();
			}
		}

		ProductOrder updateOrder = orderService.updateOrderStatus(id, status);
		
		try {
			commonUtil.sendMailForProductOrder(updateOrder, status);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Status Updated");
		} else {
			session.setAttribute("errorMsg", "status not updated");
		}
		return "redirect:/user/user-orders";
    }

    @GetMapping("/profile")
    public String Profile()
    {
        return "user/profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute UserDtls user,@RequestParam MultipartFile img,HttpSession session)
    {
        UserDtls updateUserProfile = uService.updateUserProfile(user, img);

        if(ObjectUtils.isEmpty(updateUserProfile))
        {
            session.setAttribute("errorMsg", "Profile Not updated");
        }
        else{
            session.setAttribute("succMsg", "Profile updated");
        }
       
        return "redirect:/user/profile";

    }

    



}
