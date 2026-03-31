package com.indona.invento.services;

import java.util.List;
import com.indona.invento.entities.UsersEntity;

public interface UsersService {

	List<UsersEntity> getAllUsers();
    UsersEntity getUserById(Long id);
    UsersEntity createUser(UsersEntity user);
    UsersEntity updateUser(Long id, UsersEntity user);
    void deleteUser(Long id);
}
