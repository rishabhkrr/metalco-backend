package com.indona.invento.dao;

import com.indona.invento.entities.SchedulerPackingInstruction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulerPackingInstructionRepository extends JpaRepository<SchedulerPackingInstruction, Long> {

}
