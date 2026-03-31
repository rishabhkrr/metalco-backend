package com.indona.invento.services.impl;

import org.springframework.security.core.GrantedAuthority; 
import org.springframework.security.core.authority.SimpleGrantedAuthority; 
import org.springframework.security.core.userdetails.UserDetails;

import com.indona.invento.entities.UsersEntity;

import java.util.Arrays; 
import java.util.Collection; 
import java.util.List; 
import java.util.stream.Collectors; 
  
public class UserInfoDetailsImpl implements UserDetails { 
	
    private static final long serialVersionUID = 1L;
	private String name; 
    private String password;
    private List<GrantedAuthority> authorities; 
  
    public UserInfoDetailsImpl(UsersEntity userInfo) { 
        name = userInfo.getUserName();
        password = userInfo.getPassword();
        String[] roles = new String[1];;
        roles[0] = userInfo.getRoleId().toString();
        authorities = Arrays.stream(roles) 
                .map(SimpleGrantedAuthority::new) 
                .collect(Collectors.toList()); 
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { 
        return authorities; 
    } 
  
    @Override
    public String getPassword() { 
        return password; 
    } 
  
    @Override
    public String getUsername() { 
        return name; 
    } 
  
    @Override
    public boolean isAccountNonExpired() { 
        return true; 
    } 
  
    @Override
    public boolean isAccountNonLocked() { 
        return true; 
    } 
  
    @Override
    public boolean isCredentialsNonExpired() { 
        return true; 
    } 
  
    @Override
    public boolean isEnabled() { 
        return true; 
    } 
} 