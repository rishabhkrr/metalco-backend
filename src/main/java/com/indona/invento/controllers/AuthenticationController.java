/**
 * 
 */
package com.indona.invento.controllers;

import com.indona.invento.services.UserMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.indona.invento.dto.AuthRequest;
import com.indona.invento.dto.ResponseDto;
import com.indona.invento.entities.UsersEntity;
import com.indona.invento.services.JwtService;
import com.indona.invento.services.impl.UserInfoServiceImpl;
import com.indona.invento.dto.LoginResponseDto;
import com.indona.invento.entities.UserMasterEntity;
import com.indona.invento.util.ResponseUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 
 */

@RestController
@RequestMapping(path = "/auth")
public class AuthenticationController {

	@Autowired
    private UserInfoServiceImpl service; 
  
    @Autowired
    private JwtService jwtService; 
  
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserMasterService userMasterService;

    @RequestMapping(value="/register", method=RequestMethod.POST)
    public ResponseEntity<ResponseDto<Long>> addNewUser(@RequestBody UsersEntity userInfo, HttpServletRequest request, Errors errors) { 
   
        try {
        	String registered = service.addUser(userInfo);
        	 return ResponseUtil.success(registered);
        } catch (RuntimeException ex) {
            return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
        
    } 
    
    @RequestMapping(value="/update", method=RequestMethod.POST)
    public ResponseEntity<ResponseDto<Long>> updateUser(@RequestBody UsersEntity userInfo, HttpServletRequest request, Errors errors) { 
   
        try {
        	String registered = service.addUser(userInfo);
        	 return ResponseUtil.success(registered);
        } catch (RuntimeException ex) {
            return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
        
    }

    @RequestMapping(value="/login", method=RequestMethod.POST)
    public ResponseEntity<ResponseDto<LoginResponseDto>> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );

        if (authentication.isAuthenticated()) {
            String token = jwtService.generateToken(authRequest.getUsername());

            // Get user details with permissions
            UserMasterEntity user = userMasterService.getByUserName(authRequest.getUsername());
            if (user != null) {
                // ✅ Check if user status is "activate"
                if ("approved".equalsIgnoreCase(user.getStatus())) {
                    LoginResponseDto loginResponse = LoginResponseDto.builder()
                            .token(token)
                            .userId(user.getId())
                            .userName(user.getUserName())
                            .unitCode(user.getUnitCode())
                            .unitName(user.getUnitName())
                            .department(user.getDepartment())
                            .designation(user.getDesignation())
                            .status(user.getStatus())
                            .userCode(user.getUserId())
                            .modulePermissions(user.getSubModulesWithAccess())  // Complete permission matrix
                            .build();

                    return ResponseUtil.success(loginResponse);
                } else {
                    // ❌ If status is not activate
                    return ResponseUtil.error(HttpStatus.FORBIDDEN, "User is not active. Please contact admin.");
                }
            } else {
                return ResponseUtil.error(HttpStatus.NOT_FOUND, "User details not found");
            }
        } else {
            return ResponseUtil.error(HttpStatus.BAD_REQUEST, "Bad Credentials");
        }
    }

    @RequestMapping(value="/invalidate", method=RequestMethod.GET)
    public void invalidate(HttpSession session) {
      session.invalidate();
    }
    
    
    // Exception handling
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Void>> handleException(Exception ex) {
        return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
