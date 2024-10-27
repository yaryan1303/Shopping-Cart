package com.ecom.Shoppingcart.config;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Service
public class AuthSucesshandlerImpl implements AuthenticationSuccessHandler {

    @Override

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

                Set<String> role= AuthorityUtils.authorityListToSet(authorities);

                if(role.contains("ROLE_ADMIN"))
                {
                    response.sendRedirect("/admin/");
                }
                else{
                    response.sendRedirect("/");
                }

    }

}
