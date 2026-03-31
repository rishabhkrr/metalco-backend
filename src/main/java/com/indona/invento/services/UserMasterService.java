package com.indona.invento.services;

import com.indona.invento.dto.UserMasterDto;
import com.indona.invento.entities.UserMasterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserMasterService {
    UserMasterEntity create(UserMasterDto dto);
    UserMasterEntity update(Long id, UserMasterDto dto);
    Page<UserMasterEntity> getAll(Pageable pageable);
    UserMasterEntity getById(Long id);
    UserMasterEntity delete(Long id);
    List<UserMasterEntity> getAllWithoutPagination();
    UserMasterEntity getByUserName(String userName);
    UserMasterEntity approveUser(Long id) throws Exception;
    UserMasterEntity rejectUser(Long id) throws Exception;

}
