package com.ecom.Shoppingcart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecom.Shoppingcart.model.UserDtls;
import java.util.List;


@Repository
public interface userRepository extends JpaRepository<UserDtls,Integer> {

    Boolean existsByEmail(String email);
    public UserDtls findByEmail(String email);

    public List<UserDtls>findByRole(String role);

    UserDtls findByResetToken(String token);

}
