package com.ecom.Shoppingcart.service.Impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.Shoppingcart.model.UserDtls;
import com.ecom.Shoppingcart.repository.userRepository;
import com.ecom.Shoppingcart.service.userService;
import com.ecom.Shoppingcart.util.AppConstant;

@Service
public class UserServiceImpl implements userService{

    @Autowired
    private userRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDtls saveUser(UserDtls user) {
       user.setRole("ROLE_USER");
       user.setEnable(true);
       user.setAccountNotLocked(true);
       user.setFailedAttempt(0);
       String encodepassword=passwordEncoder.encode(user.getPassword());
       user.setPassword(encodepassword);
       return userRepository.save(user); 
    }

    @Override
    public UserDtls getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<UserDtls> getUsers(String role) {
        return userRepository.findByRole(role);

        
    }

    @Override
    public Boolean updateAccountStatus(Integer id, Boolean status) {
        Optional<UserDtls> byUser = userRepository.findById(id);

        if(byUser.isPresent())
        {
            UserDtls userDtls=byUser.get();
            userDtls.setEnable(status);
            userRepository.save(userDtls);
            return true;


        }
        return false;
    }

    @Override
    public void increaseFailedAttempt(UserDtls user) {
        int attempt=user.getFailedAttempt()+1;
        user.setFailedAttempt(attempt);
        userRepository.save(user);
    }

    @Override
    public void userAccountLock(UserDtls user) {
        user.setAccountNotLocked(false);
        user.setLockTime(new Date());
        userRepository.save(user);
    }

    @Override
    public boolean unlockAccountTimeExpired(UserDtls user) {
       long lockTime = user.getLockTime().getTime();
       long unlockTime=lockTime+AppConstant.UNLOCK_DURATION_TIME;

       long currentMillis = System.currentTimeMillis();

       if(currentMillis>unlockTime)
       {
        user.setAccountNotLocked(true);
        user.setFailedAttempt(0);
        user.setLockTime(null);
        userRepository.save(user);
        return true;

       }

       return false;


    }

    @Override
    public void resetAttempt(int userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resetAttempt'");
    }

    @Override
	public void updateUserResetToken(String email, String resetToken) {
		UserDtls userDtl = userRepository.findByEmail(email);
		userDtl.setResetToken(resetToken);
		userRepository.save(userDtl);
	}

    @Override
    public UserDtls getUserByToken(String token) {
        return userRepository.findByResetToken(token);
    }

    @Override
    public UserDtls updateUser(UserDtls user) {
        return userRepository.save(user);
    }

    @Override
	public UserDtls updateUserProfile(UserDtls user, MultipartFile img) {

		UserDtls dbUser = userRepository.findById(user.getId()).get();

		if (!img.isEmpty()) {
			dbUser.setProfileImage(img.getOriginalFilename());
		}

		if (!ObjectUtils.isEmpty(dbUser)) {

			dbUser.setName(user.getName());
			dbUser.setMobileNumber(user.getMobileNumber());
			dbUser.setAddress(user.getAddress());
			dbUser.setCity(user.getCity());
			dbUser.setState(user.getState());
			dbUser.setPincode(user.getPincode());
			dbUser = userRepository.save(dbUser);
		}

		try {
			if (!img.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
						+ img.getOriginalFilename());

//			System.out.println(path);
				Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dbUser;
	}

    @Override
    public UserDtls saveAdmin(UserDtls user) {
        user.setRole("ROLE_ADMIN");
       user.setEnable(true);
       user.setAccountNotLocked(true);
       user.setFailedAttempt(0);
       String encodepassword=passwordEncoder.encode(user.getPassword());
       user.setPassword(encodepassword);
       return userRepository.save(user); 
    }

    @Override
    public Boolean existsEmail(String email) {
        return userRepository.existsByEmail(email);
    }



}
