package com.indona.invento.services;

import java.util.List;

import com.indona.invento.entities.UserRolesEntity;

public interface UserRolesService {
	String getRoleKey(Long id);
	
	List<UserRolesEntity> getAllUserRoles();
    UserRolesEntity getUserRoleById(Long id);
    UserRolesEntity createUserRole(UserRolesEntity userRole);
    UserRolesEntity updateUserRole(Long id, UserRolesEntity userRole);
    void deleteUserRole(Long id);
}
