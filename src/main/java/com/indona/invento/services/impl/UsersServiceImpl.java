package com.indona.invento.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.indona.invento.dao.UserInfoRepository;
import com.indona.invento.entities.UsersEntity;
import com.indona.invento.services.UsersService;

@Service
public class UsersServiceImpl implements UsersService {

	@Autowired
    private UserInfoRepository userRepository;

//	@Autowired
//    private PasswordEncoder encoder;
	
    @Override
    public List<UsersEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UsersEntity getUserById(Long id) {
        return userRepository.findById(id).get();
    }

    @Override
    public UsersEntity createUser(UsersEntity user) {
//    	user.setPassword(encoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public UsersEntity updateUser(Long id, UsersEntity user) {
        if (userRepository.existsById(id)) {
        	UsersEntity updateUser = new UsersEntity();
        	updateUser.setId(id);
        	updateUser.setRoleId(user.getRoleId());
        	updateUser.setUserEmail(user.getUserEmail());
        	updateUser.setUserName(user.getUserName());
        	updateUser.setPassword(user.getPassword());
        	updateUser.setWarehouseId(user.getWarehouseId());
        	updateUser.setStoreId(user.getStoreId());
        	updateUser.setDateTime(user.getDateTime());
            return userRepository.save(updateUser);
        }
        return null; // Or throw an exception indicating user not found
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
