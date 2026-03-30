package com.indona.invento.dao;

import com.indona.invento.entities.SOSchedulePickListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SOSchedulePickListRepository extends JpaRepository<SOSchedulePickListEntity, Long> {

    List<SOSchedulePickListEntity> findByMrNumberAndLineNumberAndItemDescription(String trim, String trim1, String trim2);

    List<SOSchedulePickListEntity> findByMrNumberAndLineNumber(String trim, String trim1);
}
