package com.ecom.Shoppingcart.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.ecom.Shoppingcart.model.ProductOrder;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.mail.javamail.MimeMessageHelper;

@Component
public class CommonUtil {

    @Autowired
    private JavaMailSender mailSender; // Remove 'static' from this field

    // Method to generate a URL based on the current request
    public String generateUrl(HttpServletRequest request) { // Remove 'static'
        String siteUrl = request.getRequestURL().toString();
        return siteUrl.replace(request.getServletPath(), "");
    }

    // Method for sending a simple email (e.g., password reset)
    public Boolean sendMail(String url, String recipientEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject("Password Reset Request");
            message.setText("Click the link to reset your password: " + url);

            mailSender.send(message);
            return true; // Email sent successfully
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            return false; // Failed to send the email
        }
    }

    // Method for sending an HTML email with product order details
    public Boolean sendMailForProductOrder(ProductOrder order, String status) throws Exception {
        try {
            // Create a MimeMessage for sending HTML email
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            
            // MimeMessageHelper for handling email content (true indicates multipart/HTML support)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(order.getOrderAddress().getEmail());
    
            // Prepare the HTML content with inline CSS
            String msg = "<html>"
                + "<head>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; }"
                + "h1 { color: #333333; font-size:2.2rem; }"
                + "p { font-size: 14px; color: #555555; }"
                + ".order-status { color: #007BFF; font-weight: bold; }"
                + ".product-details { border: 1px solid #dddddd; padding: 10px; margin-top: 20px; }"
                + ".product-details p { margin: 5px 0; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<p>Hello <strong>[[name]]</strong>,</p>"
                + "<p>Thank you for your order. The current status is <span class='order-status'>[[orderStatus]]</span>.</p>"
                + "<div class='product-details'>"
                + "<h3>Product Details:</h3>"
                + "<p><strong>Name:</strong> [[productName]]</p>"
                + "<p><strong>Category:</strong> [[category]]</p>"
                + "<p><strong>Quantity:</strong> [[quantity]]</p>"
                + "<p><strong>Price:</strong> [[price]]</p>"
                + "<p><strong>Payment Type:</strong> [[paymentType]]</p>"
                + "</div>"
                + "</body>"
                + "</html>";
    
            // Replace placeholders with actual values from the order object
            msg = msg.replace("[[name]]", order.getOrderAddress().getFirstName());
            msg = msg.replace("[[orderStatus]]", status);
            msg = msg.replace("[[productName]]", order.getProduct().getTitle());
            msg = msg.replace("[[category]]", order.getProduct().getCategory());
            msg = msg.replace("[[quantity]]", order.getQuanity().toString());
            msg = msg.replace("[[price]]", order.getPrice().toString());
            msg = msg.replace("[[paymentType]]", order.getPaymentType());
    
            // Set the subject and enable HTML content in the email
            helper.setSubject("Order Details:");
            helper.setText(msg, true); // true enables HTML format
    
            // Send the email
            mailSender.send(mimeMessage);
    
            return true; // Email sent successfully
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            return false; // Failed to send the email
        }
    }
    
}
