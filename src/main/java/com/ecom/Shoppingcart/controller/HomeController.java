package com.ecom.Shoppingcart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.Shoppingcart.model.Category;
import com.ecom.Shoppingcart.model.Product;
import com.ecom.Shoppingcart.model.UserDtls;
import com.ecom.Shoppingcart.service.CartService;
import com.ecom.Shoppingcart.service.CategoryService;
import com.ecom.Shoppingcart.service.ProductService;
import com.ecom.Shoppingcart.service.userService;
import com.ecom.Shoppingcart.util.CommonUtil;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private userService uService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    @Autowired
    private CartService cartService;


    @ModelAttribute
    public void getUserDetails(Principal p,Model m)
    {
        if(p!=null)
        {
            String email=p.getName();
            UserDtls userDtls=uService.getUserByEmail(email);
            m.addAttribute("user",userDtls);

            Integer countCart = cartService.getCountCart(userDtls.getId());

            m.addAttribute("countCart", countCart);

        }
        List<Category> allActiveCategory = categoryService.getAllActiveCategory();
        m.addAttribute("categorys", allActiveCategory);

    }

    @GetMapping("/")
    public String home(Model m) {
        List<Category> allCategory = categoryService.getAllCategory();
        List<Product> allProducts = productService.getAllProducts();

        m.addAttribute("products", allProducts);
        m.addAttribute("category", allCategory);

        return "index";
    }

    @GetMapping("/signin")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/products")
    public String products(Model m, @RequestParam(value = "category", defaultValue = "") String category, @RequestParam(defaultValue = "") String ch) {
        List<Category> categories = categoryService.getAllActiveCategory();
        m.addAttribute("paramValue", category);
        m.addAttribute("categories", categories);

        List<Product> products;
        if (category.isEmpty()) {
            // Fetch all products if no category is specified
            products = productService.getAllProducts();
        } else {
            // Fetch products based on the selected category
            products = productService.getAllActiveProducts(category);
        }
        m.addAttribute("products", products);
        return "product";
    }

    @GetMapping("/product/{id}")
    public String product(@PathVariable Integer id, Model m) {
        Product product = productService.getProductById(id);
        m.addAttribute("product", product);
        return "view-product";
    }

    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session) throws IOException {
        Boolean existsEmail = uService.existsEmail(user.getEmail());

        if (existsEmail) {
            session.setAttribute("errorMsg", "Email already exists");
        } else {
            String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
            user.setProfileImage(imageName);
            UserDtls saveUser = uService.saveUser(user);

            if (!ObjectUtils.isEmpty(saveUser)) {
                if (!file.isEmpty()) {
                    File saveFile = new ClassPathResource("static/img").getFile();

                    Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator + file.getOriginalFilename());

                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                }
                session.setAttribute("succMsg", "Registered successfully");
            } else {
                session.setAttribute("errorMsg", "Something went wrong on the server");
            }
        }

        return "redirect:/register";
    }

    //Forgot Password

    @GetMapping("/forgot-password")
    public String showForgotPassword(Model model) {
        // Add any required model attributes here
        return "Forgot_password";
    }

	@PostMapping("/forgot_password")
    public String ProcessForgotPassword(@RequestParam String email,HttpSession session,HttpServletRequest request)
    {
        UserDtls userByEmail = uService.getUserByEmail(email);

        if(ObjectUtils.isEmpty(userByEmail))
        {
            session.setAttribute("errorMsg", "invalid email");
        }
        else{
            //Generate Url
             String resetToken= UUID.randomUUID().toString();

             uService.updateUserResetToken(email, resetToken);

             //Generate Url
             String url = commonUtil.generateUrl(request)+"/reset-password?token="+resetToken;
           Boolean sendMail = commonUtil.sendMail(url,email);
           if(sendMail)
           {
            session.setAttribute("succMsg", "Please check your mail...Password Reset Link sent");
           }
           else{
            session.setAttribute("errorMsg", "Something wrong on server !Mail not send");
           }
        }
        return "redirect:/forgot-password";

    }
    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam String token,HttpSession session,Model m)
    {
        UserDtls userByToken = uService.getUserByToken(token);

        if (userByToken == null) {
			m.addAttribute("msg", "Your link is invalid or expired !!");
			return "message";
		}
		m.addAttribute("token", token);
		return "reset_password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,@RequestParam String password,HttpSession session,Model m)
    {
        UserDtls userByToken = uService.getUserByToken(token);

        if(userByToken==null)
        {
            m.addAttribute("msg", "Your link is invalid or expired !!");
            return "message";

        }

        else{
            userByToken.setPassword(passwordEncoder.encode(password));
            userByToken.setResetToken(null);
            uService.updateUser(userByToken);
            m.addAttribute("msg", "Password reset Successfully");

            return "message";
    
        }
    }

    @GetMapping("/search")
    public String searchProduct(@RequestParam String ch,Model m)
    {
        List<Product> searchProduct = productService.searchProduct(ch);
        m.addAttribute("products", searchProduct);

        List<Category> categories = categoryService.getAllActiveCategory();
        m.addAttribute("categories", categories);

        return "product";

    }

    

    
}
