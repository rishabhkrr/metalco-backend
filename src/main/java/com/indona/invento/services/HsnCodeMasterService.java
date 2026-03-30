package com.indona.invento.services;

import com.indona.invento.dto.HsnCodeMasterDto;
import com.indona.invento.entities.HsnCodeMasterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HsnCodeMasterService {
    HsnCodeMasterEntity createHsnCode(HsnCodeMasterDto dto);

    HsnCodeMasterEntity updateHsnCode(Long id, HsnCodeMasterDto dto);

    Page<HsnCodeMasterEntity> getAllHsnCodes(Pageable pageable);


    HsnCodeMasterEntity getHsnCodeById(Long id);

    HsnCodeMasterEntity deleteHsnCodeAndReturn(Long id);

    List<HsnCodeMasterEntity> getAllHsnCodesWithoutPagination();

    HsnCodeMasterEntity approveHsnCode(Long id) throws Exception;

    HsnCodeMasterEntity rejectHsnCode(Long id) throws Exception;

}
