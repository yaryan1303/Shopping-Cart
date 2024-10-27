package com.ecom.Shoppingcart.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ecom.Shoppingcart.model.UserDtls;
import com.ecom.Shoppingcart.util.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthfailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private com.ecom.Shoppingcart.repository.userRepository userRepository;

    @Autowired
    private com.ecom.Shoppingcart.service.userService userService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String email = request.getParameter("username");

        UserDtls userDtls = userRepository.findByEmail(email);

        if (userDtls != null) {
            if (userDtls.isEnable()) {
                if (userDtls.getAccountNotLocked()) {
                    if (userDtls.getFailedAttempt() < AppConstant.ATTEMPT_TIME) {
                        userService.increaseFailedAttempt(userDtls);
                    } else {
                        userService.userAccountLock(userDtls);
                        exception = new LockedException("Your account is locked after 3 failed attempts.");
                    }
                } else {
                    if (userService.unlockAccountTimeExpired(userDtls)) {
                        exception = new LockedException("Your account has been unlocked. Please try to log in.");
                    } else {
                        exception = new LockedException("Your account is still locked. Please try again later.");
                    }
                }
            } else {
                exception = new LockedException("Your account is inactive.");
            }
        } else {
            exception = new LockedException("Invalid email or password.");
        }

        super.setDefaultFailureUrl("/signin?error");
        super.onAuthenticationFailure(request, response, exception);
    }
}
