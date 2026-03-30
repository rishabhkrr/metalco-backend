package com.indona.invento.services.impl;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.security.core.userdetails.UserDetails; 
import org.springframework.security.core.userdetails.UserDetailsService; 
import org.springframework.security.core.userdetails.UsernameNotFoundException; 
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.stereotype.Service;

import com.indona.invento.dao.UserInfoRepository;
import com.indona.invento.dao.UserMasterRepository;
import com.indona.invento.entities.UsersEntity;
import com.indona.invento.entities.UserMasterEntity;
import com.indona.invento.services.UserRolesService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.HashMap;

  
@Service
public class UserInfoServiceImpl implements UserDetailsService { 
  
    @Autowired
    private UserInfoRepository repository; 
    
    @Autowired
    private UserMasterRepository userMasterRepository;
    
    @Autowired
    private UserRolesService roleService;
    
//    @Autowired
//    private PasswordEncoder encoder;
    
    private EntityManagerFactory entityManagerFactory;
    
    @Autowired
    public void UserService(UserInfoRepository userInfoRepository) {
        this.repository = userInfoRepository;
    }
  
    public String addUser(UsersEntity userInfo) {
    	// Check in UserMaster system instead
    	if(userMasterRepository.findByUserName(userInfo.getUserName()) != null) {
    		return "User already exist with same username";
    	} else {
//    		userInfo.setPassword(encoder.encode(userInfo.getPassword()));
            repository.save(userInfo); 
            return "User created"; 
    	}
        
    }
    
    public String updateUser(UsersEntity userInfo) {
    	EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            // Retrieve the user from the database
            UsersEntity existingUser = entityManager.find(UsersEntity.class, userInfo.getId());
            if (existingUser != null) {
                // Update the user properties with new values
                existingUser.setUserName(userInfo.getUserName());
                existingUser.setUserEmail(userInfo.getUserEmail());
                existingUser.setRoleId(userInfo.getRoleId());
                existingUser.setPassword(userInfo.getPassword());
            }
            // Persist the changes
            entityManager.merge(existingUser);
            transaction.commit();
            return "User updated";
        } catch (Exception ex) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            ex.printStackTrace(); // Handle exception as per your application's requirement
            return "Request failed";
        } finally {
            entityManager.close();
        }
        
    }
    
    public HashMap<String, Object> findUserByUserName(String username) throws UsernameNotFoundException {
    	HashMap<String, Object> userInfo = new HashMap<String, Object>();
    	
    	// Use UserMaster system instead of old Users system
    	UserMasterEntity userDetail = userMasterRepository.findByUserName(username);
    	
    	if (userDetail == null) {
    		throw new UsernameNotFoundException("User not found with username: " + username);
    	}

        userInfo.put("userId", userDetail.getId());
    	userInfo.put("displayname", userDetail.getUserName());
    	userInfo.put("email", userDetail.getUserName()); // UserMaster doesn't have email, using username
    	userInfo.put("roles", "USER"); // Default role since UserMaster doesn't use roleId
    	userInfo.put("store", userDetail.getUnitCode()); // Using unitCode as store
    	userInfo.put("unit", userDetail.getUnitName()); // Changed from warehouse to unit

    	// Add new UserMaster specific fields
    	userInfo.put("unitCode", userDetail.getUnitCode());
    	userInfo.put("unitName", userDetail.getUnitName());
    	userInfo.put("department", userDetail.getDepartment());
    	userInfo.put("designation", userDetail.getDesignation());
    	userInfo.put("status", userDetail.getStatus());
    	userInfo.put("modulePermissions", userDetail.getSubModulesWithAccess()); // Complete permission matrix

    	return userInfo;
    }
	
	public boolean userStatus(String email) {
		try {
			
			return true;
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		// Use UserMaster system instead of old Users system
		UserMasterEntity user = userMasterRepository.findByUserName(username); 
		  
        // Converting userDetail to UserDetails 
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        
        // Create a UserDetails object from UserMasterEntity
        return org.springframework.security.core.userdetails.User.builder()
        	.username(user.getUserName())
        	.password(user.getPassword())
        	.authorities("USER") // Default authority
        	.build();
	}
} 
