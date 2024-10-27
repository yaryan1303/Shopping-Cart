package com.ecom.Shoppingcart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ecom.Shoppingcart.model.UserDtls;
import com.ecom.Shoppingcart.repository.userRepository;

public class UserDetailsServiceImpl  implements UserDetailsService {

    @Autowired
    private userRepository userrepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDtls user = userrepository.findByEmail(username);

        if(user==null)
        {
            throw new UsernameNotFoundException("user not found");
        }

        return new CustomUser(user);
    }

}
