package com.ecom.Shoppingcart.controller;

import com.ecom.Shoppingcart.model.Category;
import com.ecom.Shoppingcart.model.Product;
import com.ecom.Shoppingcart.model.ProductOrder;
import com.ecom.Shoppingcart.model.UserDtls;
import com.ecom.Shoppingcart.repository.ProductRepository;
import com.ecom.Shoppingcart.service.CategoryService;
import com.ecom.Shoppingcart.service.OrderService;
import com.ecom.Shoppingcart.service.ProductService;
import com.ecom.Shoppingcart.service.userService;
import com.ecom.Shoppingcart.util.CommonUtil;
import com.ecom.Shoppingcart.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path; // Use java.nio.file.Path
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;



@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;
    private Category saveCategory;

    @Autowired
    private userService uService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private OrderService orderService;

    @GetMapping("/")
    public String index() {
        return "admin/index";
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

    @GetMapping("/loadAddProduct")
    public String loadAddProduct(Model m) {
        List<Category>category=categoryService.getAllCategory();
        m.addAttribute("categories", category);
        return "admin/add_product";
    }

    @GetMapping("/category")
    public String category(Model m) {
        m.addAttribute("categorys", categoryService.getAllCategory());
        return "admin/category";
    }

    @PostMapping("/saveCategory")
    public String saveCategory(@ModelAttribute Category category, 
                               @RequestParam("file") MultipartFile file,
                               HttpSession session) throws IOException {

        String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
        category.setImageName(imageName);

        // Check if category name already exists
        Boolean existCategory = categoryService.existCategory(category.getName());

        if (existCategory) {
            session.setAttribute("errorMsg", "Category Name already exists");
        } else {
            Category savedCategory = categoryService.saveCategory(category);

            if (ObjectUtils.isEmpty(savedCategory)) {
                session.setAttribute("errorMsg", "Not saved! Internal server error");
            } else {
                // Use ClassPathResource to access static files directory
                File saveFile = new ClassPathResource("static/img").getFile();

                // Construct the path for the new file
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator 
                                      + "category_img" + File.separator 
                                      + file.getOriginalFilename());

                System.out.println(path);

                // Copy the file to the destination path
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                session.setAttribute("succMsg", "Saved successfully");
            }
        }

        return "redirect:/admin/category";
    }

    @GetMapping("/deleteCategory/{id}")
    public String DeleteCategory(@PathVariable int  id)
    {
        categoryService.deleteById(id);
        return "redirect:/admin/category";

    }

    @GetMapping("/loadEditCategory/{id}")
    public String ShowFormForUpdate(@PathVariable  int id,Model m)
    {
        Category category=categoryService.getCategoryById(id);
        m.addAttribute("category", category);

        return "admin/edit_Category";

    }

    @PostMapping("/updateCategory")
public String updateCategory(@ModelAttribute Category category, @RequestPart("file") MultipartFile file, HttpSession session) throws IOException {
    Category oldCategory = categoryService.getCategoryById(category.getId());
    String imageName = (file != null && !file.isEmpty()) ? file.getOriginalFilename() : oldCategory.getImageName();

    // Update fields
    oldCategory.setName(category.getName());
    oldCategory.setIsActive(category.getIsActive());
    oldCategory.setImageName(imageName);

    // Save updated category
    Category updatedCategory = categoryService.saveCategory(oldCategory);

    if (updatedCategory != null) {
        // Handle file upload if a new file is provided
        if (file != null && !file.isEmpty()) {
            File saveFile = new ClassPathResource("static/img/category_img").getFile();
            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + imageName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        }
        session.setAttribute("succMsg", "Category Updated Successfully");
    } else {
        session.setAttribute("errorMsg", "Something went wrong on the server");
    }

    return "redirect:/admin/loadEditCategory/" + category.getId();
}

@PostMapping("/saveProduct")
public String saveProduct(@ModelAttribute Product product,@RequestParam ("file") MultipartFile file,HttpSession session)
throws IOException
{
    

    String imageName=file.isEmpty() ? "default.jpg":file.getOriginalFilename();
    product.setImage(imageName);
    product.setDiscount(0);
    product.setDiscountPrice(product.getPrice());
    Product saveProduct=productService.saveProduct(product);
    
    if(!ObjectUtils.isEmpty(saveProduct))
    {
        File saveFile = new ClassPathResource("static/img/product_img").getFile();
        Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + imageName);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        session.setAttribute("succMsg","Product saves Success");
    }
    else{
        session.setAttribute("errorMsg","Something wrong on server");

    }
    return "redirect:/admin/loadAddProduct";

}


        @GetMapping("/products")
        public String veiwProduct(Model m )
        {
            m.addAttribute("products",productService.getAllProducts());
            return "admin/products";
        }
        @GetMapping("/editProduct/{id}")
        public String ShowFormForUpdateProduct(@PathVariable  int id,Model m)
            {
                Product product=productService.getProductById(id);
                m.addAttribute("product", product);

                return "admin/edit_Product";

            }
        @GetMapping("deleteProduct/{id}")
        public String DeleteProduct(@PathVariable int  id,HttpSession session)
            {
                productService.deleteProduct(id);
                return "redirect:/admin/products";

            }

        @PostMapping("/updateProduct")
        public String UpdateProduct(@ModelAttribute Product product,Model m,@RequestPart("file") MultipartFile file,HttpSession session)throws IOException
        {
            Product oldupdateProduct=productService.getProductById(product.getId());
            String imageName = (file != null && !file.isEmpty()) ? file.getOriginalFilename() : oldupdateProduct.getImage();

            oldupdateProduct.setTitle(product.getTitle());
            oldupdateProduct.setCategory(product.getCategory());
            oldupdateProduct.setDescription(product.getDescription());
            oldupdateProduct.setPrice(product.getPrice());
            oldupdateProduct.setStock(product.getStock());
            oldupdateProduct.setImage(imageName);

            if(product.getDiscount()<0 || product.getDiscount()>100)
            {
                session.setAttribute("errorMsg", "Discount must be between 0 and 100%");

            }
            else{
            oldupdateProduct.setDiscount(product.getDiscount());

            Double discount= product.getPrice()*( product.getDiscount()/100.0);

            Double discountPrice=product.getPrice()-discount;
            oldupdateProduct.setDiscountPrice(discount);


            oldupdateProduct.setDiscountPrice(discountPrice);


            Product upadateProduct=productService.saveProduct(oldupdateProduct);

            if (upadateProduct != null) {
                // Handle file upload if a new file is provided
                if (file != null && !file.isEmpty()) {
                    File saveFile = new ClassPathResource("static/img/product_img").getFile();
                    Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + imageName);
                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                }
                session.setAttribute("succMsg", "Product Updated Successfully");
            } else {
                session.setAttribute("errorMsg", "Something went wrong on the server");
            }
        }

            return "redirect:/admin/editProduct/"+product.getId();

        }

        @GetMapping("/users")
        public String getAllUsers(Model m)
        {
           List<UserDtls>users= uService.getUsers("ROLE_USER");
           m.addAttribute("users", users);
            return "/admin/users";
        }

        @GetMapping("/updateSts")
        public String updateUserAccountStatus(@RequestParam Boolean status ,@RequestParam Integer id,HttpSession session)
        {
            boolean f=uService.updateAccountStatus(id,status);

            if(f){
                session.setAttribute("succMsg", "Account Status updated");

            }
            else{
                session.setAttribute("errorMsg", "Account Status updated");
            }
            return "redirect:/admin/users";
        }
        @GetMapping("/orders")
        public String getAllOrders(Model m)
        {
            List<ProductOrder> allOrders = orderService.getAllOrders();
            m.addAttribute("orders", allOrders);
          
            return "admin/order";
        }
        @PostMapping("/update-order-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

		OrderStatus[] values = OrderStatus.values();
		String status = null;

		for (OrderStatus orderSt : values) {
			if (orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}

		ProductOrder updateOrder = orderService.updateOrderStatus(id, status);

		try {
			commonUtil.sendMailForProductOrder(updateOrder,status);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Status Updated");
		} else {
			session.setAttribute("errorMsg", "status not updated");
		}
		return "redirect:/admin/orders";
	}
    @GetMapping("/profile")
    public String Profile()
    {
        return "admin/profile";
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
       
        return "redirect:/admin/profile";

    }

    @GetMapping("/search-order")
    public String searchOrder(@RequestParam String orderId,Model m,HttpSession session)
    {
        ProductOrder ordersByOrderId = orderService.getOrdersByOrderId(orderId);
        m.addAttribute("orders",ordersByOrderId);

        if(ObjectUtils.isEmpty(ordersByOrderId))
        {

        session.setAttribute("errorMsg", "Order id not exit");

        }
        else{
            session.setAttribute("succMsg", "order found");
        }



        return "admin/order";
        
    }
    @GetMapping("/search")
    public String searchProduct(@RequestParam String ch,Model m)
    {
        List<Product> searchProduct = productService.searchProduct(ch);
        m.addAttribute("products", searchProduct);

        return "admin/products";

    }

    @GetMapping("/add-admin")
    public String addAdmin() {

        return "admin/addAdmin";
    }

    @PostMapping("/saveAdmin")
    public String saveAdmin(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session) throws IOException {
        String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		user.setProfileImage(imageName);
		UserDtls saveUser = uService.saveAdmin(user);

		if (!ObjectUtils.isEmpty(saveUser)) {
			if (!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
						+ file.getOriginalFilename());

//				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			session.setAttribute("succMsg", "Register successfully");
		} else {
			session.setAttribute("errorMsg", "something wrong on server");
		}

		return "redirect:/admin/add-admin";
    }

    @GetMapping("/admins")
        public String getAllAdmin(Model m)
        {
           List<UserDtls>users= uService.getUsers("ROLE_ADMIN");
           m.addAttribute("users", users);
            return "/admin/allAdmin";
        }
    
    








}
