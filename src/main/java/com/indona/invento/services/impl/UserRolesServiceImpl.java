package com.indona.invento.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.indona.invento.dao.UserRolesRepository;
import com.indona.invento.entities.UserRolesEntity;
import com.indona.invento.services.UserRolesService;

@Service
public class UserRolesServiceImpl implements UserRolesService {

	@Autowired
	private UserRolesRepository userRolesRepository;
	
	@Override
	public String getRoleKey(Long id) {
		return userRolesRepository.findById(id).get().getRoleKey();
	}
	
	@Override
    public List<UserRolesEntity> getAllUserRoles() {
        return userRolesRepository.findAll();
    }

    @Override
    public UserRolesEntity getUserRoleById(Long id) {
        return userRolesRepository.findById(id).orElse(null);
    }

    @Override
    public UserRolesEntity createUserRole(UserRolesEntity userRole) {
        return userRolesRepository.save(userRole);
    }

    @Override
    public UserRolesEntity updateUserRole(Long id, UserRolesEntity userRole) {
        if (userRolesRepository.existsById(id)) {
            userRole.setId(id);
            return userRolesRepository.save(userRole);
        }
        return null; // Or throw an exception indicating user role not found
    }

    @Override
    public void deleteUserRole(Long id) {
        userRolesRepository.deleteById(id);
    }

}
